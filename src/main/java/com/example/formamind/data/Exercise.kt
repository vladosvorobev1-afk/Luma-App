package com.example.formamind.data

/**
 * Тип упражнения — определяет, как показывать экран и что делать пользователю.
 */
enum class ExerciseType {
    BREATHING,      // Дыхательное упражнение (таймер + анимация)
    TEXT,           // Просто текст с инструкцией
    JOURNAL,        // Запись мысли (поле ввода)
    TIMER           // Таймер на N секунд
}

/**
 * Модель упражнения — один шаг внутри курса.
 * Например: "Дыхание по квадрату", "Дневник мыслей".
 */
data class Exercise(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val type: ExerciseType,
    val durationSec: Int,           // сколько секунд длится (0 = без таймера)
    val instruction: String,        // подробная инструкция для TTS
    val orderIndex: Int             // порядок в курсе
)

/**
 * Предустановленные упражнения для MVP.
 * Пока хардкод, позже — из сети через Retrofit.
 */
object DefaultExercises {
    val all = listOf(
        // === КУРС "Тревога" ===
        Exercise(
            id = "anxiety_1",
            courseId = "anxiety",
            title = "Дыхание по квадрату",
            description = "4 секунды вдох, 4 задержка, 4 выдох, 4 задержка",
            type = ExerciseType.BREATHING,
            durationSec = 300,
            instruction = "Сядь удобно. Дыши по квадрату: вдох на четыре счёта, задержка на четыре, выдох на четыре, задержка на четыре. Повторяй цикл в течение пяти минут.",
            orderIndex = 1
        ),
        Exercise(
            id = "anxiety_2",
            courseId = "anxiety",
            title = "Заземление 5-4-3-2-1",
            description = "Техника возвращения в настоящий момент",
            type = ExerciseType.TEXT,
            durationSec = 0,
            instruction = "Назови пять вещей, которые видишь. Четыре — которые слышишь. Три — которых можешь коснуться. Две — которые чувствуешь запахом. Одну — которую чувствуешь на вкус.",
            orderIndex = 2
        ),
        Exercise(
            id = "anxiety_3",
            courseId = "anxiety",
            title = "Дневник мыслей",
            description = "Запиши тревожную мысль и проверь её",
            type = ExerciseType.JOURNAL,
            durationSec = 0,
            instruction = "Запиши тревожную мысль. Спроси себя: это факт или предположение? Какие есть доказательства за и против? Как бы я сказал другу в такой ситуации?",
            orderIndex = 3
        ),

        // === КУРС "Сон" ===
        Exercise(
            id = "sleep_1",
            courseId = "sleep",
            title = "Прогрессивная релаксация",
            description = "Напрягай и расслабляй группы мышц",
            type = ExerciseType.TEXT,
            durationSec = 0,
            instruction = "Ляг. Начни с пальцев ног: напряги на пять секунд, затем расслабь. Поднимайся вверх — икры, бёдра, живот, руки, шея, лицо. На каждом шаге задерживай напряжение на пять секунд.",
            orderIndex = 1
        ),
        Exercise(
            id = "sleep_2",
            courseId = "sleep",
            title = "Дыхание 4-7-8",
            description = "Вдох 4, задержка 7, выдох 8",
            type = ExerciseType.BREATHING,
            durationSec = 240,
            instruction = "Вдох на четыре счёта через нос. Задержка на семь. Медленный выдох на восемь через рот. Повтори четыре раза.",
            orderIndex = 2
        ),

        // === КУРС "Прокрастинация" ===
        Exercise(
            id = "procrastination_1",
            courseId = "procrastination",
            title = "Правило 5 минут",
            description = "Начни делать что-то всего 5 минут",
            type = ExerciseType.TIMER,
            durationSec = 300,
            instruction = "Выбери задачу, которую откладываешь. Поставь таймер на пять минут и начни. Через пять минут ты можешь остановиться — но обычно ты продолжишь.",
            orderIndex = 1
        ),
        Exercise(
            id = "procrastination_2",
            courseId = "procrastination",
            title = "Разбивка задачи",
            description = "Раздели большую задачу на маленькие шаги",
            type = ExerciseType.JOURNAL,
            durationSec = 0,
            instruction = "Запиши большую задачу. Раздели её на шаги по 15 минут каждый. Начни с самого маленького и лёгкого.",
            orderIndex = 2
        ),

        // === КУРС "Стресс" ===
        Exercise(
            id = "stress_1",
            courseId = "stress",
            title = "Сканирование тела",
            description = "Быстрая проверка, где живёт напряжение",
            type = ExerciseType.TEXT,
            durationSec = 0,
            instruction = "Закрой глаза. Пройди вниманием от макушки до стоп. Заметь, где есть напряжение. Не пытайся его убрать — просто заметь.",
            orderIndex = 1
        ),
        Exercise(
            id = "stress_2",
            courseId = "stress",
            title = "Дыхание 4-4-4-4",
            description = "Квадратное дыхание для быстрого снятия стресса",
            type = ExerciseType.BREATHING,
            durationSec = 180,
            instruction = "Вдох на четыре счёта, задержка на четыре, выдох на четыре, задержка на четыре. Три минуты.",
            orderIndex = 2
        ),

        // === КУРС "Фокус" ===
        Exercise(
            id = "focus_1",
            courseId = "focus",
            title = "Помодоро 25 минут",
            description = "Работа 25 минут, отдых 5",
            type = ExerciseType.TIMER,
            durationSec = 1500,
            instruction = "Выбери одну задачу. Работай 25 минут без отвлечений. Потом отдохни 5 минут.",
            orderIndex = 1
        ),
        Exercise(
            id = "focus_2",
            courseId = "focus",
            title = "Список отвлечений",
            description = "Записывай мысли, которые уводят от задачи",
            type = ExerciseType.JOURNAL,
            durationSec = 0,
            instruction = "Каждый раз, когда ловишь себя на отвлечении, запиши мысль в список. Вернись к задаче.",
            orderIndex = 2
        )
    )

    /** Получить все упражнения конкретного курса, отсортированные по порядку. */
    fun byCourse(courseId: String): List<Exercise> =
        all.filter { it.courseId == courseId }.sortedBy { it.orderIndex }
}
