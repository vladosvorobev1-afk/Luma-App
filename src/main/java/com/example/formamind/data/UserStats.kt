package com.example.formamind.data

/**
 * Агрегированная статистика пользователя для экрана Stats.
 */
data class UserStats(
    val totalSessions: Int = 0,
    val totalDurationSeconds: Long = 0L,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val activeDays: Int = 0
) {
    /** Суммарное время в формате «X ч Y мин» (или «Y мин», если меньше часа). */
    val formattedTotalDuration: String
        get() {
            val totalMinutes = totalDurationSeconds / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "$hours ч $minutes мин" else "$minutes мин"
        }
}