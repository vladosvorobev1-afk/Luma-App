package com.example.formamind.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    /** Вспомогательная функция: возвращает epochDay для дня, сдвинутого на N дней назад от сегодня. */
    private fun daysAgo(days: Long): Long = LocalDate.now().toEpochDay() - days

    @Test
    fun `empty list returns zero streaks`() {
        val (current, best) = computeStreak(emptyList())
        assertEquals(0, current)
        assertEquals(0, best)
    }

    @Test
    fun `single day today returns streak 1`() {
        val (current, best) = computeStreak(listOf(daysAgo(0)))
        assertEquals(1, current)
        assertEquals(1, best)
    }

    @Test
    fun `single day yesterday returns streak 1`() {
        val (current, best) = computeStreak(listOf(daysAgo(1)))
        assertEquals(1, current)
        assertEquals(1, best)
    }

    @Test
    fun `single day two days ago breaks current streak`() {
        val (current, best) = computeStreak(listOf(daysAgo(2)))
        assertEquals(0, current)
        assertEquals(1, best)
    }

    @Test
    fun `three days in a row ending today returns streak 3`() {
        val days = listOf(daysAgo(0), daysAgo(1), daysAgo(2)).sortedDescending()
        val (current, best) = computeStreak(days)
        assertEquals(3, current)
        assertEquals(3, best)
    }

    @Test
    fun `three days in a row ending yesterday returns streak 3`() {
        val days = listOf(daysAgo(1), daysAgo(2), daysAgo(3)).sortedDescending()
        val (current, best) = computeStreak(days)
        assertEquals(3, current)
        assertEquals(3, best)
    }

    @Test
    fun `broken streak returns correct current and best`() {
        // 5 дней назад, 4 дня назад, 3 дня назад (серия 3), потом пропуск,
        // потом вчера и сегодня (серия 2).
        // ВАЖНО: сортируем по убыванию, как это делает SQL DESC.
        val days = listOf(
            daysAgo(0), daysAgo(1),          // серия 2 (текущая)
            daysAgo(5), daysAgo(4), daysAgo(3) // серия 3 (лучшая)
        ).sortedDescending()
        val (current, best) = computeStreak(days)
        assertEquals(2, current)
        assertEquals(3, best)
    }

    @Test
    fun `all days old but continuous returns current 0 and best n`() {
        // 10, 9, 8 дней назад — серия прервалась, current = 0, best = 3.
        val days = listOf(daysAgo(8), daysAgo(9), daysAgo(10)).sortedDescending()
        val (current, best) = computeStreak(days)
        assertEquals(0, current)
        assertEquals(3, best)
    }
}