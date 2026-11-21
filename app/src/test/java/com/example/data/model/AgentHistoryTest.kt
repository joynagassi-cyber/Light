package com.example.data.model

import com.example.data.preferences.AgentConversationThread
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AgentHistoryTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val today = LocalDate.of(2026, 3, 30) // Just after the spring clock change.
    private fun thread(age: Long, hour: Long = 0, id: String = "day$age"): AgentConversationThread = AgentConversationThread(
        id = id,
        updatedAtMillis = today.minusDays(age).atStartOfDay(zone).plusHours(hour).toInstant().toEpochMilli(),
    )

    @Test fun filtersUseLocalCalendarBoundariesAndRespectInclusiveEdges() {
        val threads = listOf(thread(30), thread(7), thread(1), thread(29), thread(6), thread(0))
        val expected = mapOf(
            AgentHistoryPeriod.TODAY to listOf("day0"),
            AgentHistoryPeriod.WEEK to listOf("day0", "day1", "day6"),
            AgentHistoryPeriod.MONTH to listOf("day0", "day1", "day6", "day7", "day29"),
            AgentHistoryPeriod.ALL to listOf("day0", "day1", "day6", "day7", "day29", "day30"),
        )
        expected.forEach { (period, ids) ->
            assertEquals(ids, groupAgentHistory(threads, period, today, zone).flatMap { it.second }.map { it.id })
        }
    }

    @Test fun unknownOrFutureDatesAreVisibleOnlyInAllAndDoNotFloatAboveTodaysThreads() {
        val legacy = AgentConversationThread(id = "legacy", updatedAtMillis = 0)
        val a = thread(0, 4, "early")
        val b = thread(0, 6, "late")
        val input = listOf(legacy, a, thread(-1), b)
        val todaySection = AgentHistorySection.TODAY to listOf(b, a)
        assertEquals(listOf(todaySection, AgentHistorySection.UNKNOWN to listOf(thread(-1), legacy)),
            groupAgentHistory(input, AgentHistoryPeriod.ALL, today, zone))
        assertEquals(listOf(todaySection), groupAgentHistory(input, AgentHistoryPeriod.TODAY, today, zone))
    }
}
