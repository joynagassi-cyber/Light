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
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
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
import com.example.R
import com.example.audio.FocusAudioTrack
import com.example.data.model.Course
import com.example.data.model.FocusSession
import com.example.data.preferences.StudyAppSettings
import com.example.ui.FocusAudioUiState
import com.example.ui.FocusKind
import com.example.ui.FocusMode
import com.example.ui.FocusTimerState
import com.example.ui.components.AnimatedAudioEqualizer
import com.example.ui.components.AppleCard
import com.example.ui.components.FocusAudioCard
import com.example.ui.components.GradientProgressRing
import com.example.ui.theme.JetBrainsMonoFamily

private data class FocusThemeStyle(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val gradientColors: List<Color>,
    val accentColor: Color,
    val containerColor: Color,
    val trackColor: Color
)

private val focusThemes = listOf(
    FocusThemeStyle(
        id = "lunar",
        title = "Lunaire",
        subtitle = "Silence et clarté",
        imageRes = R.drawable.focus_theme_lunar,
        gradientColors = listOf(Color(0xFF927BFF), Color(0xFFC1A7FF), Color(0xFFF4D5FF)),
        accentColor = Color(0xFF6652B4),
        containerColor = Color(0xFFEAE5FA),
        trackColor = Color(0xFFE2DDF2)
    ),
    FocusThemeStyle(
        id = "ocean",
        title = "Océan nuit",
        subtitle = "Respiration profonde",
        imageRes = R.drawable.focus_theme_ocean,
        gradientColors = listOf(Color(0xFF55C5D8), Color(0xFF579BEB), Color(0xFF8B91FF)),
        accentColor = Color(0xFF176F82),
        containerColor = Color(0xFFDDEFF4),
        trackColor = Color(0xFFD6E8EF)
    ),
    FocusThemeStyle(
        id = "forest",
        title = "Forêt nuit",
        subtitle = "Calme naturel",
        imageRes = R.drawable.focus_theme_forest,
        gradientColors = listOf(Color(0xFF73D5B2), Color(0xFF6BB990), Color(0xFFB9D887)),
        accentColor = Color(0xFF2D7456),
        containerColor = Color(0xFFDDEFE5),
        trackColor = Color(0xFFD7E9DF)
    ),
    FocusThemeStyle(
        id = "desk",
        title = "Bureau zen",
        subtitle = "Lumière de travail",
        imageRes = R.drawable.focus_theme_desk,
        gradientColors = listOf(Color(0xFFFFC77D), Color(0xFFF3A45F), Color(0xFFE9847A)),
        accentColor = Color(0xFF93511F),
        containerColor = Color(0xFFFFEBD8),
        trackColor = Color(0xFFF4E1D1)
    ),
    FocusThemeStyle(
        id = "nebula",
        title = "Nébuleuse",
        subtitle = "Plongée immersive",
        imageRes = R.drawable.focus_theme_nebula,
        gradientColors = listOf(Color(0xFFB682FF), Color(0xFFCE78E4), Color(0xFFF186C1)),
        accentColor = Color(0xFF7643A7),
        containerColor = Color(0xFFF0E2F7),
        trackColor = Color(0xFFE7D9F0)
    ),
    FocusThemeStyle(
        id = "mountain",
        title = "Montagne",
        subtitle = "Cap sur l’essentiel",
        imageRes = R.drawable.focus_theme_mountain,
        gradientColors = listOf(Color(0xFF68C4E6), Color(0xFF7D9BEE), Color(0xFFB6C7FF)),
        accentColor = Color(0xFF385B98),
        containerColor = Color(0xFFE1EAF7),
        trackColor = Color(0xFFD8E2F2)
    )
)

private fun focusThemeFor(id: String): FocusThemeStyle = focusThemes.firstOrNull { it.id == id } ?: focusThemes.first()

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
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusTheme = focusThemeFor(appSettings.focusThemeId)
    val isStopwatch = focusState.kind == FocusKind.STOPWATCH

    val progress = if (isStopwatch) {
        (focusState.elapsedSeconds % 60) / 60f
    } else {
        1f - (focusState.remainingSeconds.toFloat() / focusState.totalSeconds.coerceAtLeast(1).toFloat())
    }

    val displayedSeconds = if (isStopwatch) focusState.elapsedSeconds else focusState.remainingSeconds
    val formattedClock = formatClockSeconds(displayedSeconds)

    val infiniteTransition = rememberInfiniteTransition(label = "focusBreath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (focusState.isRunning) 1.045f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .paint(
                painter = painterResource(focusTheme.imageRes),
                contentScale = ContentScale.Crop,
                alpha = 0.32f
            ),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isStopwatch) {
                            "Chronomètre libre · suivez chaque bloc réel"
                        } else {
                            "Pomodoro · codez et révisez sans distractions"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("focus_settings_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Paramètres du minuteur",
                            tint = focusTheme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Top-level Switch: Pomodoro vs Chronomètre
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
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
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
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

        // Sub-modes for Pomodoro
        if (!isStopwatch) {
            item {
                val pomodoroModes = listOf(
                    FocusMode.POMODORO to "${appSettings.pomodoroMinutes}m",
                    FocusMode.DEEP_CODE to "${appSettings.deepCodeMinutes}m",
                    FocusMode.SHORT_BREAK to "${appSettings.shortBreakMinutes}m"
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pomodoroModes.forEach { (mode, minsLabel) ->
                        val selected = focusState.mode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectMode(mode) },
                            label = { Text("${mode.label} · $minsLabel") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = focusTheme.containerColor,
                                selectedLabelColor = focusTheme.accentColor,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = focusTheme.accentColor
                            ),
                            modifier = Modifier.testTag("focus_mode_${mode.id}")
                        )
                    }
                }
            }
        }

        item {
            FocusVisualPicker(
                selectedIllustrationId = appSettings.focusIllustrationId,
                selectedThemeId = appSettings.focusThemeId,
                onSelectIllustration = onSelectFocusIllustration,
                onSelectTheme = onSelectFocusTheme,
                focusTheme = focusTheme
            )
        }

        // Main Timer / Stopwatch Card
        item {
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                contentPadding = PaddingValues(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientProgressRing(
                        progress = progress,
                        strokeWidth = 12.dp,
                        trackColor = focusTheme.trackColor,
                        progressColors = focusTheme.gradientColors + focusTheme.gradientColors.first(),
                        modifier = Modifier.size(216.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(172.dp)
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
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = formattedClock,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (formattedClock.length > 5) 36.sp else 44.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("focus_clock_display")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = focusState.selectedTaskTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = focusTheme.accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (audioState.isPlaying) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            onClick = onToggleAudioPlayPause,
                            shape = CircleShape,
                            color = focusTheme.containerColor,
                            modifier = Modifier.testTag("timer_audio_status_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AnimatedAudioEqualizer(tint = focusTheme.accentColor)
                                Text(
                                    text = audioState.selectedTrack.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = focusTheme.accentColor,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Ambiance disponible ci-dessous",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                .size(50.dp)
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
                                .height(54.dp)
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
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (isStopwatch) {
                            Surface(
                                onClick = onRecordLap,
                                shape = CircleShape,
                                color = focusTheme.containerColor,
                                modifier = Modifier
                                    .size(50.dp)
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
                            border = BorderStroke(1.dp, focusTheme.accentColor),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(50.dp)
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

        // Ambient, White Noise & Lo-Fi Audio Player Card
        item {
            FocusAudioCard(
                audioState = audioState,
                onSelectTrack = onSelectAudioTrack,
                onTogglePlayPause = onToggleAudioPlayPause,
                onVolumeChange = onAudioVolumeChange,
                onToggleAutoPlay = onToggleAutoPlayAudio,
                accentColor = focusTheme.accentColor,
                softContainerColor = focusTheme.containerColor,
                activeBrush = Brush.linearGradient(focusTheme.gradientColors)
            )
        }

        // Laps Card (Stopwatch mode)
        if (isStopwatch && focusState.laps.isNotEmpty()) {
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "Tours enregistrés (${focusState.laps.size})",
                        style = MaterialTheme.typography.titleSmall,
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

        if (courses.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Associer à une matière L3",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        courses.forEach { course ->
                            val selected = focusState.selectedCourseId == course.id
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCourse(if (selected) null else course) },
                                label = { Text("${course.code} · ${course.title.take(18)}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = focusTheme.containerColor,
                                    selectedLabelColor = focusTheme.accentColor
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sessions enregistrées (${sessions.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (sessions.isEmpty()) {
            item {
                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Lancez votre première session Pomodoro ou Chronomètre pour démarrer votre série d'étude.",
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

@Composable
private fun FocusVisualPicker(
    selectedIllustrationId: String,
    selectedThemeId: String,
    onSelectIllustration: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    focusTheme: FocusThemeStyle
) {
    val illustrations = listOf(
        Triple("student", "Études", R.drawable.focus_art_student),
        Triple("coder", "Code", R.drawable.focus_art_coder),
        Triple("planner", "Planning", R.drawable.focus_art_planner),
        Triple("desk_writer", "Bureau", R.drawable.focus_art_desk_writer),
        Triple("book_reader", "Lecture", R.drawable.focus_art_book_reader),
        Triple("flashcards", "Fiches", R.drawable.focus_art_flashcards)
    )
    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(15.dp)
    ) {
        Text(
            text = "Votre univers Focus",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Choisissez un personnage et un décor de nuit illustré en PNG.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Ces styles restent propres à l'écran Focus.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${focusTheme.title} · ${focusTheme.subtitle}",
            style = MaterialTheme.typography.labelSmall,
            color = focusTheme.accentColor,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Text("Illustration", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            illustrations.forEach { (id, title, imageRes) ->
                val selected = selectedIllustrationId == id
                Surface(
                    onClick = { onSelectIllustration(id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(
                        if (selected) 1.5.dp else 0.8.dp,
                        if (selected) focusTheme.accentColor else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .width(88.dp)
                        .testTag("focus_illustration_$id")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Image(
                            painter = painterResource(imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Thèmes PNG de concentration", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            focusThemes.forEach { theme ->
                val selected = selectedThemeId == theme.id
                Surface(
                    onClick = { onSelectTheme(theme.id) },
                    shape = RoundedCornerShape(15.dp),
                    color = if (selected) theme.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        if (selected) 1.5.dp else 0.8.dp,
                        if (selected) theme.accentColor else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .width(84.dp)
                        .testTag("focus_theme_${theme.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(theme.imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 70.dp, height = 34.dp)
                                .clip(RoundedCornerShape(11.dp))
                        )
                        Text(
                            theme.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) theme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

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

private fun focusIllustrationResource(id: String): Int = when (id) {
    "coder" -> R.drawable.focus_art_coder
    "planner" -> R.drawable.focus_art_planner
    "desk_writer" -> R.drawable.focus_art_desk_writer
    "book_reader" -> R.drawable.focus_art_book_reader
    "flashcards" -> R.drawable.focus_art_flashcards
    "lab_student" -> R.drawable.focus_art_lab_student
    "music_student" -> R.drawable.focus_art_music_student
    "math_student" -> R.drawable.focus_art_math_student
    "group_study" -> R.drawable.focus_art_group_study
    "tablet_notes" -> R.drawable.focus_art_tablet_notes
    "speech_practice" -> R.drawable.focus_art_speech_practice
    else -> R.drawable.focus_art_student
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
