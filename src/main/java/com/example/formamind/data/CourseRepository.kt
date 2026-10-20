package com.example.formamind.data

import android.content.Context
import androidx.room.withTransaction
import com.example.formamind.data.local.AppDatabase
import com.example.formamind.data.local.CourseEntity
import com.example.formamind.data.local.CourseWithCount
import com.example.formamind.data.local.ExerciseEntity
import com.example.formamind.data.local.SessionEntity
import com.example.formamind.data.remote.CourseApi
import com.example.formamind.data.remote.NetworkModule
import com.example.formamind.data.remote.toDomainCourses
import com.example.formamind.data.remote.toDomainExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Репозиторий — единая точка доступа к данным.
 * ...
 * (комментарии сохранены, добавлен блок СТАТИСТИКА)
 */
class CourseRepository(
    context: Context,
    private val api: CourseApi = NetworkModule.courseApi
) {

    private val db = AppDatabase.getInstance(context)
    private val sessionDao = db.sessionDao()
    private val courseDao = db.courseDao()
    private val exerciseDao = db.exerciseDao()

    // ==================== КУРСЫ ====================

    fun getCoursesFlow(): Flow<List<Course>> =
        courseDao.getAllWithCount().map { items ->
            if (items.isEmpty()) {
                DefaultCourses.all.map { course ->
                    course.copy(
                        exercisesCount = DefaultExercises.byCourse(course.id).size
                    )
                }
            } else {
                items.map { it.toDomain() }
            }
        }

    suspend fun getAllCourses(): List<Course> {
        val items = courseDao.getAllOnce()
        return if (items.isEmpty()) {
            DefaultCourses.all.map { course ->
                course.copy(
                    exercisesCount = DefaultExercises.byCourse(course.id).size
                )
            }
        } else {
            items.map { entity ->
                val count = exerciseDao.countForCourse(entity.id)
                entity.toDomain(exercisesCount = count)
            }
        }
    }

    suspend fun getCourseById(id: String): Course? {
        val item = courseDao.getByIdWithCount(id)
        return if (item != null) {
            item.toDomain()
        } else {
            DefaultCourses.all.find { it.id == id }?.let { course ->
                course.copy(
                    exercisesCount = DefaultExercises.byCourse(course.id).size
                )
            }
        }
    }

    // ==================== УПРАЖНЕНИЯ ====================

    fun getExercisesFlow(courseId: String): Flow<List<Exercise>> =
        exerciseDao.getByCourse(courseId).map { entities ->
            if (entities.isEmpty()) DefaultExercises.byCourse(courseId)
            else entities.map { it.toDomain() }
        }

    suspend fun getExercisesForCourse(courseId: String): List<Exercise> {
        val entities = exerciseDao.getByCourseOnce(courseId)
        return if (entities.isEmpty()) DefaultExercises.byCourse(courseId)
        else entities.map { it.toDomain() }
    }

    suspend fun getExerciseById(id: String): Exercise? {
        val entities = exerciseDao.getAllOnce()
        val source = if (entities.isEmpty()) DefaultExercises.all
        else entities.map { it.toDomain() }
        return source.find { it.id == id }
    }

    // ==================== ОБНОВЛЕНИЕ ИЗ СЕТИ ====================

    suspend fun refreshFromNetwork(): Result<Unit> = runCatching {
        val response = api.getCourses()

        val courses = response.toDomainCourses()
        val exercises = response.toDomainExercises()

        db.withTransaction {
            courseDao.clearAll()
            exerciseDao.clearAll()

            courseDao.insertAll(
                courses.mapIndexed { index, course ->
                    course.toEntity(orderIndex = index)
                }
            )

            exerciseDao.insertAll(
                exercises.map { it.toEntity() }
            )
        }
    }

    // ==================== ИСТОРИЯ (Room) ====================

    suspend fun saveSession(
        exerciseId: String,
        exerciseTitle: String,
        courseId: String,
        durationSec: Int,
        note: String = ""
    ) {
        sessionDao.insert(
            SessionEntity(
                exerciseId = exerciseId,
                exerciseTitle = exerciseTitle,
                courseId = courseId,
                completedAt = System.currentTimeMillis(),
                durationSec = durationSec,
                note = note
            )
        )
    }

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAll()

    fun getSessionCount(): Flow<Int> = sessionDao.countFlow()

    suspend fun clearHistory() = sessionDao.clearAll()

    // ==================== СТАТИСТИКА (Streak) ====================

    /**
     * Собирает статистику из двух Flow (суммарное время и дни активности).
     */
    fun observeUserStats(): Flow<UserStats> {
        return combine(
            sessionDao.countFlow(),
            sessionDao.getTotalDurationFlow(),
            sessionDao.getActiveDaysEpochFlow()
        ) { count, totalDuration, activeDays ->
            val (currentStreak, bestStreak) = computeStreak(activeDays)
            UserStats(
                totalSessions = count,
                totalDurationSeconds = totalDuration,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                activeDays = activeDays.size
            )
        }
    }
}

// ==================== ЛОГИКА STREAK ====================

/**
 * Считает текущую и лучшую серию дней подряд.
 *
 * @param activeDaysEpoch Список дней (epochDay), отсортированный по убыванию (от новых к старым).
 * @return Pair(currentStreak, bestStreak)
 */
internal fun computeStreak(activeDaysEpoch: List<Long>): Pair<Int, Int> {
    if (activeDaysEpoch.isEmpty()) return 0 to 0

    val todayEpoch = LocalDate.now().toEpochDay()
    val yesterdayEpoch = todayEpoch - 1

    // 1. Считаем текущую серию (currentStreak)
    var currentStreak = 0
    var expectedDay = when {
        activeDaysEpoch.first() == todayEpoch -> todayEpoch
        activeDaysEpoch.first() == yesterdayEpoch -> yesterdayEpoch
        else -> return 0 to computeBestStreak(activeDaysEpoch) // Серия прервалась
    }

    for (day in activeDaysEpoch) {
        if (day == expectedDay) {
            currentStreak++
            expectedDay--
        } else if (day < expectedDay) {
            break // Разрыв в днях
        }
    }

    // 2. Считаем лучшую серию (bestStreak)
    val bestStreak = computeBestStreak(activeDaysEpoch)

    return currentStreak to bestStreak
}

/** Вспомогательная функция для подсчета максимальной серии за всё время. */
private fun computeBestStreak(activeDaysEpoch: List<Long>): Int {
    if (activeDaysEpoch.isEmpty()) return 0
    var maxStreak = 1
    var currentStreak = 1

    // Идем от старых к новым или от новых к старым — неважно, главное проверить разницу.
    // Список приходит по убыванию, поэтому prevDay - nextDay должно быть 1.
    for (i in 0 until activeDaysEpoch.size - 1) {
        val currentDay = activeDaysEpoch[i]
        val nextDay = activeDaysEpoch[i + 1]
        if (currentDay - nextDay == 1L) {
            currentStreak++
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak
            }
        } else {
            currentStreak = 1
        }
    }
    return maxStreak
}

// ==================== МАППЕРЫ ENTITY ↔ DOMAIN ====================

private fun CourseEntity.toDomain(exercisesCount: Int): Course = Course(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    exercisesCount = exercisesCount
)

private fun CourseWithCount.toDomain(): Course = Course(
    id = course.id,
    title = course.title,
    description = course.description,
    emoji = course.emoji,
    exercisesCount = exercisesCount
)

private fun Course.toEntity(orderIndex: Int): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    orderIndex = orderIndex
)

private fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    courseId = courseId,
    title = title,
    description = description,
    type = parseExerciseType(type),
    durationSec = durationSec,
    instruction = instruction,
    orderIndex = orderIndex
)

private fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    courseId = courseId,
    title = title,
    description = description,
    type = type.name,
    durationSec = durationSec,
    instruction = instruction,
    orderIndex = orderIndex
)

private fun parseExerciseType(raw: String): ExerciseType =
    runCatching { ExerciseType.valueOf(raw.uppercase()) }
        .getOrDefault(ExerciseType.TEXT)