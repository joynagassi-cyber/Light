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
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalElanGradient

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
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalElanGradient.current
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
        modifier = modifier.fillMaxSize(),
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
                        text = "Mode Focus",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = if (isStopwatch) {
                            "Chronomètre libre · suivez chaque bloc réel"
                        } else {
                            "Pomodoro · codez et révisez sans distractions"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(0.8.dp, AppleDivider),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("focus_settings_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Paramètres du minuteur",
                            tint = gradient.accentColor,
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
                color = Color.White,
                border = BorderStroke(0.8.dp, AppleDivider),
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
                            color = if (selected) gradient.accentColor else Color.Transparent,
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
                                    tint = if (selected) Color.White else AppleTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = kind.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else AppleTextSecondary
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
                                selectedContainerColor = gradient.softContainer,
                                selectedLabelColor = gradient.accentColor,
                                containerColor = Color.White.copy(alpha = 0.85f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = AppleDivider,
                                selectedBorderColor = gradient.accentColor
                            ),
                            modifier = Modifier.testTag("focus_mode_${mode.id}")
                        )
                    }
                }
            }
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
                        modifier = Modifier.size(216.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_study_companion),
                            contentDescription = "Session Focus en cours",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(172.dp)
                                .scale(breathScale)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = formattedClock,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (formattedClock.length > 5) 36.sp else 44.sp,
                        color = AppleTextPrimary,
                        modifier = Modifier.testTag("focus_clock_display")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = focusState.selectedTaskTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = gradient.accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (audioState.isPlaying) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            onClick = onToggleAudioPlayPause,
                            shape = CircleShape,
                            color = gradient.softContainer,
                            modifier = Modifier.testTag("timer_audio_status_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AnimatedAudioEqualizer(tint = gradient.accentColor)
                                Text(
                                    text = audioState.selectedTrack.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = gradient.accentColor,
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
                                tint = AppleTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Ambiance disponible ci-dessous",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextSecondary
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
                            color = gradient.softContainer,
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("focus_reset_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Réinitialiser",
                                    tint = gradient.accentColor
                                )
                            }
                        }

                        Button(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = gradient.accentColor,
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
                                color = gradient.softContainer,
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("focus_lap_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Flag,
                                        contentDescription = "Enregistrer un tour",
                                        tint = gradient.accentColor
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onFinishAndLog,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, gradient.accentColor),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("focus_log_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Terminer et enregistrer",
                                tint = gradient.accentColor,
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
                onToggleAutoPlay = onToggleAutoPlayAudio
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
                        color = AppleTextPrimary
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
                                color = gradient.accentColor
                            )
                            Text(
                                text = "+${formatClockSeconds(splitSecs)}",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = AppleTextSecondary
                            )
                            Text(
                                text = formatClockSeconds(totalSecs),
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelLarge,
                                color = AppleTextPrimary
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
                        color = AppleTextPrimary
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
                                    selectedContainerColor = gradient.softContainer,
                                    selectedLabelColor = gradient.accentColor
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
                    color = AppleTextPrimary
                )
            }
        }

        if (sessions.isEmpty()) {
            item {
                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Lancez votre première session Pomodoro ou Chronomètre pour démarrer votre série d'étude.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
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
                                .background(gradient.softContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (session.mode == "stopwatch") Icons.Rounded.AvTimer else Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.taskTitle.ifBlank { "Session Focus" },
                                style = MaterialTheme.typography.titleSmall,
                                color = AppleTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$modeBadge · ${session.courseTitle.ifBlank { "Génie Logiciel" }} · ${session.dateIso}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = gradient.softContainer
                        ) {
                            Text(
                                text = "${session.durationMinutes} min",
                                style = MaterialTheme.typography.labelMedium,
                                color = gradient.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
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
