package com.example.formamind.data.local

import androidx.room.Embedded

/**
 * Результат JOIN-запроса: курс + количество его упражнений.
 *
 * Это НЕ таблица. Это DTO для ответа Room.
 * Room умеет маппить результат SELECT в такой класс.
 *
 * @Embedded — говорит Room: "поля CourseEntity положи сюда же,
 * на верхний уровень". В итоге мы получаем плоский объект:
 *   id, title, description, emoji, orderIndex, exercisesCount.
 */
data class CourseWithCount(
    @Embedded
    val course: CourseEntity,

    /** COUNT(*) из подзапроса. */
    val exercisesCount: Int
)