package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Course
import com.example.data.model.FocusSession
import com.example.data.model.StudyTask
import com.example.data.model.UserProfile
import com.example.data.model.adaptation
import com.example.ui.components.AppleCard
import com.example.ui.components.CategoryDistributionDonutChart
import com.example.ui.components.GradientProgressRing
import com.example.ui.components.StudyTrendLineChart
import com.example.ui.components.WeeklyStudyBarChart
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalElanGradient
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private data class StudyBadge(
    val title: String,
    val subtitle: String,
    val unlocked: Boolean,
    val icon: ImageVector
)

private enum class ProgressChartMode(val key: String, val label: String) {
    TREND_14D("trend_14d", "Courbe 14j"),
    BARS_7D("bars_7d", "Semaine 7j"),
    CATEGORIES("categories", "Catégories")
}

@Composable
fun ProgressScreen(
    profile: UserProfile?,
    courses: List<Course>,
    tasks: List<StudyTask>,
    sessions: List<FocusSession>,
    onUpdateCourseGrade: (String, Double) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalElanGradient.current
    val adapt = remember(profile) { profile.adaptation() }
    var chartMode by remember { mutableStateOf(ProgressChartMode.TREND_14D) }

    val completedTasks = tasks.count { it.completed }
    val totalTasks = tasks.size.coerceAtLeast(1)
    val taskRatio = completedTasks.toFloat() / totalTasks.toFloat()

    val totalFocusMinutes = sessions.filter { it.mode != "short_break" }.sumOf { it.durationMinutes }
    val totalHoursText = "${totalFocusMinutes / 60}h ${(totalFocusMinutes % 60).toString().padStart(2, '0')}m"

    val avgGrade = if (courses.isNotEmpty()) courses.map { it.grade }.average() else 16.0

    val last7Days = remember { (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) } }
    val dayLabels7 = remember(last7Days) {
        last7Days.map { day ->
            day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
                .take(3)
        }
    }
    val minutesPerDay7 = remember(last7Days, sessions) {
        last7Days.map { day ->
            val iso = day.toString()
            sessions.filter { it.dateIso == iso && it.mode != "short_break" }
                .sumOf { it.durationMinutes }
                .toInt()
        }
    }

    val last14Days = remember { (13 downTo 0).map { LocalDate.now().minusDays(it.toLong()) } }
    val dayLabels14 = remember(last14Days) {
        last14Days.map { day -> "${day.dayOfMonth}/${day.monthValue}" }
    }
    val minutesPerDay14 = remember(last14Days, sessions) {
        last14Days.map { day ->
            val iso = day.toString()
            sessions.filter { it.dateIso == iso && it.mode != "short_break" }
                .sumOf { it.durationMinutes }
                .toInt()
        }
    }

    val badges = listOf(
        StudyBadge(
            title = "Première Étincelle",
            subtitle = "Compléter 1 session Focus ou Chronomètre",
            unlocked = sessions.isNotEmpty(),
            icon = Icons.Rounded.Timer
        ),
        StudyBadge(
            title = "Série Constante",
            subtitle = "Maintenir 3 jours d'étude",
            unlocked = (profile?.streakDays ?: 0L) >= 3L,
            icon = Icons.Rounded.LocalFireDepartment
        ),
        StudyBadge(
            title = "Architecte IA",
            subtitle = "Planifier avec Gemini IA",
            unlocked = tasks.any { it.aiGenerated },
            icon = Icons.Rounded.AutoAwesome
        ),
        StudyBadge(
            title = "Mention Excellence",
            subtitle = "${adapt.metricScoreLabel} >= 15.5/20",
            unlocked = avgGrade >= 15.5,
            icon = Icons.Rounded.Verified
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progression & Bilan",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "${adapt.headerSubtitle} • ${adapt.ageBadge}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(0.8.dp, AppleDivider),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("progress_settings_button")
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

        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    GradientProgressRing(
                        progress = taskRatio,
                        strokeWidth = 12.dp,
                        modifier = Modifier.size(116.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(taskRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "accompli",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextSecondary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Temps Focus cumulé",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppleTextSecondary
                        )
                        Text(
                            text = totalHoursText,
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppleTextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column {
                                Text(
                                    text = "$completedTasks/${tasks.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = gradient.accentColor
                                )
                                Text(
                                    text = "Tâches",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppleTextSecondary
                                )
                            }
                            Column {
                                Text(
                                    text = String.format(Locale.FRANCE, "%.1f/20", avgGrade),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppleEmerald
                                )
                                Text(
                                    text = adapt.metricScoreLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppleTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Multi-Mode ProgressChart Card
        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Graphique d'étude",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "${sessions.filter { it.mode != "short_break" }.size} sessions",
                        style = MaterialTheme.typography.labelMedium,
                        color = gradient.accentColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressChartMode.entries.forEach { mode ->
                        val selected = chartMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { chartMode = mode },
                            label = { Text(mode.label, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = AppleDivider,
                                selectedBorderColor = gradient.accentColor
                            ),
                            modifier = Modifier.testTag("chart_mode_${mode.key}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (chartMode) {
                    ProgressChartMode.TREND_14D -> StudyTrendLineChart(
                        dayLabels = dayLabels14,
                        minutesPerDay = minutesPerDay14
                    )

                    ProgressChartMode.BARS_7D -> WeeklyStudyBarChart(
                        dayLabels = dayLabels7,
                        minutesPerDay = minutesPerDay7,
                        activeIndex = 6
                    )

                    ProgressChartMode.CATEGORIES -> CategoryDistributionDonutChart(
                        tasks = tasks
                    )
                }
            }
        }

        item {
            Text(
                text = "Progression • ${adapt.workspaceSectionTitle}",
                style = MaterialTheme.typography.titleLarge,
                color = AppleTextPrimary
            )
        }

        items(courses, key = { it.id }) { course ->
            val ratio = (course.completedMinutes.toFloat() / course.targetMinutes.coerceAtLeast(1).toFloat())
                .coerceIn(0f, 1f)
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${course.code} · ${course.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = AppleTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${course.completedMinutes / 60}h / ${course.targetMinutes / 60}h · ${course.professor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onUpdateCourseGrade(course.id, (course.grade - 0.5).coerceAtLeast(0.0)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Diminuer la note",
                                tint = AppleTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = gradient.softContainer
                        ) {
                            Text(
                                text = String.format(Locale.FRANCE, "%.1f", course.grade),
                                style = MaterialTheme.typography.labelLarge,
                                color = gradient.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = { onUpdateCourseGrade(course.id, (course.grade + 0.5).coerceAtMost(20.0)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Augmenter la note",
                                tint = gradient.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = gradient.accentColor,
                    trackColor = gradient.softContainer,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        item {
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_achievement_trophy),
                        contentDescription = "Trophée d'excellence académique",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.28f))
                            .padding(18.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = "Trophées & Succès",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "${badges.count { it.unlocked }} / ${badges.size} accomplissements débloqués",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    badges.forEach { badge ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (badge.unlocked) gradient.softContainer
                                        else Color(0xFFF1F3F8)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badge.icon,
                                    contentDescription = null,
                                    tint = if (badge.unlocked) gradient.accentColor else AppleTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (badge.unlocked) AppleTextPrimary else AppleTextSecondary
                                )
                                Text(
                                    text = badge.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleTextSecondary
                                )
                            }
                            if (badge.unlocked) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = "Débloqué",
                                    tint = AppleAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
