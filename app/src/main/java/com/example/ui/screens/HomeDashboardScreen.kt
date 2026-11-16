package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Course
import com.example.data.model.FocusSession
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.data.model.UserProfile
import com.example.data.model.adaptation
import com.example.ui.components.AppleCard
import com.example.ui.components.GradientHeroCard
import com.example.ui.components.SyncStatusPill
import com.example.ui.components.TaskCategoryPill
import com.example.ui.components.accentColor
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalElanGradient
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    profile: UserProfile?,
    photoUrl: String?,
    courses: List<Course>,
    tasks: List<StudyTask>,
    sessions: List<FocusSession>,
    onToggleTask: (StudyTask) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToAgent: () -> Unit,
    onOpenStopwatch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGradientPicker: () -> Unit,
    onOpenCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalElanGradient.current
    val todayIso = LocalDate.now().toString()
    val todayTasks = tasks.filter { it.dueDateIso == todayIso }
    val completedToday = todayTasks.count { it.completed }
    val dailyGoal = (profile?.dailyGoalTasks ?: 5L).toInt().coerceAtLeast(1)
    val goalProgress by animateFloatAsState(
        targetValue = (completedToday.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "dailyGoalProgress"
    )

    val weekAgoIso = LocalDate.now().minusDays(6).toString()
    val weeklyMinutes = sessions
        .filter { it.dateIso >= weekAgoIso && it.mode != "short_break" }
        .sumOf { it.durationMinutes }
    val weeklyHoursText = "${weeklyMinutes / 60}h ${(weeklyMinutes % 60).toString().padStart(2, '0')}m"

    val avgGrade = if (courses.isNotEmpty()) {
        courses.map { it.grade }.average()
    } else {
        16.0
    }

    val adapt = profile.adaptation()
    val firstName = profile?.displayName?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }
        ?: adapt.roleLabel

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(gradient.linearBrush())
                            .padding(2.dp)
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Photo de profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_study_companion),
                                contentDescription = "Avatar ${adapt.roleLabel}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Bonjour, $firstName",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppleTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${adapt.headerSubtitle} · ${adapt.ageBadge}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SyncStatusPill()
                    Surface(
                        onClick = onOpenGradientPicker,
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleDivider),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("open_gradient_studio_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = "Personnaliser le dégradé",
                                tint = gradient.accentColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Surface(
                        onClick = onOpenSettings,
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleDivider),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("home_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Ouvrir les paramètres",
                                tint = AppleTextPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // Organized Quick Shortcuts Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onNavigateToAgent,
                    shape = RoundedCornerShape(16.dp),
                    color = gradient.softContainer,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_quick_agent_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = gradient.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agent IA",
                            style = MaterialTheme.typography.labelLarge,
                            color = gradient.accentColor
                        )
                    }
                }

                Surface(
                    onClick = onOpenStopwatch,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleDivider),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_quick_stopwatch_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AvTimer,
                            contentDescription = null,
                            tint = AppleTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chronomètre",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppleTextPrimary
                        )
                    }
                }
            }
        }

        item {
            GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Objectif du jour",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedToday / $dailyGoal tâches",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${profile?.streakDays ?: 1} j",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.28f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = adapt.heroQuote,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onNavigateToPlanner,
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.testTag("hero_smart_plan_cta")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Plan IA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = gradient.accentColor
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoMetricCard(
                        icon = Icons.Rounded.Book,
                        iconTint = gradient.accentColor,
                        iconBg = gradient.softContainer,
                        value = "${courses.size}",
                        label = adapt.workspaceSectionTitle,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProgress
                    )
                    BentoMetricCard(
                        icon = Icons.Rounded.TaskAlt,
                        iconTint = AppleEmerald,
                        iconBg = AppleEmerald.copy(alpha = 0.12f),
                        value = "${todayTasks.count { !it.completed }}",
                        label = "À faire aujourd'hui",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPlanner
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoMetricCard(
                        icon = Icons.Rounded.Schedule,
                        iconTint = AppleSky,
                        iconBg = AppleSky.copy(alpha = 0.12f),
                        value = weeklyHoursText,
                        label = "Focus cette semaine",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProgress
                    )
                    BentoMetricCard(
                        icon = Icons.Rounded.School,
                        iconTint = AppleAmber,
                        iconBg = AppleAmber.copy(alpha = 0.12f),
                        value = String.format(Locale.FRANCE, "%.1f/20", avgGrade),
                        label = adapt.metricScoreLabel,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProgress
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = adapt.scheduleSectionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppleTextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = onNavigateToPlanner) {
                        Text("Tout voir", color = gradient.accentColor)
                    }
                    Surface(
                        onClick = onOpenCreateTask,
                        shape = CircleShape,
                        color = gradient.softContainer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_add_task_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Nouvelle tâche",
                                tint = gradient.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenCreateTask
                ) {
                    Text(
                        text = "Journée libre",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aucune tâche prévue pour aujourd'hui. Appuyez ici pour créer une session d'étude.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }
        } else {
            items(todayTasks.take(4), key = { it.id }) { task ->
                TodayTaskCard(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    onStartFocus = { onStartFocusForTask(task) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = adapt.workspaceSectionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppleTextPrimary
                )
                TextButton(onClick = onNavigateToProgress) {
                    Text("Détails", color = gradient.accentColor)
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(courses, key = { it.id }) { course ->
                    CourseMiniCard(
                        course = course,
                        onClick = onNavigateToProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoMetricCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppleCard(
        modifier = modifier,
        onClick = onClick,
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppleTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TodayTaskCard(
    task: StudyTask,
    onToggle: () -> Unit,
    onStartFocus: () -> Unit
) {
    val gradient = LocalElanGradient.current
    val category = TaskCategory.fromKey(task.category)
    val categoryColor = category.accentColor()

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(if (task.completed) categoryColor.copy(alpha = 0.35f) else categoryColor)
            )
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("toggle_task_${task.id}")
            ) {
                Icon(
                    imageVector = if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (task.completed) "Marquer comme non terminée" else "Marquer comme terminée",
                    tint = if (task.completed) categoryColor else AppleTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategoryPill(category = category, showIcon = false)
                    Text(
                        text = task.courseTitle.ifBlank { "Génie Logiciel" },
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient.accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (task.aiGenerated) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Planifié par IA",
                            tint = gradient.midColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.completed) AppleTextSecondary else AppleTextPrimary,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${task.scheduledTime.ifBlank { "Flexible" }} · ${task.estimatedMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary
                )
            }

            if (!task.completed) {
                Surface(
                    onClick = onStartFocus,
                    shape = CircleShape,
                    color = gradient.softContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Lancer Focus sur cette tâche",
                            tint = gradient.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseMiniCard(
    course: Course,
    onClick: () -> Unit
) {
    val gradient = LocalElanGradient.current
    val progress = (course.completedMinutes.toFloat() / course.targetMinutes.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)
    val paletteColors = listOf(
        gradient.accentColor,
        AppleSky,
        AppleEmerald,
        AppleAmber,
        AppleRose,
        Color(0xFF8B5CF6)
    )
    val accent = paletteColors[(course.colorIndex.toInt()).mod(paletteColors.size)]

    AppleCard(
        modifier = Modifier.width(205.dp),
        onClick = onClick,
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accent.copy(alpha = 0.14f)
            ) {
                Text(
                    text = course.code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                text = String.format(Locale.FRANCE, "%.1f/20", course.grade),
                style = MaterialTheme.typography.labelMedium,
                color = AppleTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = course.title,
            style = MaterialTheme.typography.titleSmall,
            color = AppleTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(40.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = course.professor.ifBlank { "Département Info" },
            style = MaterialTheme.typography.bodySmall,
            color = AppleTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = accent,
            trackColor = accent.copy(alpha = 0.14f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${course.completedMinutes / 60}h / ${course.targetMinutes / 60}h · ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = AppleTextSecondary
        )
    }
}
