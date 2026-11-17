package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class HabitCalendarLogicTest {
    @Test
    fun completionHistoryIsScopedToMonthAndIgnoresInvalidDates() {
        val completions = setOf(
            "2024-01-31",
            "2024-02-01",
            "2024-02-29",
            "2024-03-01",
            "2024-02-invalid",
            "2024-02-30"
        )

        assertEquals(
            setOf(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)),
            completedHabitDatesInMonth(completions, YearMonth.of(2024, 2))
        )
    }

    @Test
    fun emptyHistoryProducesNoCompletedDates() {
        assertEquals(emptySet<LocalDate>(), completedHabitDatesInMonth(emptySet(), YearMonth.of(2026, 11)))
    }
}
