package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarMonthGridTest {
    @Test
    fun leapFebruaryStartsOnThursdayAndShowsEveryDayInFiveCompleteWeeks() {
        val dates = calendarMonthGridDates(YearMonth.of(2024, 2))

        assertEquals(35, dates.size)
        assertEquals(listOf(null, null, null), dates.take(3))
        assertEquals(LocalDate.of(2024, 2, 1), dates[3])
        assertEquals(DayOfWeek.THURSDAY, dates[3]!!.dayOfWeek)
        assertEquals(29, dates.filterNotNull().size)
        assertEquals(LocalDate.of(2024, 2, 29), dates.last { it != null })
        assertTrue(dates.drop(32).all { it == null })
    }

    @Test
    fun monthBeginningOnSaturdayUsesSixRowsWithMondayFirstPadding() {
        val dates = calendarMonthGridDates(YearMonth.of(2025, 3))

        assertEquals(42, dates.size)
        assertEquals(listOf(null, null, null, null, null), dates.take(5))
        assertEquals(LocalDate.of(2025, 3, 1), dates[5])
        assertEquals(LocalDate.of(2025, 3, 31), dates[35])
        assertTrue(dates.drop(36).all { it == null })
    }

    @Test
    fun everyMonthHasWholeWeeksAndContainsEachValidDateOnce() {
        for (monthNumber in 1..12) {
            val month = YearMonth.of(2024, monthNumber)
            val dates = calendarMonthGridDates(month)
            assertEquals(0, dates.size % 7)
            assertTrue(dates.size in 28..42)
            assertEquals(month.lengthOfMonth(), dates.filterNotNull().size)
            assertEquals((1..month.lengthOfMonth()).toList(), dates.filterNotNull().map { it.dayOfMonth })
            assertEquals(month.atDay(1).dayOfWeek.value - 1, dates.indexOfFirst { it != null })
        }
    }
}
