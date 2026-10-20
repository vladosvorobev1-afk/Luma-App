package com.example.formamind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запись о выполненном упражнении.
 *
 * Сохраняется каждый раз, когда пользователь нажимает "Готово"
 * на экране упражнения. Позже используется для:
 *  - истории тренировок
 *  - streak (серия дней подряд)
 *  - статистики
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val exerciseId: String,
    val exerciseTitle: String,
    val courseId: String,

    /** Unix-время в миллисекундах, когда упражнение было завершено. */
    val completedAt: Long,

    /** Сколько секунд заняло упражнение (или 0, если без таймера). */
    val durationSec: Int,

    /** Для упражнений типа JOURNAL — текст записи. Для остальных — пустая строка. */
    val note: String = ""
)