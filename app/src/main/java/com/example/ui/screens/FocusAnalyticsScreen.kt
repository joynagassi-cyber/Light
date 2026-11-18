package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.FocusProfile
import com.example.data.model.FocusSession
import com.example.ui.components.AppleCard
import com.example.ui.components.FocusActivityHeatmapChart
import com.example.ui.components.SessionDepthDonutChart
import com.example.ui.components.StackedModeBarChart
import com.example.ui.components.WeekdayRadarAndBarsChart
import com.example.ui.components.buildActivityHeatmapGrid
import com.example.ui.components.buildModeStackedBars
import com.example.ui.components.buildWeekdayRhythm
import com.example.ui.components.catalogIconColor
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleSky
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

enum class AnalyticsWindow(val key: String, val days: Int, val label: String) {
    DAYS_7("7d", 7, "7 jours"),
    DAYS_14("14d", 14, "14 jours"),
    DAYS_30("30d", 30, "30 jours")
}

data class DailyFocusPoint(
    val date: LocalDate,
    val shortLabel: String,
    val fullLabel: String,
    val minutes: Int,
    val sessionCount: Int
)

fun aggregateDailyFocusPoints(
    sessions: List<FocusSession>,
    windowDays: Int,
    today: LocalDate = LocalDate.now()
): List<DailyFocusPoint> {
    val safeDays = windowDays.coerceIn(1, 90)
    val studySessions = sessions.filter { it.mode != "short_break" && it.durationMinutes > 0 }
    val grouped = studySessions.groupBy { it.dateIso }

    return (safeDays - 1 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val iso = day.toString()
        val daySessions = grouped[iso].orEmpty()
        val totalMins = daySessions.sumOf { it.durationMinutes.toInt() }
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
            day.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replace(".", "")
        }"
        DailyFocusPoint(
            date = day,
            shortLabel = shortLabel,
            fullLabel = fullLabel,
            minutes = totalMins,
            sessionCount = daySessions.size
        )
    }
}

fun computeFocusStreakDays(
    sessions: List<FocusSession>,
    today: LocalDate = LocalDate.now()
): Int {
    val activeDates = sessions
        .asSequence()
        .filter { it.mode != "short_break" && it.durationMinutes > 0 }
        .mapNotNull { runCatching { LocalDate.parse(it.dateIso) }.getOrNull() }
        .toSet()
    if (activeDates.isEmpty()) return 0

    var cursor = if (today in activeDates) today else today.minusDays(1)
    if (cursor !in activeDates) return 0
    var streak = 0
    while (cursor in activeDates) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

@Composable
fun FocusAnalyticsScreen(
    sessions: List<FocusSession>,
    courses: List<Course>,
    onStartFocusSession: () -> Unit,
    onOpenSettings: () -> Unit,
    focusProfiles: List<FocusProfile> = emptyList(),
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    var windowKey by rememberSaveable { mutableStateOf(AnalyticsWindow.DAYS_14.key) }
    val selectedWindow = AnalyticsWindow.entries.firstOrNull { it.key == windowKey } ?: AnalyticsWindow.DAYS_14

    val today = remember { LocalDate.now() }
    val dailyPoints = remember(sessions, selectedWindow, today) {
        aggregateDailyFocusPoints(sessions, selectedWindow.days, today)
    }

    val totalMinutes = remember(dailyPoints) { dailyPoints.sumOf { it.minutes } }
    val totalWindowSessions = remember(dailyPoints) { dailyPoints.sumOf { it.sessionCount } }
    val activeDaysCount = remember(dailyPoints) { dailyPoints.count { it.minutes > 0 } }
    val avgMinutesPerDay = remember(totalMinutes, selectedWindow) {
        (totalMinutes.toFloat() / selectedWindow.days.toFloat()).roundToInt()
    }
    val peakPoint = remember(dailyPoints) { dailyPoints.maxByOrNull { it.minutes } }
    val streakDays = remember(sessions, today) { computeFocusStreakDays(sessions, today) }

    val windowIsoSet = remember(dailyPoints) { dailyPoints.map { it.date.toString() }.toSet() }
    val windowStudySessions = remember(sessions, windowIsoSet) {
        sessions.filter { it.mode != "short_break" && it.durationMinutes > 0 && it.dateIso in windowIsoSet }
    }

    val heatmapCells = remember(sessions, today) {
        buildActivityHeatmapGrid(sessions = sessions, weeks = 12, today = today)
    }
    val stackedModeDays = remember(sessions, selectedWindow, today) {
        buildModeStackedBars(sessions = sessions, windowDays = selectedWindow.days, today = today)
    }
    val weekdayRhythm = remember(windowStudySessions) {
        buildWeekdayRhythm(windowStudySessions)
    }

    val courseMap = remember(courses) { courses.associateBy { it.id } }
    val courseBreakdown = remember(windowStudySessions, courseMap) {
        windowStudySessions
            .groupBy { it.courseTitle.ifBlank { courseMap[it.courseId]?.title ?: "Session libre" } }
            .map { (title, list) ->
                val course = list.firstNotNullOfOrNull { courseMap[it.courseId] }
                val mins = list.sumOf { it.durationMinutes.toInt() }
                Triple(title, mins, catalogIconColor(course?.colorIndex ?: 0L))
            }
            .sortedByDescending { it.second }
    }

    val modeStats = remember(windowStudySessions) {
        val total = windowStudySessions.sumOf { it.durationMinutes.toInt() }.coerceAtLeast(1)
        listOf(
            Triple("Pomodoro", "pomodoro", gradient.accentColor),
            Triple("Deep Code", "deep_code", AppleIndigo),
            Triple("Chronomètre", "stopwatch", AppleEmerald)
        ).map { (label, modeKey, color) ->
            val list = windowStudySessions.filter {
                if (modeKey == "pomodoro") it.mode == "pomodoro" || it.mode.isBlank() else it.mode == modeKey
            }
            val mins = list.sumOf { it.durationMinutes.toInt() }
            val pct = ((mins * 100f) / total.toFloat()).roundToInt()
            ModeSummary(label = label, minutes = mins, sessions = list.size, percentage = pct, color = color)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_analytics_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Courbe de durée d'étude quotidienne & constance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("focus_analytics_settings_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Paramètres",
                            tint = gradient.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Timeframe Segmented Pill Selector
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnalyticsWindow.entries.forEach { window ->
                        val selected = selectedWindow == window
                        Surface(
                            onClick = { windowKey = window.key },
                            shape = CircleShape,
                            color = if (selected) gradient.accentColor else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analytics_window_${window.key}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = window.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Smooth Line Chart Card (Daily Study Duration)
        item {
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_analytics_chart_card"),
                cornerRadius = 26.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(gradient.softContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Insights,
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Durée d'étude quotidienne",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$activeDaysCount jour(s) actif(s) · $totalWindowSessions session(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = gradient.softContainer
                    ) {
                        Text(
                            text = formatMinutesHuman(totalMinutes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = gradient.accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FocusAnalyticsLineChart(
                    points = dailyPoints,
                    averageMinutes = avgMinutesPerDay,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // KPI Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiTile(
                        title = "Total période",
                        value = formatMinutesHuman(totalMinutes),
                        subtitle = "Sur ${selectedWindow.label}",
                        icon = Icons.Rounded.Schedule,
                        accentColor = gradient.accentColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analytics_kpi_total")
                    )
                    AnalyticsKpiTile(
                        title = "Moyenne / jour",
                        value = formatMinutesHuman(avgMinutesPerDay),
                        subtitle = "$activeDaysCount j actifs",
                        icon = Icons.Rounded.TrendingUp,
                        accentColor = AppleIndigo,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analytics_kpi_average")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiTile(
                        title = "Meilleur jour",
                        value = formatMinutesHuman(peakPoint?.minutes ?: 0),
                        subtitle = peakPoint?.takeIf { it.minutes > 0 }?.fullLabel ?: "Aucune session",
                        icon = Icons.Rounded.Bolt,
                        accentColor = AppleAmber,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analytics_kpi_peak")
                    )
                    AnalyticsKpiTile(
                        title = "Série en cours",
                        value = "$streakDays j",
                        subtitle = if (streakDays > 0) "Rythme actif" else "Démarrez aujourd'hui",
                        icon = Icons.Rounded.LocalFireDepartment,
                        accentColor = AppleEmerald,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analytics_kpi_streak")
                    )
                }
            }
        }

        // Study Time by Course / Subject
        item {
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Répartition par matière",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        onClick = onStartFocusSession,
                        shape = CircleShape,
                        color = gradient.softContainer,
                        modifier = Modifier.testTag("analytics_start_focus_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Lancer Focus",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = gradient.accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (courseBreakdown.isEmpty()) {
                    Text(
                        text = "Aucune session enregistrée sur cette fenêtre. Lancez un minuteur Focus pour suivre la répartition par matière.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maxCourseMins = courseBreakdown.maxOf { it.second }.coerceAtLeast(1)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        courseBreakdown.take(5).forEach { (courseTitle, mins, color) ->
                            val ratio = (mins.toFloat() / maxCourseMins.toFloat()).coerceIn(0.05f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = courseTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatMinutesHuman(mins),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(CircleShape),
                                    color = color,
                                    trackColor = color.copy(alpha = 0.14f),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }

        // Breakdown by Focus Mode
        item {
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "Modes utilisés (${selectedWindow.label})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modeStats.forEach { stat ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = stat.color.copy(alpha = 0.10f),
                            border = BorderStroke(0.8.dp, stat.color.copy(alpha = 0.24f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = stat.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = stat.color,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatMinutesHuman(stat.minutes),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stat.sessions} sess. · ${stat.percentage}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Premium Chart 1: 12-Week Calendar Activity Heatmap
        item {
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_analytics_heatmap_card"),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Heatmap d'assiduité (12 semaines)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Touchez une case pour consulter le volume du jour",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FocusActivityHeatmapChart(
                    cells = heatmapCells,
                    showCombinedDetails = false
                )
            }
        }

        // Premium Chart 2: Stacked Daily Mode Composition Bars
        item {
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_analytics_stacked_card"),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "Mix quotidien par mode (${selectedWindow.label})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Barres empilées Pomodoro, Deep Code & Chronomètre",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                StackedModeBarChart(days = stackedModeDays)
            }
        }

        // Premium Chart 3: Weekly Rhythm Radar & Horizontal Bars
        item {
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_analytics_radar_card"),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "Rythme hebdomadaire (Lun – Dim)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                WeekdayRadarAndBarsChart(points = weekdayRhythm)
            }
        }

        // Premium Chart 4: Session Depth & Focus Profiles Interactive Donut
        item {
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_analytics_depth_card"),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "Profondeur & Profils Focus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                SessionDepthDonutChart(
                    sessions = windowStudySessions,
                    profiles = focusProfiles
                )
            }
        }
    }
}

private data class ModeSummary(
    val label: String,
    val minutes: Int,
    val sessions: Int,
    val percentage: Int,
    val color: Color
)

@Composable
fun FocusAnalyticsLineChart(
    points: List<DailyFocusPoint>,
    averageMinutes: Int,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    if (points.isEmpty()) return

    var selectedIndex by remember(points.size) { mutableIntStateOf(points.lastIndex) }
    val safeSelected = selectedIndex.coerceIn(0, points.lastIndex)
    val selectedPoint = points[safeSelected]

    val rawMax = points.maxOfOrNull { it.minutes } ?: 0
    val yMax = remember(rawMax) {
        when {
            rawMax <= 30 -> 60
            rawMax <= 60 -> 60
            rawMax <= 120 -> 120
            rawMax <= 180 -> 180
            else -> (((rawMax + 59) / 60) * 60)
        }
    }

    val animatedRatios = points.mapIndexed { idx, pt ->
        val ratio by animateFloatAsState(
            targetValue = (pt.minutes.toFloat() / yMax.toFloat()).coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 240f),
            label = "analyticsLinePt_$idx"
        )
        ratio
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Callout tooltip bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = gradient.softContainer.copy(alpha = 0.72f),
            border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.22f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("analytics_selected_day_callout")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = gradient.accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Column {
                        Text(
                            text = selectedPoint.fullLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedPoint.sessionCount > 0) {
                                "${selectedPoint.sessionCount} session(s) Focus"
                            } else {
                                "Aucune session enregistrée"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatMinutesHuman(selectedPoint.minutes),
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = gradient.accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        val avgLineColor = AppleEmerald.copy(alpha = 0.65f)
        val strokeBrush = gradient.appBrush()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Y-axis scale ticks (D3-style clean domain ticks)
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(top = 6.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatMinutesCompactAxis(yMax),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatMinutesCompactAxis(yMax / 2),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "0m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .testTag("focus_analytics_line_chart")
                    .pointerInput(points.size) {
                        detectTapGestures { offset ->
                            if (points.size > 1 && size.width > 0) {
                                val hPad = 10.dp.toPx()
                                val usable = (size.width - hPad * 2f).coerceAtLeast(1f)
                                val rel = ((offset.x - hPad) / usable).coerceIn(0f, 1f)
                                selectedIndex = (rel * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
                            }
                        }
                    }
                    .pointerInput(points.size) {
                        detectHorizontalDragGestures { change, _ ->
                            if (points.size > 1 && size.width > 0) {
                                val hPad = 10.dp.toPx()
                                val usable = (size.width - hPad * 2f).coerceAtLeast(1f)
                                val rel = ((change.position.x - hPad) / usable).coerceIn(0f, 1f)
                                selectedIndex = (rel * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
                            }
                        }
                    }
            ) {
                val hPad = 10.dp.toPx()
                val vPadTop = 12.dp.toPx()
                val vPadBottom = 16.dp.toPx()
                val usableW = (size.width - hPad * 2f).coerceAtLeast(1f)
                val usableH = (size.height - vPadTop - vPadBottom).coerceAtLeast(1f)
                val bottomY = vPadTop + usableH

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 7f), 0f)
                listOf(0f, 0.5f, 1f).forEach { frac ->
                    val y = vPadTop + usableH * (1f - frac)
                    drawLine(
                        color = gridColor,
                        start = Offset(hPad, y),
                        end = Offset(size.width - hPad, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (frac > 0f) dashEffect else null
                    )
                }

                if (averageMinutes > 0) {
                    val avgRatio = (averageMinutes.toFloat() / yMax.toFloat()).coerceIn(0f, 1f)
                    val avgY = vPadTop + usableH * (1f - avgRatio)
                    drawLine(
                        color = avgLineColor,
                        start = Offset(hPad, avgY),
                        end = Offset(size.width - hPad, avgY),
                        strokeWidth = 1.3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                }

                val count = animatedRatios.size
                val stepX = if (count > 1) usableW / (count - 1) else 0f
                val coords = animatedRatios.mapIndexed { idx, ratio ->
                    val x = if (count > 1) hPad + idx * stepX else size.width / 2f
                    val y = vPadTop + usableH * (1f - ratio)
                    Offset(x, y)
                }

                if (coords.size >= 2) {
                    // Monotone cubic curve (D3 curveMonotoneX style) so values never dip below baseline 0
                    val linePath = buildMonotonePath(coords, minY = vPadTop, maxY = bottomY)
                    val areaPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, bottomY)
                        lineTo(coords.first().x, bottomY)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                gradient.accentColor.copy(alpha = 0.28f),
                                gradient.midColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = vPadTop,
                            endY = bottomY
                        )
                    )

                    drawPath(
                        path = linePath,
                        brush = strokeBrush,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                coords.forEachIndexed { idx, pt ->
                    val isSel = idx == safeSelected
                    if (isSel) {
                        drawLine(
                            color = gradient.accentColor.copy(alpha = 0.35f),
                            start = Offset(pt.x, vPadTop),
                            end = Offset(pt.x, bottomY),
                            strokeWidth = 1.4.dp.toPx()
                        )
                        drawCircle(
                            color = gradient.accentColor.copy(alpha = 0.20f),
                            radius = 10.dp.toPx(),
                            center = pt
                        )
                    }
                    if (isSel || count <= 14 || points[idx].minutes > 0) {
                        drawCircle(
                            color = Color.White,
                            radius = if (isSel) 5.5.dp.toPx() else 3.2.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = gradient.accentColor,
                            radius = if (isSel) 3.8.dp.toPx() else 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        // X-axis date ticks aligned under chart area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = when {
                points.size <= 7 -> 1
                points.size <= 14 -> 3
                else -> 6
            }
            points.forEachIndexed { idx, pt ->
                val show = idx == safeSelected || idx == 0 || idx == points.lastIndex || idx % step == 0
                if (show) {
                    Text(
                        text = pt.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (idx == safeSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (idx == safeSelected) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { selectedIndex = idx }
                    )
                }
            }
        }

        if (averageMinutes > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 46.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 2.dp)
                        .background(AppleEmerald)
                )
                Text(
                    text = "Moyenne : ${formatMinutesHuman(averageMinutes)} / jour",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun buildMonotonePath(coords: List<Offset>, minY: Float, maxY: Float): Path {
    val path = Path()
    if (coords.isEmpty()) return path
    path.moveTo(coords.first().x, coords.first().y)
    if (coords.size == 1) return path

    val n = coords.size
    val d = FloatArray(n - 1)
    val dx = FloatArray(n - 1)
    for (i in 0 until n - 1) {
        val span = (coords[i + 1].x - coords[i].x).coerceAtLeast(1f)
        dx[i] = span
        d[i] = (coords[i + 1].y - coords[i].y) / span
    }

    val m = FloatArray(n)
    m[0] = d[0]
    for (i in 1 until n - 1) {
        m[i] = if (d[i - 1] * d[i] <= 0f) 0f else (d[i - 1] + d[i]) * 0.5f
    }
    m[n - 1] = d[n - 2]

    // Fritsch-Carlson clamping so curve remains monotonic without overshooting 0 or peak
    for (i in 0 until n - 1) {
        if (d[i] == 0f) {
            m[i] = 0f
            m[i + 1] = 0f
        } else {
            val a = m[i] / d[i]
            val b = m[i + 1] / d[i]
            val h = kotlin.math.hypot(a, b)
            if (h > 3f) {
                val t = 3f / h
                m[i] = t * a * d[i]
                m[i + 1] = t * b * d[i]
            }
        }
    }

    for (i in 0 until n - 1) {
        val p0 = coords[i]
        val p1 = coords[i + 1]
        val span = dx[i] / 3f
        val c1y = (p0.y + m[i] * span).coerceIn(minY, maxY)
        val c2y = (p1.y - m[i + 1] * span).coerceIn(minY, maxY)
        path.cubicTo(p0.x + span, c1y, p1.x - span, c2y, p1.x, p1.y)
    }
    return path
}

@Composable
private fun AnalyticsKpiTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatMinutesHuman(totalMinutes: Int): String {
    val safe = totalMinutes.coerceAtLeast(0)
    val h = safe / 60
    val m = safe % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m.toString().padStart(2, '0')}m"
        h > 0 -> "${h}h 00m"
        else -> "${m} min"
    }
}

private fun formatMinutesCompactAxis(minutes: Int): String {
    return if (minutes >= 60 && minutes % 60 == 0) {
        "${minutes / 60}h"
    } else if (minutes >= 60) {
        "${minutes / 60}h${minutes % 60}"
    } else {
        "${minutes}m"
    }
}
