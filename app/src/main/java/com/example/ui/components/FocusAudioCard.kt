package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.AudioCategory
import com.example.audio.FocusAudioCatalog
import com.example.audio.FocusAudioTrack
import com.example.ui.FocusAudioUiState
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalElanGradient
import kotlin.math.roundToInt

@Composable
fun FocusAudioCard(
    audioState: FocusAudioUiState,
    onSelectTrack: (FocusAudioTrack) -> Unit,
    onTogglePlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleAutoPlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalElanGradient.current
    var selectedCategory by remember { mutableStateOf(AudioCategory.ALL) }

    val filteredTracks = remember(selectedCategory) {
        if (selectedCategory == AudioCategory.ALL) {
            FocusAudioCatalog.tracks
        } else {
            FocusAudioCatalog.tracks.filter { it.category == selectedCategory }
        }
    }

    AppleCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        // Header Row: Icon/Equalizer + Title + Play/Pause Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (audioState.isPlaying) gradient.linearBrush()
                        else BrushBackgroundFallback(gradient.softContainer)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (audioState.isPlaying) {
                    AnimatedAudioEqualizer(tint = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Headphones,
                        contentDescription = null,
                        tint = gradient.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ambiance sonore & Lo-Fi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
                Text(
                    text = if (audioState.isPlaying) {
                        "En lecture · ${audioState.selectedTrack.title}"
                    } else {
                        "${audioState.selectedTrack.title} · En pause"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (audioState.isPlaying) gradient.accentColor else AppleTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = onTogglePlayPause,
                shape = CircleShape,
                color = if (audioState.isPlaying) gradient.accentColor else gradient.softContainer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("audio_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (audioState.isPlaying) "Mettre l'audio en pause" else "Lire la piste audio",
                        tint = if (audioState.isPlaying) Color.White else gradient.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AudioCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = gradient.softContainer,
                        selectedLabelColor = gradient.accentColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = AppleDivider,
                        selectedBorderColor = gradient.accentColor
                    ),
                    modifier = Modifier.testTag("audio_category_${cat.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Track Cards Horizontal Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredTracks.forEach { track ->
                val isSelected = audioState.selectedTrack.id == track.id
                val isPlayingThis = isSelected && audioState.isPlaying
                Surface(
                    modifier = Modifier
                        .width(164.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectTrack(track) }
                        .testTag("audio_track_${track.id}"),
                    shape = RoundedCornerShape(18.dp),
                    color = when {
                        isPlayingThis -> gradient.softContainer
                        isSelected -> gradient.softContainer.copy(alpha = 0.65f)
                        else -> Color(0xFFF6F7FB)
                    },
                    border = BorderStroke(
                        width = if (isSelected) 1.4.dp else 0.8.dp,
                        color = if (isSelected) gradient.accentColor else AppleDivider
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) gradient.accentColor
                                        else Color.White
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = track.icon(),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else gradient.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White else gradient.softContainer
                            ) {
                                Text(
                                    text = track.category.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = gradient.accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Volume Control Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                contentDescription = "Volume minimum",
                tint = AppleTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Slider(
                value = audioState.volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = gradient.accentColor,
                    activeTrackColor = gradient.accentColor,
                    inactiveTrackColor = gradient.softContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("audio_volume_slider")
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = "Volume maximum",
                tint = gradient.accentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${(audioState.volume * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = AppleTextSecondary,
                modifier = Modifier.width(34.dp)
            )
        }

        // Auto-play with Focus timer toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(
                    text = "Lancer automatiquement avec Focus",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AppleTextPrimary
                )
                Text(
                    text = "Synchronise la lecture avec Démarrer / Pause",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextSecondary
                )
            }
            Switch(
                checked = audioState.autoPlayWithTimer,
                onCheckedChange = onToggleAutoPlay,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = gradient.accentColor
                ),
                modifier = Modifier.testTag("audio_autoplay_switch")
            )
        }
    }
}

@Composable
private fun BrushBackgroundFallback(color: Color) = androidx.compose.ui.graphics.SolidColor(color)

@Composable
fun AnimatedAudioEqualizer(
    tint: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val bar1 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(370, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq3"
    )

    Row(
        modifier = modifier.height(18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        listOf(bar1, bar2, bar3).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((18f * fraction).dp)
                    .clip(CircleShape)
                    .background(tint)
            )
        }
    }
}

private fun FocusAudioTrack.icon(): ImageVector = when (id) {
    "lofi_chill" -> Icons.Rounded.MusicNote
    "lofi_synth" -> Icons.Rounded.GraphicEq
    "rain_ambient" -> Icons.Rounded.WaterDrop
    "warm_cafe" -> Icons.Rounded.LocalCafe
    "alpha_waves" -> Icons.Rounded.Spa
    else -> Icons.Rounded.Air
}
