package com.example.formamind.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.formamind.data.DefaultExercises

/**
 * Worker, который выполняется в фоне по расписанию.
 *
 * Задача — показать уведомление-напоминание с Deep Link на упражнение.
 *
 * Почему CoroutineWorker, а не Worker:
 *  - внутри можно использовать suspend-функции
 *  - в нашем случае — нет, но на будущее удобнее
 *  - это рекомендуемый тип Worker'а в документации Android
 *
 * WorkManager сам:
 *  - вызовет doWork() в подходящий момент
 *  - гарантирует выполнение (даже после перезагрузки устройства)
 *  - повторит попытку, если вернуть Result.retry()
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Берем ID первого упражнения из локального fallback-списка.
        // Если список вдруг пуст (чего быть не должно), уведомление просто не покажется.
        val defaultExerciseId = DefaultExercises.all.firstOrNull()?.id

        if (defaultExerciseId != null) {
            NotificationHelper.showReminder(applicationContext, defaultExerciseId)
        }

        // Result.success() — задача выполнена.
        // Другие варианты:
        //  - Result.failure() — задача провалилась, повторов не будет
        //  - Result.retry()  — повторить позже (по backoff policy)
        return Result.success()
    }

    companion object {
        /** Имя задачи для WorkManager — должно быть уникальным. */
        const val WORK_NAME = "daily_reminder_work"
    }
}