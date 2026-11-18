package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FocusProfile
import com.example.data.model.FocusSession
import com.example.data.model.StudyTask
import com.example.ui.FocusTimerState
import com.example.ui.components.AppleCard
import com.example.ui.components.CatalogColorPicker
import com.example.ui.components.CatalogIcon
import com.example.ui.components.CatalogIconPickerSheet
import com.example.ui.components.catalogIconColor
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalLightGradient

@Composable
fun FocusProfilesScreen(
    profiles: List<FocusProfile>,
    tasks: List<StudyTask>,
    sessions: List<FocusSession>,
    focusState: FocusTimerState,
    initialSelectedProfileId: String = "",
    externalCreateTrigger: Int = 0,
    onCreateProfile: (title: String, learningTopic: String, iconKey: String, colorIndex: Long, defaultMinutes: Long) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onAssignTaskToProfile: (taskId: String, profile: FocusProfile?) -> Unit,
    onToggleTaskCompleted: (StudyTask) -> Unit,
    onStartFocusForTaskInProfile: (FocusProfile, StudyTask) -> Unit,
    onToggleTimerPlayPause: () -> Unit,
    onOpenTimerScreen: () -> Unit,
    onOpenCreateTaskForProfile: (FocusProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProfileId by rememberSaveable(initialSelectedProfileId) {
        mutableStateOf(initialSelectedProfileId)
    }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(externalCreateTrigger) {
        if (externalCreateTrigger > 0) {
            showCreateDialog = true
        }
    }

    val selectedProfile = remember(profiles, selectedProfileId) {
        profiles.firstOrNull { it.id == selectedProfileId }
    }

    if (selectedProfile != null) {
        BackHandler {
            selectedProfileId = ""
        }
        FocusProfileDetailView(
            profile = selectedProfile,
            allTasks = tasks,
            sessions = sessions,
            focusState = focusState,
            onBack = { selectedProfileId = "" },
            onDeleteProfile = {
                onDeleteProfile(selectedProfile.id)
                selectedProfileId = ""
            },
            onAssignTask = { task ->
                onAssignTaskToProfile(task.id, selectedProfile)
            },
            onUnassignTask = { task ->
                onAssignTaskToProfile(task.id, null)
            },
            onToggleTaskCompleted = onToggleTaskCompleted,
            onStartFocusForTask = { task ->
                onStartFocusForTaskInProfile(selectedProfile, task)
            },
            onToggleTimerPlayPause = onToggleTimerPlayPause,
            onOpenTimerScreen = onOpenTimerScreen,
            onOpenCreateTask = { onOpenCreateTaskForProfile(selectedProfile) },
            modifier = modifier
        )
    } else {
        FocusProfilesDirectoryView(
            profiles = profiles,
            tasks = tasks,
            focusState = focusState,
            onOpenProfile = { selectedProfileId = it.id },
            onOpenCreateDialog = { showCreateDialog = true },
            onOpenTimerScreen = onOpenTimerScreen,
            modifier = modifier
        )
    }

    if (showCreateDialog) {
        CreateFocusProfileDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, topic, iconKey, colorIndex, defaultMinutes ->
                onCreateProfile(title, topic, iconKey, colorIndex, defaultMinutes)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun FocusProfilesDirectoryView(
    profiles: List<FocusProfile>,
    tasks: List<StudyTask>,
    focusState: FocusTimerState,
    onOpenProfile: (FocusProfile) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenTimerScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val activeProfile = profiles.firstOrNull { it.id == focusState.selectedFocusProfileId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_profiles_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Profils de Focus",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Créez des espaces dédiés pour apprendre ce que vous voulez et suivez vos tâches en focus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onOpenCreateDialog,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gradient.accentColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("create_focus_profile_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Créer", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (activeProfile != null) {
            item {
                val accent = catalogIconColor(activeProfile.colorIndex)
                AppleCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_focus_profile_summary_card"),
                    cornerRadius = 22.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CatalogIcon(
                                iconKey = activeProfile.iconKey,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (focusState.isRunning) "SESSION EN COURS · ${activeProfile.title}" else "PROFIL SÉLECTIONNÉ · ${activeProfile.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "En focus sur : ${focusState.selectedTaskTitle}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            onClick = { onOpenProfile(activeProfile) },
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Ouvrir",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        if (profiles.isEmpty()) {
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(gradient.softContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "Aucun profil de focus pour le moment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Créez un profil pour n'importe quel sujet (ex: Japonais, Rust, Piano, Architecture) et assignez-y vos tâches.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenCreateDialog,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Créer mon premier profil")
                        }
                    }
                }
            }
        } else {
            items(profiles, key = { it.id }) { profile ->
                val accent = catalogIconColor(profile.colorIndex)
                val assignedTasks = tasks.filter { it.focusProfileId == profile.id }
                val isProfileFocusedNow = focusState.selectedFocusProfileId == profile.id

                Surface(
                    onClick = { onOpenProfile(profile) },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(
                        if (isProfileFocusedNow) 1.6.dp else 0.9.dp,
                        if (isProfileFocusedNow) accent else MaterialTheme.colorScheme.outlineVariant
                    ),
                    shadowElevation = if (isProfileFocusedNow) 5.dp else 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_profile_card_${profile.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CatalogIcon(
                                    iconKey = profile.iconKey,
                                    contentDescription = profile.title,
                                    tint = accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = profile.learningTopic.ifBlank { "Apprentissage libre" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "Ouvrir ${profile.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isProfileFocusedNow) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = accent.copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, accent.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (focusState.isRunning) {
                                            "En focus : ${focusState.selectedTaskTitle}"
                                        } else {
                                            "Ciblé pour focus : ${focusState.selectedTaskTitle}"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatChip(
                                text = "${assignedTasks.count { !it.completed }} tâche(s) à faire",
                                accent = accent
                            )
                            ProfileStatChip(
                                text = "${profile.defaultMinutes} min / bloc",
                                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                                neutral = true
                            )
                            ProfileStatChip(
                                text = "${profile.totalFocusMinutes} min cumulées",
                                accent = AppleEmerald,
                                neutral = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusProfileDetailView(
    profile: FocusProfile,
    allTasks: List<StudyTask>,
    sessions: List<FocusSession>,
    focusState: FocusTimerState,
    onBack: () -> Unit,
    onDeleteProfile: () -> Unit,
    onAssignTask: (StudyTask) -> Unit,
    onUnassignTask: (StudyTask) -> Unit,
    onToggleTaskCompleted: (StudyTask) -> Unit,
    onStartFocusForTask: (StudyTask) -> Unit,
    onToggleTimerPlayPause: () -> Unit,
    onOpenTimerScreen: () -> Unit,
    onOpenCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = catalogIconColor(profile.colorIndex)
    val assignedTasks = allTasks.filter { it.focusProfileId == profile.id }
    val assignableTasks = allTasks.filter { it.focusProfileId != profile.id && !it.completed }
    val profileSessions = sessions.filter { it.focusProfileId == profile.id }
    val isThisProfileInFocus = focusState.selectedFocusProfileId == profile.id

    var showAssignDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_profile_detail_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Detail Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("focus_profile_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Retour aux profils de focus",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profil de focus",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accent
                        )
                        Text(
                            text = profile.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.testTag("focus_profile_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Supprimer le profil",
                        tint = AppleRose
                    )
                }
            }
        }

        // Profile Hero Card with Learning Goal
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accent.copy(alpha = 0.14f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CatalogIcon(
                                iconKey = profile.iconKey,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Objectif d'apprentissage",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.learningTopic.ifBlank { "Apprendre ce que vous voulez à votre rythme" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileStatChip(text = "${profile.defaultMinutes} min / session", accent = accent)
                        ProfileStatChip(text = "${profile.totalFocusMinutes} min au total", accent = AppleEmerald)
                        ProfileStatChip(text = "${assignedTasks.size} tâche(s)", accent = MaterialTheme.colorScheme.onSurfaceVariant, neutral = true)
                    }
                }
            }
        }

        // Live Focus Banner explicitly showing WHICH assigned task the user is focusing on right now
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isThisProfileInFocus) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (isThisProfileInFocus) 1.5.dp else 0.9.dp,
                    if (isThisProfileInFocus) accent else MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = if (isThisProfileInFocus) 4.dp else 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_profile_live_status_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isThisProfileInFocus) {
                        val clockSecs = if (focusState.elapsedSeconds > 0 && focusState.remainingSeconds == 0) {
                            focusState.elapsedSeconds
                        } else {
                            focusState.remainingSeconds
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (focusState.isRunning) accent else accent.copy(alpha = 0.20f)
                            ) {
                                Text(
                                    text = if (focusState.isRunning) "EN FOCUS ACTUELLEMENT" else "FOCUS EN PAUSE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (focusState.isRunning) Color.White else accent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = formatProfileClock(clockSecs),
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        }

                        Text(
                            text = "Vous vous êtes mis en focus pour : ${focusState.selectedTaskTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("focus_profile_current_task_text")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onToggleTimerPlayPause,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (focusState.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (focusState.isRunning) "Pause" else "Reprendre")
                            }
                            OutlinedButton(
                                onClick = onOpenTimerScreen,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, accent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Minuteur", color = accent)
                            }
                        }
                    } else {
                        val lastSession = profileSessions.firstOrNull()
                        Text(
                            text = if (lastSession != null) {
                                "Dernier focus sur ce profil : ${lastSession.taskTitle}"
                            } else {
                                "Aucune tâche en focus actuellement"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Appuyez sur « Mettre en focus » sur une tâche ci-dessous pour démarrer une session dédiée et afficher votre progression ici.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Assigned Tasks Section Header + Assign / Create Task CTAs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tâches assignées (${assignedTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showAssignDialog = true },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("assign_task_to_profile_button")
                    ) {
                        Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assigner", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onOpenCreateTask,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("create_task_for_profile_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nouvelle", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (assignedTasks.isEmpty()) {
            item {
                AppleCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                    Text(
                        text = "Aucune tâche n'est encore assignée à ce profil. Utilisez « Assigner » pour lier une tâche existante ou « Nouvelle » pour en créer une.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(assignedTasks, key = { it.id }) { task ->
                val isTaskCurrentlyFocused = isThisProfileInFocus &&
                    (focusState.selectedTaskId == task.id || focusState.selectedTaskTitle == task.title)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isTaskCurrentlyFocused) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (isTaskCurrentlyFocused) 1.4.dp else 0.8.dp,
                        if (isTaskCurrentlyFocused) accent else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_assigned_task_${task.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { onToggleTaskCompleted(task) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = if (task.completed) "Marquer comme non terminée" else "Marquer comme terminée",
                                    tint = if (task.completed) AppleEmerald else accent
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${task.dueDateIso} · ${task.estimatedMinutes} min" +
                                        if (task.courseTitle.isNotBlank()) " · ${task.courseTitle}" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onUnassignTask(task) },
                                modifier = Modifier.testTag("unassign_task_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LinkOff,
                                    contentDescription = "Retirer du profil",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isTaskCurrentlyFocused) {
                                Surface(
                                    shape = CircleShape,
                                    color = accent.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = if (focusState.isRunning) "En focus" else "Sélectionnée",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = { onStartFocusForTask(task) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("focus_profile_start_task_${task.id}")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTaskCurrentlyFocused && focusState.isRunning) "Relancer Focus" else "Mettre en focus",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sessions logged under this Focus Profile
        if (profileSessions.isNotEmpty()) {
            item {
                Text(
                    text = "Sessions réalisées (${profileSessions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(profileSessions.take(8), key = { it.id }) { session ->
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Focus : ${session.taskTitle.ifBlank { profile.title }}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = session.dateIso,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f)) {
                            Text(
                                text = "${session.durationMinutes} min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAssignDialog) {
        AssignTaskToProfileDialog(
            profile = profile,
            availableTasks = assignableTasks,
            onAssign = { task ->
                onAssignTask(task)
                showAssignDialog = false
            },
            onDismiss = { showAssignDialog = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer ce profil ?") },
            text = {
                Text("Les tâches assignées à « ${profile.title} » resteront conservées dans votre planning.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteProfile()
                    }
                ) {
                    Text("Supprimer", color = AppleRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignTaskToProfileDialog(
    profile: FocusProfile,
    availableTasks: List<StudyTask>,
    onAssign: (StudyTask) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = catalogIconColor(profile.colorIndex)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Assigner une tâche à « ${profile.title} »",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (availableTasks.isEmpty()) {
                Text(
                    text = "Toutes vos tâches actives sont déjà assignées ou terminées. Créez une nouvelle tâche dédiée !",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTasks.take(12).forEach { task ->
                        Surface(
                            onClick = { onAssign(task) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("assign_candidate_task_${task.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${task.dueDateIso} · ${task.estimatedMinutes} min",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Assigner ${task.title}",
                                    tint = accent
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFocusProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, learningTopic: String, iconKey: String, colorIndex: Long, defaultMinutes: Long) -> Unit
) {
    val gradient = LocalLightGradient.current
    var title by remember { mutableStateOf("") }
    var learningTopic by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf("brain") }
    var colorIndex by remember { mutableLongStateOf(0L) }
    var defaultMinutes by remember { mutableLongStateOf(25L) }
    var showIconSheet by remember { mutableStateOf(false) }

    val selectedColor = catalogIconColor(colorIndex)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nouveau profil de Focus",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nom du profil") },
                placeholder = { Text("Ex : Apprendre Rust, Japonais, Guitare...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_profile_title_input")
            )

            OutlinedTextField(
                value = learningTopic,
                onValueChange = { learningTopic = it },
                label = { Text("Ce que je veux apprendre") },
                placeholder = { Text("Ex : Maîtriser les coroutines et Flow") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_profile_topic_input")
            )

            Text(
                text = "Durée par défaut",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15L, 25L, 45L, 60L, 90L).forEach { mins ->
                    val selected = defaultMinutes == mins
                    FilterChip(
                        selected = selected,
                        onClick = { defaultMinutes = mins },
                        label = { Text("${mins}m") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedColor.copy(alpha = 0.16f),
                            selectedLabelColor = selectedColor
                        )
                    )
                }
            }

            Text(
                text = "Couleur & icône",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            CatalogColorPicker(
                selectedIndex = colorIndex,
                onColorSelected = { colorIndex = it },
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                onClick = { showIconSheet = true },
                shape = RoundedCornerShape(14.dp),
                color = selectedColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, selectedColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CatalogIcon(
                        iconKey = iconKey,
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Changer l'icône du profil",
                        style = MaterialTheme.typography.labelLarge,
                        color = selectedColor,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = selectedColor
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(title, learningTopic, iconKey, colorIndex, defaultMinutes)
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor),
                    modifier = Modifier.testTag("confirm_create_focus_profile_button")
                ) {
                    Text("Créer le profil")
                }
            }
        }
    }

    if (showIconSheet) {
        CatalogIconPickerSheet(
            selectedIconKey = iconKey,
            selectedColor = selectedColor,
            onIconSelected = { iconKey = it },
            onDismiss = { showIconSheet = false }
        )
    }
}

@Composable
private fun ProfileStatChip(
    text: String,
    accent: Color,
    neutral: Boolean = false
) {
    Surface(
        shape = CircleShape,
        color = if (neutral) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f) else accent.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

private fun formatProfileClock(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hrs = safe / 3600
    val mins = (safe % 3600) / 60
    val secs = safe % 60
    return if (hrs > 0) {
        "${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    } else {
        "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    }
}
