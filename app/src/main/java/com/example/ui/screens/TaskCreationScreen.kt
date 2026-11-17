package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.StudyProject
import com.example.data.model.TaskCategory
import com.example.data.model.UserProfile
import com.example.data.model.adaptation
import com.example.ui.components.AppleCard
import com.example.ui.components.accentColor
import com.example.ui.components.icon
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GradientScreenSurface
import com.example.ui.theme.LocalLightGradient
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationScreen(
    courses: List<Course>,
    projects: List<StudyProject> = emptyList(),
    initialDateIso: String,
    initialQuadrant: Int? = null,
    initialProjectId: String = "",
    profile: UserProfile? = null,
    onCancel: () -> Unit,
    onCreateTask: (
        course: Course?,
        title: String,
        notes: String,
        startDateIso: String,
        dueDateIso: String,
        scheduledTime: String,
        priority: String,
        category: String,
        estimatedMinutes: Long,
        quadrant: Long?,
        projectId: String
    ) -> Unit
) {
    BackHandler(onBack = onCancel)

    val gradient = LocalLightGradient.current
    val focusManager = LocalFocusManager.current
    val adapt = remember(profile) { profile.adaptation() }

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCourse by remember(courses) { mutableStateOf(courses.firstOrNull()) }
    var selectedProjectId by remember(initialProjectId) { mutableStateOf(initialProjectId) }
    var selectedDate by remember(initialDateIso) {
        mutableStateOf(runCatching { LocalDate.parse(initialDateIso) }.getOrElse { LocalDate.now() })
    }
    var selectedStartDate by remember(initialDateIso) {
        mutableStateOf(runCatching { LocalDate.parse(initialDateIso) }.getOrElse { LocalDate.now() })
    }
    var isPickingStartDate by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("10:00") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.REVISION) }
    var selectedPriority by remember { mutableStateOf("medium") }
    var selectedDuration by remember { mutableLongStateOf(45L) }
    var selectedQuadrant by remember(initialQuadrant) {
        mutableStateOf(initialQuadrant?.toLong()?.takeIf { it in 1L..4L })
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formattedDateLabel = remember(selectedDate) {
        val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
        selectedDate.format(formatter).replaceFirstChar { it.uppercase() }
    }
    val formattedStartDateLabel = remember(selectedStartDate) {
        selectedStartDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH))
    }

    val canSubmit = title.isNotBlank()

    fun submitTask() {
        if (!canSubmit) return
        onCreateTask(
            selectedCourse,
            title.trim(),
            notes.trim(),
            selectedStartDate.toString(),
            selectedDate.toString(),
            selectedTime,
            selectedPriority,
            selectedCategory.key,
            selectedDuration,
            selectedQuadrant,
            selectedProjectId
        )
    }

    GradientScreenSurface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
            ) {
                // Clean iOS-inspired Modal Navigation Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onCancel,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("cancel_task_creation_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Fermer",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nouvelle tâche",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = adapt.headerSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = { if (canSubmit) submitTask() },
                        enabled = canSubmit,
                        shape = CircleShape,
                        color = if (canSubmit) gradient.accentColor else Color.White.copy(alpha = 0.65f),
                        shadowElevation = if (canSubmit) 4.dp else 0.dp,
                        modifier = Modifier
                            .testTag("header_save_task_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = if (canSubmit) Color.White else AppleTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Créer",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (canSubmit) Color.White else AppleTextTertiary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Floating Text Field: Title
                    IosFloatingTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Titre de la session",
                        placeholder = "Ex : Implémenter les patterns Observer & Strategy",
                        leadingIcon = Icons.Rounded.Title,
                        singleLine = true,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.testTag("new_task_title_input")
                    )

                    // Floating Text Field: Notes
                    IosFloatingTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes, chapitre ou lien TP (optionnel)",
                        placeholder = "Notions clés, exercices à revoir...",
                        leadingIcon = Icons.Rounded.EditNote,
                        singleLine = false,
                        maxLines = 3,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() },
                        modifier = Modifier.testTag("new_task_notes_input")
                    )

                    // Category Selector Card (Distinct Color Tags)
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Catégorie",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedCategory.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = selectedCategory.accentColor()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskCategory.entries.forEach { category ->
                                val isSelected = selectedCategory == category
                                val catColor = category.accentColor()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = category.icon(),
                                            contentDescription = null,
                                            tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = category.label,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = catColor.copy(alpha = 0.15f),
                                        selectedLabelColor = catColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = catColor
                                    ),
                                    modifier = Modifier.testTag("category_chip_${category.key}")
                                )
                            }
                        }
                    }

                    if (projects.isNotEmpty()) {
                        AppleCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, contentPadding = PaddingValues(16.dp)) {
                            Text("Projet associé", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("Facultatif · le projet apparaîtra comme groupe dans le Gantt.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedProjectId.isBlank(),
                                    onClick = { selectedProjectId = "" },
                                    label = { Text("Aucun") },
                                    modifier = Modifier.testTag("task_project_none")
                                )
                                projects.forEach { project ->
                                    FilterChip(
                                        selected = selectedProjectId == project.id,
                                        onClick = { selectedProjectId = project.id },
                                        label = { Text(project.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        modifier = Modifier.testTag("task_project_${project.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Date & Time Picker Card (iOS Grouped Inset Style)
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "Date & créneau",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick iOS Date Pills
                        val today = LocalDate.now()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Aujourd'hui" to today,
                                "Demain" to today.plusDays(1),
                                "Dans 3 jours" to today.plusDays(3),
                                "Semaine pro." to today.plusDays(7)
                            ).forEach { (label, date) ->
                                val selected = selectedDate == date
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedDate = date
                                        if (selectedStartDate > date) selectedStartDate = date
                                    },
                                    label = { Text(label) },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    isPickingStartDate = true
                                    showDatePicker = true
                                }
                                .padding(vertical = 9.dp, horizontal = 4.dp)
                                .testTag("open_start_date_picker_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Début prévu", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formattedStartDateLabel, style = MaterialTheme.typography.titleSmall, color = gradient.accentColor)
                        }

                        // Deadline date picker row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    isPickingStartDate = false
                                    showDatePicker = true
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("open_date_picker_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(gradient.softContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = gradient.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Échéance choisie",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formattedDateLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Calendrier",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = gradient.accentColor
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = "Ouvrir le sélecteur de date",
                                    tint = gradient.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Interactive Time Picker Row + Quick Time Slots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showTimePicker = true }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("open_time_picker_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(gradient.softContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccessTime,
                                        contentDescription = null,
                                        tint = gradient.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Heure prévue",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedTime,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ajuster",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = gradient.accentColor
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = "Ouvrir le sélecteur d'heure",
                                    tint = gradient.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("08:30", "10:00", "14:00", "16:30", "19:00").forEach { slot ->
                                val selected = selectedTime == slot
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedTime = slot },
                                    label = { Text(slot) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = gradient.softContainer,
                                        selectedLabelColor = gradient.accentColor
                                    )
                                )
                            }
                        }
                    }

                    // Course Module Selector
                    if (courses.isNotEmpty()) {
                        AppleCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 22.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(
                                text = adapt.taskModuleLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedCourse != null) {
                                Text(
                                    text = selectedCourse?.title ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = gradient.accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                courses.forEach { course ->
                                    val selected = selectedCourse?.id == course.id
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            selectedCourse = if (selected) null else course
                                        },
                                        label = { Text(course.code) },
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
                        }
                    }

                    // Priority & Duration Card
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "Priorité",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // iOS Segmented Control
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    Triple("high", "Haute", AppleRose),
                                    Triple("medium", "Moyenne", AppleAmber),
                                    Triple("low", "Douce", AppleEmerald)
                                ).forEach { (key, label, dotColor) ->
                                    val isSelected = selectedPriority == key
                                    Surface(
                                        onClick = { selectedPriority = key },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Durée estimée",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Focus Pomodoro",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(25L, 45L, 60L, 90L).forEach { mins ->
                                val selected = selectedDuration == mins
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedDuration = mins },
                                    label = { Text("${mins}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = gradient.softContainer,
                                        selectedLabelColor = gradient.accentColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Matrice d'Eisenhower",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (selectedQuadrant) {
                                    1L -> "I · Urgent & imp."
                                    2L -> "II · Important"
                                    3L -> "III · Urgent"
                                    4L -> "IV · Optionnel"
                                    else -> "Classement auto"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = gradient.accentColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                null to "Auto",
                                1L to "I · Urgent & imp.",
                                2L to "II · Important",
                                3L to "III · Urgent",
                                4L to "IV · Plus tard"
                            ).forEach { (qVal, label) ->
                                val isSel = selectedQuadrant == qVal
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedQuadrant = qVal },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = gradient.softContainer,
                                        selectedLabelColor = gradient.accentColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSel,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = gradient.accentColor
                                    ),
                                    modifier = Modifier.testTag("quadrant_chip_${qVal ?: 0}")
                                )
                            }
                        }
                    }
                }

                // Bottom Floating Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = { submitTask() },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradient.accentColor,
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.65f),
                            disabledContentColor = AppleTextTertiary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_new_task_button")
                    ) {
                        Text(
                            text = "Enregistrer dans le planning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerDate = if (isPickingStartDate) selectedStartDate else selectedDate
        val initialMillis = remember(pickerDate) {
            pickerDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (isPickingStartDate) {
                                selectedStartDate = date
                                if (selectedDate < date) selectedDate = date
                            } else {
                                selectedDate = date
                                if (selectedStartDate > date) selectedStartDate = date
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Valider", color = gradient.accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = gradient.accentColor,
                    todayDateBorderColor = gradient.accentColor,
                    todayContentColor = gradient.accentColor
                )
            )
        }
    }

    if (showTimePicker) {
        val parts = selectedTime.split(":")
        val initHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 10
        val initMin = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMin,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Choisir l'heure", style = MaterialTheme.typography.titleLarge) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = gradient.accentColor,
                            timeSelectorSelectedContainerColor = gradient.softContainer,
                            timeSelectorSelectedContentColor = gradient.accentColor
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hh = timePickerState.hour.toString().padStart(2, '0')
                        val mm = timePickerState.minute.toString().padStart(2, '0')
                        selectedTime = "$hh:$mm"
                        showTimePicker = false
                    }
                ) {
                    Text("Valider", color = gradient.accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun IosFloatingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {}
) {
    val gradient = LocalLightGradient.current
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "floatingFieldBorder"
    )
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 7.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "floatingFieldElevation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = elevation,
        border = BorderStroke(if (isFocused) 1.5.dp else 0.9.dp, borderColor)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = AppleTextTertiary) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isFocused) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (value.isNotEmpty() && isFocused) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Effacer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedLabelColor = gradient.accentColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = gradient.accentColor
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .onFocusChanged { isFocused = it.isFocused }
        )
    }
}
