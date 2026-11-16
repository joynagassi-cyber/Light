package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lightDataStore: DataStore<Preferences> by preferencesDataStore(name = "elan_prefs")

data class StudyAppSettings(
    val selectedGradientId: String = "aurore_lavande",
    val pomodoroMinutes: Int = 25,
    val deepCodeMinutes: Int = 50,
    val shortBreakMinutes: Int = 5,
    val defaultCalendarView: String = "week",
    val hapticsEnabled: Boolean = true,
    val autoLogStopwatch: Boolean = true,
    val selectedAudioTrackId: String = "lofi_chill",
    val audioVolume: Float = 0.65f,
    val autoPlayAudioWithFocus: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationFrequency: String = "daily",
    val googleAuthPausedForTesting: Boolean = true
)

class ThemePreferencesStore(private val context: Context) {
    private val gradientKey = stringPreferencesKey("selected_gradient_id")
    private val pomodoroMinutesKey = intPreferencesKey("pomodoro_minutes")
    private val deepCodeMinutesKey = intPreferencesKey("deep_code_minutes")
    private val shortBreakMinutesKey = intPreferencesKey("short_break_minutes")
    private val defaultCalendarViewKey = stringPreferencesKey("default_calendar_view")
    private val hapticsEnabledKey = booleanPreferencesKey("haptics_enabled")
    private val autoLogStopwatchKey = booleanPreferencesKey("auto_log_stopwatch")
    private val selectedAudioTrackKey = stringPreferencesKey("selected_audio_track_id")
    private val audioVolumeKey = floatPreferencesKey("audio_volume")
    private val autoPlayAudioKey = booleanPreferencesKey("auto_play_audio_with_focus")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val notificationFrequencyKey = stringPreferencesKey("notification_frequency")
    private val googleAuthPausedKey = booleanPreferencesKey("google_auth_paused_for_testing")

    val selectedGradientId: Flow<String> = context.lightDataStore.data.map { prefs ->
        prefs[gradientKey] ?: "aurore_lavande"
    }

    val googleAuthPausedForTesting: Flow<Boolean> = context.lightDataStore.data.map { prefs ->
        prefs[googleAuthPausedKey] ?: true
    }

    val appSettings: Flow<StudyAppSettings> = context.lightDataStore.data.map { prefs ->
        StudyAppSettings(
            selectedGradientId = prefs[gradientKey] ?: "aurore_lavande",
            pomodoroMinutes = (prefs[pomodoroMinutesKey] ?: 25).coerceIn(10, 90),
            deepCodeMinutes = (prefs[deepCodeMinutesKey] ?: 50).coerceIn(20, 120),
            shortBreakMinutes = (prefs[shortBreakMinutesKey] ?: 5).coerceIn(3, 30),
            defaultCalendarView = prefs[defaultCalendarViewKey] ?: "week",
            hapticsEnabled = prefs[hapticsEnabledKey] ?: true,
            autoLogStopwatch = prefs[autoLogStopwatchKey] ?: true,
            selectedAudioTrackId = prefs[selectedAudioTrackKey] ?: "lofi_chill",
            audioVolume = (prefs[audioVolumeKey] ?: 0.65f).coerceIn(0f, 1f),
            autoPlayAudioWithFocus = prefs[autoPlayAudioKey] ?: false,
            notificationsEnabled = prefs[notificationsEnabledKey] ?: true,
            notificationFrequency = prefs[notificationFrequencyKey] ?: "daily",
            googleAuthPausedForTesting = prefs[googleAuthPausedKey] ?: true
        )
    }

    suspend fun saveSelectedGradient(gradientId: String) {
        context.lightDataStore.edit { prefs ->
            prefs[gradientKey] = gradientId
        }
    }

    suspend fun updateTimerDurations(pomodoro: Int, deepCode: Int, shortBreak: Int) {
        context.lightDataStore.edit { prefs ->
            prefs[pomodoroMinutesKey] = pomodoro.coerceIn(10, 90)
            prefs[deepCodeMinutesKey] = deepCode.coerceIn(20, 120)
            prefs[shortBreakMinutesKey] = shortBreak.coerceIn(3, 30)
        }
    }

    suspend fun updateDefaultCalendarView(viewKey: String) {
        context.lightDataStore.edit { prefs ->
            prefs[defaultCalendarViewKey] = viewKey
        }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.lightDataStore.edit { prefs ->
            prefs[hapticsEnabledKey] = enabled
        }
    }

    suspend fun updateAutoLogStopwatch(enabled: Boolean) {
        context.lightDataStore.edit { prefs ->
            prefs[autoLogStopwatchKey] = enabled
        }
    }

    suspend fun updateSelectedAudioTrack(trackId: String) {
        context.lightDataStore.edit { prefs ->
            prefs[selectedAudioTrackKey] = trackId
        }
    }

    suspend fun updateAudioVolume(volume: Float) {
        context.lightDataStore.edit { prefs ->
            prefs[audioVolumeKey] = volume.coerceIn(0f, 1f)
        }
    }

    suspend fun updateAutoPlayAudioWithFocus(enabled: Boolean) {
        context.lightDataStore.edit { prefs ->
            prefs[autoPlayAudioKey] = enabled
        }
    }

    suspend fun updateNotificationPreferences(enabled: Boolean, frequency: String) {
        context.lightDataStore.edit { prefs ->
            prefs[notificationsEnabledKey] = enabled
            prefs[notificationFrequencyKey] = frequency
        }
    }

    suspend fun updateGoogleAuthPausedForTesting(paused: Boolean) {
        context.lightDataStore.edit { prefs ->
            prefs[googleAuthPausedKey] = paused
        }
    }
}
