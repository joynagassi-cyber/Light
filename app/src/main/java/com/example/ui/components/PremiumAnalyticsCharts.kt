package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.FocusProfile
import com.example.data.model.FocusSession
import com.example.data.model.StudyHabit
import com.example.data.model.StudyTask
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.LocalThemeSurfaceConfig
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

data class ActivityHeatmapCell(
    val date: LocalDate,
    val weekIndex: Int,
    val dayOfWeekIndex: Int,
    val focusMinutes: Int,
    val sessionCount: Int,
    val completedTasks: Int,
    val completedHabits: Int,
    val totalScore: Int,
    val intensityLevel: Int
)

data class StackedModeDay(
    val date: LocalDate,
    val shortLabel: String,
    val fullLabel: String,
    val pomodoroMinutes: Int,
    val deepCodeMinutes: Int,
    val stopwatchMinutes: Int
) {
    val totalMinutes: Int get() = pomodoroMinutes + deepCodeMinutes + stopwatchMinutes
}

data class WeekdayRhythmPoint(
    val dayOfWeekIndex: Int,
    val shortLabel: String,
    val fullLabel: String,
    val totalMinutes: Int,
    val sessionCount: Int
)

data class DepthOrProfileSlice(
    val id: String,
    val label: String,
    val minutes: Int,
    val sessions: Int,
    val color: Color
)

data class MasteryAxisScore(
    val key: String,
    val label: String,
    val scorePercent: Int,
    val detail: String,
    val color: Color
)

data class DailyVelocityPoint(
    val date: LocalDate,
    val shortLabel: String,
    val completedCount: Int,
    val pendingCount: Int
)

data class CourseMatrixBubble(
    val courseId: String,
    val code: String,
    val title: String,
    val progressPercent: Int,
    val grade: Double,
    val completedHours: Int,
    val targetHours: Int,
    val color: Color
)

fun buildActivityHeatmapGrid(
    sessions: List<FocusSession>,
    tasks: List<StudyTask> = emptyList(),
    habits: List<StudyHabit> = emptyList(),
    weeks: Int = 12,
    today: LocalDate = LocalDate.now()
): List<ActivityHeatmapCell> {
    val safeWeeks = weeks.coerceIn(4, 24)
    val endWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val startMonday = endWeekMonday.minusWeeks((safeWeeks - 1).toLong())

    val studySessions = sessions.filter { it.mode != "short_break" && it.durationMinutes > 0 }
    val sessionsByDate = studySessions.groupBy { it.dateIso }
    val completedTasksByDate = tasks.filter { it.completed && it.dueDateIso.isNotBlank() }.groupBy { it.dueDateIso }
    val habitsCountByDate = mutableMapOf<String, Int>()
    habits.forEach { habit ->
        habit.completedDates.distinct().forEach { dateIso ->
            habitsCountByDate[dateIso] = (habitsCountByDate[dateIso] ?: 0) + 1
        }
    }

    val rawCells = mutableListOf<ActivityHeatmapCell>()
    for (week in 0 until safeWeeks) {
        for (dayIdx in 0 until 7) {
            val date = startMonday.plusWeeks(week.toLong()).plusDays(dayIdx.toLong())
            if (date.isAfter(today)) {
                rawCells += ActivityHeatmapCell(
                    date = date,
                    weekIndex = week,
                    dayOfWeekIndex = dayIdx,
                    focusMinutes = 0,
                    sessionCount = 0,
                    completedTasks = 0,
                    completedHabits = 0,
                    totalScore = 0,
                    intensityLevel = -1
                )
            } else {
                val iso = date.toString()
                val daySessions = sessionsByDate[iso].orEmpty()
                val focusMins = daySessions.sumOf { it.durationMinutes.toInt() }
                val doneTasks = completedTasksByDate[iso]?.size ?: 0
                val doneHabits = habitsCountByDate[iso] ?: 0
                val score = focusMins + (doneTasks * 20) + (doneHabits * 15)
                rawCells += ActivityHeatmapCell(
                    date = date,
                    weekIndex = week,
                    dayOfWeekIndex = dayIdx,
                    focusMinutes = focusMins,
                    sessionCount = daySessions.size,
                    completedTasks = doneTasks,
                    completedHabits = doneHabits,
                    totalScore = score,
                    intensityLevel = 0
                )
            }
        }
    }

    val peakScore = rawCells.filter { it.intensityLevel >= 0 }.maxOfOrNull { it.totalScore }?.coerceAtLeast(60) ?: 60
    return rawCells.map { cell ->
        if (cell.intensityLevel < 0) {
            cell
        } else {
            val ratio = cell.totalScore.toFloat() / peakScore.toFloat()
            val level = when {
                cell.totalScore <= 0 -> 0
                ratio <= 0.25f -> 1
                ratio <= 0.50f -> 2
                ratio <= 0.75f -> 3
                else -> 4
            }
            cell.copy(intensityLevel = level)
        }
    }
}

fun buildModeStackedBars(
    sessions: List<FocusSession>,
    windowDays: Int,
    today: LocalDate = LocalDate.now()
): List<StackedModeDay> {
    val safeDays = windowDays.coerceIn(1, 30)
    val grouped = sessions
        .filter { it.mode != "short_break" && it.durationMinutes > 0 }
        .groupBy { it.dateIso }

    return (safeDays - 1 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val dayList = grouped[day.toString()].orEmpty()
        var pomo = 0
        var deep = 0
        var watch = 0
        dayList.forEach { s ->
            val mins = s.durationMinutes.toInt()
            when (s.mode) {
                "deep_code" -> deep += mins
                "stopwatch" -> watch += mins
                else -> pomo += mins
            }
        }
        val shortLabel = if (safeDays <= 7) {
            day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
                .take(3)
        } else {
            "${day.dayOfMonth}/${day.monthValue}"
        }
        val fullLabel = "${
            day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
        } ${day.dayOfMonth} ${
            day.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).replace(".", "")
        }"
        StackedModeDay(
            date = day,
            shortLabel = shortLabel,
            fullLabel = fullLabel,
            pomodoroMinutes = pomo,
            deepCodeMinutes = deep,
            stopwatchMinutes = watch
        )
    }
}

fun buildWeekdayRhythm(sessions: List<FocusSession>): List<WeekdayRhythmPoint> {
    val study = sessions.filter { it.mode != "short_break" && it.durationMinutes > 0 }
    val buckets = Array(7) { mutableListOf<FocusSession>() }
    study.forEach { s ->
        val parsed = runCatching { LocalDate.parse(s.dateIso) }.getOrNull() ?: return@forEach
        val idx = (parsed.dayOfWeek.value - 1).coerceIn(0, 6)
        buckets[idx].add(s)
    }
    return (0 until 7).map { idx ->
        val dow = DayOfWeek.of(idx + 1)
        val short = dow.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
            .replace(".", "")
            .replaceFirstChar { it.uppercase() }
            .take(3)
        val full = dow.getDisplayName(TextStyle.FULL, Locale.FRENCH)
            .replaceFirstChar { it.uppercase() }
        val list = buckets[idx]
        WeekdayRhythmPoint(
            dayOfWeekIndex = idx,
            shortLabel = short,
            fullLabel = full,
            totalMinutes = list.sumOf { it.durationMinutes.toInt() },
            sessionCount = list.size
        )
    }
}

fun buildSessionDepthSlices(
    sessions: List<FocusSession>,
    accentColor: Color
): List<DepthOrProfileSlice> {
    val study = sessions.filter { it.mode != "short_break" && it.durationMinutes > 0 }
    val shortSessions = study.filter { it.durationMinutes < 20L }
    val standardSessions = study.filter { it.durationMinutes in 20L..39L }
    val deepSessions = study.filter { it.durationMinutes >= 40L }
    return listOf(
        DepthOrProfileSlice(
            id = "short",
            label = "Sprint (<20m)",
            minutes = shortSessions.sumOf { it.durationMinutes.toInt() },
            sessions = shortSessions.size,
            color = AppleSky
        ),
        DepthOrProfileSlice(
            id = "standard",
            label = "Pomodoro (20–39m)",
            minutes = standardSessions.sumOf { it.durationMinutes.toInt() },
            sessions = standardSessions.size,
            color = accentColor
        ),
        DepthOrProfileSlice(
            id = "deep",
            label = "Deep Work (40m+)",
            minutes = deepSessions.sumOf { it.durationMinutes.toInt() },
            sessions = deepSessions.size,
            color = AppleIndigo
        )
    )
}

fun buildFocusProfileSlices(
    sessions: List<FocusSession>,
    profiles: List<FocusProfile>,
    accentColor: Color
): List<DepthOrProfileSlice> {
    val study = sessions.filter { it.mode != "short_break" && it.durationMinutes > 0 }
    val profileById = profiles.associateBy { it.id }
    val grouped = study.groupBy { s ->
        if (s.focusProfileId.isNotBlank()) {
            profileById[s.focusProfileId]?.title ?: s.focusProfileTitle.ifBlank { "Profil Focus" }
        } else {
            "Session libre"
        }
    }
    val paletteColors = listOf(accentColor, AppleIndigo, AppleEmerald, AppleAmber, AppleSky, AppleRose)
    return grouped.entries
        .sortedByDescending { entry -> entry.value.sumOf { it.durationMinutes } }
        .take(5)
        .mapIndexed { idx, (label, list) ->
            val matchingProfile = list.firstNotNullOfOrNull { profileById[it.focusProfileId] }
            val color = if (matchingProfile != null) {
                catalogIconColor(matchingProfile.colorIndex)
            } else {
                paletteColors[idx % paletteColors.size]
            }
            DepthOrProfileSlice(
                id = "fp_$idx",
                label = label,
                minutes = list.sumOf { it.durationMinutes.toInt() },
                sessions = list.size,
                color = color
            )
        }
}

fun buildFiveAxisMasteryScores(
    tasks: List<StudyTask>,
    sessions: List<FocusSession>,
    courses: List<Course>,
    habits: List<StudyHabit>,
    accentColor: Color,
    today: LocalDate = LocalDate.now()
): List<MasteryAxisScore> {
    val totalTasks = tasks.size
    val doneTasks = tasks.count { it.completed }
    val executionPct = if (totalTasks > 0) ((doneTasks * 100f) / totalTasks).roundToInt().coerceIn(0, 100) else 0

    val cutoff14 = today.minusDays(13)
    val recentStudy = sessions.filter { s ->
        s.mode != "short_break" && s.durationMinutes > 0 &&
            (runCatching { LocalDate.parse(s.dateIso) }.getOrNull()?.let { !it.isBefore(cutoff14) } ?: false)
    }
    val recentFocusMinutes = recentStudy.sumOf { it.durationMinutes.toInt() }
    val focusPct = ((recentFocusMinutes / 420f) * 100f).roundToInt().coerceIn(0, 100)

    val avgGrade = if (courses.isNotEmpty()) courses.map { it.grade }.average() else 0.0
    val gradePct = if (courses.isNotEmpty()) ((avgGrade / 20.0) * 100.0).roundToInt().coerceIn(0, 100) else 0

    val activeDates14 = recentStudy.map { it.dateIso }.toSet().size
    val consistencyPct = ((activeDates14 / 10f) * 100f).roundToInt().coerceIn(0, 100)

    val last7Isos = (0L..6L).map { today.minusDays(it).toString() }.toSet()
    val habitPct = if (habits.isNotEmpty()) {
        val possible = habits.size * 7
        val done = habits.sumOf { h -> h.completedDates.count { it in last7Isos } }
        ((done * 100f) / possible.coerceAtLeast(1)).roundToInt().coerceIn(0, 100)
    } else {
        (consistencyPct * 0.8f).roundToInt().coerceIn(0, 100)
    }

    return listOf(
        MasteryAxisScore("execution", "Exécution", executionPct, "$doneTasks/$totalTasks tâches", AppleEmerald),
        MasteryAxisScore("focus", "Focus", focusPct, "${recentFocusMinutes}m / 14j", accentColor),
        MasteryAxisScore("grade", "Moyenne", gradePct, String.format(Locale.FRANCE, "%.1f/20", avgGrade), AppleIndigo),
        MasteryAxisScore("regularity", "Régularité", consistencyPct, "$activeDates14/14 j actifs", AppleAmber),
        MasteryAxisScore("habits", "Habitudes", habitPct, if (habits.isNotEmpty()) "${habits.size} rituels" else "Rythme", AppleSky)
    )
}

fun buildDailyVelocitySeries(
    tasks: List<StudyTask>,
    days: Int = 7,
    today: LocalDate = LocalDate.now()
): List<DailyVelocityPoint> {
    val safeDays = days.coerceIn(1, 14)
    val grouped = tasks.groupBy { it.dueDateIso }
    return (safeDays - 1 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val dayTasks = grouped[day.toString()].orEmpty()
        val label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
            .replace(".", "")
            .replaceFirstChar { it.uppercase() }
            .take(3)
        DailyVelocityPoint(
            date = day,
            shortLabel = label,
            completedCount = dayTasks.count { it.completed },
            pendingCount = dayTasks.count { !it.completed }
        )
    }
}

fun buildCoursePerformancePoints(courses: List<Course>): List<CourseMatrixBubble> {
    return courses.map { course ->
        val target = course.targetMinutes.coerceAtLeast(60L)
        val pct = ((course.completedMinutes * 100f) / target.toFloat()).roundToInt().coerceIn(0, 100)
        CourseMatrixBubble(
            courseId = course.id,
            code = course.code.ifBlank { course.title.take(4).uppercase() },
            title = course.title,
            progressPercent = pct,
            grade = course.grade.coerceIn(0.0, 20.0),
            completedHours = (course.completedMinutes / 60L).toInt(),
            targetHours = (target / 60L).toInt().coerceAtLeast(1),
            color = catalogIconColor(course.colorIndex)
        )
    }
}

@Composable
fun FocusActivityHeatmapChart(
    cells: List<ActivityHeatmapCell>,
    showCombinedDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    if (cells.isEmpty()) return

    val weeksCount = remember(cells) { (cells.maxOfOrNull { it.weekIndex } ?: 0) + 1 }
    val validCells = remember(cells) { cells.filter { it.intensityLevel >= 0 } }
    var selectedDateIso by rememberSaveable { mutableStateOf(validCells.lastOrNull()?.date?.toString() ?: "") }
    val selectedCell = validCells.firstOrNull { it.date.toString() == selectedDateIso } ?: validCells.lastOrNull()
    val activeDaysCount = remember(validCells) { validCells.count { it.intensityLevel > 0 } }

    val monthLabels = remember(cells, weeksCount) {
        val byWeek = cells.filter { it.dayOfWeekIndex == 0 }.associateBy { it.weekIndex }
        var lastMonth = -1
        (0 until weeksCount).map { w ->
            val d = byWeek[w]?.date
            if (d != null && d.monthValue != lastMonth) {
                lastMonth = d.monthValue
                d.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                    .replace(".", "")
                    .replaceFirstChar { it.uppercase() }
            } else ""
        }
    }

    val emptyCellColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOled) 0.75f else 0.72f)
    val levelColors = remember(gradient.accentColor, emptyCellColor, isOled) {
        listOf(
            emptyCellColor,
            gradient.accentColor.copy(alpha = if (isOled) 0.28f else 0.22f),
            gradient.accentColor.copy(alpha = if (isOled) 0.48f else 0.44f),
            gradient.accentColor.copy(alpha = if (isOled) 0.74f else 0.72f),
            gradient.accentColor
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_analytics_heatmap"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Callout pill for tapped heatmap day
        selectedCell?.let { cell ->
            val formattedDate = remember(cell.date) {
                val dow = cell.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                    .replace(".", "")
                    .replaceFirstChar { it.uppercase() }
                val month = cell.date.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).replace(".", "")
                "$dow ${cell.date.dayOfMonth} $month"
            }
            val detailText = if (showCombinedDetails) {
                "${formatCompactMins(cell.focusMinutes)} · ${cell.completedTasks} tâche(s) · ${cell.completedHabits} hab."
            } else {
                if (cell.sessionCount > 0) "${cell.sessionCount} session(s) Focus" else "Aucune session enregistrée"
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isOled) 0.45f else 0.75f),
                border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.24f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_heatmap_callout")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = detailText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (showCombinedDetails) "${cell.totalScore} pts" else formatCompactMins(cell.focusMinutes),
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = gradient.accentColor
                    )
                }
            }
        }

        // Month labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(16.dp)
                    .height(128.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEachIndexed { idx, dayChar ->
                    Text(
                        text = if (idx % 2 == 0) dayChar else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val selectionRingColor = MaterialTheme.colorScheme.onSurface
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(128.dp)
                    .testTag("focus_heatmap_canvas")
                    .pointerInput(cells) {
                        detectTapGestures { offset ->
                            if (weeksCount > 0 && size.width > 0 && size.height > 0) {
                                val col = ((offset.x / size.width.toFloat()) * weeksCount).toInt().coerceIn(0, weeksCount - 1)
                                val row = ((offset.y / size.height.toFloat()) * 7f).toInt().coerceIn(0, 6)
                                cells.firstOrNull { it.weekIndex == col && it.dayOfWeekIndex == row && it.intensityLevel >= 0 }
                                    ?.let { selectedDateIso = it.date.toString() }
                            }
                        }
                    }
            ) {
                val gap = 4.dp.toPx()
                val cellW = ((size.width - gap * (weeksCount - 1)) / weeksCount.toFloat()).coerceAtLeast(4f)
                val cellH = ((size.height - gap * 6f) / 7f).coerceAtLeast(4f)
                val cellSize = minOf(cellW, cellH)
                val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                val gridOriginX = ((size.width - (cellSize * weeksCount + gap * (weeksCount - 1))) / 2f).coerceAtLeast(0f)

                cells.forEach { cell ->
                    val x = gridOriginX + cell.weekIndex * (cellSize + gap)
                    val y = cell.dayOfWeekIndex * (cellSize + gap)
                    if (cell.intensityLevel < 0) {
                        drawRoundRect(
                            color = emptyCellColor.copy(alpha = 0.22f),
                            topLeft = Offset(x, y),
                            size = Size(cellSize, cellSize),
                            cornerRadius = corner
                        )
                    } else {
                        val fillColor = levelColors[cell.intensityLevel.coerceIn(0, 4)]
                        drawRoundRect(
                            color = fillColor,
                            topLeft = Offset(x, y),
                            size = Size(cellSize, cellSize),
                            cornerRadius = corner
                        )
                        if (cell.date.toString() == selectedDateIso) {
                            drawRoundRect(
                                color = selectionRingColor,
                                topLeft = Offset(x - 1.dp.toPx(), y - 1.dp.toPx()),
                                size = Size(cellSize + 2.dp.toPx(), cellSize + 2.dp.toPx()),
                                cornerRadius = corner,
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$activeDaysCount jours actifs sur ${weeksCount * 7}j",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Moins",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                levelColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
                Text(
                    text = "Plus",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StackedModeBarChart(
    days: List<StackedModeDay>,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    if (days.isEmpty()) return

    var selectedIdx by remember(days.size) { mutableIntStateOf(days.lastIndex) }
    val safeIdx = selectedIdx.coerceIn(0, days.lastIndex)
    val selectedDay = days[safeIdx]
    val maxTotal = (days.maxOfOrNull { it.totalMinutes } ?: 60).coerceAtLeast(30)

    val animatedTotals = days.mapIndexed { idx, d ->
        val anim by animateFloatAsState(
            targetValue = (d.totalMinutes.toFloat() / maxTotal.toFloat()).coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 240f),
            label = "stackedDay_$idx"
        )
        anim
    }

    val pomoColor = gradient.accentColor
    val deepColor = AppleIndigo
    val watchColor = AppleEmerald
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOled) 0.55f else 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_analytics_stacked_bar_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOled) 0.65f else 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedDay.fullLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pomo ${selectedDay.pomodoroMinutes}m · Deep ${selectedDay.deepCodeMinutes}m · Chrono ${selectedDay.stopwatchMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatCompactMins(selectedDay.totalMinutes),
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = pomoColor
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(days.size) {
                    detectTapGestures { offset ->
                        if (size.width > 0 && days.isNotEmpty()) {
                            val idx = ((offset.x / size.width.toFloat()) * days.size).toInt().coerceIn(0, days.lastIndex)
                            selectedIdx = idx
                        }
                    }
                }
                .pointerInput(days.size) {
                    detectHorizontalDragGestures { change, _ ->
                        if (size.width > 0 && days.isNotEmpty()) {
                            val idx = ((change.position.x / size.width.toFloat()) * days.size).toInt().coerceIn(0, days.lastIndex)
                            selectedIdx = idx
                        }
                    }
                }
        ) {
            val topPad = 8.dp.toPx()
            val bottomPad = 6.dp.toPx()
            val usableH = (size.height - topPad - bottomPad).coerceAtLeast(1f)
            val bottomY = topPad + usableH
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 7f), 0f)
            listOf(0f, 0.5f, 1f).forEach { f ->
                val y = topPad + usableH * (1f - f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = if (f > 0f) dash else null
                )
            }

            val slotW = size.width / days.size.toFloat()
            val barW = (slotW * 0.56f).coerceIn(6.dp.toPx(), 24.dp.toPx())
            val corner = CornerRadius(barW / 2f, barW / 2f)

            days.forEachIndexed { idx, day ->
                val x = idx * slotW + (slotW - barW) / 2f
                val isSel = idx == safeIdx
                drawRoundRect(
                    color = if (isSel) pomoColor.copy(alpha = 0.14f) else trackColor,
                    topLeft = Offset(x, topPad),
                    size = Size(barW, usableH),
                    cornerRadius = corner
                )

                val totalMins = day.totalMinutes
                if (totalMins > 0) {
                    val totalBarH = (usableH * animatedTotals[idx]).coerceAtLeast(4.dp.toPx())
                    var curBottom = bottomY
                    val segments = listOf(
                        day.pomodoroMinutes to pomoColor,
                        day.deepCodeMinutes to deepColor,
                        day.stopwatchMinutes to watchColor
                    ).filter { it.first > 0 }

                    segments.forEach { (mins, color) ->
                        val segH = (totalBarH * (mins.toFloat() / totalMins.toFloat())).coerceAtLeast(3.dp.toPx())
                        val segTop = (curBottom - segH).coerceAtLeast(topPad)
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, segTop),
                            size = Size(barW, (curBottom - segTop).coerceAtLeast(2f)),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        curBottom = segTop - 1.5.dp.toPx()
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = when {
                days.size <= 7 -> 1
                days.size <= 14 -> 3
                else -> 6
            }
            days.forEachIndexed { idx, day ->
                if (idx == safeIdx || idx == 0 || idx == days.lastIndex || idx % step == 0) {
                    Text(
                        text = day.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (idx == safeIdx) FontWeight.Bold else FontWeight.Normal,
                        color = if (idx == safeIdx) pomoColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { selectedIdx = idx }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ChartLegendDot("Pomodoro", pomoColor)
            ChartLegendDot("Deep Code", deepColor)
            ChartLegendDot("Chronomètre", watchColor)
        }
    }
}

@Composable
fun WeekdayRadarAndBarsChart(
    points: List<WeekdayRhythmPoint>,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    if (points.size != 7) return

    var viewAsRadar by rememberSaveable { mutableStateOf(true) }
    val peakDay = remember(points) { points.maxByOrNull { it.totalMinutes } }
    var selectedDayIdx by rememberSaveable { mutableIntStateOf(peakDay?.dayOfWeekIndex ?: 0) }
    val selectedDay = points.getOrElse(selectedDayIdx) { points.first() }
    val maxMins = (points.maxOfOrNull { it.totalMinutes } ?: 60).coerceAtLeast(30)

    val animatedRatios = points.mapIndexed { idx, p ->
        val anim by animateFloatAsState(
            targetValue = (p.totalMinutes.toFloat() / maxMins.toFloat()).coerceIn(0.06f, 1f),
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 220f),
            label = "radarDay_$idx"
        )
        anim
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_analytics_radar_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (peakDay != null && peakDay.totalMinutes > 0) {
                        "Pic : ${peakDay.fullLabel} (${formatCompactMins(peakDay.totalMinutes)})"
                    } else {
                        "Rythme du lundi au dimanche"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = gradient.accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    listOf(true to "Radar", false to "Barres").forEach { (isRadar, label) ->
                        val active = viewAsRadar == isRadar
                        Surface(
                            onClick = { viewAsRadar = isRadar },
                            shape = CircleShape,
                            color = if (active) gradient.accentColor else Color.Transparent,
                            modifier = Modifier.testTag("weekday_mode_${if (isRadar) "radar" else "bars"}")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        if (viewAsRadar) {
            val webColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
            val accent = gradient.accentColor
            val mid = gradient.midColor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(196.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val radius = minOf(cx, cy) * 0.74f
                                var bestIdx = 0
                                var bestDist = Float.MAX_VALUE
                                for (i in 0 until 7) {
                                    val angle = (-PI / 2.0 + i * (2.0 * PI / 7.0)).toFloat()
                                    val vx = cx + cos(angle) * radius
                                    val vy = cy + sin(angle) * radius
                                    val d = hypot(offset.x - vx, offset.y - vy)
                                    if (d < bestDist) {
                                        bestDist = d
                                        bestIdx = i
                                    }
                                }
                                selectedDayIdx = bestIdx
                            }
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (minOf(size.width, size.height) / 2f) * 0.78f
                    val sides = 7

                    listOf(0.25f, 0.5f, 0.75f, 1f).forEach { ring ->
                        val ringPath = Path()
                        for (i in 0 until sides) {
                            val angle = (-PI / 2.0 + i * (2.0 * PI / sides)).toFloat()
                            val pt = Offset(
                                center.x + cos(angle) * radius * ring,
                                center.y + sin(angle) * radius * ring
                            )
                            if (i == 0) ringPath.moveTo(pt.x, pt.y) else ringPath.lineTo(pt.x, pt.y)
                        }
                        ringPath.close()
                        drawPath(
                            path = ringPath,
                            color = webColor,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    for (i in 0 until sides) {
                        val angle = (-PI / 2.0 + i * (2.0 * PI / sides)).toFloat()
                        drawLine(
                            color = webColor,
                            start = center,
                            end = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val polyVertices = animatedRatios.mapIndexed { i, r ->
                        val angle = (-PI / 2.0 + i * (2.0 * PI / sides)).toFloat()
                        Offset(
                            center.x + cos(angle) * radius * r,
                            center.y + sin(angle) * radius * r
                        )
                    }
                    val polyPath = Path().apply {
                        polyVertices.forEachIndexed { idx, pt ->
                            if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                        close()
                    }
                    drawPath(
                        path = polyPath,
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.42f), mid.copy(alpha = 0.16f)),
                            center = center,
                            radius = radius
                        )
                    )
                    drawPath(
                        path = polyPath,
                        color = accent,
                        style = Stroke(width = 2.4.dp.toPx(), join = StrokeJoin.Round)
                    )

                    polyVertices.forEachIndexed { idx, pt ->
                        val isSel = idx == selectedDayIdx
                        if (isSel) {
                            drawCircle(color = accent.copy(alpha = 0.22f), radius = 9.dp.toPx(), center = pt)
                        }
                        drawCircle(color = Color.White, radius = if (isSel) 5.dp.toPx() else 3.5.dp.toPx(), center = pt)
                        drawCircle(color = accent, radius = if (isSel) 3.4.dp.toPx() else 2.2.dp.toPx(), center = pt)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                points.forEach { p ->
                    val ratio = (p.totalMinutes.toFloat() / maxMins.toFloat()).coerceIn(0f, 1f)
                    val isPeak = p.dayOfWeekIndex == peakDay?.dayOfWeekIndex && p.totalMinutes > 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDayIdx = p.dayOfWeekIndex },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = p.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPeak) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(34.dp)
                        )
                        LinearProgressIndicator(
                            progress = { if (p.totalMinutes > 0) ratio.coerceAtLeast(0.05f) else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (isPeak) gradient.accentColor else AppleIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOled) 0.7f else 0.55f),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = formatCompactMins(p.totalMinutes),
                            fontFamily = JetBrainsMonoFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(48.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Day selector chips + detail pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            points.forEach { pt ->
                val sel = pt.dayOfWeekIndex == selectedDayIdx
                Surface(
                    onClick = { selectedDayIdx = pt.dayOfWeekIndex },
                    shape = CircleShape,
                    color = if (sel) gradient.accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pt.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                        color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isOled) 0.4f else 0.65f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDay.fullLabel} · ${selectedDay.sessionCount} session(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatCompactMins(selectedDay.totalMinutes),
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = gradient.accentColor
                )
            }
        }
    }
}

@Composable
fun SessionDepthDonutChart(
    sessions: List<FocusSession>,
    profiles: List<FocusProfile> = emptyList(),
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    var modeProfiles by rememberSaveable { mutableStateOf(false) }

    val slices = remember(sessions, profiles, modeProfiles, gradient.accentColor) {
        if (modeProfiles) {
            buildFocusProfileSlices(sessions, profiles, gradient.accentColor)
        } else {
            buildSessionDepthSlices(sessions, gradient.accentColor)
        }
    }
    val nonZero = remember(slices) { slices.filter { it.minutes > 0 } }
    val totalMinutes = remember(slices) { slices.sumOf { it.minutes } }
    var selectedSliceId by remember(slices) { mutableStateOf(nonZero.firstOrNull()?.id ?: slices.firstOrNull()?.id ?: "") }
    val activeSlice = slices.firstOrNull { it.id == selectedSliceId } ?: nonZero.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_analytics_depth_donut_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (modeProfiles) "Par profil d'apprentissage" else "Par durée de session",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    listOf(false to "Profondeur", true to "Profils").forEach { (isProf, label) ->
                        val sel = modeProfiles == isProf
                        Surface(
                            onClick = { modeProfiles = isProf },
                            shape = CircleShape,
                            color = if (sel) gradient.accentColor else Color.Transparent,
                            modifier = Modifier.testTag("depth_donut_mode_${if (isProf) "profiles" else "depth"}")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val emptyRingColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isOled) 0.5f else 0.75f)
            Box(
                modifier = Modifier.size(134.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(134.dp)) {
                    val strokePx = 18.dp.toPx()
                    val arcSize = Size(size.width - strokePx, size.height - strokePx)
                    val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                    if (totalMinutes <= 0 || nonZero.isEmpty()) {
                        drawArc(
                            color = emptyRingColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    } else {
                        var startAngle = -90f
                        val gap = if (nonZero.size > 1) 5f else 0f
                        nonZero.forEach { slice ->
                            val rawSweep = 360f * (slice.minutes.toFloat() / totalMinutes.toFloat())
                            val sweep = (rawSweep - gap).coerceAtLeast(6f)
                            val isSel = activeSlice?.id == slice.id
                            drawArc(
                                color = if (isSel) slice.color else slice.color.copy(alpha = 0.72f),
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = if (isSel) strokePx + 3.dp.toPx() else strokePx,
                                    cap = StrokeCap.Round
                                )
                            )
                            startAngle += rawSweep
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (activeSlice != null && totalMinutes > 0) formatCompactMins(activeSlice.minutes) else "0m",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = activeSlice?.color ?: MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeSlice?.let { "${it.sessions} sess." } ?: "0 sess.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (slices.isEmpty()) {
                    Text(
                        text = "Aucune session enregistrée.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    slices.forEach { slice ->
                        val pct = if (totalMinutes > 0) ((slice.minutes * 100f) / totalMinutes).roundToInt() else 0
                        val sel = activeSlice?.id == slice.id
                        Surface(
                            onClick = { selectedSliceId = slice.id },
                            shape = RoundedCornerShape(12.dp),
                            color = if (sel) slice.color.copy(alpha = 0.14f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$pct%",
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slice.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MasteryFiveAxisRadarChart(
    axes: List<MasteryAxisScore>,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    if (axes.size != 5) return

    var selectedAxisKey by rememberSaveable { mutableStateOf(axes.first().key) }
    val activeAxis = axes.firstOrNull { it.key == selectedAxisKey } ?: axes.first()
    val overallScore = remember(axes) { (axes.sumOf { it.scorePercent }.toFloat() / axes.size.toFloat()).roundToInt() }

    val animatedRatios = axes.mapIndexed { idx, axis ->
        val anim by animateFloatAsState(
            targetValue = (axis.scorePercent / 100f).coerceIn(0.08f, 1f),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 220f),
            label = "masteryAxis_$idx"
        )
        anim
    }

    val webColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
    val accent = gradient.accentColor
    val mid = gradient.midColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_mastery_radar"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Indice d'équilibre global",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = CircleShape,
                color = gradient.softContainer
            ) {
                Text(
                    text = "$overallScore / 100",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(194.dp)
                    .pointerInput(axes) {
                        detectTapGestures { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val radius = minOf(cx, cy) * 0.78f
                            var bestIdx = 0
                            var bestDist = Float.MAX_VALUE
                            for (i in 0 until 5) {
                                val angle = (-PI / 2.0 + i * (2.0 * PI / 5.0)).toFloat()
                                val vx = cx + cos(angle) * radius
                                val vy = cy + sin(angle) * radius
                                val d = hypot(offset.x - vx, offset.y - vy)
                                if (d < bestDist) {
                                    bestDist = d
                                    bestIdx = i
                                }
                            }
                            selectedAxisKey = axes[bestIdx].key
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (minOf(size.width, size.height) / 2f) * 0.8f
                val sides = 5

                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { ring ->
                    val path = Path()
                    for (i in 0 until sides) {
                        val angle = (-PI / 2.0 + i * (2.0 * PI / sides)).toFloat()
                        val pt = Offset(
                            center.x + cos(angle) * radius * ring,
                            center.y + sin(angle) * radius * ring
                        )
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    path.close()
                    drawPath(path = path, color = webColor, style = Stroke(width = 1.dp.toPx()))
                }

                for (i in 0 until sides) {
                    val angle = (-PI / 2.0 + i * (2.0 * PI / sides)).toFloat()
                    drawLine(
                        color = webColor,
                        start = center,
                        end = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val vertices = animatedRatios.mapIndexed { idx, r ->
                    val angle = (-PI / 2.0 + idx * (2.0 * PI / sides)).toFloat()
                    Offset(center.x + cos(angle) * radius * r, center.y + sin(angle) * radius * r)
                }
                val poly = Path().apply {
                    vertices.forEachIndexed { idx, pt ->
                        if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                    close()
                }
                drawPath(
                    path = poly,
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.44f), mid.copy(alpha = 0.16f)),
                        center = center,
                        radius = radius
                    )
                )
                drawPath(path = poly, color = accent, style = Stroke(width = 2.6.dp.toPx(), join = StrokeJoin.Round))

                vertices.forEachIndexed { idx, pt ->
                    val axis = axes[idx]
                    val sel = axis.key == activeAxis.key
                    if (sel) {
                        drawCircle(color = axis.color.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = pt)
                    }
                    drawCircle(color = Color.White, radius = if (sel) 5.5.dp.toPx() else 3.8.dp.toPx(), center = pt)
                    drawCircle(color = axis.color, radius = if (sel) 3.8.dp.toPx() else 2.5.dp.toPx(), center = pt)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                axes.forEach { axis ->
                    val sel = axis.key == activeAxis.key
                    Surface(
                        onClick = { selectedAxisKey = axis.key },
                        shape = RoundedCornerShape(12.dp),
                        color = if (sel) axis.color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOled) 0.6f else 0.5f),
                        border = BorderStroke(
                            if (sel) 1.dp else 0.5.dp,
                            if (sel) axis.color else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${axis.scorePercent}%",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = axis.color
                            )
                            Text(
                                text = axis.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Text(
                text = "${activeAxis.label} : ${activeAxis.detail}",
                style = MaterialTheme.typography.labelSmall,
                color = activeAxis.color,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun TaskVelocityGroupedChart(
    series: List<DailyVelocityPoint>,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    if (series.isEmpty()) return

    var selectedIdx by remember(series.size) { mutableIntStateOf(series.lastIndex) }
    val safeIdx = selectedIdx.coerceIn(0, series.lastIndex)
    val selected = series[safeIdx]
    val maxCount = series.maxOfOrNull { maxOf(it.completedCount, it.pendingCount) }?.coerceAtLeast(3) ?: 3
    val totalDone = series.sumOf { it.completedCount }
    val totalAll = series.sumOf { it.completedCount + it.pendingCount }.coerceAtLeast(1)
    val velocityPct = ((totalDone * 100f) / totalAll).roundToInt()

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    val doneColor = AppleEmerald
    val pendingColor = gradient.accentColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_velocity_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOled) 0.65f else 0.55f)
            ) {
                Text(
                    text = "${selected.shortLabel} · ${selected.completedCount} faites / ${selected.pendingCount} restantes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = doneColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "$velocityPct% réalisé",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = doneColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .pointerInput(series.size) {
                    detectTapGestures { offset ->
                        if (size.width > 0) {
                            selectedIdx = ((offset.x / size.width.toFloat()) * series.size).toInt().coerceIn(0, series.lastIndex)
                        }
                    }
                }
        ) {
            val topPad = 10.dp.toPx()
            val bottomPad = 6.dp.toPx()
            val usableH = (size.height - topPad - bottomPad).coerceAtLeast(1f)
            val bottomY = topPad + usableH
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 7f), 0f)
            listOf(0f, 0.5f, 1f).forEach { frac ->
                val y = topPad + usableH * (1f - frac)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = if (frac > 0f) dash else null
                )
            }

            val slotW = size.width / series.size.toFloat()
            val barW = (slotW * 0.28f).coerceIn(6.dp.toPx(), 16.dp.toPx())
            val innerGap = 3.dp.toPx()
            val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            val trendCoords = mutableListOf<Offset>()

            series.forEachIndexed { idx, pt ->
                val centerX = idx * slotW + slotW / 2f
                val doneH = usableH * (pt.completedCount.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                val pendH = usableH * (pt.pendingCount.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)

                if (idx == safeIdx) {
                    drawRoundRect(
                        color = gradient.accentColor.copy(alpha = 0.08f),
                        topLeft = Offset(idx * slotW + 2.dp.toPx(), topPad),
                        size = Size(slotW - 4.dp.toPx(), usableH),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }

                // Completed bar (left)
                val leftBarX = centerX - barW - innerGap / 2f
                drawRoundRect(
                    color = doneColor,
                    topLeft = Offset(leftBarX, bottomY - doneH.coerceAtLeast(3.dp.toPx())),
                    size = Size(barW, doneH.coerceAtLeast(3.dp.toPx())),
                    cornerRadius = corner
                )

                // Pending bar (right)
                val rightBarX = centerX + innerGap / 2f
                drawRoundRect(
                    color = pendingColor.copy(alpha = 0.72f),
                    topLeft = Offset(rightBarX, bottomY - pendH.coerceAtLeast(3.dp.toPx())),
                    size = Size(barW, pendH.coerceAtLeast(3.dp.toPx())),
                    cornerRadius = corner
                )

                trendCoords += Offset(centerX, bottomY - doneH)
            }

            if (trendCoords.size >= 2) {
                val path = Path().apply {
                    moveTo(trendCoords.first().x, trendCoords.first().y)
                    for (i in 0 until trendCoords.lastIndex) {
                        val p0 = trendCoords[i]
                        val p1 = trendCoords[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = path,
                    color = AppleAmber,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                trendCoords.forEach { pt ->
                    drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = pt)
                    drawCircle(color = AppleAmber, radius = 2.2.dp.toPx(), center = pt)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            series.forEachIndexed { idx, pt ->
                Text(
                    text = pt.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (idx == safeIdx) FontWeight.Bold else FontWeight.Normal,
                    color = if (idx == safeIdx) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedIdx = idx }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ChartLegendDot("Complétées", doneColor)
            ChartLegendDot("Restantes", pendingColor)
            ChartLegendDot("Tendance", AppleAmber)
        }
    }
}

@Composable
fun CoursePerformanceBubbleChart(
    bubbles: List<CourseMatrixBubble>,
    modifier: Modifier = Modifier
) {
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    if (bubbles.isEmpty()) {
        Text(
            text = "Ajoutez des matières dans Paramètres pour visualiser la matrice Progression / Note.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.testTag("progress_course_matrix_chart")
        )
        return
    }

    var selectedCourseId by remember(bubbles) { mutableStateOf(bubbles.first().courseId) }
    val selected = bubbles.firstOrNull { it.courseId == selectedCourseId } ?: bubbles.first()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val selectionColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_course_matrix_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = selected.color.copy(alpha = if (isOled) 0.22f else 0.12f),
            border = BorderStroke(0.8.dp, selected.color.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${selected.code} · ${selected.title}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${selected.completedHours}h / ${selected.targetHours}h (${selected.progressPercent}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = String.format(Locale.FRANCE, "%.1f/20", selected.grade),
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = selected.color
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("20", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(bubbles) {
                        detectTapGestures { offset ->
                            val pad = 18.dp.toPx()
                            val usableW = (size.width - pad * 2f).coerceAtLeast(1f)
                            val usableH = (size.height - pad * 2f).coerceAtLeast(1f)
                            var best = bubbles.first()
                            var bestDist = Float.MAX_VALUE
                            bubbles.forEach { b ->
                                val bx = pad + usableW * (b.progressPercent / 100f)
                                val by = pad + usableH * (1f - (b.grade.toFloat() / 20f))
                                val d = hypot(offset.x - bx, offset.y - by)
                                if (d < bestDist) {
                                    bestDist = d
                                    best = b
                                }
                            }
                            selectedCourseId = best.courseId
                        }
                    }
            ) {
                val pad = 18.dp.toPx()
                val usableW = (size.width - pad * 2f).coerceAtLeast(1f)
                val usableH = (size.height - pad * 2f).coerceAtLeast(1f)

                // Quadrant background tint (Top-right mastery quadrant highlighted softly)
                drawRoundRect(
                    color = AppleEmerald.copy(alpha = if (isOled) 0.08f else 0.06f),
                    topLeft = Offset(pad + usableW * 0.5f, pad),
                    size = Size(usableW * 0.5f, usableH * 0.5f),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                // Outer frame & crosshairs at 50% / 10/20
                drawRoundRect(
                    color = gridColor,
                    topLeft = Offset(pad, pad),
                    size = Size(usableW, usableH),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
                val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                drawLine(
                    color = gridColor,
                    start = Offset(pad + usableW * 0.5f, pad),
                    end = Offset(pad + usableW * 0.5f, pad + usableH),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dash
                )
                drawLine(
                    color = gridColor,
                    start = Offset(pad, pad + usableH * 0.5f),
                    end = Offset(pad + usableW, pad + usableH * 0.5f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dash
                )

                val maxHours = bubbles.maxOfOrNull { it.targetHours }?.coerceAtLeast(10) ?: 20
                bubbles.forEach { b ->
                    val bx = pad + usableW * (b.progressPercent / 100f).coerceIn(0f, 1f)
                    val by = pad + usableH * (1f - (b.grade.toFloat() / 20f).coerceIn(0f, 1f))
                    val sizeRatio = (b.targetHours.toFloat() / maxHours.toFloat()).coerceIn(0.4f, 1f)
                    val r = (10.dp.toPx() + 8.dp.toPx() * sizeRatio)
                    val isSel = b.courseId == selected.courseId

                    drawCircle(
                        color = b.color.copy(alpha = if (isSel) 0.34f else 0.22f),
                        radius = r + if (isSel) 4.dp.toPx() else 0f,
                        center = Offset(bx, by)
                    )
                    drawCircle(
                        color = b.color,
                        radius = r * 0.72f,
                        center = Offset(bx, by)
                    )
                    if (isSel) {
                        drawCircle(
                            color = selectionColor,
                            radius = r + 3.dp.toPx(),
                            center = Offset(bx, by),
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 34.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0% accompli", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("50%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("100% maîtrise", style = MaterialTheme.typography.labelSmall, color = AppleEmerald)
        }
    }
}

@Composable
private fun ChartLegendDot(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatCompactMins(totalMinutes: Int): String {
    val safe = totalMinutes.coerceAtLeast(0)
    val h = safe / 60
    val m = safe % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m.toString().padStart(2, '0')}m"
        h > 0 -> "${h}h 00m"
        else -> "${m}m"
    }
}
