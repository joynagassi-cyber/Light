package com.example.ui.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.MAX_TASK_PREDECESSORS
import com.example.data.model.wouldCreateTaskCycle
import com.example.data.model.StudyProject
import com.example.data.model.StudyTask
import com.example.ui.components.AppleCard
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.LightSpacing
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class GanttStatusFilter(val label: String) {
    ALL("Tout"), OPEN("En cours"), DONE("Terminées")
}

@Composable
fun GanttScreen(
    tasks: List<StudyTask>,
    courses: List<Course>,
    projects: List<StudyProject>,
    onCreateProject: (String, String, String, String) -> Unit,
    onUpdateProject: (StudyProject) -> Unit,
    onDeleteProject: (String) -> Unit,
    onCreateTask: (String) -> Unit,
    onToggleTask: (StudyTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onMoveTask: ((StudyTask, LocalDate, String?) -> Unit)? = null,
    onSetPredecessors: ((StudyTask, List<String>) -> Unit)? = null,
    externalCreateTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    var scaleKey by rememberSaveable { mutableStateOf(GanttScale.WEEK.key) }
    var anchorIso by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var statusFilter by rememberSaveable { mutableStateOf(GanttStatusFilter.ALL) }
    var selectedCourseId by rememberSaveable { mutableStateOf("all") }
    var selectedProjectId by rememberSaveable { mutableStateOf("all") }
    var pageMenuExpanded by remember { mutableStateOf(false) }
    var showCreateChooser by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var projectBeingEdited by remember { mutableStateOf<StudyProject?>(null) }
    var projectPendingDeletion by remember { mutableStateOf<StudyProject?>(null) }
    val tasksById = remember(tasks) { tasks.associateBy { it.id } }
    var dependencyTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    var taskPendingDeletion by remember { mutableStateOf<StudyTask?>(null) }
    LaunchedEffect(externalCreateTrigger) {
        if (externalCreateTrigger > 0) showCreateChooser = true
    }
    val scale = GanttScale.fromKey(scaleKey)
    val anchor = remember(anchorIso) { runCatching { LocalDate.parse(anchorIso) }.getOrElse { LocalDate.now() } }
    val window = remember(anchor, scale) { ganttWindow(anchor, scale) }
    val validTasks = remember(tasks, projects) {
        val projectIds = projects.mapTo(mutableSetOf()) { it.id }
        tasks.filter { it.ganttSpan() != null }.map { task ->
            // A deleted/unavailable project must not hide its task from the timeline.
            if (task.projectId.isNotBlank() && task.projectId !in projectIds) task.copy(projectId = "") else task
        }
    }
    val visibleProjects = remember(projects, validTasks, window, selectedProjectId) {
        projects.filter { project ->
            val span = project.ganttSpan()
            val projectPeriodVisible = span != null && span.start <= window.endInclusive && span.endInclusive >= window.start
            val hasVisibleTask = validTasks.any { it.projectId == project.id && window.contains(it) }
            (selectedProjectId == "all" || project.id == selectedProjectId) && (projectPeriodVisible || hasVisibleTask)
        }
    }
    val visibleTasks = remember(validTasks, window, statusFilter, selectedCourseId, selectedProjectId, projects) {
        validTasks.filter { task ->
            val statusMatches = when (statusFilter) {
                GanttStatusFilter.ALL -> true
                GanttStatusFilter.OPEN -> !task.completed
                GanttStatusFilter.DONE -> task.completed
            }
            val courseMatches = selectedCourseId == "all" || task.courseId == selectedCourseId
            val projectMatches = selectedProjectId == "all" || task.projectId == selectedProjectId
            statusMatches && courseMatches && projectMatches && window.contains(task)
        }.sortedWith(compareBy<StudyTask> { it.dueDateIso }.thenBy { it.scheduledTime }.thenBy { it.title })
    }
    val overdueCount = validTasks.count { !it.completed && it.dueDateIso < LocalDate.now().toString() }
    val completedCount = validTasks.count { it.completed }
    val completion = if (validTasks.isEmpty()) 0f else completedCount.toFloat() / validTasks.size
    val unassignedGroups = visibleTasks.filter { it.projectId.isBlank() }.groupBy { task ->
        "course:${task.courseId.ifBlank { task.courseTitle.ifBlank { "sans-matiere" } }}"
    }
    val periodLabel = remember(window, scale) { formatWindow(window, scale) }
    val columnWidth = when (scale) {
        GanttScale.DAY -> 42.dp
        GanttScale.WEEK -> 60.dp
        GanttScale.MONTH -> 34.dp
        GanttScale.QUARTER -> 52.dp
    }
    val leftWidth = 164.dp
    val timelineScroll = rememberScrollState()
    LaunchedEffect(scaleKey, anchorIso) {
        timelineScroll.scrollTo(0)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Planning Gantt", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("Projets, tâches et jalons dans un planning visuel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                IconButton(onClick = { pageMenuExpanded = true }, modifier = Modifier.testTag("gantt_more_menu")) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Options du planning", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = pageMenuExpanded, onDismissRequest = { pageMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Créer un projet") },
                        leadingIcon = { Icon(Icons.Rounded.Add, null) },
                        onClick = { pageMenuExpanded = false; projectBeingEdited = null; showProjectDialog = true },
                        modifier = Modifier.testTag("gantt_create_project")
                    )
                    DropdownMenuItem(
                        text = { Text("Ajouter une tâche ou un jalon") },
                        leadingIcon = { Icon(Icons.Rounded.Add, null) },
                        onClick = { pageMenuExpanded = false; onCreateTask(selectedProjectId.takeUnless { it == "all" }.orEmpty()) },
                        modifier = Modifier.testTag("gantt_add_item_menu")
                    )
                    DropdownMenuItem(
                        text = { Text("Revenir à aujourd'hui") },
                        leadingIcon = { Icon(Icons.Rounded.Today, null) },
                        onClick = { anchorIso = LocalDate.now().toString(); pageMenuExpanded = false },
                        modifier = Modifier.testTag("gantt_today_menu")
                    )
                }
            }
        }

        AppleCard(contentPadding = PaddingValues(14.dp), cornerRadius = 20.dp) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric("${visibleTasks.size}", "éléments visibles", gradient.accentColor, Modifier.weight(1f))
                SummaryMetric("${validTasks.count { it.category == "milestone" }}", "jalons", Color(0xFFC18A25), Modifier.weight(1f))
                SummaryMetric("$overdueCount", "en retard", if (overdueCount > 0) Color(0xFFC5445C) else MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxWidth(completion).fillMaxHeight().background(gradient.accentColor))
            }
            Text("${(completion * 100).toInt()} % des tâches terminées", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            GanttScale.entries.forEach { option ->
                FilterChip(
                    selected = scale == option,
                    onClick = { scaleKey = option.key },
                    label = { Text(option.label) },
                    modifier = Modifier.testTag("gantt_scale_${option.key}")
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { anchorIso = stepAnchor(anchor, scale, -1).toString() }, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Période précédente", modifier = Modifier.size(17.dp))
            }
            Text(periodLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = { anchorIso = LocalDate.now().toString() }, modifier = Modifier.size(42.dp).testTag("gantt_go_today")) {
                Icon(Icons.Rounded.Today, contentDescription = "Aujourd'hui", modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = { anchorIso = stepAnchor(anchor, scale, 1).toString() }, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Rounded.ArrowForwardIos, contentDescription = "Période suivante", modifier = Modifier.size(17.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(selected = selectedProjectId == "all", onClick = { selectedProjectId = "all" }, label = { Text("Tous les projets") }, modifier = Modifier.testTag("gantt_project_all"))
            projects.forEach { project ->
                FilterChip(selected = selectedProjectId == project.id, onClick = { selectedProjectId = project.id }, label = { Text(project.title, maxLines = 1) }, modifier = Modifier.testTag("gantt_project_${project.id}"))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(selected = selectedCourseId == "all", onClick = { selectedCourseId = "all" }, label = { Text("Toutes les matières") }, modifier = Modifier.testTag("gantt_course_all"))
            courses.forEach { course ->
                FilterChip(selected = selectedCourseId == course.id, onClick = { selectedCourseId = course.id }, label = { Text(course.code.ifBlank { course.title }, maxLines = 1) }, modifier = Modifier.testTag("gantt_course_${course.id}"))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            GanttStatusFilter.entries.forEach { filter ->
                FilterChip(selected = statusFilter == filter, onClick = { statusFilter = filter }, label = { Text(filter.label) }, modifier = Modifier.testTag("gantt_status_${filter.name.lowercase()}") )
            }
            Text("Libellés fixes · glissez sur le diagramme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
        }

        if (visibleTasks.isEmpty() && visibleProjects.isEmpty()) {
            AppleCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
                Text(if (validTasks.isEmpty()) "Aucun élément planifié" else "Aucune tâche sur cette période", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("Créez un projet ou ajoutez une tâche depuis le menu ⋮. Les dates de début et d'échéance apparaîtront dans le diagramme.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp))
            }
        } else {
            AppleCard(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(0.dp), cornerRadius = 20.dp) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .testTag("gantt_timeline_header"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PROJET / TÂCHE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(leftWidth).padding(start = 14.dp, top = 8.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(timelineScroll).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.width(columnWidth * window.columnCount)) {
                                Row {
                                    timelineLabels(window, scale).forEach { label ->
                                        Box(Modifier.width(columnWidth).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        visibleProjects.forEach { project ->
                            item(key = "project_${project.id}") {
                                GanttProjectRow(
                                    project = project,
                                    window = window,
                                    scale = scale,
                                    leftWidth = leftWidth,
                                    columnWidth = columnWidth,
                                    timelineScroll = timelineScroll,
                                    onAddTask = { onCreateTask(project.id) },
                                    onEdit = { projectBeingEdited = project; showProjectDialog = true },
                                    onDelete = { projectPendingDeletion = project }
                                )
                            }
                            val projectTasks = visibleTasks.filter { it.projectId == project.id }
                            items(projectTasks, key = { "gantt_${it.id}" }) { task ->
                                GanttTaskRow(task, window, scale, leftWidth, columnWidth, timelineScroll, onToggleTask, onMoveTask,
                                    tasksById, onSetPredecessors?.let { { dependencyTargetId = task.id } }
                                ) { taskPendingDeletion = task }
                            }
                        }
                        unassignedGroups.forEach { (groupKey, groupTasks) ->
                            val groupName = groupTasks.firstOrNull()?.courseTitle?.ifBlank { "Sans matière" }.orEmpty()
                            item(key = "group_$groupKey") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        groupName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = gradient.accentColor,
                                        modifier = Modifier.width(leftWidth).padding(start = 14.dp, top = 12.dp, bottom = 6.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.weight(1f).height(32.dp))
                                }
                            }
                            items(groupTasks, key = { "gantt_${it.id}" }) { task ->
                                GanttTaskRow(task, window, scale, leftWidth, columnWidth, timelineScroll, onToggleTask, onMoveTask,
                                    tasksById, onSetPredecessors?.let { { dependencyTargetId = task.id } }
                                ) { taskPendingDeletion = task }
                            }
                        }
                    }
                }
            }
        }
    }

    val dependencyTarget = dependencyTargetId?.let(tasksById::get)
    if (dependencyTarget != null && onSetPredecessors != null) {
        GanttDependenciesDialog(dependencyTarget, tasksById, onDismiss = { dependencyTargetId = null }) { ids ->
            onSetPredecessors(dependencyTarget, ids)
            dependencyTargetId = null
        }
    }

    taskPendingDeletion?.let { task ->
        AlertDialog(
            onDismissRequest = { taskPendingDeletion = null },
            title = { Text("Supprimer cet élément ?") },
            text = { Text("« ${task.title} » sera retiré du planning. Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = { onDeleteTask(task.id); taskPendingDeletion = null }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { taskPendingDeletion = null }) { Text("Annuler") } }
        )
    }
    projectPendingDeletion?.let { project ->
        AlertDialog(
            onDismissRequest = { projectPendingDeletion = null },
            title = { Text("Supprimer le projet ?") },
            text = { Text("« ${project.title} » sera supprimé. Les tâches resteront dans le planning sans être rattachées à ce projet.") },
            confirmButton = {
                TextButton(onClick = { onDeleteProject(project.id); if (selectedProjectId == project.id) selectedProjectId = "all"; projectPendingDeletion = null }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { projectPendingDeletion = null }) { Text("Annuler") } }
        )
    }
    if (showCreateChooser) {
        AlertDialog(
            onDismissRequest = { showCreateChooser = false },
            title = { Text("Ajouter au planning") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick = {
                            showCreateChooser = false
                            onCreateTask(selectedProjectId.takeUnless { it == "all" }.orEmpty())
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = gradient.softContainer,
                        modifier = Modifier.fillMaxWidth().testTag("gantt_fab_choose_task")
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Nouvelle tâche ou jalon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = gradient.accentColor)
                            Text("Planifier une tâche avec date, priorité et catégorie", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        onClick = {
                            showCreateChooser = false
                            projectBeingEdited = null
                            showProjectDialog = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth().testTag("gantt_fab_choose_project")
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Nouveau projet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Regrouper plusieurs tâches sur une période donnée", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateChooser = false }) { Text("Annuler") }
            }
        )
    }

    if (showProjectDialog) {
        ProjectEditorDialog(
            project = projectBeingEdited,
            onDismiss = { showProjectDialog = false },
            onCreate = onCreateProject,
            onUpdate = onUpdateProject
        )
    }
}

@Composable
private fun GanttProjectRow(
    project: StudyProject,
    window: GanttWindow,
    scale: GanttScale,
    leftWidth: Dp,
    columnWidth: Dp,
    timelineScroll: ScrollState,
    onAddTask: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val gradient = LocalLightGradient.current
    var menuExpanded by remember(project.id) { mutableStateOf(false) }
    val chartWidth = columnWidth * window.columnCount
    val statusLabel = when (project.status) {
        "completed" -> "Terminé"
        "on_hold" -> "En pause"
        else -> "En cours"
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .testTag("gantt_project_row_${project.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.width(leftWidth).padding(start = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(project.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$statusLabel · ${shortGanttDate(project.startDateIso)} – ${shortGanttDate(project.dueDateIso)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Actions pour ${project.title}", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Ajouter une tâche") }, leadingIcon = { Icon(Icons.Rounded.Add, null) }, onClick = { menuExpanded = false; onAddTask() })
                    DropdownMenuItem(text = { Text("Modifier le projet") }, leadingIcon = { Icon(Icons.Rounded.EditNote, null) }, onClick = { menuExpanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Supprimer le projet") }, leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null) }, onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
        Box(modifier = Modifier.weight(1f).horizontalScroll(timelineScroll).height(68.dp)) {
            Box(modifier = Modifier.width(chartWidth).height(68.dp)) {
              Row {
                repeat(window.columnCount) { index ->
                    Box(
                        modifier = Modifier.width(columnWidth).height(68.dp)
                            .background(
                                if (scale == GanttScale.DAY) {
                                    if (window.start == LocalDate.now() && index == LocalTime.now().hour) gradient.softContainer.copy(alpha = 0.3f) else Color.Transparent
                                } else if (window.dateColumn(LocalDate.now()) == index) {
                                    gradient.softContainer.copy(alpha = 0.3f)
                                } else Color.Transparent
                            )
                    ) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.92f).height(67.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {}
                    }
                }
            }
            projectBarPosition(project, window, scale)?.let { (start, end) ->
                Box(
                    modifier = Modifier.offset(x = columnWidth * start + 2.dp)
                        .width((columnWidth * (end - start) - 4.dp).coerceAtLeast(8.dp))
                        .height(13.dp).align(Alignment.CenterStart)
                        .clip(CircleShape).background(gradient.accentColor.copy(alpha = 0.72f))
                        .testTag("gantt_project_bar_${project.id}")
                )
            }
          }
        }
    }
}

private fun projectBarPosition(project: StudyProject, window: GanttWindow, scale: GanttScale): Pair<Float, Float>? {
    val columns = window.projectColumnRange(project) ?: return null
    if (scale == GanttScale.DAY) return 0f to window.columnCount.toFloat()
    return columns.first.toFloat() to (columns.last + 1f).coerceAtMost(window.columnCount.toFloat())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectEditorDialog(
    project: StudyProject?,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit,
    onUpdate: (StudyProject) -> Unit
) {
    var title by remember(project?.id) { mutableStateOf(project?.title.orEmpty()) }
    var description by remember(project?.id) { mutableStateOf(project?.description.orEmpty()) }
    var startDate by remember(project?.id) { mutableStateOf(project?.startDateIso ?: LocalDate.now().toString()) }
    var dueDate by remember(project?.id) { mutableStateOf(project?.dueDateIso ?: LocalDate.now().plusDays(30).toString()) }
    var status by remember(project?.id) { mutableStateOf(project?.status ?: "active") }
    var datePickerTarget by remember(project?.id) { mutableStateOf<String?>(null) }
    val datesValid = runCatching { !LocalDate.parse(dueDate).isBefore(LocalDate.parse(startDate)) }.getOrDefault(false)
    fun readableDate(value: String): String = runCatching {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH))
    }.getOrDefault(value)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (project == null) "Nouveau projet" else "Modifier le projet") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(title, { title = it }, label = { Text("Nom du projet") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description (facultative)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = readableDate(startDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Début") },
                        trailingIcon = { IconButton(onClick = { datePickerTarget = "start" }) { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Choisir la date de début") } },
                        modifier = Modifier.weight(1f).clickable { datePickerTarget = "start" },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = readableDate(dueDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Échéance") },
                        trailingIcon = { IconButton(onClick = { datePickerTarget = "due" }) { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Choisir l'échéance") } },
                        modifier = Modifier.weight(1f).clickable { datePickerTarget = "due" },
                        singleLine = true
                    )
                }
                if (!datesValid) Text("Vérifiez les dates : l'échéance doit suivre le début.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                if (project != null) {
                    Text("État du projet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("active" to "En cours", "on_hold" to "En pause", "completed" to "Terminé").forEach { (key, label) ->
                            FilterChip(selected = status == key, onClick = { status = key }, label = { Text(label) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && datesValid,
                onClick = {
                    if (project == null) onCreate(title.trim(), description.trim(), startDate, dueDate)
                    else onUpdate(project.copy(title = title.trim(), description = description.trim(), startDateIso = startDate, dueDateIso = dueDate, status = status))
                    onDismiss()
                }
            ) { Text(if (project == null) "Créer" else "Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
    datePickerTarget?.let { target ->
        val currentDate = if (target == "start") startDate else dueDate
        val initialMillis = runCatching {
            LocalDate.parse(currentDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { datePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        if (target == "start") startDate = picked else dueDate = picked
                    }
                    datePickerTarget = null
                }) { Text("Choisir") }
            },
            dismissButton = { TextButton(onClick = { datePickerTarget = null }) { Text("Annuler") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GanttTaskRow(
    task: StudyTask,
    window: GanttWindow,
    scale: GanttScale,
    leftWidth: Dp,
    columnWidth: Dp,
    timelineScroll: ScrollState,
    onToggleTask: (StudyTask) -> Unit,
    onMoveTask: ((StudyTask, LocalDate, String?) -> Unit)?,
    tasksById: Map<String, StudyTask>,
    onEditDependencies: (() -> Unit)?,
    onDeleteTask: (String) -> Unit
) {
    val span = task.ganttSpan() ?: return
    val gradient = LocalLightGradient.current
    var menuExpanded by remember(task.id) { mutableStateOf(false) }
    var showMovePicker by remember(task.id) { mutableStateOf(false) }
    val rowHeight = LightSpacing.six + LightSpacing.two
    val chartWidth = columnWidth * window.columnCount
    val textColor = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val issue = predecessorIssue(task, tasksById)
    val barColor = when {
        task.completed -> AppleEmerald
        issue != null -> MaterialTheme.colorScheme.error
        span.isMilestone -> AppleAmber
        else -> gradient.accentColor
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(rowHeight).background(MaterialTheme.colorScheme.surface).testTag("gantt_task_${task.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.width(leftWidth).padding(start = 4.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onToggleTask(task) }, modifier = Modifier.size(38.dp).testTag("gantt_toggle_${task.id}")) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = if (task.completed) "Réouvrir ${task.title}" else "Terminer ${task.title}", tint = if (task.completed) Color(0xFF61A781) else MaterialTheme.colorScheme.outline, modifier = Modifier.size(19.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${span.start.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))} → ${span.endInclusive.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (task.predecessorIds.isNotEmpty()) {
                    val status = issue ?: "${task.predecessorIds.size} dépendance(s)"
                    Text(status, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (issue == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Actions pour ${task.title}", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text(if (task.completed) "Réouvrir" else "Marquer terminée") }, leadingIcon = { Icon(Icons.Rounded.CheckCircle, null) }, onClick = { onToggleTask(task); menuExpanded = false })
                    if (onEditDependencies != null) DropdownMenuItem(text = { Text("Dépendances…") }, onClick = { menuExpanded = false; onEditDependencies() })
                    if (onMoveTask != null) DropdownMenuItem(text = { Text("Déplacer…") }, onClick = { menuExpanded = false; showMovePicker = true })
                    DropdownMenuItem(text = { Text("Supprimer") }, leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null) }, onClick = { onDeleteTask(task.id); menuExpanded = false })
                }
            }
        }
        Box(modifier = Modifier.weight(1f).horizontalScroll(timelineScroll).height(rowHeight)) {
          Box(modifier = Modifier.width(chartWidth).height(rowHeight)) {
            Row {
                repeat(window.columnCount) { index ->
                    val isToday = when (scale) {
                        GanttScale.DAY -> window.start == LocalDate.now() && index == LocalTime.now().hour
                        else -> window.dateColumn(LocalDate.now()) == index
                    }
                    Box(
                        modifier = Modifier.width(columnWidth).height(rowHeight)
                            .background(if (isToday) gradient.softContainer.copy(alpha = 0.34f) else Color.Transparent)
                    ) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.92f).height(rowHeight - 1.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                        ) {}
                    }
                }
            }
            window.ganttBarRange(task, scale)?.let { (startFraction, endFraction) ->
                GanttTaskBar(task, span, scale, columnWidth, startFraction, endFraction, barColor, onMoveTask)
            }
          }
        }
    }
    if (showMovePicker && onMoveTask != null) {
        GanttMoveDatePicker(span.start, onDismiss = { showMovePicker = false }) { date ->
            onMoveTask(task, date, null)
            showMovePicker = false
        }
    }

}

@Composable
private fun GanttTaskBar(
    task: StudyTask,
    span: GanttTaskSpan,
    scale: GanttScale,
    columnWidth: Dp,
    first: Float,
    last: Float,
    color: Color,
    onMoveTask: ((StudyTask, LocalDate, String?) -> Unit)?,
) {
    val dragEnabled = onMoveTask != null && (scale != GanttScale.DAY || span.start == span.endInclusive || span.isMilestone)
    val moveCallback by rememberUpdatedState(onMoveTask)
    val currentTask by rememberUpdatedState(task)
    var draggedColumns by remember(task.id, scale, span) { mutableFloatStateOf(0f) }
    val preview = ganttDragMove(span, scale, draggedColumns)?.visualColumns ?: 0f
    val barWidth = (columnWidth * (last - first)).coerceAtLeast(LightSpacing.one)
    val markerSize = LightSpacing.two
    val width = if (span.isMilestone) markerSize else barWidth
    val left = if (span.isMilestone) columnWidth * ((first + last) / 2f) - width / 2 else columnWidth * first
    val gesture = if (!dragEnabled) Modifier else Modifier.pointerInput(task.id, scale, span, columnWidth, first, last) {
        detectDragGesturesAfterLongPress(
            onDragEnd = {
                val move = ganttDragMove(span, scale, draggedColumns)
                draggedColumns = 0f
                if (move != null) moveCallback?.invoke(currentTask, move.start, move.time)
            },
            onDragCancel = { draggedColumns = 0f },
            onDrag = { change, delta ->
                change.consume()
                draggedColumns += delta.x / columnWidth.toPx()
            },
        )
    }
    Box(
        modifier = Modifier.offset(x = left + columnWidth * preview).width(width)
            .height(LightSpacing.six).then(gesture).testTag(if (span.isMilestone) "gantt_milestone_${task.id}" else "gantt_bar_${task.id}")
            .semantics { contentDescription = if (dragEnabled) "${task.title}, maintenir puis glisser pour déplacer" else task.title },
        contentAlignment = Alignment.Center,
    ) {
        if (span.isMilestone) {
            Box(Modifier.size(markerSize).rotate(45f).background(color, RoundedCornerShape(LightSpacing.half)))
        } else {
            Box(Modifier.fillMaxWidth().height(LightSpacing.two).clip(CircleShape).background(color))
        }
    }
}

private fun predecessorIssue(task: StudyTask, tasksById: Map<String, StudyTask>): String? {
    val span = task.ganttSpan() ?: return null
    val starts = if (span.isMilestone) span.endInclusive else span.start
    val predecessors = task.predecessorIds.map { tasksById[it] }
    return when {
        predecessors.any { it == null } -> "Lien introuvable"
        predecessors.filterNotNull().any { it.ganttSpan()?.endInclusive?.isAfter(starts) == true } -> "Dates en conflit"
        predecessors.filterNotNull().any { !it.completed } -> "Prédécesseur ouvert"
        else -> null
    }
}

@Composable
private fun GanttDependenciesDialog(
    task: StudyTask,
    tasksById: Map<String, StudyTask>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    var selected by remember(task.id) { mutableStateOf(task.predecessorIds.toSet()) }
    val candidates = remember(task.id, tasksById) {
        tasksById.values.filter { it.id != task.id }.sortedBy { it.title.lowercase(Locale.ROOT) }
            .map { it to wouldCreateTaskCycle(task.id, it.id, tasksById) }
    }
    val missing = selected.filterNot(tasksById::containsKey)
    val cyclic = candidates.any { (candidate, cycle) -> cycle && candidate.id in selected }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dépendances") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LightSpacing.one)) {
                Text("Sélectionnez jusqu’à $MAX_TASK_PREDECESSORS tâches à réaliser avant « ${task.title} ».")
                if (missing.isNotEmpty()) TextButton(onClick = { selected = selected - missing.toSet() }) { Text("Retirer les liens introuvables") }
                if (candidates.isEmpty()) Text("Aucune autre tâche disponible.")
                LazyColumn(Modifier.heightIn(max = LightSpacing.six * 5)) {
                    items(candidates, key = { it.first.id }) { (candidate, cycle) ->
                        val checked = candidate.id in selected
                        val enabled = checked || (!cycle && selected.size < MAX_TASK_PREDECESSORS)
                        fun toggle() { selected = if (checked) selected - candidate.id else selected + candidate.id }
                        Row(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = ::toggle), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked, onCheckedChange = null, enabled = enabled, modifier = Modifier.size(LightSpacing.six))
                            Column(Modifier.weight(1f)) {
                                Text(candidate.title)
                                Text(when {
                                    cycle -> "Créerait une boucle"
                                    candidate.completed -> "Terminée · ${candidate.dueDateIso}"
                                    else -> "Ouverte · ${candidate.dueDateIso}"
                                }, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(selected.toList()) }, enabled = !cyclic && missing.isEmpty()) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

private fun timelineLabels(window: GanttWindow, scale: GanttScale): List<String> = when (scale) {
    GanttScale.DAY -> (0 until 24).map { it.toString().padStart(2, '0') }
    GanttScale.WEEK -> (0..6).map { window.start.plusDays(it.toLong()).format(DateTimeFormatter.ofPattern("EE d", Locale.FRENCH)) }
    GanttScale.MONTH -> (1..window.columnCount).map { if ((it - 1) % 5 == 0 || it == window.columnCount) it.toString() else "·" }
    GanttScale.QUARTER -> (1..13).map { "S$it" }
}

private fun formatWindow(window: GanttWindow, scale: GanttScale): String = when (scale) {
    GanttScale.DAY -> window.start.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)).replaceFirstChar { it.uppercase() }
    GanttScale.WEEK -> "${window.start.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))} – ${window.endInclusive.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH))}"
    GanttScale.MONTH -> window.start.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)).replaceFirstChar { it.uppercase() }
    GanttScale.QUARTER -> "T${(window.start.monthValue - 1) / 3 + 1} · ${window.start.year}"
}

private fun stepAnchor(anchor: LocalDate, scale: GanttScale, direction: Long): LocalDate = when (scale) {
    GanttScale.DAY -> anchor.plusDays(direction)
    GanttScale.WEEK -> anchor.plusWeeks(direction)
    GanttScale.MONTH -> anchor.plusMonths(direction)
    GanttScale.QUARTER -> anchor.plusMonths(direction * 3)
}

private fun shortGanttDate(iso: String): String = runCatching {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))
}.getOrDefault(iso)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GanttMoveDatePicker(start: LocalDate, onDismiss: () -> Unit, onMove: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = start.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
    DatePickerDialog(onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onMove(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
            }) { Text("Déplacer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    ) { DatePicker(state) }
}
