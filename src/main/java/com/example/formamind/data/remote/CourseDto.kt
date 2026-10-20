@file:Suppress("OPT_IN_USAGE")
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
package com.example.formamind.data.remote

import com.example.formamind.data.Course
import com.example.formamind.data.Exercise
import com.example.formamind.data.ExerciseType
import kotlinx.serialization.Serializable

/**
 * DTO-модели для сетевого слоя.
 *
 * Структура совпадает с JSON по адресу:
 * https://gist.githubusercontent.com/vladosvorobev1-afk/8608caadd5bc64c27947d71445e54bea/raw/courses.json
 *
 * DTO — это "сырые" данные из сети. Они НЕ используются в UI напрямую.
 * Их задача — донести JSON до маппера, который превратит их в domain-модели
 * (Course, Exercise). Это классический подход в MVVM: сеть → DTO → domain → UI.
 */

// ==================== КОРЕНЬ JSON ====================

@Serializable
data class CoursesResponseDto(
    val courses: List<CourseDto> = emptyList(),
    val exercises: List<ExerciseDto> = emptyList()
)

// ==================== COURSE ====================

@Serializable
data class CourseDto(
    val id: String,
    val title: String,
    val description: String = "",
    val emoji: String = "",
    val exercisesCount: Int = 0
)

// ==================== EXERCISE ====================

@Serializable
data class ExerciseDto(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String = "",
    val type: String,
    val durationSec: Int = 0,
    val instruction: String = "",
    val orderIndex: Int = 0
)

// ==================== МАППЕРЫ: DTO → domain ====================

/**
 * Превращает ответ сети в список domain-курсов.
 *
 * Вариант Б: exercisesCount считаем из реального количества упражнений
 * в ответе для данного курса — цифра на UI всегда честная.
 */
fun CoursesResponseDto.toDomainCourses(): List<Course> =
    courses.map { dto ->
        val realCount = exercises.count { it.courseId == dto.id }
        dto.toDomain(realExercisesCount = realCount)
    }

/**
 * Превращает ответ сети в список domain-упражнений.
 */
fun CoursesResponseDto.toDomainExercises(): List<Exercise> =
    exercises.map { it.toDomain() }

/**
 * Один DTO курса → один domain-курс.
 */
fun CourseDto.toDomain(realExercisesCount: Int): Course =
    Course(
        id = id,
        title = title,
        description = description,
        emoji = emoji,
        exercisesCount = realExercisesCount
    )

/**
 * Один DTO упражнения → одно domain-упражнение.
 */
fun ExerciseDto.toDomain(): Exercise =
    Exercise(
        id = id,
        courseId = courseId,
        title = title,
        description = description,
        type = parseExerciseType(type),
        durationSec = durationSec,
        instruction = instruction,
        orderIndex = orderIndex
    )

/**
 * Безопасный парсинг строки в enum ExerciseType.
 *
 * Если с сервера придёт неизвестный тип (например, "MEDITATION") —
 * не крашим приложение, а откатываемся к TEXT.
 */
private fun parseExerciseType(raw: String): ExerciseType =
    runCatching { ExerciseType.valueOf(raw.uppercase()) }
        .getOrDefault(ExerciseType.TEXT)