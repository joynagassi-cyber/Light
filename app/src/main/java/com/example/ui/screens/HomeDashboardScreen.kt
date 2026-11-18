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
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.res.stringResource
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
import com.example.ui.components.CatalogIcon
import com.example.ui.components.catalogIconColor
import com.example.ui.components.GradientHeroCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SyncStatusPill
import com.example.ui.components.TaskCategoryPill
import com.example.ui.components.accentColor
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import com.example.ui.theme.appVerticalBrush
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
    isOnline: Boolean = true,
    hasPendingWrites: Boolean = false,
    isLocalMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
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

    val adapt = profile.adaptation()
    val firstName = profile?.displayName?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }
    val greeting = firstName?.let { "Bonjour, $it" } ?: "Bonjour"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(gradient.appBrush())
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

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = gradient.accentColor
                        )
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onOpenGradientPicker,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
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
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("home_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Ouvrir les paramètres",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
            if (!isLocalMode && (!isOnline || hasPendingWrites)) {
                Spacer(modifier = Modifier.height(10.dp))
                SyncStatusPill(
                    isOnline = isOnline,
                    hasPendingWrites = hasPendingWrites,
                    isLocalMode = false
                )
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
                            color = Color.White
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
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onNavigateToPlanner,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BentoMetricCard(
                    icon = Icons.Rounded.Book,
                    iconTint = gradient.accentColor,
                    iconBg = gradient.softContainer,
                    value = "${courses.size}",
                    label = "Matières",
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
                BentoMetricCard(
                    icon = Icons.Rounded.Schedule,
                    iconTint = AppleSky,
                    iconBg = AppleSky.copy(alpha = 0.12f),
                    value = weeklyHoursText,
                    label = "Focus · 7 jours",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProgress
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
                    text = adapt.scheduleSectionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
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
                EmptyStateCard(
                    title = "Journée libre",
                    description = "Aucune tâche prévue aujourd'hui. Ajoutez-en une pour définir votre prochaine étape.",
                    actionLabel = "Créer une tâche",
                    onAction = onOpenCreateTask
                )
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
                    color = MaterialTheme.colorScheme.onSurface
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
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TodayTaskCard(
    task: StudyTask,
    onToggle: () -> Unit,
    onStartFocus: () -> Unit
) {
    val gradient = LocalLightGradient.current
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
                    tint = if (task.completed) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${task.scheduledTime.ifBlank { "Flexible" }} · ${task.estimatedMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val progress = (course.completedMinutes.toFloat() / course.targetMinutes.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)
    val accent = catalogIconColor(course.colorIndex)

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
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CatalogIcon(
                        iconKey = course.iconKey,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }
            Text(
                text = String.format(Locale.FRANCE, "%.1f/20", course.grade),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = course.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(48.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = course.professor.ifBlank { "Département Info" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
