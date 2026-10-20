package com.example.formamind.data

/**
 * Модель курса — тема, по которой пользователь проходит упражнения.
 * Например: "Тревога", "Сон", "Прокрастинация".
 */
data class Course(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val exercisesCount: Int
)

/**
 * Предустановленные курсы MVP.
 * Лежат локально, позже можно будет тянуть через Retrofit.
 */
object DefaultCourses {
    val all = listOf(
        Course(
            id = "anxiety",
            title = "Тревога",
            description = "Дыхание, заземление, дневник мыслей",
            emoji = "🌊",
            exercisesCount = 5
        ),
        Course(
            id = "sleep",
            title = "Сон",
            description = "Релаксация, техника 4-7-8, ритуалы",
            emoji = "🌙",
            exercisesCount = 4
        ),
        Course(
            id = "procrastination",
            title = "Прокрастинация",
            description = "Разбивка задач, правило 5 минут",
            emoji = "⚡",
            exercisesCount = 4
        ),
        Course(
            id = "stress",
            title = "Стресс",
            description = "Быстрые техники для снятия напряжения",
            emoji = "🧘",
            exercisesCount = 3
        )
    )
}