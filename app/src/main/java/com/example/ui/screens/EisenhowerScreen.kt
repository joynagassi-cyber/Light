package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.StudyTask
import com.example.data.model.resolvedQuadrant
import com.example.ui.components.AppleCard
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalLightGradient
import java.time.LocalDate

private data class MatrixQuadrantMeta(
    val number: Int,
    val roman: String,
    val title: String,
    val hint: String,
    val color: Color
)

private val MatrixQuadrants = listOf(
    MatrixQuadrantMeta(1, "I", "Urgent et important", "À faire en priorité", AppleRose),
    MatrixQuadrantMeta(2, "II", "Non urgent mais important", "Planifier & approfondir", AppleAmber),
    MatrixQuadrantMeta(3, "III", "Urgent mais non important", "Traiter vite ou alléger", AppleSky),
    MatrixQuadrantMeta(4, "IV", "Pas urgent et non important", "Plus tard / optionnel", AppleEmerald)
)

@Composable
fun EisenhowerScreen(
    tasks: List<StudyTask>,
    onToggleTask: (StudyTask) -> Unit,
    onMoveTaskQuadrant: (String, Int) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    onOpenCreateTaskForQuadrant: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val todayIso = remember { LocalDate.now().toString() }
    var hideCompleted by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    val visibleTasks = remember(tasks, hideCompleted) {
        if (hideCompleted) tasks.filter { !it.completed } else tasks
    }
    val byQuadrant = remember(visibleTasks, todayIso) {
        visibleTasks.groupBy { it.resolvedQuadrant(todayIso) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("eisenhower_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Matrice d'Eisenhower",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "${visibleTasks.size} tâche(s) classées sur 4 quadrants",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { onOpenCreateTaskForQuadrant(1) },
                        shape = CircleShape,
                        color = gradient.accentColor,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("matrix_add_task_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Nouvelle tâche",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Box {
                        Surface(
                            onClick = { menuExpanded = true },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(0.8.dp, AppleDivider),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("matrix_menu_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Options de la matrice",
                                    tint = AppleTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = Color.White
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (hideCompleted) "Afficher les terminées" else "Masquer les terminées")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (hideCompleted) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                        contentDescription = null,
                                        tint = gradient.accentColor
                                    )
                                },
                                onClick = {
                                    hideCompleted = !hideCompleted
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Paramètres") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = null,
                                        tint = AppleTextSecondary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Row 1: Quadrants I & II
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuadrantCard(
                    meta = MatrixQuadrants[0],
                    tasks = byQuadrant[1].orEmpty(),
                    onToggleTask = onToggleTask,
                    onMoveTaskQuadrant = onMoveTaskQuadrant,
                    onStartFocusForTask = onStartFocusForTask,
                    onQuickAdd = { onOpenCreateTaskForQuadrant(1) },
                    modifier = Modifier.weight(1f)
                )
                QuadrantCard(
                    meta = MatrixQuadrants[1],
                    tasks = byQuadrant[2].orEmpty(),
                    onToggleTask = onToggleTask,
                    onMoveTaskQuadrant = onMoveTaskQuadrant,
                    onStartFocusForTask = onStartFocusForTask,
                    onQuickAdd = { onOpenCreateTaskForQuadrant(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Quadrants III & IV
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuadrantCard(
                    meta = MatrixQuadrants[2],
                    tasks = byQuadrant[3].orEmpty(),
                    onToggleTask = onToggleTask,
                    onMoveTaskQuadrant = onMoveTaskQuadrant,
                    onStartFocusForTask = onStartFocusForTask,
                    onQuickAdd = { onOpenCreateTaskForQuadrant(3) },
                    modifier = Modifier.weight(1f)
                )
                QuadrantCard(
                    meta = MatrixQuadrants[3],
                    tasks = byQuadrant[4].orEmpty(),
                    onToggleTask = onToggleTask,
                    onMoveTaskQuadrant = onMoveTaskQuadrant,
                    onStartFocusForTask = onStartFocusForTask,
                    onQuickAdd = { onOpenCreateTaskForQuadrant(4) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuadrantCard(
    meta: MatrixQuadrantMeta,
    tasks: List<StudyTask>,
    onToggleTask: (StudyTask) -> Unit,
    onMoveTaskQuadrant: (String, Int) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppleCard(
        modifier = modifier.testTag("matrix_quadrant_${meta.number}"),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(meta.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = meta.roman,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = meta.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = meta.color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onQuickAdd,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Ajouter au quadrant ${meta.roman}",
                    tint = meta.color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = meta.color.copy(alpha = 0.16f))
        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(meta.color.copy(alpha = 0.05f))
                    .clickable(onClick = onQuickAdd)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = meta.hint,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppleTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+ Ajouter",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = meta.color
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tasks.take(6).forEach { task ->
                    MatrixTaskItem(
                        task = task,
                        accentColor = meta.color,
                        currentQuadrant = meta.number,
                        onToggle = { onToggleTask(task) },
                        onMoveQuadrant = { targetQ -> onMoveTaskQuadrant(task.id, targetQ) },
                        onStartFocus = { onStartFocusForTask(task) }
                    )
                }
                if (tasks.size > 6) {
                    Text(
                        text = "+${tasks.size - 6} autres tâches",
                        style = MaterialTheme.typography.labelSmall,
                        color = meta.color,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixTaskItem(
    task: StudyTask,
    accentColor: Color,
    currentQuadrant: Int,
    onToggle: () -> Unit,
    onMoveQuadrant: (Int) -> Unit,
    onStartFocus: () -> Unit
) {
    var itemMenuOpen by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.06f))
                .clickable { itemMenuOpen = true }
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = "Valider ${task.title}",
                tint = if (task.completed) accentColor else accentColor.copy(alpha = 0.78f),
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggle)
                    .padding(1.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (task.completed) AppleTextSecondary else AppleTextPrimary,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${task.dueDateIso} · ${task.estimatedMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = itemMenuOpen,
            onDismissRequest = { itemMenuOpen = false },
            containerColor = Color.White
        ) {
            if (!task.completed) {
                DropdownMenuItem(
                    text = { Text("Lancer Focus") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = accentColor
                        )
                    },
                    onClick = {
                        itemMenuOpen = false
                        onStartFocus()
                    }
                )
            }
            MatrixQuadrants.forEach { target ->
                DropdownMenuItem(
                    text = { Text("Quadrant ${target.roman} · ${target.title}") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = target.color
                        )
                    },
                    trailingIcon = if (target.number == currentQuadrant) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = target.color
                            )
                        }
                    } else null,
                    onClick = {
                        itemMenuOpen = false
                        onMoveQuadrant(target.number)
                    }
                )
            }
        }
    }
}
