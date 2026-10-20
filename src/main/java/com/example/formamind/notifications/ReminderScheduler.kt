package com.example.formamind.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Планировщик ежедневных напоминаний.
 *
 * Что делает:
 *  - Регистрирует в WorkManager периодическую задачу ReminderWorker
 *  - Время берётся из настроек (передаётся в schedule)
 *  - Умеет отменять задачу, если пользователь выключил напоминания
 *
 * Почему enqueueUniquePeriodicWork:
 *  - Защита от дубликатов при каждом запуске приложения
 *  - Используем REPLACE (не KEEP), потому что при смене времени
 *    надо пересоздать задачу с новым расписанием
 */
object ReminderScheduler {

    /**
     * Запланировать ежедневное напоминание на указанное время.
     *
     * @param hour час (0..23)
     * @param minute минута (0..59)
     */
    fun schedule(context: Context, hour: Int, minute: Int) {
        val delayMs = calculateDelayUntil(hour, minute)

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            // REPLACE — потому что при смене времени надо пересоздать задачу.
            // KEEP бы оставил старое расписание и настройки бы не применились.
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Отменить напоминание.
     * Вызывается, когда пользователь выключил switch «Напоминания».
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(ReminderWorker.WORK_NAME)
    }

    /**
     * Считает задержку до ближайшего указанного времени.
     *
     * Если сейчас 15:00, а targetHour = 20, minute = 0 — задержка 5 часов.
     * Если сейчас 22:00, а targetHour = 20 — задержка 22 часа (на завтра).
     */
    private fun calculateDelayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Если сегодняшнее время уже прошло — переносим на завтра.
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}