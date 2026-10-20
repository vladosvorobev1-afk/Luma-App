package com.example.formamind.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO — Data Access Object для таблицы `sessions`.
 *
 * Все запросы к базе выполняются через методы этого интерфейса.
 * Room сам сгенерирует реализацию.
 */
@Dao
interface SessionDao {

    /** Сохранить новую запись. Возвращает id вставленной строки. */
    @Insert
    suspend fun insert(session: SessionEntity): Long

    /**
     * Получить все записи, отсортированные по времени (сначала свежие).
     * Возвращает Flow — UI автоматически обновляется при изменении базы.
     */
    @Query("SELECT * FROM sessions ORDER BY completedAt DESC")
    fun getAll(): Flow<List<SessionEntity>>

    /** Сколько всего записей. */
    @Query("SELECT COUNT(*) FROM sessions")
    fun countFlow(): Flow<Int>

    /** Все записи за конкретный день (для streak). */
    @Query("SELECT * FROM sessions WHERE completedAt >= :dayStart AND completedAt < :dayEnd ORDER BY completedAt DESC")
    suspend fun getByDay(dayStart: Long, dayEnd: Long): List<SessionEntity>

    /** Удалить все записи. */
    @Query("DELETE FROM sessions")
    suspend fun clearAll()

    // ==================== СТАТИСТИКА / STREAK ====================

    /**
     * Суммарная длительность всех сессий в секундах.
     *
     * COALESCE(SUM(...), 0) — если строк нет, SUM вернёт NULL,
     * COALESCE подставит 0, чтобы Flow<Long> не был nullable.
     */
    @Query("SELECT COALESCE(SUM(durationSec), 0) FROM sessions")
    fun getTotalDurationFlow(): Flow<Long>

    /**
     * Уникальные дни активности в формате epochDay (LocalDate.toEpochDay()).
     *
     * completedAt — миллисекунды. Делим на 86_400_000 → получаем дни
     * от начала эпохи UTC. SQLite-деление целочисленное — округление вниз.
     *
     * ВАЖНО: граница дня считается по UTC, а не по локальному времени.
     * Для streak (день зашёл/не зашёл) это приемлемо: пользователь
     * занимается в разное время, а разница в 2-3 часа не критична для MVP.
     * Если надо строго по локальной зоне — переедем на хранение epochDay
     * при вставке, но это отдельный рефакторинг.
     */
    @Query(
        "SELECT DISTINCT completedAt / 86400000 AS epochDay " +
                "FROM sessions " +
                "ORDER BY epochDay DESC"
    )
    fun getActiveDaysEpochFlow(): Flow<List<Long>>
}