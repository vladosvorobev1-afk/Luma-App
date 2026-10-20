package com.example.formamind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Кэш курсов в Room.
 *
 * Хранит курсы, скачанные из сети, чтобы:
 *  - показывать данные моментально при следующем запуске (без сети тоже)
 *  - не дёргать API каждый раз
 *
 * Структура совпадает с domain-моделью Course,
 * но БЕЗ поля exercisesCount — оно вычисляется динамически
 * из таблицы exercises (см. CourseDao).
 */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val description: String,
    val emoji: String,

    /**
     * Порядок отображения курсов в списке.
     * Нужен, чтобы сохранить порядок, в котором они пришли с сервера.
     */
    val orderIndex: Int
)