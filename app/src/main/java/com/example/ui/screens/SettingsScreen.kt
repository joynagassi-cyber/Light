package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Course
import com.example.data.model.NotificationFrequency
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptation
import com.example.data.preferences.StudyAppSettings
import com.example.notifications.LightNotificationHelper
import com.example.ui.components.AppleCard
import com.example.ui.components.CatalogColorPicker
import com.example.ui.components.CatalogIcon
import com.example.ui.components.CatalogIconPickerSheet
import com.example.ui.components.catalogIconColor
import com.example.ui.components.CalendarViewMode
import com.example.ui.components.GradientSwatchCard
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.LightColorThemes
import com.example.ui.theme.LightGradients
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import com.example.ui.theme.appVerticalBrush
import java.time.LocalDate

@Composable
fun SettingsScreen(
    profile: UserProfile?,
    photoUrl: String?,
    selectedGradientId: String,
    appSettings: StudyAppSettings,
    courses: List<Course>,
    onSelectGradient: (String) -> Unit,
    onUpdateProfile: (String, String, Long, String?, Long?, Long?, String?, String?) -> Unit,
    onUpdateNotifications: (Boolean, NotificationFrequency) -> Unit = { _, _ -> },
    onReopenOnboarding: () -> Unit = {},
    onUpdateTimerDurations: (Int, Int, Int) -> Unit,
    onUpdateDefaultCalendarView: (String) -> Unit,
    onUpdateHapticsEnabled: (Boolean) -> Unit,
    onUpdateAutoLogStopwatch: (Boolean) -> Unit,
    onUpdateAutoPlayAudioWithFocus: (Boolean) -> Unit,
    onSelectTheme: (String) -> Unit = {},
    onSelectAppearanceMode: (String) -> Unit = {},
    onSetGlassEnabled: (Boolean) -> Unit = {},
    onCreateCourse: (String, String, String, Long, Int, Double, String) -> Unit,
    onDeleteCourse: (String) -> Unit,
    isGoogleAuthPaused: Boolean = false,
    onResumeGoogleSignIn: () -> Unit = {},
    onPauseGoogleSignIn: () -> Unit = {},
    onSignOut: () -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradient = LocalLightGradient.current
    val adapt = remember(profile) { profile.adaptation() }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val currentFreq = NotificationFrequency.fromKey(profile?.notificationFrequency ?: appSettings.notificationFrequency)
        val nextFreq = if (currentFreq == NotificationFrequency.OFF) NotificationFrequency.DAILY else currentFreq
        onUpdateNotifications(granted, if (granted) nextFreq else NotificationFrequency.OFF)
    }
    val themeRows = remember { LightColorThemes.all.chunked(2) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onClose != null) {
                    Surface(
                        onClick = onClose,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_close_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Fermer les paramètres",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Paramètres",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Profil, minuteur, calendrier et thèmes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: Profil & Sync Cloud
        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(gradient.appBrush())
                            .padding(2.5.dp)
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
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.displayName?.ifBlank { adapt.roleLabel } ?: adapt.roleLabel,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = adapt.headerSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = gradient.accentColor
                        )
                        Text(
                            text = "${adapt.gender.label} · ${adapt.notificationSummary}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Modifier le profil",
                            tint = gradient.accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGoogleAuthPaused) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = "Mode Test · Google en pause",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    } else {
                        SyncStatusPill()
                    }
                    Text(
                        text = "Objectif : ${profile?.dailyGoalTasks ?: 5} tâches/jour",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = onReopenOnboarding,
                    shape = RoundedCornerShape(14.dp),
                    color = gradient.softContainer,
                    border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_reopen_onboarding_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = gradient.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reconfigurer l'onboarding interactif",
                            style = MaterialTheme.typography.labelLarge,
                            color = gradient.accentColor
                        )
                    }
                }
            }
        }

        // Notifications & Fréquence Card
        item {
            val notifEnabled = profile?.notificationsEnabled ?: appSettings.notificationsEnabled
            val currentFreq = NotificationFrequency.fromKey(profile?.notificationFrequency ?: appSettings.notificationFrequency)
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = gradient.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Notifications & fréquence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchRow(
                    title = "Rappels et alertes intelligentes",
                    subtitle = if (notifEnabled) "Cadence actuelle : ${currentFreq.label}" else "Rappels désactivés",
                    checked = notifEnabled && currentFreq != NotificationFrequency.OFF,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !LightNotificationHelper.hasNotificationPermission(context)
                        ) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val nextFreq = if (checked && currentFreq == NotificationFrequency.OFF) {
                                NotificationFrequency.DAILY
                            } else if (!checked) {
                                NotificationFrequency.OFF
                            } else {
                                currentFreq
                            }
                            onUpdateNotifications(checked, nextFreq)
                        }
                    },
                    testTag = "settings_notifications_switch"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotificationFrequency.entries.forEach { freq ->
                        val selected = if (!notifEnabled) freq == NotificationFrequency.OFF else currentFreq == freq
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val willEnable = freq != NotificationFrequency.OFF
                                if (willEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !LightNotificationHelper.hasNotificationPermission(context)
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onUpdateNotifications(willEnable, freq)
                                }
                            },
                            label = { Text(freq.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = gradient.accentColor
                            ),
                            modifier = Modifier.testTag("settings_notif_freq_${freq.key}")
                        )
                    }
                }
            }
        }

        // Section 2: Focus Timer & Chronomètre Settings
        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Minuteur & Chronomètre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                DurationStepperRow(
                    label = "Pomodoro standard",
                    minutes = appSettings.pomodoroMinutes,
                    onDecrease = {
                        onUpdateTimerDurations(
                            (appSettings.pomodoroMinutes - 5).coerceAtLeast(10),
                            appSettings.deepCodeMinutes,
                            appSettings.shortBreakMinutes
                        )
                    },
                    onIncrease = {
                        onUpdateTimerDurations(
                            (appSettings.pomodoroMinutes + 5).coerceAtMost(90),
                            appSettings.deepCodeMinutes,
                            appSettings.shortBreakMinutes
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                DurationStepperRow(
                    label = "Deep Code",
                    minutes = appSettings.deepCodeMinutes,
                    onDecrease = {
                        onUpdateTimerDurations(
                            appSettings.pomodoroMinutes,
                            (appSettings.deepCodeMinutes - 5).coerceAtLeast(20),
                            appSettings.shortBreakMinutes
                        )
                    },
                    onIncrease = {
                        onUpdateTimerDurations(
                            appSettings.pomodoroMinutes,
                            (appSettings.deepCodeMinutes + 5).coerceAtMost(120),
                            appSettings.shortBreakMinutes
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                DurationStepperRow(
                    label = "Pause courte",
                    minutes = appSettings.shortBreakMinutes,
                    onDecrease = {
                        onUpdateTimerDurations(
                            appSettings.pomodoroMinutes,
                            appSettings.deepCodeMinutes,
                            (appSettings.shortBreakMinutes - 1).coerceAtLeast(3)
                        )
                    },
                    onIncrease = {
                        onUpdateTimerDurations(
                            appSettings.pomodoroMinutes,
                            appSettings.deepCodeMinutes,
                            (appSettings.shortBreakMinutes + 1).coerceAtMost(30)
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                SettingsSwitchRow(
                    title = "Enregistrer le chronomètre au bilan",
                    subtitle = "Synchronise les sessions chrono dans votre série",
                    checked = appSettings.autoLogStopwatch,
                    onCheckedChange = onUpdateAutoLogStopwatch,
                    testTag = "settings_stopwatch_autolog_switch"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                SettingsSwitchRow(
                    title = "Ambiance audio auto avec Focus",
                    subtitle = "Démarre Lo-Fi / Bruit blanc avec le minuteur",
                    checked = appSettings.autoPlayAudioWithFocus,
                    onCheckedChange = onUpdateAutoPlayAudioWithFocus,
                    testTag = "settings_audio_autoplay_switch"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                SettingsSwitchRow(
                    title = "Signaux tactiles & transitions",
                    subtitle = "Retour doux lors des validations",
                    checked = appSettings.hapticsEnabled,
                    onCheckedChange = onUpdateHapticsEnabled,
                    testTag = "settings_haptics_switch"
                )
            }
        }

        // Section 3: Vue Calendrier par défaut
        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Vue Calendrier par défaut",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Affichée à l'ouverture de l'onglet Calendrier",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalendarViewMode.entries.forEach { mode ->
                        val selected = appSettings.defaultCalendarView == mode.key
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateDefaultCalendarView(mode.key) },
                            label = { Text(mode.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = gradient.accentColor
                            ),
                            modifier = Modifier.testTag("settings_default_view_${mode.key}")
                        )
                    }
                }
            }
        }

        // Section 4: Apparence (curated palettes and display modes)
        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Apparence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choisissez un rendu lisible, adapté à votre écran et à votre façon de travailler.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppearanceMode.entries.forEach { mode ->
                        val selected = appSettings.appearanceMode == mode.key
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectAppearanceMode(mode.key) },
                            label = { Text(mode.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor
                            ),
                            modifier = Modifier.testTag("appearance_mode_${mode.key}")
                        )
                    }
                }
                Text(
                    text = AppearanceMode.fromKey(appSettings.appearanceMode).description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Surfaces en verre", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Transparence et contours lumineux, combinables avec le mode clair ou OLED.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = appSettings.glassEnabled,
                        onCheckedChange = onSetGlassEnabled,
                        modifier = Modifier.testTag("glass_style_toggle"),
                        colors = SwitchDefaults.colors(checkedTrackColor = gradient.accentColor)
                    )
                }
            }
        }
        item {
            Column {
                Text(
                    text = "10 palettes classiques",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Des couleurs unies, distinctes des dégradés et conçues pour plusieurs styles de travail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(themeRows) { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { preset ->
                    val selected = appSettings.selectedThemeId == preset.id
                    Surface(
                        onClick = { onSelectTheme(preset.id) },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (selected) 2.dp else 1.dp,
                            if (selected) preset.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("theme_preset_${preset.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = preset.primary,
                                modifier = Modifier.size(22.dp)
                            ) {}
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text(preset.audience, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Section 5: Gradient studio
        item {
            Column {
                Text(
                    text = "Studio dégradés · ${LightGradients.all.size} palettes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Un style coloré, avec dégradés animés et ambiance lumineuse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val chunkedGradients = LightGradients.all.chunked(2)
        items(chunkedGradients) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { preset ->
                    GradientSwatchCard(
                        preset = preset,
                        isSelected = appSettings.selectedThemeId == "gradient" && preset.id == selectedGradientId,
                        onClick = {
                            onSelectTheme("gradient")
                            onSelectGradient(preset.id)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Section 5: Matières L3
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
                TextButton(onClick = { showAddCourseDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter")
                }
            }
        }

        items(courses, key = { it.id }) { course ->
            var courseMenuExpanded by remember(course.id) { mutableStateOf(false) }
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = catalogIconColor(course.colorIndex).copy(alpha = 0.14f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CatalogIcon(
                                    iconKey = course.iconKey,
                                    contentDescription = null,
                                    tint = catalogIconColor(course.colorIndex),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${course.code} · ${course.title}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = course.professor.ifBlank { "Département Génie Logiciel" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Box {
                        IconButton(
                            onClick = { courseMenuExpanded = true },
                            modifier = Modifier.size(44.dp).testTag("course_actions_${course.id}")
                        ) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Actions pour ${course.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = courseMenuExpanded, onDismissRequest = { courseMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Supprimer la matière") },
                                leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null) },
                                onClick = { courseMenuExpanded = false; onDeleteCourse(course.id) }
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Compte Google / Mode Test
        item {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isGoogleAuthPaused) "Connexion Google en pause" else "Compte Google & Sync Cloud",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGoogleAuthPaused) {
                        "Vous testez actuellement l'application en local sans compte Google. Activez Google Sign-In à tout moment pour synchroniser vos données dans le cloud."
                    } else {
                        "Votre compte Google est actif. Vous pouvez mettre la connexion Google en pause pour effectuer des tests locaux."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (isGoogleAuthPaused) {
                    Button(
                        onClick = onResumeGoogleSignIn,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradient.accentColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("resume_google_signin_button")
                    ) {
                        Text("Activer la connexion Google Cloud")
                    }
                } else {
                    OutlinedButton(
                        onClick = onPauseGoogleSignIn,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pause_google_signin_button")
                    ) {
                        Text("Mettre en pause Google (Mode Test)")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleRose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Se déconnecter de Google")
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        val currentYear = remember { LocalDate.now().year }
        var name by remember { mutableStateOf(profile?.displayName ?: "") }
        var gender by remember { mutableStateOf(UserGender.fromKey(profile?.gender)) }
        var birthYearText by remember { mutableStateOf((profile?.birthYear ?: (currentYear - 21L)).toString()) }
        var ageText by remember { mutableStateOf((profile?.age ?: 21L).toString()) }
        var profession by remember { mutableStateOf(UserProfession.fromKey(profile?.profession)) }
        var customProf by remember { mutableStateOf(profile?.customProfession ?: "") }
        var major by remember { mutableStateOf(profile?.major ?: profession.defaultFocusDomain()) }
        var goal by remember { mutableStateOf((profile?.dailyGoalTasks ?: 5L).toString()) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Modifier & adapter le profil") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Prénom / Nom") },
                        singleLine = true
                    )
                    Text("Genre", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserGender.entries.forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g.label) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { raw ->
                                val d = raw.filter { it.isDigit() }.take(2)
                                ageText = d
                                d.toIntOrNull()?.let { a ->
                                    if (a in 6..100) birthYearText = (currentYear - a).toString()
                                }
                            },
                            label = { Text("Âge") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = birthYearText,
                            onValueChange = { raw ->
                                val d = raw.filter { it.isDigit() }.take(4)
                                birthYearText = d
                                d.toIntOrNull()?.let { y ->
                                    if (y in 1930..2020) ageText = (currentYear - y).coerceIn(6, 100).toString()
                                }
                            },
                            label = { Text("Année naiss.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text("Profession", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserProfession.entries.forEach { p ->
                            FilterChip(
                                selected = profession == p,
                                onClick = { profession = p },
                                label = { Text(if (p == UserProfession.OTHER) "Autre" else p.labelFor(gender)) }
                            )
                        }
                    }
                    if (profession == UserProfession.OTHER) {
                        OutlinedTextField(
                            value = customProf,
                            onValueChange = { customProf = it },
                            label = { Text("Précisez la profession") },
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = major,
                        onValueChange = { major = it },
                        label = { Text("Filière / Domaine") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Objectif tâches / jour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAge = ageText.toLongOrNull()?.coerceIn(6L, 100L) ?: 21L
                        val parsedYear = birthYearText.toLongOrNull()?.coerceIn(1930L, 2020L) ?: (currentYear - parsedAge)
                        onUpdateProfile(
                            name.ifBlank { profession.labelFor(gender, customProf) },
                            major.ifBlank { profession.defaultFocusDomain() },
                            goal.toLongOrNull()?.coerceIn(1L, 50L) ?: 5L,
                            gender.key,
                            parsedYear,
                            parsedAge,
                            profession.key,
                            customProf
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showAddCourseDialog) {
        var code by remember { mutableStateOf("GL-307") }
        var title by remember { mutableStateOf("") }
        var professor by remember { mutableStateOf("") }
        var targetHours by remember { mutableStateOf("20") }
        var iconKey by remember { mutableStateOf("book") }
        var colorIndex by remember { mutableStateOf((courses.size % 7).toLong()) }
        var showIconPicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Nouvelle matière L3") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code (ex: GL-307)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Intitulé du module") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = professor,
                        onValueChange = { professor = it },
                        label = { Text("Professeur") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetHours,
                        onValueChange = { targetHours = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Objectif d'heures") },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = catalogIconColor(colorIndex).copy(alpha = 0.14f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CatalogIcon(iconKey, null, catalogIconColor(colorIndex), Modifier.size(22.dp))
                            }
                        }
                        OutlinedButton(onClick = { showIconPicker = true }) {
                            Text("Choisir une icône")
                        }
                    }
                    Text("Couleur", style = MaterialTheme.typography.labelMedium)
                    CatalogColorPicker(
                        selectedIndex = colorIndex,
                        onColorSelected = { colorIndex = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && code.isNotBlank()) {
                            onCreateCourse(
                                code,
                                title,
                                professor,
                                colorIndex,
                                targetHours.toIntOrNull() ?: 20,
                                15.0,
                                iconKey
                            )
                            showAddCourseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)
                ) {
                    Text("Ajouter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Annuler")
                }
            }
        )
        if (showIconPicker) {
            CatalogIconPickerSheet(
                selectedIconKey = iconKey,
                selectedColor = catalogIconColor(colorIndex),
                onIconSelected = { iconKey = it },
                onDismiss = { showIconPicker = false }
            )
        }
    }
}

@Composable
private fun DurationStepperRow(
    label: String,
    minutes: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val gradient = LocalLightGradient.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Diminuer $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = gradient.softContainer
            ) {
                Text(
                    text = "$minutes min",
                    style = MaterialTheme.typography.labelLarge,
                    color = gradient.accentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Augmenter $label",
                    tint = gradient.accentColor
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val gradient = LocalLightGradient.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = gradient.accentColor
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
