package com.example.data.model

import com.example.data.preferences.AgentConversationThread
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal enum class AgentHistoryPeriod(val label: String, private val days: Long? = null) {
    ALL("Tout"), TODAY("Aujourd’hui", 1), WEEK("7 jours", 7), MONTH("30 jours", 30);

    fun includes(ageDays: Long?): Boolean = days == null || (ageDays != null && ageDays in 0 until days)
}

internal enum class AgentHistorySection(val label: String) {
    TODAY("Aujourd’hui"), YESTERDAY("Hier"), WEEK("2 à 6 jours"), MONTH("7 à 29 jours"),
    OLDER("Plus ancien"), UNKNOWN("Date indisponible");

    companion object {
        fun fromAge(days: Long?): AgentHistorySection {
            return when (days ?: return UNKNOWN) {
                0L -> TODAY
                1L -> YESTERDAY
                in 2L..6L -> WEEK
                in 7L..29L -> MONTH
                else -> OLDER
            }
        }
    }
}

internal fun groupAgentHistory(
    threads: List<AgentConversationThread>, period: AgentHistoryPeriod, today: LocalDate, zone: ZoneId,
): List<Pair<AgentHistorySection, List<AgentConversationThread>>> {
    val grouped = threads.sortedByDescending { it.updatedAtMillis }.mapNotNull { thread ->
        val age = thread.updatedAtMillis.takeIf { it > 0 }?.let {
            val lastUpdated = Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
            ChronoUnit.DAYS.between(lastUpdated, today).takeIf { days -> days >= 0 }
        }
        if (period.includes(age)) AgentHistorySection.fromAge(age) to thread else null
    }.groupBy({ it.first }, { it.second })
    return AgentHistorySection.entries.mapNotNull { section -> grouped[section]?.let { section to it } }
}
