package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.NotificationFrequency
import com.example.data.preferences.StudyAppSettings
import com.example.ui.FocusAudioUiState

@Composable
fun NotificationFrequencyPage(
    currentFrequency: NotificationFrequency,
    notifEnabled: Boolean,
    onUpdateNotifications: (Boolean, NotificationFrequency) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Fréquence des rappels", onBack)
        Text("Actuel: ${currentFrequency.label} — ${if (notifEnabled) "activé" else "désactivé"}")
        NotificationFrequency.entries.forEach { freq ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(freq.label, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = { onUpdateNotifications(freq != NotificationFrequency.OFF, freq); onBack() }) { Text(if (freq == currentFrequency) "✓" else "Choisir") }
            }
        }
    }
}

@Composable
fun CalendarViewPage(
    currentViewKey: String,
    onUpdateDefaultCalendarView: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Vue Calendrier", onBack)
        Text("Actuel: $currentViewKey")
        com.example.ui.components.CalendarViewMode.entries.forEach { mode ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(mode.label, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = { onUpdateDefaultCalendarView(mode.key); onBack() }) { Text(if (mode.key == currentViewKey) "✓" else "Choisir") }
            }
        }
    }
}

@Composable
fun AppearancePage(
    currentModeKey: String,
    onSelectAppearanceMode: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Apparence", onBack)
        Text("Actuel: $currentModeKey")
        com.example.ui.theme.AppearanceMode.entries.forEach { mode ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(mode.label, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = { onSelectAppearanceMode(mode.key); onBack() }) { Text(if (mode.key == currentModeKey) "✓" else "Choisir") }
            }
        }
    }
}

@Composable
fun TimerDurationsPage(
    appSettings: StudyAppSettings,
    onUpdateTimerDurations: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Durées du minuteur", onBack)
        Text("Pomodoro: ${appSettings.pomodoroMinutes} min")
        Text("Deep Code: ${appSettings.deepCodeMinutes} min")
        Text("Pause: ${appSettings.shortBreakMinutes} min")
        androidx.compose.material3.Button(onClick = { onUpdateTimerDurations(appSettings.pomodoroMinutes, appSettings.deepCodeMinutes, appSettings.shortBreakMinutes); onBack() }) { Text("Confirmer") }
    }
}

@Composable
fun CoursesPage(
    courses: List<Course>,
    onDeleteCourse: (String) -> Unit,
    onAddCourse: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Matières", onBack)
        Text("${courses.size} matière(s)")
        androidx.compose.material3.Button(onClick = onAddCourse) { Text("Ajouter") }
        courses.forEach { c ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text("${c.code} · ${c.title}", modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = { onDeleteCourse(c.id) }) { Text("Supprimer") }
            }
        }
    }
}

@Composable
fun AddCoursePage(
    courses: List<Course>,
    onCreateCourse: (String, String, String, Long, Int, Double, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Nouvelle matière", onBack)
        var code by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("GL-307") }
        var title by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
        androidx.compose.material3.OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth())
        androidx.compose.material3.OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Intitulé") }, modifier = Modifier.fillMaxWidth())
        androidx.compose.material3.Button(onClick = { if (title.isNotBlank() && code.isNotBlank()) { onCreateCourse(code, title, "", 0, 20, 15.0, "book"); onBack() } }, enabled = title.isNotBlank() && code.isNotBlank()) { Text("Ajouter") }
    }
}

@Composable
fun FocusUniversePage(
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
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Univers & audio Focus", onBack)
        Text("Illustration: ${appSettings.focusIllustrationId}")
        Text("Thème: ${appSettings.focusThemeId}")
        Text("Audio: ${audioState.selectedTrack.title}")
    }
}

@Composable
fun ThemesPage(
    selectedThemeId: String,
    onSelectTheme: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Palettes classiques", onBack)
        Text("Actuel: $selectedThemeId")
        com.example.ui.theme.LightColorThemes.all.forEach { preset ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(preset.name, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = { onSelectTheme(preset.id); onBack() }) { Text(if (preset.id == selectedThemeId) "✓" else "Choisir") }
            }
        }
    }
}

@Composable
fun GradientsPage(
    selectedGradientId: String,
    selectedThemeId: String,
    onSelectGradient: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsPageHeader("Studio dégradés", onBack)
        Text("Actuel: $selectedGradientId (${selectedThemeId})")
        com.example.ui.theme.LightGradients.all.forEach { preset ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(preset.name, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = { onSelectTheme("gradient"); onSelectGradient(preset.id); onBack() }) { Text(if (preset.id == selectedGradientId && selectedThemeId == "gradient") "✓" else "Choisir") }
            }
        }
    }
}
