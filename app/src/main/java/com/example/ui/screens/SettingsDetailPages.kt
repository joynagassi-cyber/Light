package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.model.Course
import com.example.data.model.NotificationFrequency
import com.example.data.preferences.StudyAppSettings
import com.example.ui.FocusAudioUiState
import com.example.ui.components.CalendarViewMode
import com.example.ui.components.CatalogColorPicker
import com.example.ui.components.CatalogIcon
import com.example.ui.components.CatalogIconPickerSheet
import com.example.ui.components.FocusAudioCard
import com.example.ui.components.FocusVisualCustomizationPicker
import com.example.ui.components.GradientSwatchCard
import com.example.ui.components.catalogIconColor
import com.example.ui.components.focusThemeFor
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LightColorThemes
import com.example.ui.theme.LightGradients
import com.example.ui.theme.LightSpacing
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush

@Composable
internal fun NotificationFrequencyPage(
    currentFrequency: NotificationFrequency,
    notifEnabled: Boolean,
    onUpdateNotifications: (Boolean, NotificationFrequency) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Fréquence des rappels", onBack)
        Text("Choisissez à quelle fréquence Light vous rappelle vos tâches.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        NotificationFrequency.entries.forEach { freq ->
            val selected = if (!notifEnabled) freq == NotificationFrequency.OFF else currentFrequency == freq
            Surface(
                onClick = {
                    val willEnable = freq != NotificationFrequency.OFF
                    onUpdateNotifications(willEnable, freq)
                    onBack()
                },
                shape = RoundedCornerShape(16.dp),
                color = if (selected) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(if (selected) 1.2.dp else 0.5.dp, if (selected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("settings_notif_freq_${freq.key}")
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(freq.label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface)
                        Text(freq.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (selected) Icon(Icons.Rounded.Add, contentDescription = null, tint = gradient.accentColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
internal fun CalendarViewPage(
    currentViewKey: String,
    onUpdateDefaultCalendarView: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Vue Calendrier par défaut", onBack)
        CalendarViewMode.entries.forEach { mode ->
            val selected = currentViewKey == mode.key
            Surface(
                onClick = { onUpdateDefaultCalendarView(mode.key); onBack() },
                shape = RoundedCornerShape(16.dp),
                color = if (selected) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(if (selected) 1.2.dp else 0.5.dp, if (selected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("settings_default_view_${mode.key}")
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(mode.label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    if (selected) Icon(Icons.Rounded.Add, contentDescription = null, tint = gradient.accentColor)
                }
            }
        }
    }
}

@Composable
internal fun AppearancePage(
    currentModeKey: String,
    onSelectAppearanceMode: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Style d'affichage", onBack)
        AppearanceMode.entries.forEach { mode ->
            val selected = currentModeKey == mode.key
            Surface(
                onClick = { onSelectAppearanceMode(mode.key); onBack() },
                shape = RoundedCornerShape(16.dp),
                color = if (selected) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(if (selected) 1.2.dp else 0.5.dp, if (selected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("appearance_mode_${mode.key}")
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface)
                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (selected) Icon(Icons.Rounded.Add, contentDescription = null, tint = gradient.accentColor)
                }
            }
        }
    }
}

@Composable
internal fun TimerDurationsPage(
    appSettings: StudyAppSettings,
    onUpdateTimerDurations: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Durées du minuteur", onBack)
        TimerStepperRow(label = "Pomodoro standard", minutes = appSettings.pomodoroMinutes, accentColor = gradient.accentColor,
            onDecrease = { onUpdateTimerDurations((appSettings.pomodoroMinutes - 5).coerceAtLeast(10), appSettings.deepCodeMinutes, appSettings.shortBreakMinutes) },
            onIncrease = { onUpdateTimerDurations((appSettings.pomodoroMinutes + 5).coerceAtMost(90), appSettings.deepCodeMinutes, appSettings.shortBreakMinutes) })
        TimerStepperRow(label = "Deep Code", minutes = appSettings.deepCodeMinutes, accentColor = gradient.accentColor,
            onDecrease = { onUpdateTimerDurations(appSettings.pomodoroMinutes, (appSettings.deepCodeMinutes - 5).coerceAtLeast(20), appSettings.shortBreakMinutes) },
            onIncrease = { onUpdateTimerDurations(appSettings.pomodoroMinutes, (appSettings.deepCodeMinutes + 5).coerceAtMost(120), appSettings.shortBreakMinutes) })
        TimerStepperRow(label = "Pause courte", minutes = appSettings.shortBreakMinutes, accentColor = gradient.accentColor,
            onDecrease = { onUpdateTimerDurations(appSettings.pomodoroMinutes, appSettings.deepCodeMinutes, (appSettings.shortBreakMinutes - 1).coerceAtLeast(3)) },
            onIncrease = { onUpdateTimerDurations(appSettings.pomodoroMinutes, appSettings.deepCodeMinutes, (appSettings.shortBreakMinutes + 1).coerceAtMost(30)) })
    }
}

@Composable
private fun TimerStepperRow(label: String, minutes: Int, accentColor: Color, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(onClick = onDecrease, shape = CircleShape, color = MaterialTheme.colorScheme.surface, border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.size(34.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Remove, contentDescription = "Diminuer $label", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp)) } }
                Text("${minutes}m", fontFamily = JetBrainsMonoFamily, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentColor)
                Surface(onClick = onIncrease, shape = CircleShape, color = accentColor, modifier = Modifier.size(34.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, contentDescription = "Augmenter $label", tint = Color.White, modifier = Modifier.size(16.dp)) } }
            }
        }
    }
}

@Composable
internal fun CoursesPage(
    courses: List<Course>,
    onDeleteCourse: (String) -> Unit,
    onAddCourse: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Matières", onBack)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${courses.size} matière(s) active(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddCourse, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor), modifier = Modifier.testTag("courses_add_button")) { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Ajouter") }
        }
        if (courses.isEmpty()) {
            Text("Aucune matière enregistrée.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            courses.forEach { course ->
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = RoundedCornerShape(10.dp), color = catalogIconColor(course.colorIndex).copy(alpha = 0.14f), modifier = Modifier.size(36.dp)) { Box(contentAlignment = Alignment.Center) { CatalogIcon(course.iconKey, null, catalogIconColor(course.colorIndex), Modifier.size(18.dp)) } }
                        Column(Modifier.weight(1f)) { Text("${course.code} · ${course.title}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(course.professor.ifBlank { "Module actif" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                        IconButton(onClick = { onDeleteCourse(course.id) }, modifier = Modifier.testTag("course_actions_${course.id}")) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "Supprimer ${course.title}", tint = Color(0xFFC62828)) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddCoursePage(
    courses: List<Course>,
    onCreateCourse: (String, String, String, Long, Int, Double, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    var code by remember { mutableStateOf("GL-307") }
    var title by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var targetHours by remember { mutableStateOf("20") }
    var iconKey by remember { mutableStateOf("book") }
    var colorIndex by remember { mutableLongStateOf((courses.size % 7).toLong()) }
    var showIconPicker by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsPageHeader("Nouvelle matière", onBack)
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code (ex: GL-307)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Intitulé du module") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = professor, onValueChange = { professor = it }, label = { Text("Professeur") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = targetHours, onValueChange = { targetHours = it.filter { ch -> ch.isDigit() } }, label = { Text("Objectif d'heures") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = catalogIconColor(colorIndex).copy(alpha = 0.14f), modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { CatalogIcon(iconKey, null, catalogIconColor(colorIndex), Modifier.size(22.dp)) } }
            OutlinedButton(onClick = { showIconPicker = true }) { Text("Choisir une icône") }
        }
        Text("Couleur", style = MaterialTheme.typography.labelMedium)
        CatalogColorPicker(selectedIndex = colorIndex, onColorSelected = { colorIndex = it }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
            TextButton(onClick = onBack) { Text("Annuler") }
            Button(onClick = {
                if (title.isNotBlank() && code.isNotBlank()) {
                    onCreateCourse(code, title, professor, colorIndex, targetHours.toIntOrNull() ?: 20, 15.0, iconKey)
                    onBack()
                }
            }, enabled = title.isNotBlank() && code.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor)) { Text("Ajouter", color = Color.White) }
        }
        if (showIconPicker) {
            CatalogIconPickerSheet(selectedIconKey = iconKey, selectedColor = catalogIconColor(colorIndex), onIconSelected = { iconKey = it }, onDismiss = { showIconPicker = false })
        }
    }
}

@Composable
internal fun FocusUniversePage(
    appSettings: StudyAppSettings,
    audioState: FocusAudioUiState,
    onSelectIllustration: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectAudioTrack: (com.example.audio.FocusAudioTrack) -> Unit,
    onToggleAudioPlayPause: () -> Unit,
    onAudioVolumeChange: (Float) -> Unit,
    onToggleAutoPlayAudio: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusTheme = focusThemeFor(appSettings.focusThemeId)
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Univers & audio Focus", onBack)
        FocusVisualCustomizationPicker(selectedIllustrationId = appSettings.focusIllustrationId, selectedThemeId = appSettings.focusThemeId, onSelectIllustration = onSelectIllustration, onSelectTheme = onSelectTheme, focusTheme = focusTheme)
        FocusAudioCard(audioState = audioState, onSelectTrack = onSelectAudioTrack, onTogglePlayPause = onToggleAudioPlayPause, onVolumeChange = onAudioVolumeChange, onToggleAutoPlay = onToggleAutoPlayAudio, accentColor = focusTheme.accentColor, softContainerColor = focusTheme.containerColor)
    }
}

@Composable
internal fun ThemesPage(
    selectedThemeId: String,
    onSelectTheme: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeRows = remember { LightColorThemes.all.chunked(2) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsPageHeader("Palettes classiques", onBack)
        themeRows.forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { preset ->
                    val selected = selectedThemeId == preset.id
                    Surface(onClick = { onSelectTheme(preset.id) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 2.dp else 0.8.dp, if (selected) preset.primary else MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.weight(1f).testTag("theme_preset_${preset.id}")) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = preset.primary, modifier = Modifier.size(18.dp)) {}
                            Column(Modifier.weight(1f)) { Text(preset.name, style = MaterialTheme.typography.titleSmall, maxLines = 1); Text(preset.audience, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun GradientsPage(
    selectedGradientId: String,
    selectedThemeId: String,
    onSelectGradient: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsPageHeader("Studio dégradés", onBack)
        LightGradients.all.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { preset ->
                    GradientSwatchCard(preset = preset, isSelected = selectedThemeId == "gradient" && preset.id == selectedGradientId, onClick = { onSelectTheme("gradient"); onSelectGradient(preset.id) }, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
