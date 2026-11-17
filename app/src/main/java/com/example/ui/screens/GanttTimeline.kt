package com.example.ui.screens

import com.example.data.model.StudyProject
import com.example.data.model.StudyTask
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** Scale presets deliberately cover intraday planning through quarter-level project reviews. */
enum class GanttScale(val key: String, val label: String) {
    DAY("day", "Jour · heures"),
    WEEK("week", "Semaine"),
    MONTH("month", "Mois"),
    QUARTER("quarter", "Trimestre");

    companion object {
        fun fromKey(key: String?): GanttScale = entries.firstOrNull { it.key == key } ?: WEEK
    }
}

data class GanttWindow(val start: LocalDate, val endInclusive: LocalDate, val columnCount: Int) {
    fun dateColumn(date: LocalDate): Int? {
        if (date < start || date > endInclusive) return null
        val daysFromStart = ChronoUnit.DAYS.between(start, date).toInt()
        val index = if (columnCount == 13) daysFromStart / 7 else daysFromStart
        return index.coerceIn(0, columnCount - 1)
    }
}

data class GanttProjectSpan(
    val start: LocalDate,
    val endInclusive: LocalDate
)

data class GanttTaskSpan(
    val start: LocalDate,
    val endInclusive: LocalDate,
    val startMinuteOfDay: Int,
    val durationMinutes: Int,
    val isMilestone: Boolean
)

fun ganttWindow(anchor: LocalDate, scale: GanttScale): GanttWindow = when (scale) {
    GanttScale.DAY -> GanttWindow(anchor, anchor, 24)
    GanttScale.WEEK -> {
        val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        GanttWindow(start, start.plusDays(6), 7)
    }
    GanttScale.MONTH -> {
        val start = anchor.withDayOfMonth(1)
        GanttWindow(start, start.withDayOfMonth(start.lengthOfMonth()), start.lengthOfMonth())
    }
    GanttScale.QUARTER -> {
        val quarterStartMonth = ((anchor.monthValue - 1) / 3) * 3 + 1
        val start = LocalDate.of(anchor.year, quarterStartMonth, 1)
        GanttWindow(start, start.plusMonths(3).minusDays(1), 13)
    }
}

fun StudyProject.ganttSpan(): GanttProjectSpan? {
    val start = runCatching { LocalDate.parse(startDateIso) }.getOrNull() ?: return null
    val due = runCatching { LocalDate.parse(dueDateIso) }.getOrNull() ?: return null
    if (due.isBefore(start)) return null
    return GanttProjectSpan(start, due)
}

fun GanttWindow.projectColumnRange(project: StudyProject): IntRange? {
    val span = project.ganttSpan() ?: return null
    if (span.endInclusive < start || span.start > endInclusive) return null
    if (columnCount == 24) return 0..23
    val first = dateColumn(maxOf(start, span.start)) ?: return null
    val last = dateColumn(minOf(endInclusive, span.endInclusive)) ?: return null
    return first..last
}

/** Existing tasks without a start date remain valid and are shown as one-day items on their due date. */
fun StudyTask.ganttSpan(): GanttTaskSpan? {
    val due = runCatching { LocalDate.parse(dueDateIso) }.getOrNull() ?: return null
    val start = runCatching { LocalDate.parse(startDateIso) }.getOrNull()?.coerceAtMost(due) ?: due
    val time = scheduledTime.split(":").let { parts ->
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        hour * 60 + minute
    }
    return GanttTaskSpan(
        start = start,
        endInclusive = due,
        startMinuteOfDay = time,
        durationMinutes = estimatedMinutes.coerceIn(5L, 600L).toInt(),
        isMilestone = category == "milestone"
    )
}

fun GanttWindow.contains(task: StudyTask): Boolean {
    val span = task.ganttSpan() ?: return false
    return span.endInclusive >= start && span.start <= endInclusive
}

/** Returns start/end in hour-column units for the intraday view. */
fun GanttWindow.taskTimeColumnRange(task: StudyTask): Pair<Float, Float>? {
    if (columnCount != 24) return null
    val span = task.ganttSpan() ?: return null
    if (span.start > start || span.endInclusive < start) return null
    val startMinute = if (span.start < start) 0 else span.startMinuteOfDay
    val endMinute = if (span.endInclusive > start || span.start < start) {
        1440
    } else {
        (startMinute + span.durationMinutes).coerceAtMost(1440)
    }
    val first = startMinute / 60f
    val last = (endMinute / 60f).coerceAtLeast(first + 0.01f).coerceAtMost(24f)
    return first to last
}

fun GanttWindow.taskColumnRange(task: StudyTask): IntRange? {
    val span = task.ganttSpan() ?: return null
    val clippedStart = maxOf(start, span.start)
    val clippedEnd = minOf(endInclusive, span.endInclusive)
    if (clippedStart > clippedEnd) return null
    val first = dateColumn(clippedStart) ?: return null
    val last = dateColumn(clippedEnd) ?: return null
    return first..last
}
