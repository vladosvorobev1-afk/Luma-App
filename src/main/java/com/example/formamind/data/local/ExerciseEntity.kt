package com.example.formamind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Кэш упражнений в Room.
 *
 * Хранит упражнения, скачанные из сети.
 * Связаны с таблицей courses через courseId (foreign key не делаем —
 * fallbackToDestructiveMigration() и так стирает всё при изменении схемы).
 *
 * Поле type храним как String (не enum) — потому что:
 *  - Room из коробки не умеет enum без TypeConverter
 *  - при парсинге мы делаем безопасный parseExerciseType() с fallback на TEXT
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String,

    val courseId: String,
    val title: String,
    val description: String,

    /** Строка типа: "BREATHING", "TEXT", "JOURNAL", "TIMER". */
    val type: String,

    val durationSec: Int,
    val instruction: String,
    val orderIndex: Int
)