package com.example.formamind.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO для таблицы `courses`.
 *
 * Логика кэша:
 *  - insertAll()          — bulk-вставка курсов из сети (REPLACE на конфликте)
 *  - getAll()             — Flow без count (просто курсы)
 *  - getAllWithCount()    — Flow с count через подзапрос (для UI)
 *  - getAllOnce()         — разовое чтение без count
 *  - getAllOnceWithCount()- разовое чтение с count
 *  - count()              — сколько курсов в кэше
 *  - clearAll()           — очистка перед обновлением
 *
 * Почему getAllWithCount через подзапрос, а не JOIN:
 *  - JOIN дал бы дубликаты курсов (по одной строке на упражнение)
 *  - Подзапрос (SELECT COUNT(*) ...) даёт ровно одну строку на курс
 *  - Это классический способ решить проблему N+1 без денормализации
 */
@Dao
interface CourseDao {

    /** Все курсы, отсортированные по orderIndex. Flow — UI подписывается. */
    @Query("SELECT * FROM courses ORDER BY orderIndex ASC")
    fun getAll(): Flow<List<CourseEntity>>

    /**
     * Все курсы + количество упражнений.
     *
     * Подзапрос считает COUNT(*) для каждого курса.
     * Это правильный способ не хранить вычисляемое поле в БД.
     */
    @Query(
        """
        SELECT c.*,
               (SELECT COUNT(*) FROM exercises e WHERE e.courseId = c.id) AS exercisesCount
        FROM courses c
        ORDER BY c.orderIndex ASC
        """
    )
    fun getAllWithCount(): Flow<List<CourseWithCount>>

    /** Однократное чтение (не Flow) — для проверки, есть ли кэш. */
    @Query("SELECT * FROM courses ORDER BY orderIndex ASC")
    suspend fun getAllOnce(): List<CourseEntity>

    /** Однократное чтение с count — для получения одного курса. */
    @Query(
        """
        SELECT c.*,
               (SELECT COUNT(*) FROM exercises e WHERE e.courseId = c.id) AS exercisesCount
        FROM courses c
        WHERE c.id = :courseId
        LIMIT 1
        """
    )
    suspend fun getByIdWithCount(courseId: String): CourseWithCount?

    /** Сколько курсов в кэше. */
    @Query("SELECT COUNT(*) FROM courses")
    suspend fun count(): Int

    /** Bulk-вставка. Если id совпадает — заменяем. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CourseEntity>)

    /** Удалить все курсы (перед обновлением из сети). */
    @Query("DELETE FROM courses")
    suspend fun clearAll()
}