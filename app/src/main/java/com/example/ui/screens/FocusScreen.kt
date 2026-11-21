package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.FocusAudioTrack
import com.example.data.model.Course
import com.example.data.model.FocusProfile
import com.example.data.model.FocusSession
import com.example.data.preferences.StudyAppSettings
import com.example.ui.FocusAudioUiState
import com.example.ui.theme.LightSpacing
import com.example.ui.FocusKind
import com.example.ui.FocusMode
import com.example.ui.FocusTimerState
import com.example.ui.components.AnimatedAudioEqualizer
import com.example.ui.components.AppleCard
import com.example.ui.components.FocusCustomizationSheet
import com.example.ui.components.FocusCustomizationTab
import com.example.ui.components.FocusThemeStyle
import com.example.ui.components.GradientProgressRing
import com.example.ui.components.focusIllustrationResource
import com.example.ui.components.focusThemeFor
import com.example.ui.theme.JetBrainsMonoFamily
import java.time.LocalDate

@Composable
fun FocusScreen(
    focusState: FocusTimerState,
    audioState: FocusAudioUiState,
    appSettings: StudyAppSettings,
    courses: List<Course>,
    sessions: List<FocusSession>,
    onSelectKind: (FocusKind) -> Unit,
    onSelectMode: (FocusMode) -> Unit,
    onSelectCourse: (Course?) -> Unit,
    onTogglePlayPause: () -> Unit,
    onRecordLap: () -> Unit,
    onReset: () -> Unit,
    onFinishAndLog: () -> Unit,
    onSelectAudioTrack: (FocusAudioTrack) -> Unit,
    onToggleAudioPlayPause: () -> Unit,
    onAudioVolumeChange: (Float) -> Unit,
    onToggleAutoPlayAudio: (Boolean) -> Unit,
    onSelectFocusIllustration: (String) -> Unit,
    onSelectFocusTheme: (String) -> Unit,
    onUpdateTimerDurations: (Int, Int, Int) -> Unit = { _, _, _ -> },
    onUpdateAutoLogStopwatch: (Boolean) -> Unit = {},
    focusProfiles: List<FocusProfile> = emptyList(),
    onSelectFocusProfile: (FocusProfile?) -> Unit = {},
    onOpenFocusProfiles: () -> Unit = {},
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusTheme = focusThemeFor(appSettings.focusThemeId)
    val isStopwatch = focusState.kind == FocusKind.STOPWATCH

    var moreMenuExpanded by remember { mutableStateOf(false) }
    var customizationSheetTab by rememberSaveable { mutableStateOf<FocusCustomizationTab?>(null) }
    var showCoursePickerDialog by remember { mutableStateOf(false) }
    var showFocusProfilePickerDialog by remember { mutableStateOf(false) }

    val progress = if (isStopwatch) {
        (focusState.elapsedSeconds % 60) / 60f
    } else {
        1f - (focusState.remainingSeconds.toFloat() / focusState.totalSeconds.coerceAtLeast(1).toFloat())
    }

    val displayedSeconds = if (isStopwatch) focusState.elapsedSeconds else focusState.remainingSeconds
    val formattedClock = formatClockSeconds(displayedSeconds)

    val selectedCourse = remember(courses, focusState.selectedCourseId) {
        courses.firstOrNull { it.id == focusState.selectedCourseId }
    }
    val selectedFocusProfile = remember(focusProfiles, focusState.selectedFocusProfileId) {
        focusProfiles.firstOrNull { it.id == focusState.selectedFocusProfileId }
    }
    val todayIso = remember { LocalDate.now().toString() }
    val todayMinutes = remember(sessions, todayIso) {
        sessions.filter { it.dateIso == todayIso }.sumOf { it.durationMinutes }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "focusBreath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (focusState.isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle atmospheric theme backdrop with high-contrast protective veil
        Image(
            painter = painterResource(focusTheme.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.24f,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("focus_list"),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Crisp High-Contrast Top Header + 3-dot Focus Customization Menu
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Focus",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = focusTheme.containerColor,
                                border = BorderStroke(0.6.dp, focusTheme.accentColor.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = focusTheme.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = focusTheme.accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = if (isStopwatch) "Chronomètre libre" else "Mode ${focusState.mode.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box {
                        Surface(
                            onClick = { moreMenuExpanded = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("focus_more_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Personnaliser et régler Focus",
                                    tint = focusTheme.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = moreMenuExpanded,
                            onDismissRequest = { moreMenuExpanded = false },
                            modifier = Modifier.testTag("focus_more_menu")
                        ) {
                            DropdownMenuItem(
                                text = { Text("Personnaliser l'univers") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = focusTheme.accentColor)
                                },
                                onClick = {
                                    moreMenuExpanded = false
                                    customizationSheetTab = FocusCustomizationTab.UNIVERSE
                                },
                                modifier = Modifier.testTag("focus_menu_customize")
                            )
                            DropdownMenuItem(
                                text = { Text("Ambiance sonore & Lo-Fi") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Headphones, contentDescription = null, tint = focusTheme.accentColor)
                                },
                                onClick = {
                                    moreMenuExpanded = false
                                    customizationSheetTab = FocusCustomizationTab.AUDIO
                                },
                                modifier = Modifier.testTag("focus_menu_audio")
                            )
                            DropdownMenuItem(
                                text = { Text("Profils de Focus") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = focusTheme.accentColor)
                                },
                                onClick = {
                                    moreMenuExpanded = false
                                    onOpenFocusProfiles()
                                },
                                modifier = Modifier.testTag("focus_menu_profiles")
                            )
                            DropdownMenuItem(
                                text = { Text("Durées & chronomètre") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = focusTheme.accentColor)
                                },
                                onClick = {
                                    moreMenuExpanded = false
                                    customizationSheetTab = FocusCustomizationTab.TIMER
                                },
                                modifier = Modifier.testTag("focus_menu_timer")
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Paramètres généraux") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Settings, contentDescription = null)
                                },
                                onClick = {
                                    moreMenuExpanded = false
                                    onOpenSettings()
                                },
                                modifier = Modifier.testTag("focus_menu_settings")
                            )
                        }
                    }
                }
            }

            // Segmented Switch: Pomodoro vs Chronomètre
            item {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FocusKind.entries.forEach { kind ->
                            val selected = focusState.kind == kind
                            Surface(
                                onClick = { onSelectKind(kind) },
                                shape = CircleShape,
                                color = if (selected) focusTheme.accentColor else Color.Transparent,
                                modifier = Modifier.testTag("focus_kind_${kind.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (kind == FocusKind.POMODORO) Icons.Rounded.Timer else Icons.Rounded.AvTimer,
                                        contentDescription = null,
                                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = kind.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sub-modes (equal-width grid row so all 3 labels fit cleanly without clipping)
            if (!isStopwatch) {
                item {
                    val pomodoroModes = listOf(
                        FocusMode.POMODORO to "${appSettings.pomodoroMinutes}m",
                        FocusMode.DEEP_CODE to "${appSettings.deepCodeMinutes}m",
                        FocusMode.SHORT_BREAK to "${appSettings.shortBreakMinutes}m"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        pomodoroModes.forEach { (mode, minsLabel) ->
                            val selected = focusState.mode == mode
                            Surface(
                                onClick = { onSelectMode(mode) },
                                shape = CircleShape,
                                color = if (selected) focusTheme.containerColor else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                border = BorderStroke(
                                    if (selected) 1.4.dp else 0.8.dp,
                                    if (selected) focusTheme.accentColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("focus_mode_${mode.id}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mode.label} · $minsLabel",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) focusTheme.accentColor else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Redesigned Hero Focus Card (everything visible above the fold, zero visual clutter)
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, focusTheme.accentColor.copy(alpha = 0.22f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_hero_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        focusTheme.containerColor.copy(alpha = 0.36f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top metadata pills inside card: Course link + Quick Lo-Fi / Ambient toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { if (courses.isNotEmpty()) showCoursePickerDialog = true },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("focus_course_picker_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Book,
                                        contentDescription = null,
                                        tint = focusTheme.accentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = selectedCourse?.code ?: focusState.selectedTaskTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (courses.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Rounded.ExpandMore,
                                            contentDescription = "Choisir une matière",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = onToggleAudioPlayPause,
                                shape = CircleShape,
                                color = if (audioState.isPlaying) focusTheme.containerColor else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    0.8.dp,
                                    if (audioState.isPlaying) focusTheme.accentColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.testTag("focus_audio_quick_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (audioState.isPlaying) {
                                        AnimatedAudioEqualizer(tint = focusTheme.accentColor)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Headphones,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = audioState.selectedTrack.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (audioState.isPlaying) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Central Progress Ring with Atmospheric Night Vignette + Illustration
                        GradientProgressRing(
                            progress = progress,
                            strokeWidth = 9.dp,
                            trackColor = focusTheme.trackColor,
                            progressColors = focusTheme.gradientColors + focusTheme.gradientColors.first(),
                            modifier = Modifier.size(134.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .scale(breathScale),
                                contentAlignment = Alignment.Center
                            ) {
                                FocusIllustrationBackdrop(
                                    focusTheme = focusTheme,
                                    modifier = Modifier.matchParentSize()
                                )
                                Image(
                                    painter = painterResource(id = focusIllustrationResource(appSettings.focusIllustrationId)),
                                    contentDescription = "Illustration Focus sélectionnée",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Prominent Monospace Digital Clock
                        Text(
                            text = formattedClock,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (formattedClock.length > 5) 32.sp else 40.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("focus_clock_display")
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Task & Status Line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (focusState.isRunning) focusTheme.accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = when {
                                        focusState.isRunning -> "En cours"
                                        isStopwatch && focusState.elapsedSeconds > 0 -> "En pause"
                                        !isStopwatch && focusState.remainingSeconds < focusState.totalSeconds -> "En pause"
                                        else -> "Prêt"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (focusState.isRunning) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = focusState.selectedTaskTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            onClick = { showFocusProfilePickerDialog = true },
                            shape = CircleShape,
                            color = if (selectedFocusProfile != null) focusTheme.containerColor else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                0.8.dp,
                                if (selectedFocusProfile != null) focusTheme.accentColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("focus_profile_picker_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = if (selectedFocusProfile != null) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = selectedFocusProfile?.title ?: focusState.selectedFocusProfileTitle.ifBlank { "Profil de focus : Libre" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedFocusProfile != null) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Control Row: Reset | Play/Pause | Lap (Stopwatch) | Log Session
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = onReset,
                                shape = CircleShape,
                                color = focusTheme.containerColor,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("focus_reset_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Réinitialiser",
                                        tint = focusTheme.accentColor
                                    )
                                }
                            }

                            Button(
                                onClick = onTogglePlayPause,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = focusTheme.accentColor,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(1f)
                                    .testTag("focus_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (focusState.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (focusState.isRunning) "Pause" else "Démarrer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (isStopwatch) {
                                Surface(
                                    onClick = onRecordLap,
                                    shape = CircleShape,
                                    color = focusTheme.containerColor,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("focus_lap_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Flag,
                                            contentDescription = "Enregistrer un tour",
                                            tint = focusTheme.accentColor
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = onFinishAndLog,
                                shape = CircleShape,
                                border = BorderStroke(1.2.dp, focusTheme.accentColor),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("focus_log_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Terminer et enregistrer",
                                    tint = focusTheme.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Stopwatch Laps Card (only when stopwatch has recorded laps)
            if (isStopwatch && focusState.laps.isNotEmpty()) {
                item {
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "Tours enregistrés (${focusState.laps.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        focusState.laps.reversed().forEachIndexed { revIdx, totalSecs ->
                            val lapNum = focusState.laps.size - revIdx
                            val prevSecs = if (lapNum > 1) focusState.laps[lapNum - 2] else 0
                            val splitSecs = (totalSecs - prevSecs).coerceAtLeast(0)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tour $lapNum",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = focusTheme.accentColor
                                )
                                Text(
                                    text = "+${formatClockSeconds(splitSecs)}",
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatClockSeconds(totalSecs),
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Clean Session History Header with Today's Total
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sessions récentes (${sessions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = focusTheme.containerColor
                    ) {
                        Text(
                            text = "Aujourd'hui : ${todayMinutes}m",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = focusTheme.accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (sessions.isEmpty()) {
                item {
                    AppleCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Lancez votre première session Pomodoro ou Chronomètre. Personnalisez l'univers ou l'audio Lo-Fi via le menu ⋮.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(sessions.take(8), key = { it.id }) { session ->
                    val modeBadge = when (session.mode) {
                        "stopwatch" -> "Chrono"
                        "deep_code" -> "Deep Code"
                        "short_break" -> "Pause"
                        else -> "Pomodoro"
                    }
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(focusTheme.containerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (session.mode == "stopwatch") Icons.Rounded.AvTimer else Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = focusTheme.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.taskTitle.ifBlank { "Session Focus" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$modeBadge · ${session.courseTitle.ifBlank { "Génie Logiciel" }} · ${session.dateIso}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = focusTheme.containerColor
                            ) {
                                Text(
                                    text = "${session.durationMinutes} min",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = focusTheme.accentColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Course / Subject Selection Dialog (keeps main page uncluttered)
    if (showCoursePickerDialog) {
        AlertDialog(
            onDismissRequest = { showCoursePickerDialog = false },
            title = { Text("Associer à une matière") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = {
                            onSelectCourse(null)
                            showCoursePickerDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (focusState.selectedCourseId.isBlank()) focusTheme.containerColor else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Session libre (aucune matière)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    courses.forEach { course ->
                        val chosen = course.id == focusState.selectedCourseId
                        Surface(
                            onClick = {
                                onSelectCourse(course)
                                showCoursePickerDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (chosen) focusTheme.containerColor else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCourse(course)
                                    showCoursePickerDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Book,
                                    contentDescription = null,
                                    tint = if (chosen) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${course.code} · ${course.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (chosen) focusTheme.accentColor else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCoursePickerDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    if (showFocusProfilePickerDialog) {
        AlertDialog(
            onDismissRequest = { showFocusProfilePickerDialog = false },
            title = { Text("Profil de Focus") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = {
                            onSelectFocusProfile(null)
                            showFocusProfilePickerDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (focusState.selectedFocusProfileId.isBlank()) focusTheme.containerColor else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Aucun profil sélectionné", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    focusProfiles.forEach { fp ->
                        val chosen = fp.id == focusState.selectedFocusProfileId
                        Surface(
                            onClick = {
                                onSelectFocusProfile(fp)
                                showFocusProfilePickerDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (chosen) focusTheme.containerColor else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = if (chosen) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fp.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                                        color = if (chosen) focusTheme.accentColor else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (fp.learningTopic.isNotBlank()) {
                                        Text(
                                            text = fp.learningTopic,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFocusProfilePickerDialog = false
                        onOpenFocusProfiles()
                    }
                ) {
                    Text("Gérer / Créer", color = focusTheme.accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFocusProfilePickerDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Separate Customization Sheet triggered from the 3-dot menu
    customizationSheetTab?.let { initialTab ->
        FocusCustomizationSheet(
            initialTab = initialTab,
            appSettings = appSettings,
            audioState = audioState,
            onSelectFocusIllustration = onSelectFocusIllustration,
            onSelectFocusTheme = onSelectFocusTheme,
            onSelectAudioTrack = onSelectAudioTrack,
            onToggleAudioPlayPause = onToggleAudioPlayPause,
            onAudioVolumeChange = onAudioVolumeChange,
            onToggleAutoPlayAudio = onToggleAutoPlayAudio,
            onUpdateTimerDurations = onUpdateTimerDurations,
            onUpdateAutoLogStopwatch = onUpdateAutoLogStopwatch,
            onOpenFullSettings = onOpenSettings,
            onDismiss = { customizationSheetTab = null }
        )
    }
}

@Composable
private fun FocusIllustrationBackdrop(
    focusTheme: FocusThemeStyle,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(focusTheme.imageRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}

private fun formatClockSeconds(totalSeconds: Int): String {
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

/** The workspace chrome is not composed while a running session is in the foreground. */
@Composable
internal fun ImmersiveFocusSession(focusState: FocusTimerState, onExit: () -> Unit) {
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.DisposableEffect(view) {
        val previous = view.keepScreenOn
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }
    val seconds = if (focusState.kind == FocusKind.STOPWATCH) focusState.elapsedSeconds else focusState.remainingSeconds
    Box(Modifier.fillMaxSize().focusSessionInsets()) {
        AppleCard(modifier = Modifier.align(Alignment.Center).padding(LightSpacing.three).widthInFocus()) {
            Column(Modifier.fillMaxWidth().padding(LightSpacing.three), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(focusState.selectedTaskTitle.ifBlank { "Session Focus" }, style = MaterialTheme.typography.titleMedium)
                Text(formatClockSeconds(seconds), style = MaterialTheme.typography.displayLarge, modifier = Modifier.testTag("immersive_focus_clock"))
            }
        }
        IconButton(onClick = onExit, modifier = Modifier.align(Alignment.TopEnd).testTag("exit_immersive_focus")) {
            Icon(Icons.Rounded.Close, contentDescription = "Quitter le plein écran", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun Modifier.focusSessionInsets() = statusBarsPadding().navigationBarsPadding().padding(LightSpacing.two)
private fun Modifier.widthInFocus() = widthIn(max = LightSpacing.six * 7)
