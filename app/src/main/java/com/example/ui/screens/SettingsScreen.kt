package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.audio.FocusAudioTrack
import com.example.data.model.Course
import com.example.data.model.NotificationFrequency
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptation
import com.example.data.preferences.StudyAppSettings
import com.example.notifications.LightNotificationHelper
import com.example.ui.FocusAudioUiState
import com.example.ui.components.CalendarViewMode
import com.example.ui.components.CatalogColorPicker
import com.example.ui.components.CatalogIcon
import com.example.ui.components.CatalogIconPickerSheet
import com.example.ui.components.FocusAudioCard
import com.example.ui.components.FocusVisualCustomizationPicker
import com.example.ui.components.GradientSwatchCard
import com.example.ui.components.catalogIconColor
import com.example.ui.components.focusIllustrationResource
import com.example.ui.components.focusIllustrationTitle
import com.example.ui.components.focusThemeFor
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.components.LightControl
import com.example.ui.components.LightPanel
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LightColorThemes
import com.example.ui.theme.LightGradients
import com.example.ui.theme.LightSpacing
import com.example.ui.theme.LightTouchTarget
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.LocalThemeSurfaceConfig
import com.example.ui.theme.appBrush
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
    audioState: FocusAudioUiState = FocusAudioUiState(),
    onSelectAudioTrack: (FocusAudioTrack) -> Unit = {},
    onToggleAudioPlayPause: () -> Unit = {},
    onAudioVolumeChange: (Float) -> Unit = {},
    onSelectFocusIllustration: (String) -> Unit = {},
    onSelectFocusTheme: (String) -> Unit = {},
    onSelectTheme: (String) -> Unit = {},
    onSelectAppearanceMode: (String) -> Unit = {},
    onSetGlassEnabled: (Boolean) -> Unit = {},
    onCreateCourse: (String, String, String, Long, Int, Double, String) -> Unit,
    onDeleteCourse: (String) -> Unit,
    isGoogleAuthPaused: Boolean = false,
    onResumeGoogleSignIn: () -> Unit = {},
    onPauseGoogleSignIn: () -> Unit = {},
    onSignOut: () -> Unit,
    onOpenNavigationLayout: (() -> Unit)? = null,
    onOpenCapabilities: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradient = LocalLightGradient.current
    val adapt = remember(profile) { profile.adaptation() }

    var showInlineProfile by rememberSaveable { mutableStateOf(false) }
    var showNotifFreqSheet by remember { mutableStateOf(false) }
    var showCalendarViewSheet by remember { mutableStateOf(false) }
    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showTimerDurationsSheet by remember { mutableStateOf(false) }
    var showCoursesSheet by remember { mutableStateOf(false) }
    var showAddCourseSheet by remember { mutableStateOf(false) }

    var focusUniverseExpanded by rememberSaveable { mutableStateOf(false) }
    var themesExpanded by rememberSaveable { mutableStateOf(false) }
    var gradientsExpanded by rememberSaveable { mutableStateOf(false) }

    val activeFocusTheme = remember(appSettings.focusThemeId) { focusThemeFor(appSettings.focusThemeId) }
    val activeColorTheme = remember(appSettings.selectedThemeId) {
        LightColorThemes.all.firstOrNull { it.id == appSettings.selectedThemeId }
    }
    val activeGradient = remember(selectedGradientId) { LightGradients.byId(selectedGradientId) }
    val themeRows = remember { LightColorThemes.all.chunked(2) }

    val notifEnabled = profile?.notificationsEnabled ?: appSettings.notificationsEnabled
    val currentFreq = NotificationFrequency.fromKey(profile?.notificationFrequency ?: appSettings.notificationFrequency)
    val notifIsActive = notifEnabled && currentFreq != NotificationFrequency.OFF
    val activeCalendarView = remember(appSettings.defaultCalendarView) {
        CalendarViewMode.fromKey(appSettings.defaultCalendarView)
    }
    val activeAppearanceMode = remember(appSettings.appearanceMode) {
        AppearanceMode.fromKey(appSettings.appearanceMode)
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val nextFreq = if (currentFreq == NotificationFrequency.OFF) NotificationFrequency.DAILY else currentFreq
        onUpdateNotifications(granted, if (granted) nextFreq else NotificationFrequency.OFF)
    }

    if (showInlineProfile) {
        BackHandler { showInlineProfile = false }
        WorkspaceProfilePage(profile, photoUrl, onUpdateProfile, { showInlineProfile = false }, modifier)
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_list"),
        contentPadding = PaddingValues(
            start = LightSpacing.two,
            end = LightSpacing.two,
            top = LightSpacing.oneAndHalf,
            bottom = 72.dp
        ),
        verticalArrangement = Arrangement.spacedBy(LightSpacing.two)
    ) {
        // Design-system aligned top bar (48dp back button + clear title)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LightSpacing.half, bottom = LightSpacing.half),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
            ) {
                if (onClose != null) {
                    LightControl(
                        onClick = onClose,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Fermer les paramètres",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Paramètres",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (onOpenNavigationLayout != null || onOpenCapabilities != null) {
            item {
                SettingsSectionBlock(title = "Navigation et agent") {
                    if (onOpenNavigationLayout != null) SettingsNavigationRow(
                        Icons.Outlined.Dashboard, "Barre de navigation", onClick = onOpenNavigationLayout,
                        testTag = "open_navigation_settings",
                    )
                    if (onOpenNavigationLayout != null && onOpenCapabilities != null) SettingsInsetDivider()
                    if (onOpenCapabilities != null) SettingsNavigationRow(
                        Icons.Outlined.Tune, "Capacités de l’agent", onClick = onOpenCapabilities,
                        testTag = "open_agent_capabilities_settings",
                    )
                }
            }
        }

        // Section 1 : Compte
        item {
            SettingsSectionBlock(title = "Compte") {
                // Profile Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (onOpenProfile != null) onOpenProfile() else showInlineProfile = true }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                        .testTag("settings_edit_profile_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${adapt.headerSubtitle} · ${profile?.dailyGoalTasks ?: 5}/jour",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                SettingsInsetDivider()

                SettingsNavigationRow(
                    icon = Icons.Outlined.AutoAwesome,
                    title = "Assistant d'onboarding",
                    valueText = "Adapter",
                    onClick = onReopenOnboarding,
                    testTag = "settings_reopen_onboarding_button"
                )
            }
        }

        // Section 2 : Général
        item {
            SettingsSectionBlock(title = "Général") {
                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    checked = notifIsActive,
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

                SettingsInsetDivider()

                SettingsNavigationRow(
                    icon = Icons.Outlined.Schedule,
                    title = "Fréquence des rappels",
                    valueText = currentFreq.label,
                    onClick = { showNotifFreqSheet = true },
                    testTag = "settings_notif_frequency_row"
                )

                SettingsInsetDivider()

                SettingsNavigationRow(
                    icon = Icons.Outlined.Tune,
                    title = "Apparence",
                    valueText = activeAppearanceMode.label,
                    onClick = { showAppearanceSheet = true },
                    testTag = "settings_appearance_mode_row"
                )

                SettingsInsetDivider()

                SettingsNavigationRow(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Vue Calendrier",
                    valueText = activeCalendarView.label,
                    onClick = { showCalendarViewSheet = true },
                    testTag = "settings_default_calendar_row"
                )
            }
        }

        // Section 3 : Focus & Minuteur
        item {
            SettingsSectionBlock(title = "Focus & Minuteur") {
                SettingsNavigationRow(
                    icon = Icons.Outlined.Timer,
                    title = "Durées du minuteur",
                    valueText = "${appSettings.pomodoroMinutes} · ${appSettings.deepCodeMinutes} · ${appSettings.shortBreakMinutes} min",
                    onClick = { showTimerDurationsSheet = true },
                    testTag = "settings_timer_durations_row"
                )

                SettingsInsetDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.AvTimer,
                    title = "Bilan chronomètre auto",
                    checked = appSettings.autoLogStopwatch,
                    onCheckedChange = onUpdateAutoLogStopwatch,
                    testTag = "settings_stopwatch_autolog_switch"
                )

                SettingsInsetDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.Headphones,
                    title = "Audio auto avec Focus",
                    checked = appSettings.autoPlayAudioWithFocus,
                    onCheckedChange = onUpdateAutoPlayAudioWithFocus,
                    testTag = "settings_audio_autoplay_switch"
                )

                SettingsInsetDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.Vibration,
                    title = "Retours haptiques",
                    checked = appSettings.hapticsEnabled,
                    onCheckedChange = onUpdateHapticsEnabled,
                    testTag = "settings_haptics_switch"
                )

                SettingsInsetDivider()

                // Collapsible Focus Universe & Audio inside the grouped Focus card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = LightTouchTarget.minimum)
                        .clickable { focusUniverseExpanded = !focusUniverseExpanded }
                        .padding(horizontal = LightSpacing.two, vertical = LightSpacing.oneAndHalf)
                        .testTag("settings_expand_focus_universe"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
                ) {
                    Image(
                        painter = painterResource(id = focusIllustrationResource(appSettings.focusIllustrationId)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(activeFocusTheme.containerColor)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Univers & audio Focus",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${focusIllustrationTitle(appSettings.focusIllustrationId)} · ${audioState.selectedTrack.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (focusUniverseExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (focusUniverseExpanded) "Masquer la personnalisation Focus" else "Personnaliser Focus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                AnimatedVisibility(
                    visible = focusUniverseExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FocusVisualCustomizationPicker(
                            selectedIllustrationId = appSettings.focusIllustrationId,
                            selectedThemeId = appSettings.focusThemeId,
                            onSelectIllustration = onSelectFocusIllustration,
                            onSelectTheme = onSelectFocusTheme,
                            focusTheme = activeFocusTheme
                        )
                        FocusAudioCard(
                            audioState = audioState,
                            onSelectTrack = onSelectAudioTrack,
                            onTogglePlayPause = onToggleAudioPlayPause,
                            onVolumeChange = onAudioVolumeChange,
                            onToggleAutoPlay = onUpdateAutoPlayAudioWithFocus,
                            accentColor = activeFocusTheme.accentColor,
                            softContainerColor = activeFocusTheme.containerColor
                        )
                    }
                }
            }
        }

        // Section 4 : Personnalisation
        item {
            SettingsSectionBlock(title = "Personnalisation") {
                SettingsToggleRow(
                    icon = Icons.Outlined.Layers,
                    title = "Surfaces en verre",
                    checked = appSettings.glassEnabled,
                    onCheckedChange = onSetGlassEnabled,
                    testTag = "glass_style_toggle"
                )

                SettingsInsetDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = LightTouchTarget.minimum)
                        .clickable { themesExpanded = !themesExpanded }
                        .padding(horizontal = LightSpacing.two, vertical = LightSpacing.oneAndHalf)
                        .testTag("settings_expand_themes"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
                ) {
                    SettingsLeadingIconBox(icon = Icons.Outlined.ColorLens)
                    Text(
                        text = "Palettes classiques",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    ) {
                        Text(
                            text = activeColorTheme?.name ?: "Dégradé",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = if (themesExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (themesExpanded) "Masquer les palettes" else "Afficher les palettes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                AnimatedVisibility(
                    visible = themesExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        themeRows.forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                pair.forEach { preset ->
                                    val selected = appSettings.selectedThemeId == preset.id
                                    Surface(
                                        onClick = { onSelectTheme(preset.id) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (selected) 2.dp else 0.8.dp,
                                            if (selected) preset.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("theme_preset_${preset.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = preset.primary,
                                                modifier = Modifier.size(18.dp)
                                            ) {}
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(preset.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                                Text(preset.audience, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                SettingsInsetDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = LightTouchTarget.minimum)
                        .clickable { gradientsExpanded = !gradientsExpanded }
                        .padding(horizontal = LightSpacing.two, vertical = LightSpacing.oneAndHalf)
                        .testTag("settings_expand_gradients"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
                ) {
                    SettingsLeadingIconBox(icon = Icons.Outlined.Palette)
                    Text(
                        text = "Studio dégradés",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    ) {
                        Text(
                            text = if (appSettings.selectedThemeId == "gradient") activeGradient.name else "${LightGradients.all.size} palettes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = if (gradientsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (gradientsExpanded) "Masquer les dégradés" else "Afficher les dégradés",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                AnimatedVisibility(
                    visible = gradientsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LightGradients.all.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Section 5 : Matières & Cloud
        item {
            SettingsSectionBlock(title = "Matières & Cloud") {
                SettingsNavigationRow(
                    icon = Icons.Outlined.Book,
                    title = adapt.workspaceSectionTitle,
                    valueText = "${courses.size} actif(s)",
                    onClick = { showCoursesSheet = true },
                    testTag = "settings_courses_row"
                )

                SettingsInsetDivider()

                if (isGoogleAuthPaused) {
                    SettingsNavigationRow(
                        icon = Icons.Outlined.CloudOff,
                        title = "Connexion Google Cloud",
                        valueText = "Mode Test actif",
                        onClick = onResumeGoogleSignIn,
                        testTag = "resume_google_signin_button"
                    )
                } else {
                    SettingsNavigationRow(
                        icon = Icons.Outlined.CloudDone,
                        title = "Passer en Mode Test",
                        valueText = "Local",
                        onClick = onPauseGoogleSignIn,
                        testTag = "pause_google_signin_button"
                    )

                    SettingsInsetDivider()

                    SettingsNavigationRow(
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        title = "Déconnexion",
                        titleColor = AppleRose,
                        iconTint = AppleRose,
                        onClick = onSignOut,
                        testTag = "sign_out_button"
                    )
                }
            }
        }
    }

    // Bottom Sheet: Notification Frequency
    if (showNotifFreqSheet) {
        ModalBottomSheet(onDismissRequest = { showNotifFreqSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Fréquence des rappels",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                NotificationFrequency.entries.forEach { freq ->
                    val selected = if (!notifEnabled) freq == NotificationFrequency.OFF else currentFreq == freq
                    SettingsSheetOptionRow(
                        label = freq.label,
                        selected = selected,
                        accentColor = gradient.accentColor,
                        onClick = {
                            val willEnable = freq != NotificationFrequency.OFF
                            if (willEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !LightNotificationHelper.hasNotificationPermission(context)
                            ) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onUpdateNotifications(willEnable, freq)
                            }
                            showNotifFreqSheet = false
                        },
                        testTag = "settings_notif_freq_${freq.key}"
                    )
                }
            }
        }
    }

    // Bottom Sheet: Default Calendar View
    if (showCalendarViewSheet) {
        ModalBottomSheet(onDismissRequest = { showCalendarViewSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Vue Calendrier par défaut",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                CalendarViewMode.entries.forEach { mode ->
                    SettingsSheetOptionRow(
                        label = mode.label,
                        selected = appSettings.defaultCalendarView == mode.key,
                        accentColor = gradient.accentColor,
                        onClick = {
                            onUpdateDefaultCalendarView(mode.key)
                            showCalendarViewSheet = false
                        },
                        testTag = "settings_default_view_${mode.key}"
                    )
                }
            }
        }
    }

    // Bottom Sheet: Appearance Mode
    if (showAppearanceSheet) {
        ModalBottomSheet(onDismissRequest = { showAppearanceSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Style d'affichage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                AppearanceMode.entries.forEach { mode ->
                    SettingsSheetOptionRow(
                        label = mode.label,
                        subtitle = mode.description,
                        selected = appSettings.appearanceMode == mode.key,
                        accentColor = gradient.accentColor,
                        onClick = {
                            onSelectAppearanceMode(mode.key)
                            showAppearanceSheet = false
                        },
                        testTag = "appearance_mode_${mode.key}"
                    )
                }
            }
        }
    }

    // Bottom Sheet: Timer Durations
    if (showTimerDurationsSheet) {
        ModalBottomSheet(onDismissRequest = { showTimerDurationsSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Durées du minuteur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                DurationStepperRow(
                    label = "Pomodoro standard",
                    minutes = appSettings.pomodoroMinutes,
                    accentColor = gradient.accentColor,
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
                DurationStepperRow(
                    label = "Deep Code",
                    minutes = appSettings.deepCodeMinutes,
                    accentColor = gradient.accentColor,
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
                DurationStepperRow(
                    label = "Pause courte",
                    minutes = appSettings.shortBreakMinutes,
                    accentColor = AppleEmerald,
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
            }
        }
    }

    // Bottom Sheet: Manage Courses
    if (showCoursesSheet) {
        ModalBottomSheet(onDismissRequest = { showCoursesSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = adapt.workspaceSectionTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            showCoursesSheet = false
                            showAddCourseSheet = true
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ajouter")
                    }
                }
                if (courses.isEmpty()) {
                    Text(
                        text = "Aucune matière enregistrée pour le moment.",
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
                        courses.forEach { course ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = catalogIconColor(course.colorIndex).copy(alpha = 0.14f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CatalogIcon(
                                                iconKey = course.iconKey,
                                                contentDescription = null,
                                                tint = catalogIconColor(course.colorIndex),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${course.code} · ${course.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = course.professor.ifBlank { "Module actif" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCourse(course.id) },
                                        modifier = Modifier.testTag("course_actions_${course.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Supprimer ${course.title}",
                                            tint = AppleRose
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet: Edit Profile
    // Bottom Sheet: Add Course
    if (showAddCourseSheet) {
        var code by remember { mutableStateOf("GL-307") }
        var title by remember { mutableStateOf("") }
        var professor by remember { mutableStateOf("") }
        var targetHours by remember { mutableStateOf("20") }
        var iconKey by remember { mutableStateOf("book") }
        var colorIndex by remember { mutableLongStateOf((courses.size % 7).toLong()) }
        var showIconPicker by remember { mutableStateOf(false) }

        ModalBottomSheet(onDismissRequest = { showAddCourseSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Nouvelle matière",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code (ex: GL-307)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Intitulé du module") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = it },
                    label = { Text("Professeur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Objectif d'heures") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    TextButton(onClick = { showAddCourseSheet = false }) {
                        Text("Annuler")
                    }
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
                                showAddCourseSheet = false
                            }
                        },
                        enabled = title.isNotBlank() && code.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)
                    ) {
                        Text("Ajouter", color = Color.White)
                    }
                }
            }
        }
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
private fun SettingsSectionBlock(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LightSpacing.one)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = LightSpacing.half)
        )
        LightPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsLeadingIconBox(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color? = null
) {
    val isOled = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    val resolvedBg = containerColor ?: if (tint == MaterialTheme.colorScheme.primary) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isOled) 0.6f else 0.9f)
    } else {
        tint.copy(alpha = 0.14f)
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(resolvedBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    valueText: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LightTouchTarget.minimum)
            .clickable(onClick = onClick)
            .padding(horizontal = LightSpacing.two, vertical = LightSpacing.oneAndHalf)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
    ) {
        SettingsLeadingIconBox(icon = icon, tint = iconTint)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (!valueText.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LightTouchTarget.minimum)
            .padding(horizontal = LightSpacing.two, vertical = LightSpacing.one),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
    ) {
        SettingsLeadingIconBox(icon = icon)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsInsetDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        thickness = LightSpacing.hairline,
        modifier = Modifier.padding(start = 64.dp, end = LightSpacing.two)
    )
}

@Composable
private fun SettingsSheetOptionRow(
    label: String,
    subtitle: String? = null,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            if (selected) 1.2.dp else 0.5.dp,
            if (selected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DurationStepperRow(
    label: String,
    minutes: Int,
    accentColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onDecrease,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Diminuer $label",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${minutes}m",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Surface(
                    onClick = onIncrease,
                    shape = CircleShape,
                    color = accentColor,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Augmenter $label",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun WorkspaceProfilePage(
    profile: UserProfile?, photoUrl: String?,
    onUpdateProfile: (String, String, Long, String?, Long?, Long?, String?, String?) -> Unit,
    onBack: () -> Unit, modifier: Modifier = Modifier,
) {
    val gradient = LocalLightGradient.current
    val currentYear = remember { LocalDate.now().year }
    var name by remember { mutableStateOf(profile?.displayName ?: "") }
    var gender by remember { mutableStateOf(UserGender.fromKey(profile?.gender)) }
    var birthYearText by remember { mutableStateOf((profile?.birthYear ?: (currentYear - 21L)).toString()) }
    var ageText by remember { mutableStateOf((profile?.age ?: 21L).toString()) }
    var profession by remember { mutableStateOf(UserProfession.fromKey(profile?.profession)) }
    var customProf by remember { mutableStateOf(profile?.customProfession ?: "") }
    var major by remember { mutableStateOf(profile?.major ?: profession.defaultFocusDomain()) }
    var goal by remember { mutableStateOf((profile?.dailyGoalTasks ?: 5L).toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_profile_page")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingsPageHeader("Profil", onBack)
        val cover = remember(profile?.userId) {
            val covers = LightGradients.all
            covers[Math.floorMod(profile?.userId.orEmpty().hashCode(), covers.size)]
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cover.appBrush()).padding(LightSpacing.three),
            verticalArrangement = Arrangement.spacedBy(LightSpacing.one)) {
            if (!photoUrl.isNullOrBlank()) AsyncImage(photoUrl, "Photo de profil", contentScale = ContentScale.Crop,
                modifier = Modifier.size(LightSpacing.six).clip(CircleShape))
            Text(profile?.displayName?.takeIf { it.isNotBlank() } ?: "Mon profil",
                style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Prénom / Nom") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = major,
            onValueChange = { major = it },
            label = { Text("Filière / Domaine") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it.filter { ch -> ch.isDigit() } },
            label = { Text("Objectif tâches / jour") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
        ) {
            TextButton(onClick = { onBack() }) {
                Text("Annuler")
            }
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
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)
            ) {
                Text("Enregistrer", color = Color.White)
            }
        }
    }
}
