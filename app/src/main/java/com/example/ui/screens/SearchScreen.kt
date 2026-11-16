package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.StudyCountdown
import com.example.data.model.StudyHabit
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.ui.components.AppleCard
import com.example.ui.components.TaskCategoryPill
import com.example.ui.components.accentColor
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalElanGradient
import java.time.LocalDate
import java.util.Locale

private enum class SearchScope(val label: String) {
    ALL("Tout"),
    TASKS("Tâches"),
    COURSES("Matières"),
    HABITS("Habitudes"),
    COUNTDOWNS("À rebours")
}

@Composable
fun SearchScreen(
    tasks: List<StudyTask>,
    courses: List<Course>,
    habits: List<StudyHabit>,
    countdowns: List<StudyCountdown>,
    onToggleTask: (StudyTask) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    onToggleHabitToday: (StudyHabit) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalElanGradient.current
    var query by rememberSaveable { mutableStateOf("") }
    var scope by rememberSaveable { mutableStateOf(SearchScope.ALL) }
    val todayIso = remember { LocalDate.now().toString() }

    val q = query.trim().lowercase(Locale.FRANCE)

    val matchedTasks = remember(tasks, q, scope) {
        if (scope != SearchScope.ALL && scope != SearchScope.TASKS) emptyList()
        else tasks.filter {
            q.isEmpty() ||
                it.title.lowercase(Locale.FRANCE).contains(q) ||
                it.courseTitle.lowercase(Locale.FRANCE).contains(q) ||
                it.notes.lowercase(Locale.FRANCE).contains(q)
        }
    }

    val matchedCourses = remember(courses, q, scope) {
        if (scope != SearchScope.ALL && scope != SearchScope.COURSES) emptyList()
        else courses.filter {
            q.isEmpty() ||
                it.code.lowercase(Locale.FRANCE).contains(q) ||
                it.title.lowercase(Locale.FRANCE).contains(q) ||
                it.professor.lowercase(Locale.FRANCE).contains(q)
        }
    }

    val matchedHabits = remember(habits, q, scope) {
        if (scope != SearchScope.ALL && scope != SearchScope.HABITS) emptyList()
        else habits.filter {
            q.isEmpty() ||
                it.title.lowercase(Locale.FRANCE).contains(q) ||
                it.subtitle.lowercase(Locale.FRANCE).contains(q)
        }
    }

    val matchedCountdowns = remember(countdowns, q, scope) {
        if (scope != SearchScope.ALL && scope != SearchScope.COUNTDOWNS) emptyList()
        else countdowns.filter {
            q.isEmpty() ||
                it.title.lowercase(Locale.FRANCE).contains(q) ||
                it.notes.lowercase(Locale.FRANCE).contains(q)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Rechercher",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppleTextPrimary
                )
                Text(
                    text = "Effectuez facilement une recherche rapide",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTextSecondary
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(0.9.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Tâche, cours L3, habitude, examen...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = gradient.accentColor
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Effacer la recherche",
                                    tint = AppleTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input")
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchScope.entries.forEach { itemScope ->
                    val selected = scope == itemScope
                    FilterChip(
                        selected = selected,
                        onClick = { scope = itemScope },
                        label = { Text(itemScope.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gradient.softContainer,
                            selectedLabelColor = gradient.accentColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = AppleDivider,
                            selectedBorderColor = gradient.accentColor
                        )
                    )
                }
            }
        }

        if (matchedTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Tâches (${matchedTasks.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppleTextSecondary
                )
            }
            items(matchedTasks.take(15), key = { "task_${it.id}" }) { task ->
                val cat = TaskCategory.fromKey(task.category)
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onToggleTask(task) }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = "Basculer",
                                tint = if (task.completed) cat.accentColor() else AppleTextSecondary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            TaskCategoryPill(category = cat, showIcon = false)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (task.completed) AppleTextSecondary else AppleTextPrimary,
                                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${task.courseTitle} · ${task.dueDateIso}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextSecondary
                            )
                        }
                        if (!task.completed) {
                            IconButton(onClick = { onStartFocusForTask(task) }, modifier = Modifier.size(44.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Focus",
                                    tint = gradient.accentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        if (matchedCourses.isNotEmpty()) {
            item {
                Text(
                    text = "Matières L3 (${matchedCourses.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppleTextSecondary
                )
            }
            items(matchedCourses, key = { "course_${it.id}" }) { course ->
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = gradient.accentColor
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${course.code} · ${course.title}",
                                style = MaterialTheme.typography.titleSmall,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "${course.professor} · ${course.completedMinutes / 60}h / ${course.targetMinutes / 60}h",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleTextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (matchedHabits.isNotEmpty()) {
            item {
                Text(
                    text = "Habitudes (${matchedHabits.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppleTextSecondary
                )
            }
            items(matchedHabits, key = { "habit_${it.id}" }) { habit ->
                val done = habit.completedDates.contains(todayIso)
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SelfImprovement,
                            contentDescription = null,
                            tint = gradient.accentColor
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "Série : ${habit.streakDays}j",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextSecondary
                            )
                        }
                        IconButton(onClick = { onToggleHabitToday(habit) }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = "Valider",
                                tint = if (done) gradient.accentColor else AppleTextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (matchedCountdowns.isNotEmpty()) {
            item {
                Text(
                    text = "Comptes à rebours (${matchedCountdowns.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppleTextSecondary
                )
            }
            items(matchedCountdowns, key = { "cd_${it.id}" }) { cd ->
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Event,
                            contentDescription = null,
                            tint = gradient.accentColor
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cd.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "Date cible : ${cd.targetDateIso}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (matchedTasks.isEmpty() && matchedCourses.isEmpty() && matchedHabits.isEmpty() && matchedCountdowns.isEmpty()) {
            item {
                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Aucun résultat pour \"$query\"",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "Essayez un mot-clé de matière, de tâche ou une catégorie.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }
        }
    }
}
