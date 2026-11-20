package com.example.ui.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import com.example.data.model.*
import com.example.ui.theme.LightSpacing
import com.example.ui.theme.LightTouchTarget
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.roundToInt

private enum class TaskPicker { START, DUE, TIME, NOTES }

internal fun <T> moveDraftItem(items: List<T>, from: Int, to: Int): List<T> {
    if (from !in items.indices || to !in items.indices || from == to) return items
    return items.toMutableList().apply { add(to, removeAt(from)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationScreen(
    courses: List<Course>, projects: List<StudyProject> = emptyList(), focusProfiles: List<FocusProfile> = emptyList(),
    initialDateIso: String, initialQuadrant: Int? = null, initialProjectId: String = "", initialFocusProfileId: String = "",
    onCancel: () -> Unit,
    onCreateTask: (Course?, String, String, String, String, String, String, String, Long, Long?, String, String, String, List<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val initialDate = remember(initialDateIso) { runCatching { LocalDate.parse(initialDateIso) }.getOrElse { LocalDate.now() }.toString() }
    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var due by rememberSaveable { mutableStateOf(initialDate) }
    var start by rememberSaveable { mutableStateOf(initialDate) }
    var time by rememberSaveable { mutableStateOf("10:00") }
    var duration by rememberSaveable { mutableLongStateOf(45L) }
    var priority by rememberSaveable { mutableStateOf("medium") }
    var category by rememberSaveable { mutableStateOf(TaskCategory.REVISION.key) }
    var courseId by rememberSaveable { mutableStateOf(courses.firstOrNull()?.id.orEmpty()) }
    var projectId by rememberSaveable { mutableStateOf(initialProjectId) }
    var focusId by rememberSaveable { mutableStateOf(initialFocusProfileId) }
    var quadrant by rememberSaveable { mutableStateOf(initialQuadrant?.toLong()?.takeIf { it in 1L..4L }) }
    var picker by remember { mutableStateOf<TaskPicker?>(null) }
    var subtasks by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    fun reorder(from: Int, to: Int) { subtasks = ArrayList(moveDraftItem(subtasks, from, to)) }

    ModalBottomSheet(onDismissRequest = onCancel, sheetState = sheetState) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = LightSpacing.two)) {
            // Make the expanded anchor taller than half the viewport, even with an empty draft.
            Column(Modifier.fillMaxWidth().height(maxHeight)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Nouvelle tâche", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("cancel_task_creation_button")) {
                        Icon(Icons.Rounded.Close, "Fermer")
                    }
                }
                OutlinedTextField(title, { title = it.take(140) }, label = { Text("Nom de la tâche") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_task_title_input"))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(LightSpacing.half)) {
                    TaskOption(Icons.Rounded.CalendarMonth, "Échéance", due) { picker = TaskPicker.DUE }
                    TaskOption(Icons.Rounded.EventAvailable, "Début", start) { picker = TaskPicker.START }
                    TaskOption(Icons.Rounded.AccessTime, "Heure", time) { picker = TaskPicker.TIME }
                    TaskMenu(Icons.Rounded.Flag, "Priorité", priority, listOf("low" to "Basse", "medium" to "Normale", "high" to "Haute")) { priority = it }
                    TaskMenu(Icons.Rounded.Category, "Catégorie", category, TaskCategory.entries.map { it.key to it.label }) { category = it }
                    TaskMenu(Icons.Rounded.Timer, "Durée", duration, listOf(15L, 25L, 30L, 45L, 60L, 90L, 120L).map { it to "$it min" }) { duration = it }
                    TaskMenu(Icons.Rounded.Book, "Cours", courseId, listOf("" to "Aucun") + courses.map { it.id to it.title }) { courseId = it }
                    TaskMenu(Icons.Rounded.Folder, "Projet", projectId, listOf("" to "Aucun") + projects.map { it.id to it.title }) { projectId = it }
                    TaskMenu(Icons.Rounded.CenterFocusStrong, "Profil Focus", focusId, listOf("" to "Aucun") + focusProfiles.map { it.id to it.title }) { focusId = it }
                    TaskMenu(Icons.Rounded.GridView, "Quadrant", quadrant, listOf<Pair<Long?, String>>(null to "Automatique") + (1L..4L).map { it to "Quadrant $it" }) { quadrant = it }
                    TaskOption(Icons.Rounded.EditNote, "Notes", if (notes.isBlank()) "Aucune" else "Ajoutées") { picker = TaskPicker.NOTES }
                }
                // Keep the only CTA visible in the collapsed state; the draft occupies the remaining visible space.
                Button(
                    onClick = {
                        val focus = focusProfiles.firstOrNull { it.id == focusId }
                        onCreateTask(courses.firstOrNull { it.id == courseId }, title.trim(), notes.trim(), start, due,
                            time, priority, category, duration, quadrant, projectId, focus?.id.orEmpty(), focus?.title.orEmpty(), normalizeSubtasks(subtasks))
                    },
                    enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth().testTag("save_new_task_button")
                ) { Text("Créer la tâche") }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = LightSpacing.one),
                    verticalArrangement = Arrangement.spacedBy(LightSpacing.one)) {
                    Text("Sous-tâches", style = MaterialTheme.typography.titleMedium)
                    subtasks.forEachIndexed { index, value ->
                        var drag by remember { mutableFloatStateOf(0f) }
                        Row(Modifier.offset { IntOffset(0, drag.roundToInt()) }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.DragHandle, "Réordonner la sous-tâche ${index + 1}",
                                Modifier.size(LightTouchTarget.minimum).padding(LightSpacing.one)
                                    .semantics {
                                        customActions = buildList {
                                            if (index > 0) add(CustomAccessibilityAction("Monter") { reorder(index, index - 1); true })
                                            if (index < subtasks.lastIndex) add(CustomAccessibilityAction("Descendre") { reorder(index, index + 1); true })
                                        }
                                    }.pointerInput(index, subtasks.size) {
                                        val row = (LightTouchTarget.minimum + LightSpacing.two).toPx()
                                        detectDragGesturesAfterLongPress(
                                            onDragEnd = { val dest = (index + (drag / row).roundToInt()).coerceIn(0, subtasks.lastIndex); drag = 0f; reorder(index, dest) },
                                            onDragCancel = { drag = 0f },
                                            onDrag = { change, delta -> change.consume(); drag += delta.y }
                                        )
                                    })
                            OutlinedTextField(value, { next -> subtasks = ArrayList(subtasks).apply { set(index, next.take(MAX_SUBTASK_LENGTH)) } },
                                label = { Text("Étape ${index + 1}") }, singleLine = true,
                                modifier = Modifier.weight(1f).testTag("task_subtask_$index"))
                            IconButton({ subtasks = ArrayList(subtasks).apply { removeAt(index) } }) { Icon(Icons.Rounded.DeleteOutline, "Retirer l’étape ${index + 1}") }
                        }
                    }
                    TextButton(onClick = { subtasks = ArrayList(subtasks).apply { add("") } }, enabled = subtasks.size < MAX_TASK_SUBTASKS,
                        modifier = Modifier.testTag("task_add_subtask")) { Text("Ajouter une sous-tâche") }
                }
            }
        }
    }

    when (picker) {
        TaskPicker.START, TaskPicker.DUE -> {
            val editingStart = picker == TaskPicker.START
            val date = LocalDate.parse(if (editingStart) start else due)
            val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
            DatePickerDialog(onDismissRequest = { picker = null },
                confirmButton = { TextButton({
                    state.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        if (editingStart) { start = selected; due = maxOf(due, start) }
                        else { due = selected; start = minOf(start, due) }
                    }
                    picker = null
                }) { Text("Valider") } },
                dismissButton = { TextButton({ picker = null }) { Text("Annuler") } }) { DatePicker(state) }
        }
        TaskPicker.TIME -> {
            val state = rememberTimePickerState(time.substringBefore(':').toInt(), time.substringAfter(':').toInt(), is24Hour = true)
            AlertDialog(onDismissRequest = { picker = null }, title = { Text("Heure prévue") }, text = { TimeInput(state) },
                confirmButton = { TextButton({ time = String.format(Locale.ROOT, "%02d:%02d", state.hour, state.minute); picker = null }) { Text("Valider") } })
        }
        TaskPicker.NOTES -> AlertDialog(onDismissRequest = { picker = null }, title = { Text("Notes") },
            text = { OutlinedTextField(notes, { notes = it.take(500) }, label = { Text("Notes de la tâche") }) },
            confirmButton = { TextButton({ picker = null }) { Text("Terminer") } })
        null -> Unit
    }
}

@Composable
private fun TaskOption(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(LightTouchTarget.minimum)) {
            Icon(icon, "$label : $value")
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun <T> TaskMenu(icon: ImageVector, label: String, selection: T, options: List<Pair<T, String>>, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TaskOption(icon, label, options.firstOrNull { it.first == selection }?.second.orEmpty()) { expanded = true }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { (value, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(value); expanded = false },
                    trailingIcon = { if (value == selection) Icon(Icons.Rounded.Check, "Sélectionné") })
            }
        }
    }
}
