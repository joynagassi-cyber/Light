package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.FocusAudioTrack
import com.example.data.preferences.StudyAppSettings
import com.example.ui.FocusAudioUiState

data class FocusThemeStyle(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val gradientColors: List<Color>,
    val accentColor: Color,
    val containerColor: Color,
    val trackColor: Color
)

data class FocusIllustrationOption(
    val id: String,
    val title: String,
    val imageRes: Int
)

val focusThemes: List<FocusThemeStyle> = listOf(
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
        id = "focusly_dusk",
        title = "Focusly Dusk",
        subtitle = "Crépuscule violet premium",
        imageRes = R.drawable.focus_theme_mountain,
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF3B82F6)),
        accentColor = Color(0xFF7C3AED),
        containerColor = Color(0xFFEDE9FE),
        trackColor = Color(0xFFDDD6FE)
    ),
    FocusThemeStyle(
        id = "cyberpunk_night",
        title = "Cyber Midnight",
        subtitle = "Néon nocturne",
        imageRes = R.drawable.focus_theme_nebula,
        gradientColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF06B6D4)),
        accentColor = Color(0xFFDB2777),
        containerColor = Color(0xFFFCE7F3),
        trackColor = Color(0xFFFBCFE8)
    ),
    FocusThemeStyle(
        id = "aurora_borealis",
        title = "Aurora Borealis",
        subtitle = "Aurore boréale",
        imageRes = R.drawable.focus_theme_forest,
        gradientColors = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF6366F1)),
        accentColor = Color(0xFF059669),
        containerColor = Color(0xFFD1FAE5),
        trackColor = Color(0xFFA7F3D0)
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
        subtitle = "Cap sur l'essentiel",
        imageRes = R.drawable.focus_theme_mountain,
        gradientColors = listOf(Color(0xFF68C4E6), Color(0xFF7D9BEE), Color(0xFFB6C7FF)),
        accentColor = Color(0xFF385B98),
        containerColor = Color(0xFFE1EAF7),
        trackColor = Color(0xFFD8E2F2)
    ),
    FocusThemeStyle(
        id = "premium",
        title = "Premium Neomorphic",
        subtitle = "Élégance serein — plein écran",
        imageRes = R.drawable.focus_bg_premium,
        gradientColors = listOf(Color(0xFFF4F1EA), Color(0xFFEAE6DC), Color(0xFFF0512B)),
        accentColor = Color(0xFFF0512B),
        containerColor = Color(0xFFFDE9E3),
        trackColor = Color(0xFFF4F1EA)
    )
)

val focusIllustrations: List<FocusIllustrationOption> = listOf(
    FocusIllustrationOption("student", "Études", R.drawable.focus_art_student),
    FocusIllustrationOption("coder", "Code", R.drawable.focus_art_coder),
    FocusIllustrationOption("planner", "Planning", R.drawable.focus_art_planner),
    FocusIllustrationOption("desk_writer", "Bureau", R.drawable.focus_art_desk_writer),
    FocusIllustrationOption("book_reader", "Lecture", R.drawable.focus_art_book_reader),
    FocusIllustrationOption("flashcards", "Fiches", R.drawable.focus_art_flashcards),
    FocusIllustrationOption("lab_student", "Labo", R.drawable.focus_art_lab_student),
    FocusIllustrationOption("music_student", "Audio", R.drawable.focus_art_music_student),
    FocusIllustrationOption("math_student", "Maths", R.drawable.focus_art_math_student),
    FocusIllustrationOption("group_study", "Équipe", R.drawable.focus_art_group_study),
    FocusIllustrationOption("tablet_notes", "Notes", R.drawable.focus_art_tablet_notes),
    FocusIllustrationOption("speech_practice", "Oral", R.drawable.focus_art_speech_practice)
)

fun focusThemeFor(id: String): FocusThemeStyle =
    focusThemes.firstOrNull { it.id == id } ?: focusThemes.first()

fun focusIllustrationResource(id: String): Int =
    focusIllustrations.firstOrNull { it.id == id }?.imageRes ?: R.drawable.focus_art_student

fun focusIllustrationTitle(id: String): String =
    focusIllustrations.firstOrNull { it.id == id }?.title ?: "Études"

enum class FocusCustomizationTab(val key: String, val label: String) {
    UNIVERSE("universe", "Univers"),
    AUDIO("audio", "Audio"),
    TIMER("timer", "Minuteur")
}

@Composable
fun FocusVisualCustomizationPicker(
    selectedIllustrationId: String,
    selectedThemeId: String,
    onSelectIllustration: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    focusTheme: FocusThemeStyle,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Personnage",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            focusIllustrations.forEach { option ->
                val selected = selectedIllustrationId == option.id
                Surface(
                    onClick = { onSelectIllustration(option.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) focusTheme.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(
                        if (selected) 1.5.dp else 0.8.dp,
                        if (selected) focusTheme.accentColor else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .width(86.dp)
                        .testTag("focus_illustration_${option.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(option.imageRes),
                            contentDescription = option.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) focusTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "Décor de concentration",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
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
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) theme.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        if (selected) 1.5.dp else 0.8.dp,
                        if (selected) theme.accentColor else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .width(96.dp)
                        .testTag("focus_theme_${theme.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(theme.imageRes),
                            contentDescription = theme.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 80.dp, height = 38.dp)
                                .clip(RoundedCornerShape(11.dp))
                        )
                        Text(
                            text = theme.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) theme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusCustomizationSheet(
    initialTab: FocusCustomizationTab,
    appSettings: StudyAppSettings,
    audioState: FocusAudioUiState,
    onSelectFocusIllustration: (String) -> Unit,
    onSelectFocusTheme: (String) -> Unit,
    onSelectAudioTrack: (FocusAudioTrack) -> Unit,
    onToggleAudioPlayPause: () -> Unit,
    onAudioVolumeChange: (Float) -> Unit,
    onToggleAutoPlayAudio: (Boolean) -> Unit,
    onUpdateTimerDurations: (Int, Int, Int) -> Unit,
    onUpdateAutoLogStopwatch: (Boolean) -> Unit,
    onOpenFullSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusTheme = focusThemeFor(appSettings.focusThemeId)
    var activeTabKey by rememberSaveable(initialTab) { mutableStateOf(initialTab.key) }
    val activeTab = FocusCustomizationTab.entries.firstOrNull { it.key == activeTabKey } ?: FocusCustomizationTab.UNIVERSE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("focus_customization_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Personnaliser Focus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Univers, son & durées séparés de l'espace de session",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FocusCustomizationTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    val icon = when (tab) {
                        FocusCustomizationTab.UNIVERSE -> Icons.Rounded.Palette
                        FocusCustomizationTab.AUDIO -> Icons.Rounded.Headphones
                        FocusCustomizationTab.TIMER -> Icons.Rounded.Timer
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { activeTabKey = tab.key },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(tab.label, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = focusTheme.containerColor,
                            selectedLabelColor = focusTheme.accentColor,
                            selectedLeadingIconColor = focusTheme.accentColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = focusTheme.accentColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("focus_sheet_tab_${tab.key}")
                    )
                }
            }

            when (activeTab) {
                FocusCustomizationTab.UNIVERSE -> {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = focusTheme.containerColor.copy(alpha = 0.45f),
                        border = BorderStroke(0.8.dp, focusTheme.accentColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FocusVisualCustomizationPicker(
                            selectedIllustrationId = appSettings.focusIllustrationId,
                            selectedThemeId = appSettings.focusThemeId,
                            onSelectIllustration = onSelectFocusIllustration,
                            onSelectTheme = onSelectFocusTheme,
                            focusTheme = focusTheme,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                FocusCustomizationTab.AUDIO -> {
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

                FocusCustomizationTab.TIMER -> {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FocusSheetStepperRow(
                                label = "Pomodoro standard",
                                minutes = appSettings.pomodoroMinutes,
                                accentColor = focusTheme.accentColor,
                                containerColor = focusTheme.containerColor,
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            FocusSheetStepperRow(
                                label = "Deep Code",
                                minutes = appSettings.deepCodeMinutes,
                                accentColor = focusTheme.accentColor,
                                containerColor = focusTheme.containerColor,
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            FocusSheetStepperRow(
                                label = "Pause courte",
                                minutes = appSettings.shortBreakMinutes,
                                accentColor = focusTheme.accentColor,
                                containerColor = focusTheme.containerColor,
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                    Text(
                                        text = "Enregistrer le chronomètre",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Ajoute automatiquement au bilan quotidien",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = appSettings.autoLogStopwatch,
                                    onCheckedChange = onUpdateAutoLogStopwatch,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = focusTheme.accentColor
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenFullSettings()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("focus_sheet_open_settings")
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tous les paramètres", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = focusTheme.accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("focus_sheet_done_button")
                ) {
                    Text("Terminer")
                }
            }
        }
    }
}

@Composable
private fun FocusSheetStepperRow(
    label: String,
    minutes: Int,
    accentColor: Color,
    containerColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(40.dp)
                    .background(containerColor, CircleShape)
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Diminuer $label", tint = accentColor)
            }
            Text(
                text = "$minutes min",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(40.dp)
                    .background(containerColor, CircleShape)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Augmenter $label", tint = accentColor)
            }
        }
    }
}
