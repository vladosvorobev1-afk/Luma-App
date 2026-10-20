package com.example.formamind.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO для таблицы `exercises`.
 *
 * Логика кэша:
 *  - insertAll()               — bulk-вставка из сети
 *  - getByCourse(courseId)     — Flow, упражнения конкретного курса
 *  - getByCourseOnce(courseId) — однократное чтение (для ViewModel)
 *  - getAllOnce()              — все упражнения разом (для getExerciseById)
 *  - countForCourse(courseId)  — сколько упражнений в курсе (для exercisesCount)
 *  - clearAll()                — очистка перед обновлением
 */
@Dao
interface ExerciseDao {

    /** Все упражнения (без фильтра). */
    @Query("SELECT * FROM exercises ORDER BY orderIndex ASC")
    fun getAll(): Flow<List<ExerciseEntity>>

    /** Все упражнения разом — для поиска по ID. */
    @Query("SELECT * FROM exercises ORDER BY orderIndex ASC")
    suspend fun getAllOnce(): List<ExerciseEntity>

    /** Упражнения конкретного курса. Flow — UI подписывается. */
    @Query("SELECT * FROM exercises WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getByCourse(courseId: String): Flow<List<ExerciseEntity>>

    /** Упражнения конкретного курса, однократно. */
    @Query("SELECT * FROM exercises WHERE courseId = :courseId ORDER BY orderIndex ASC")
    suspend fun getByCourseOnce(courseId: String): List<ExerciseEntity>

    /** Сколько упражнений в конкретном курсе (для exercisesCount). */
    @Query("SELECT COUNT(*) FROM exercises WHERE courseId = :courseId")
    suspend fun countForCourse(courseId: String): Int

    /** Bulk-вставка. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    /** Удалить все упражнения. */
    @Query("DELETE FROM exercises")
    suspend fun clearAll()
}