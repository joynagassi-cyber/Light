package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CalendarViewMonth
import androidx.compose.material.icons.rounded.CalendarViewWeek
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AiStudyPlanResult
import com.example.data.model.Course
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.ui.AiPlanUiState
import com.example.ui.components.AppleCard
import com.example.ui.components.LightControl
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.CalendarShowcaseCard
import com.example.ui.components.CalendarViewMode
import com.example.ui.components.TaskCategoryPill
import com.example.ui.components.accentColor
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import com.example.ui.theme.appVerticalBrush
import java.time.LocalDate

private enum class TaskFilter(val label: String) {
    ALL("Toutes"),
    PENDING("À faire"),
    HIGH("Priorité haute"),
    DONE("Terminées")
}

private fun CalendarViewMode.menuIcon(): ImageVector = when (this) {
    CalendarViewMode.DAY -> Icons.Rounded.Today
    CalendarViewMode.THREE_DAYS -> Icons.Rounded.ViewColumn
    CalendarViewMode.WEEK -> Icons.Rounded.CalendarViewWeek
    CalendarViewMode.MONTH -> Icons.Rounded.CalendarViewMonth
    CalendarViewMode.QUARTER -> Icons.Rounded.DateRange
    CalendarViewMode.YEAR -> Icons.Rounded.CalendarMonth
}

@Composable
fun PlannerScreen(
    selectedDateIso: String,
    defaultCalendarViewKey: String,
    onSelectDate: (String) -> Unit,
    courses: List<Course>,
    tasks: List<StudyTask>,
    aiPlanState: AiPlanUiState,
    onGenerateAiPlan: (String, String, Int) -> Unit,
    onApplyAiPlan: (AiStudyPlanResult) -> Unit,
    onResetAiPlan: () -> Unit,
    onOpenCreateTask: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGantt: () -> Unit = {},
    onToggleTask: (StudyTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    var aiExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf<TaskCategory?>(null) }
    var showSelectedDayOnly by remember { mutableStateOf(false) }
    var viewsMenuExpanded by remember { mutableStateOf(false) }
    var calendarMode by rememberSaveable {
        mutableStateOf(CalendarViewMode.fromKey(defaultCalendarViewKey))
    }

    LaunchedEffect(defaultCalendarViewKey) {
        calendarMode = CalendarViewMode.fromKey(defaultCalendarViewKey)
    }

    val filteredTasks = remember(tasks, selectedFilter, selectedCategoryFilter, selectedDateIso, showSelectedDayOnly) {
        tasks.filter { task ->
            val dateOk = !showSelectedDayOnly || task.dueDateIso == selectedDateIso
            val filterOk = when (selectedFilter) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.completed
                TaskFilter.HIGH -> task.priority == "high" && !task.completed
                TaskFilter.DONE -> task.completed
            }
            val categoryOk = selectedCategoryFilter == null ||
                TaskCategory.fromKey(task.category) == selectedCategoryFilter
            dateOk && filterOk && categoryOk
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Planning",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // Tappable active view indicator pill
                    LightControl(
                        onClick = { viewsMenuExpanded = true },
                        color = gradient.softContainer,
                        modifier = Modifier.testTag("calendar_active_view_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = calendarMode.menuIcon(),
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Vue : ${calendarMode.label}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = gradient.accentColor
                            )
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = "Changer de vue",
                                tint = gradient.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Right actions: + Add Task & ⋮ Three-Dot Menu organizing all 6 views
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LightControl(
                        onClick = onOpenCreateTask,
                        color = gradient.accentColor,
                        modifier = Modifier.testTag("add_task_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Ajouter une tâche",
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }

                    Box {
                        LightControl(
                            onClick = { viewsMenuExpanded = true },
                            modifier = Modifier.testTag("calendar_views_menu_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Organiser les 6 vues du calendrier",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = viewsMenuExpanded,
                            onDismissRequest = { viewsMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "VUES DU CALENDRIER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            CalendarViewMode.entries.forEach { mode ->
                                val selected = calendarMode == mode
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = mode.label,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = mode.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = mode.menuIcon(),
                                            contentDescription = null,
                                            tint = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Vue active",
                                                tint = gradient.accentColor
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        calendarMode = mode
                                        viewsMenuExpanded = false
                                    },
                                    modifier = Modifier.testTag("calendar_view_${mode.key}")
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = {
                                    Text(if (aiExpanded) "Masquer Assistant IA" else "Planificateur IA")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = gradient.accentColor
                                    )
                                },
                                onClick = {
                                    aiExpanded = !aiExpanded
                                    viewsMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(if (showSelectedDayOnly) "Afficher toutes les dates" else "Uniquement le jour choisi")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    showSelectedDayOnly = !showSelectedDayOnly
                                    viewsMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Paramètres du calendrier") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    viewsMenuExpanded = false
                                    onOpenSettings()
                                },
                                modifier = Modifier.testTag("planner_settings_button")
                            )
                            DropdownMenuItem(
                                text = { Text("Ouvrir le diagramme Gantt") },
                                leadingIcon = { Icon(Icons.Rounded.DateRange, contentDescription = null, tint = gradient.accentColor) },
                                onClick = {
                                    viewsMenuExpanded = false
                                    onOpenGantt()
                                },
                                modifier = Modifier.testTag("planner_open_gantt")
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CalendarShowcaseCard(
                    mode = calendarMode,
                    selectedDateIso = selectedDateIso,
                    tasks = tasks,
                    onSelectDate = { iso ->
                        onSelectDate(iso)
                        if (calendarMode != CalendarViewMode.YEAR && calendarMode != CalendarViewMode.QUARTER) {
                            showSelectedDayOnly = true
                        }
                    },
                    onSelectMode = { nextMode ->
                        calendarMode = nextMode
                    },
                    onToggleTask = onToggleTask,
                    onStartFocusForTask = onStartFocusForTask
                )

                if (showSelectedDayOnly) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtré sur le $selectedDateIso",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toutes les dates",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = gradient.accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSelectedDayOnly = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            SmartAiPlannerCard(
                expanded = aiExpanded,
                onToggleExpanded = { aiExpanded = !aiExpanded },
                aiPlanState = aiPlanState,
                onGenerate = onGenerateAiPlan,
                onApplyPlan = onApplyAiPlan,
                onReset = onResetAiPlan
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { filter ->
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = gradient.accentColor
                            )
                        )
                    }
                }

                // Category color-tag filter strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allCatSelected = selectedCategoryFilter == null
                    FilterChip(
                        selected = allCatSelected,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Toutes catégories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gradient.softContainer,
                            selectedLabelColor = gradient.accentColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = allCatSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = gradient.accentColor
                        ),
                        modifier = Modifier.testTag("planner_category_filter_all")
                    )

                    TaskCategory.entries.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        val catColor = category.accentColor()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else category
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                            },
                            label = { Text(category.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.14f),
                                selectedLabelColor = catColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = catColor
                            ),
                            modifier = Modifier.testTag("planner_category_filter_${category.key}")
                        )
                    }
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Aucune tâche dans cette vue",
                    description = "Ajoutez une tâche ou demandez au planificateur IA d'organiser vos révisions.",
                    actionLabel = "Créer une tâche",
                    onAction = onOpenCreateTask
                )
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                PlannerTaskRow(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    onDelete = { onDeleteTask(task.id) },
                    onStartFocus = { onStartFocusForTask(task) }
                )
            }
        }
    }
}

@Composable
private fun SmartAiPlannerCard(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    aiPlanState: AiPlanUiState,
    onGenerate: (String, String, Int) -> Unit,
    onApplyPlan: (AiStudyPlanResult) -> Unit,
    onReset: () -> Unit
) {
    val gradient = LocalLightGradient.current
    var goalText by remember { mutableStateOf("") }
    var horizonDays by remember { mutableIntStateOf(4) }
    var dailyMinutes by remember { mutableIntStateOf(60) }

    val quickGoals = listOf(
        "Préparer le partiel Systèmes Distribués",
        "Réviser Design Patterns & Clean Architecture",
        "Maîtriser Dijkstra, BFS et graphes pour le TD"
    )

    AppleCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(dampingRatio = 0.82f, stiffness = 260f)),
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggleExpanded() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradient.appBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Assistant Planification IA",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Découpe intelligente pour votre semestre L3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Réduire" else "Déplier",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickGoals.forEach { preset ->
                        Surface(
                            onClick = { goalText = preset },
                            shape = CircleShape,
                            color = gradient.softContainer
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = gradient.accentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    placeholder = { Text("Ex : Préparer l'examen de Bases de Données vendredi") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_goal_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 7).forEach { days ->
                        val selected = horizonDays == days
                        FilterChip(
                            selected = selected,
                            onClick = { horizonDays = days },
                            label = { Text("${days}j") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    listOf(45, 60, 90).forEach { mins ->
                        val selected = dailyMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = { dailyMinutes = mins },
                            label = { Text("${mins}m/j") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (aiPlanState) {
                    is AiPlanUiState.Idle, is AiPlanUiState.Error -> {
                        if (aiPlanState is AiPlanUiState.Error) {
                            Surface(
                                color = AppleRose.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = aiPlanState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleRose,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val targetIso = LocalDate.now().plusDays(horizonDays.toLong()).toString()
                                onGenerate(goalText, targetIso, dailyMinutes)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = gradient.accentColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_ai_plan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Générer mon planning IA")
                        }
                    }

                    is AiPlanUiState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = gradient.accentColor,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Le modèle actif prépare votre planning…",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onReset, modifier = Modifier.testTag("cancel_ai_plan_button")) { Text("Annuler") }
                        }
                    }

                    is AiPlanUiState.Success -> {
                        val plan = aiPlanState.result
                        Surface(
                            color = gradient.softContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = plan.summary,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Astuce : ${plan.strategyTip}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = gradient.onSoftContainer
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                plan.tasks.forEach { item ->
                                    val itemCat = TaskCategory.fromKey(item.category)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(itemCat.accentColor())
                                        )
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${itemCat.label} · ${item.estimatedMinutes}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = itemCat.accentColor()
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReset,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Annuler")
                            }
                            Button(
                                onClick = { onApplyPlan(plan) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gradient.accentColor,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("apply_ai_plan_button")
                            ) {
                                Text("Ajouter (${plan.tasks.size})")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerTaskRow(
    task: StudyTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    val gradient = LocalLightGradient.current
    var actionsExpanded by remember(task.id) { mutableStateOf(false) }
    val category = TaskCategory.fromKey(task.category)
    val categoryColor = category.accentColor()
    val priorityLabel = when (task.priority) {
        "high" -> "Haute"
        "medium" -> "Moyenne"
        else -> "Douce"
    }
    val priorityColor = when (task.priority) {
        "high" -> AppleRose
        "medium" -> AppleAmber
        else -> AppleEmerald
    }

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distinct vertical category color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.completed) categoryColor.copy(alpha = 0.35f) else categoryColor
                    )
            )

            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Basculer l'état de la tâche",
                    tint = if (task.completed) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategoryPill(category = category)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    if (task.aiGenerated) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = gradient.softContainer
                        ) {
                            Text(
                                text = "IA",
                                style = MaterialTheme.typography.labelSmall,
                                color = gradient.accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.notes.isNotBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                task.subtasks.forEachIndexed { index, subtask ->
                    Text("${index + 1}. $subtask", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${task.courseTitle.ifBlank { "Génie Logiciel" }} · ${task.dueDateIso} · ${task.scheduledTime.ifBlank { "Libre" }} · ${task.estimatedMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(
                    onClick = { actionsExpanded = true },
                    modifier = Modifier.size(44.dp).testTag("planner_task_actions_${task.id}")
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Actions pour ${task.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                    if (!task.completed) {
                        DropdownMenuItem(
                            text = { Text("Démarrer une session Focus") },
                            leadingIcon = { Icon(Icons.Rounded.PlayArrow, null) },
                            onClick = { actionsExpanded = false; onStartFocus() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Supprimer la tâche") },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null) },
                        onClick = { actionsExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}
