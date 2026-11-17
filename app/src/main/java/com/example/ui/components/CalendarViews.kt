package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import com.example.ui.theme.appVerticalBrush
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class CalendarViewMode(val key: String, val label: String, val subtitle: String) {
    DAY("day", "Jour", "Timeline horaire détaillée"),
    THREE_DAYS("3days", "3 Jours", "Blocs sur 3 journées"),
    WEEK("week", "Semaine", "Ruban hebdomadaire"),
    MONTH("month", "Mois", "Grille mensuelle"),
    QUARTER("quarter", "Trimestre", "Vue 3 mois"),
    YEAR("year", "Année", "Vue annuelle complète");

    companion object {
        fun fromKey(key: String?): CalendarViewMode =
            entries.firstOrNull { it.key == key } ?: WEEK
    }
}

@Composable
fun CalendarModeSelectorRow(
    selectedMode: CalendarViewMode,
    onSelectMode: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CalendarViewMode.entries.forEach { mode ->
            val selected = selectedMode == mode
            FilterChip(
                selected = selected,
                onClick = { onSelectMode(mode) },
                label = {
                    Text(
                        text = mode.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = gradient.accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = gradient.accentColor
                ),
                modifier = Modifier.testTag("calendar_view_${mode.key}")
            )
        }
    }
}

@Composable
fun CalendarShowcaseCard(
    mode: CalendarViewMode,
    selectedDateIso: String,
    tasks: List<StudyTask>,
    onSelectDate: (String) -> Unit,
    onSelectMode: (CalendarViewMode) -> Unit,
    onToggleTask: (StudyTask) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val selectedDate = remember(selectedDateIso) {
        runCatching { LocalDate.parse(selectedDateIso) }.getOrElse { LocalDate.now() }
    }
    val today = LocalDate.now()
    val tasksByDate = remember(tasks) { tasks.groupBy { it.dueDateIso } }

    val periodTitle = remember(mode, selectedDate) {
        when (mode) {
            CalendarViewMode.DAY -> selectedDate.format(
                DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
            ).replaceFirstChar { it.uppercase() }

            CalendarViewMode.THREE_DAYS -> {
                val end = selectedDate.plusDays(2)
                "${selectedDate.dayOfMonth} – ${end.dayOfMonth} ${
                    end.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                }"
            }

            CalendarViewMode.WEEK -> {
                val start = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = start.plusDays(6)
                "Sem. ${start.dayOfMonth} – ${end.dayOfMonth} ${
                    end.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).replace(".", "")
                }"
            }

            CalendarViewMode.MONTH -> "${
                selectedDate.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    .replaceFirstChar { it.uppercase() }
            } ${selectedDate.year}"

            CalendarViewMode.QUARTER -> {
                val quarter = ((selectedDate.monthValue - 1) / 3) + 1
                "Trimestre T$quarter · ${selectedDate.year}"
            }

            CalendarViewMode.YEAR -> "Année ${selectedDate.year}"
        }
    }

    fun stepPeriod(direction: Long) {
        val next = when (mode) {
            CalendarViewMode.DAY -> selectedDate.plusDays(direction)
            CalendarViewMode.THREE_DAYS -> selectedDate.plusDays(direction * 3L)
            CalendarViewMode.WEEK -> selectedDate.plusWeeks(direction)
            CalendarViewMode.MONTH -> selectedDate.plusMonths(direction)
            CalendarViewMode.QUARTER -> selectedDate.plusMonths(direction * 3L)
            CalendarViewMode.YEAR -> selectedDate.plusYears(direction)
        }
        onSelectDate(next.toString())
    }

    AppleCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Unified Period Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { stepPeriod(-1L) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("calendar_prev_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Période précédente",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = periodTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selectedDate != today) {
                    Text(
                        text = "Revenir à aujourd'hui",
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient.accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectDate(today.toString()) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = { stepPeriod(1L) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("calendar_next_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Période suivante",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (mode) {
            CalendarViewMode.DAY -> DayTimelineView(
                date = selectedDate,
                dayTasks = tasksByDate[selectedDate.toString()].orEmpty(),
                onToggleTask = onToggleTask,
                onStartFocusForTask = onStartFocusForTask
            )

            CalendarViewMode.THREE_DAYS -> ThreeDayColumnsView(
                startDate = selectedDate,
                today = today,
                tasksByDate = tasksByDate,
                onSelectDate = { onSelectDate(it.toString()) }
            )

            CalendarViewMode.WEEK -> WeekStripCalendarView(
                selectedDate = selectedDate,
                today = today,
                tasksByDate = tasksByDate,
                onSelectDate = { onSelectDate(it.toString()) }
            )

            CalendarViewMode.MONTH -> MonthGridCalendarView(
                selectedDate = selectedDate,
                today = today,
                tasksByDate = tasksByDate,
                onSelectDate = { onSelectDate(it.toString()) }
            )

            CalendarViewMode.QUARTER -> QuarterMonthsCalendarView(
                selectedDate = selectedDate,
                today = today,
                tasksByDate = tasksByDate,
                onSelectDate = { date ->
                    onSelectDate(date.toString())
                },
                onOpenMonth = { monthDate ->
                    onSelectDate(monthDate.toString())
                    onSelectMode(CalendarViewMode.MONTH)
                }
            )

            CalendarViewMode.YEAR -> YearOverviewCalendarView(
                selectedDate = selectedDate,
                today = today,
                tasks = tasks,
                onSelectDate = { date ->
                    onSelectDate(date.toString())
                    onSelectMode(CalendarViewMode.MONTH)
                }
            )
        }
    }
}

@Composable
private fun DayTimelineView(
    date: LocalDate,
    dayTasks: List<StudyTask>,
    onToggleTask: (StudyTask) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit
) {
    val gradient = LocalLightGradient.current
    val sorted = remember(dayTasks) {
        dayTasks.sortedBy { it.scheduledTime.ifBlank { "23:59" } }
    }
    val timeBuckets = remember(sorted) {
        val scheduled = sorted.filter { it.scheduledTime.isNotBlank() }
        val flexible = sorted.filter { it.scheduledTime.isBlank() }
        scheduled to flexible
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_day_view"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chronological time-slot overview matching reference Day View
        val slots = listOf("09:00", "11:00", "14:00", "16:00", "19:00")
        if (sorted.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = gradient.softContainer.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aucun bloc planifié le ${date.dayOfMonth}/${date.monthValue}. Touchez + pour ajouter un créneau.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            val (scheduled, flexible) = timeBuckets
            scheduled.forEach { task ->
                DayTimelineBlock(
                    task = task,
                    onToggleTask = onToggleTask,
                    onStartFocusForTask = onStartFocusForTask
                )
            }
            if (flexible.isNotEmpty()) {
                Text(
                    text = "Créneaux flexibles",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                flexible.forEach { task ->
                    DayTimelineBlock(
                        task = task,
                        onToggleTask = onToggleTask,
                        onStartFocusForTask = onStartFocusForTask
                    )
                }
            }
            val occupiedHours = scheduled.mapNotNull { it.scheduledTime.take(2).toIntOrNull() }.toSet()
            val freeSlot = slots.firstOrNull { slot ->
                val h = slot.take(2).toIntOrNull() ?: -1
                h !in occupiedHours
            }
            if (freeSlot != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = freeSlot,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(44.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Text(
                        text = "Créneau libre",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DayTimelineBlock(
    task: StudyTask,
    onToggleTask: (StudyTask) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit
) {
    val gradient = LocalLightGradient.current
    val cat = TaskCategory.fromKey(task.category)
    val catColor = cat.accentColor()
    val blockHeightExtra = if (task.estimatedMinutes >= 50) 8.dp else 0.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .width(44.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = task.scheduledTime.ifBlank { "Libre" },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = catColor
            )
            Text(
                text = "${task.estimatedMinutes}m",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(catColor.copy(alpha = 0.09f))
                .border(0.8.dp, catColor.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp + blockHeightExtra),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(catColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${cat.label} · ${task.courseTitle.ifBlank { "Génie Logiciel" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = { onToggleTask(task) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Basculer la tâche",
                    tint = if (task.completed) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!task.completed) {
                IconButton(
                    onClick = { onStartFocusForTask(task) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Lancer Focus",
                        tint = gradient.accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreeDayColumnsView(
    startDate: LocalDate,
    today: LocalDate,
    tasksByDate: Map<String, List<StudyTask>>,
    onSelectDate: (LocalDate) -> Unit
) {
    val gradient = LocalLightGradient.current
    val days = remember(startDate) { listOf(startDate, startDate.plusDays(1), startDate.plusDays(2)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_3days_view"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { index, day ->
            val iso = day.toString()
            val dayTasks = tasksByDate[iso].orEmpty().sortedBy { it.scheduledTime.ifBlank { "23:59" } }
            val isSelected = index == 0
            val isToday = day == today
            val dayName = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
                .take(3)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSelected) gradient.softContainer.copy(alpha = 0.72f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                    .border(
                        width = if (isSelected) 1.2.dp else 0.8.dp,
                        color = if (isSelected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelectDate(day) }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$dayName ${day.dayOfMonth}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday || isSelected) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                    )
                    if (dayTasks.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = gradient.accentColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${dayTasks.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = gradient.accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (dayTasks.isEmpty()) {
                    Text(
                        text = "Journée libre",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    dayTasks.take(4).forEach { task ->
                        val catColor = TaskCategory.fromKey(task.category).accentColor()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = catColor.copy(alpha = 0.14f),
                            border = BorderStroke(0.6.dp, catColor.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
                                Text(
                                    text = "${task.scheduledTime.ifBlank { "Jour" }} · ${task.estimatedMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = catColor,
                                    maxLines = 1
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (dayTasks.size > 4) {
                        Text(
                            text = "+${dayTasks.size - 4} autres",
                            style = MaterialTheme.typography.labelSmall,
                            color = gradient.accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekStripCalendarView(
    selectedDate: LocalDate,
    today: LocalDate,
    tasksByDate: Map<String, List<StudyTask>>,
    onSelectDate: (LocalDate) -> Unit
) {
    val gradient = LocalLightGradient.current
    val weekStart = remember(selectedDate) {
        selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_week_view"),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            val iso = day.toString()
            val isSelected = day == selectedDate
            val isToday = day == today
            val dayTasks = tasksByDate[iso].orEmpty()
            val dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
                .take(3)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isSelected) Modifier.background(gradient.appBrush())
                        else if (isToday) Modifier.border(1.dp, gradient.accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        else Modifier
                    )
                    .clickable { onSelectDate(day) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else if (isToday) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(5.dp))
                CategoryDotsStrip(dayTasks = dayTasks, isSelected = isSelected)
            }
        }
    }
}

@Composable
private fun MonthGridCalendarView(
    selectedDate: LocalDate,
    today: LocalDate,
    tasksByDate: Map<String, List<StudyTask>>,
    onSelectDate: (LocalDate) -> Unit
) {
    val gradient = LocalLightGradient.current
    val ym = remember(selectedDate) { YearMonth.from(selectedDate) }
    val firstDayOfMonth = ym.atDay(1)
    val leadingBlanks = (firstDayOfMonth.dayOfWeek.value - 1).coerceIn(0, 6)
    val daysInMonth = ym.lengthOfMonth()
    val totalCells = ((leadingBlanks + daysInMonth + 6) / 7) * 7
    val weekdayHeaders = listOf("Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_month_view"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayHeaders.forEach { lbl ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        for (rowStart in 0 until totalCells step 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIdx = rowStart + col
                    val dayNum = cellIdx - leadingBlanks + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f).height(46.dp))
                    } else {
                        val date = ym.atDay(dayNum)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val dayTasks = tasksByDate[date.toString()].orEmpty()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) Modifier.background(gradient.appBrush())
                                    else if (isToday) Modifier.background(gradient.softContainer)
                                    else Modifier
                                )
                                .clickable { onSelectDate(date) }
                        ) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else if (isToday) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            CategoryDotsStrip(dayTasks = dayTasks, isSelected = isSelected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuarterMonthsCalendarView(
    selectedDate: LocalDate,
    today: LocalDate,
    tasksByDate: Map<String, List<StudyTask>>,
    onSelectDate: (LocalDate) -> Unit,
    onOpenMonth: (LocalDate) -> Unit
) {
    val gradient = LocalLightGradient.current
    val firstMonthNum = (((selectedDate.monthValue - 1) / 3) * 3) + 1
    val months = remember(selectedDate.year, firstMonthNum) {
        (0..2).map { YearMonth.of(selectedDate.year, firstMonthNum + it) }
    }

    Column(
        modifier = Modifier.fillMaxWidth().testTag("calendar_quarter_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        months.forEach { ym ->
            val monthTasks = tasksByDate.filterKeys { it.startsWith(ym.toString()) }.values.flatten()
            val pendingCount = monthTasks.count { !it.completed }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                border = BorderStroke(
                    width = if (ym == YearMonth.from(selectedDate)) 1.2.dp else 0.7.dp,
                    color = if (ym == YearMonth.from(selectedDate)) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ym.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (ym == YearMonth.from(selectedDate)) gradient.accentColor else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onOpenMonth(ym.atDay(1)) }.padding(vertical = 2.dp)
                        )
                        Text(
                            text = when (pendingCount) {
                                0 -> "Aucune tâche en attente  ›"
                                1 -> "1 tâche à faire  ›"
                                else -> "$pendingCount tâches à faire  ›"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onOpenMonth(ym.atDay(1)) }.padding(4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    MiniMonthGrid(
                        month = ym,
                        selectedDate = selectedDate,
                        today = today,
                        tasksByDate = tasksByDate,
                        cellHeight = 48.dp,
                        dayTextSize = 12,
                        onSelectDate = onSelectDate
                    )
                }
            }
        }
    }
}

@Composable
private fun YearOverviewCalendarView(
    selectedDate: LocalDate,
    today: LocalDate,
    tasks: List<StudyTask>,
    onSelectDate: (LocalDate) -> Unit
) {
    val gradient = LocalLightGradient.current
    val year = selectedDate.year
    val months = remember(year) { (1..12).map { YearMonth.of(year, it) } }
    val tasksByDate = remember(tasks) { tasks.filter { it.dueDateIso.isNotBlank() }.groupBy { it.dueDateIso } }

    Column(
        modifier = Modifier.fillMaxWidth().testTag("calendar_year_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        months.chunked(2).forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMonths.forEach { ym ->
                    val monthTasks = tasks.filter { it.dueDateIso.startsWith(ym.toString()) }
                    val isCurrent = ym == YearMonth.from(today)
                    val isSelectedMonth = ym == YearMonth.from(selectedDate)
                    Surface(
                        onClick = { onSelectDate(ym.atDay(1)) },
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isSelectedMonth -> gradient.softContainer
                            isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                        },
                        border = BorderStroke(
                            width = if (isSelectedMonth || isCurrent) 1.2.dp else 0.7.dp,
                            color = if (isSelectedMonth) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ym.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                                        .replace(".", "").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelectedMonth || isCurrent) gradient.accentColor else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onSelectDate(ym.atDay(1)) }.padding(vertical = 2.dp)
                                )
                                if (monthTasks.isNotEmpty()) {
                                    Text("${monthTasks.size}", style = MaterialTheme.typography.labelSmall, color = gradient.accentColor)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            MiniMonthGrid(
                                month = ym,
                                selectedDate = selectedDate,
                                today = today,
                                tasksByDate = tasksByDate,
                                cellHeight = 24.dp,
                                dayTextSize = 9,
                                onSelectDate = null
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Monday-first, complete-week cell list used by both compact and full-size month grids. */
internal fun calendarMonthGridDates(month: YearMonth): List<LocalDate?> {
    val leadingDays = month.atDay(1).dayOfWeek.value - 1
    val cellCount = ((leadingDays + month.lengthOfMonth() + 6) / 7) * 7
    return (0 until cellCount).map { index ->
        val dayOfMonth = index - leadingDays + 1
        if (dayOfMonth in 1..month.lengthOfMonth()) month.atDay(dayOfMonth) else null
    }
}

/** A responsive seven-column month grid shared by the full-size quarter and compact year views. */
@Composable
private fun MiniMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    tasksByDate: Map<String, List<StudyTask>>,
    cellHeight: androidx.compose.ui.unit.Dp,
    dayTextSize: Int,
    onSelectDate: ((LocalDate) -> Unit)?
) {
    val gradient = LocalLightGradient.current
    val weekdayLabels = listOf("L", "Ma", "Me", "J", "V", "S", "D")
    val weekdayHeaderHeight = if (cellHeight >= 40.dp) 24.dp else cellHeight
    Row(Modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
            Box(Modifier.weight(1f).height(weekdayHeaderHeight), contentAlignment = Alignment.Center) {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    val weeks = remember(month) { calendarMonthGridDates(month).chunked(7) }
    weeks.forEach { week ->
        Row(Modifier.fillMaxWidth()) {
            week.forEach { date ->
                if (date == null) {
                    Spacer(Modifier.weight(1f).height(cellHeight))
                } else {
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val taskCount = tasksByDate[date.toString()].orEmpty().size
                    val hasTasks = taskCount > 0
                    val accessibilityLabel = remember(date, isToday, isSelected, taskCount, onSelectDate != null) {
                        if (onSelectDate == null) "" else buildString {
                            append(date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)).replaceFirstChar { it.uppercase() })
                            if (isToday) append(", aujourd'hui")
                            if (isSelected) append(", sélectionné")
                            if (taskCount > 0) append(", $taskCount tâche${if (taskCount == 1) "" else "s"}")
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(cellHeight).padding(1.dp)
                            .clip(CircleShape)
                            .background(when {
                                isSelected -> gradient.accentColor
                                isToday -> gradient.softContainer
                                hasTasks -> gradient.accentColor.copy(alpha = 0.14f)
                                else -> Color.Transparent
                            })
                            .then(
                                onSelectDate?.let { selectDate ->
                                    Modifier.semantics(mergeDescendants = true) {
                                        contentDescription = accessibilityLabel
                                    }.clickable { selectDate(date) }
                                } ?: Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = dayTextSize.sp),
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else if (isToday) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                        )
                        if (hasTasks && !isSelected && !isToday) {
                            Box(
                                Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp).size(3.dp)
                                    .clip(CircleShape).background(gradient.accentColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDotsStrip(
    dayTasks: List<StudyTask>,
    isSelected: Boolean
) {
    val distinctCategories = remember(dayTasks) {
        dayTasks.map { TaskCategory.fromKey(it.category) }.distinct().take(3)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(6.dp)
    ) {
        distinctCategories.forEach { cat ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else cat.accentColor())
            )
        }
    }
}
