package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.model.AgentCapabilities
import com.example.data.model.AgentProvider
import com.example.data.model.AgentReflection
import com.example.data.model.AgentSkill
import com.example.data.model.AgentUsageRecord
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.util.UUID
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val googleAuthPausedForTesting: Boolean = false,
    val selectedThemeId: String = "gradient",
    val appearanceMode: String = "light",
    val glassEnabled: Boolean = false,
    val focusIllustrationId: String = "student",
    val focusThemeId: String = "lunar",
    val agentProvider: String = "agnes_ai",
    val agentBaseUrl: String = "https://apihub.agnes-ai.com/v1",
    val agentModel: String = "agnes-2.5-flash",
    val agentReflection: String = "balanced",
    val agentCapabilities: Set<String> = AgentCapabilities.defaults,
    val agentSkills: List<AgentSkill> = emptyList(),
    val doclingBaseUrl: String = "",
    // Phase 4a — recherche & images: désactivés par défaut, clés locales chiffrées, opt-in explicite
    val searchProvider: String = "none",
    val searchEnabled: Boolean = false,
    val imageProvider: String = "none",
    val imageEnabled: Boolean = false,
    val imageSize: String = "1024x1024"
)

private val FOCUS_ILLUSTRATION_IDS = setOf(
    "student", "coder", "planner", "desk_writer", "book_reader", "flashcards",
    "lab_student", "music_student", "math_student", "group_study", "tablet_notes", "speech_practice"
)
private val FOCUS_THEME_IDS = setOf("lunar", "focusly_dusk", "cyberpunk_night", "aurora_borealis", "ocean", "forest", "desk", "nebula", "mountain", "premium")

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
    // Test mode removed — paused key kept for migration only, no longer used
    private val selectedThemeKey = stringPreferencesKey("selected_theme_id")
    private val appearanceModeKey = stringPreferencesKey("appearance_mode")
    private val glassEnabledKey = booleanPreferencesKey("glass_enabled")
    private val focusIllustrationKey = stringPreferencesKey("focus_illustration_id")
    private val focusThemeKey = stringPreferencesKey("focus_theme_id")
    private val agentProviderKey = stringPreferencesKey("agent_provider")
    private val agentBaseUrlKey = stringPreferencesKey("agent_base_url")
    private val agentModelKey = stringPreferencesKey("agent_model")
    private val agentReflectionKey = stringPreferencesKey("agent_reflection")
    private val agentCapabilitiesKey = stringPreferencesKey("agent_capabilities")
    private val agentSkillsKey = stringPreferencesKey("agent_skills")
    private fun agentApiKeyStore(providerId: String) = AgentApiKeyStore(context.applicationContext, "agent_${AgentProvider.fromId(providerId).id}")
    private val doclingTokenStore by lazy { AgentApiKeyStore(context.applicationContext, "docling") }
    private val doclingBaseUrlKey = stringPreferencesKey("docling_base_url")
    private val agentUsageKey = stringPreferencesKey("agent_usage_history")
    private val searchProviderKey = stringPreferencesKey("search_provider")
    private val searchEnabledKey = booleanPreferencesKey("search_enabled")
    private val imageProviderKey = stringPreferencesKey("image_provider")
    private val imageEnabledKey = booleanPreferencesKey("image_enabled")
    private val imageSizeKey = stringPreferencesKey("image_size")
    private fun searchApiKeyStore(providerId: String) = AgentApiKeyStore(context.applicationContext, "search_${providerId}")
    private fun imageApiKeyStore(providerId: String) = AgentApiKeyStore(context.applicationContext, "image_${providerId}")

    val selectedGradientId: Flow<String> = context.lightDataStore.data.map { prefs ->
        prefs[gradientKey] ?: "aurore_lavande"
    }

    @Deprecated("Test mode removed")
    val googleAuthPausedForTesting: Flow<Boolean> = context.lightDataStore.data.map { false }

    val appSettings: Flow<StudyAppSettings> = context.lightDataStore.data.map { prefs ->
        val savedAppearance = prefs[appearanceModeKey] ?: "light"
        val savedAgentProvider = AgentProvider.fromId(prefs[agentProviderKey])
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
            googleAuthPausedForTesting = false,
            selectedThemeId = prefs[selectedThemeKey] ?: "gradient",
            appearanceMode = savedAppearance.takeUnless { it == "glass" } ?: "light",
            glassEnabled = prefs[glassEnabledKey] ?: (savedAppearance == "glass"),
            focusIllustrationId = prefs[focusIllustrationKey]?.takeIf { it in FOCUS_ILLUSTRATION_IDS } ?: "student",
            focusThemeId = prefs[focusThemeKey]?.takeIf { it in FOCUS_THEME_IDS } ?: "lunar",
            agentProvider = savedAgentProvider.id,
            agentBaseUrl = prefs[agentBaseUrlKey]?.takeIf { it.isNotBlank() } ?: savedAgentProvider.baseUrl,
            agentModel = prefs[agentModelKey]?.takeIf { it.isNotBlank() } ?: savedAgentProvider.defaultModel,
            agentReflection = AgentReflection.fromKey(prefs[agentReflectionKey]).key,
            agentCapabilities = prefs[agentCapabilitiesKey]?.split(",")?.filter { key -> AgentCapabilities.all.any { it.first == key } }?.toSet()
                ?: AgentCapabilities.defaults,
            agentSkills = decodeAgentSkills(prefs[agentSkillsKey]),
            doclingBaseUrl = prefs[doclingBaseUrlKey] ?: "",
            searchProvider = prefs[searchProviderKey] ?: "none",
            searchEnabled = prefs[searchEnabledKey] ?: false,
            imageProvider = prefs[imageProviderKey] ?: "none",
            imageEnabled = prefs[imageEnabledKey] ?: false,
            imageSize = prefs[imageSizeKey] ?: "1024x1024"
        )
    }

    val agentUsage: Flow<List<AgentUsageRecord>> = context.lightDataStore.data.map { prefs ->
        decodeAgentUsage(prefs[agentUsageKey])
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

    suspend fun updateSelectedTheme(themeId: String) {
        val validThemeIds = setOf("gradient", "paper", "graphite", "ocean", "forest", "plum", "sand", "rose", "cobalt", "saffron", "mint")
        context.lightDataStore.edit { prefs ->
            prefs[selectedThemeKey] = themeId.takeIf { it in validThemeIds } ?: "gradient"
        }
    }

    suspend fun updateAppearanceMode(mode: String) {
        context.lightDataStore.edit { prefs ->
            if (glassEnabledKey !in prefs && prefs[appearanceModeKey] == "glass") {
                prefs[glassEnabledKey] = true
            }
            prefs[appearanceModeKey] = mode.takeIf { it in setOf("light", "oled") } ?: "light"
        }
    }

    suspend fun updateGlassEnabled(enabled: Boolean) {
        context.lightDataStore.edit { prefs ->
            prefs[glassEnabledKey] = enabled
        }
    }

    suspend fun updateFocusIllustration(id: String) {
        context.lightDataStore.edit { prefs ->
            prefs[focusIllustrationKey] = id.takeIf { it in FOCUS_ILLUSTRATION_IDS } ?: "student"
        }
    }

    suspend fun updateFocusTheme(id: String) {
        context.lightDataStore.edit { prefs ->
            prefs[focusThemeKey] = id.takeIf { it in FOCUS_THEME_IDS } ?: "lunar"
        }
    }

    suspend fun updateAgentCapabilities(capabilities: Set<String>) {
        context.lightDataStore.edit { prefs ->
            prefs[agentCapabilitiesKey] = capabilities
                .filter { key -> AgentCapabilities.all.any { it.first == key } }
                .distinct()
                .joinToString(",")
        }
    }

    suspend fun updateAgentPreferences(
        provider: String,
        baseUrl: String,
        model: String,
        reflection: String,
        capabilities: Set<String>,
        skills: List<AgentSkill>
    ) {
        context.lightDataStore.edit { prefs ->
            prefs[agentProviderKey] = AgentProvider.fromId(provider).id
            prefs[agentBaseUrlKey] = baseUrl.trim().take(500)
            prefs[agentModelKey] = model.trim().take(120)
            prefs[agentReflectionKey] = AgentReflection.fromKey(reflection).key
            prefs[agentCapabilitiesKey] = capabilities.filter { key -> AgentCapabilities.all.any { it.first == key } }.distinct().joinToString(",")
            prefs[agentSkillsKey] = encodeAgentSkills(skills.take(20))
        }
    }

    suspend fun saveAgentApiKey(providerId: String, apiKey: String) {
        withContext(Dispatchers.IO) { agentApiKeyStore(providerId).save(apiKey.trim()) }
    }

    suspend fun readAgentApiKey(providerId: String): String? = withContext(Dispatchers.IO) { agentApiKeyStore(providerId).read() }

    suspend fun hasAgentApiKey(providerId: String): Boolean = withContext(Dispatchers.IO) { agentApiKeyStore(providerId).read()?.isNotBlank() == true }

    suspend fun clearAgentApiKey(providerId: String) {
        withContext(Dispatchers.IO) { agentApiKeyStore(providerId).clear() }
    }

    suspend fun saveDoclingSettings(baseUrl: String, token: String? = null, clearToken: Boolean = false) {
        context.lightDataStore.edit { prefs -> prefs[doclingBaseUrlKey] = baseUrl.trim().take(500) }
        if (clearToken) withContext(Dispatchers.IO) { doclingTokenStore.clear() }
        else if (!token.isNullOrBlank()) withContext(Dispatchers.IO) { doclingTokenStore.save(token.trim()) }
    }

    suspend fun readDoclingToken(): String? = withContext(Dispatchers.IO) { doclingTokenStore.read() }
    suspend fun hasDoclingToken(): Boolean = withContext(Dispatchers.IO) { doclingTokenStore.read()?.isNotBlank() == true }

    suspend fun recordAgentUsage(record: AgentUsageRecord) {
        context.lightDataStore.edit { prefs ->
            val existing = decodeAgentUsage(prefs[agentUsageKey])
            prefs[agentUsageKey] = encodeAgentUsage((existing + record).takeLast(500))
        }
    }

    suspend fun clearAgentUsage() {
        context.lightDataStore.edit { it.remove(agentUsageKey) }
    }

    private fun encodeAgentUsage(records: List<AgentUsageRecord>): String = buildJsonArray {
        records.forEach { row ->
            add(buildJsonObject {
                put("id", row.id)
                put("providerId", row.providerId)
                put("modelId", row.modelId)
                put("createdAtMillis", row.createdAtMillis)
                put("inputTokens", row.inputTokens)
                put("outputTokens", row.outputTokens)
                put("estimatedCostUsd", row.estimatedCostUsd?.let { JsonPrimitive(it) } ?: JsonNull)
                put("costNote", row.costNote)
            })
        }
    }.toString()

    private fun decodeAgentUsage(value: String?): List<AgentUsageRecord> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching {
            Json.parseToJsonElement(value).jsonArray.mapNotNull { element ->
            val obj = element.jsonObject
            AgentUsageRecord(
                id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                providerId = obj["providerId"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                modelId = obj["modelId"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                createdAtMillis = obj["createdAtMillis"]?.jsonPrimitive?.longOrNull ?: 0L,
                inputTokens = obj["inputTokens"]?.jsonPrimitive?.longOrNull ?: 0L,
                outputTokens = obj["outputTokens"]?.jsonPrimitive?.longOrNull ?: 0L,
                estimatedCostUsd = obj["estimatedCostUsd"]?.jsonPrimitive?.doubleOrNull,
                costNote = obj["costNote"]?.jsonPrimitive?.contentOrNull.orEmpty()
            )
            }.takeLast(500)
        }.getOrDefault(emptyList())
    }

    private fun encodeAgentSkills(skills: List<AgentSkill>): String = buildJsonArray {
        skills.forEach { skill ->
            add(buildJsonObject {
                put("id", skill.id)
                put("name", skill.name)
                put("instructions", skill.instructions)
                put("enabled", skill.enabled)
            })
        }
    }.toString()

    private fun decodeAgentSkills(value: String?): List<AgentSkill> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching {
            Json.parseToJsonElement(value).jsonArray.mapNotNull { element ->
            val obj = element.jsonObject
            val name = obj["name"]?.jsonPrimitive?.contentOrNull?.take(80) ?: return@mapNotNull null
            val instructions = obj["instructions"]?.jsonPrimitive?.contentOrNull?.take(2000) ?: return@mapNotNull null
            AgentSkill(
                id = obj["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString(),
                name = name,
                instructions = instructions,
                enabled = obj["enabled"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: true
            )
            }.take(20)
        }.getOrDefault(emptyList())
    }

    @Deprecated("Test mode removed")
    suspend fun updateGoogleAuthPausedForTesting(paused: Boolean) { /* no-op */ }

    suspend fun updateSearchSettings(provider: String, enabled: Boolean) {
        context.lightDataStore.edit { prefs ->
            prefs[searchProviderKey] = provider.trim().take(20)
            prefs[searchEnabledKey] = enabled
        }
    }

    suspend fun saveSearchApiKey(providerId: String, apiKey: String) {
        withContext(Dispatchers.IO) { searchApiKeyStore(providerId).save(apiKey.trim()) }
    }

    suspend fun readSearchApiKey(providerId: String): String? = withContext(Dispatchers.IO) { searchApiKeyStore(providerId).read() }
    suspend fun hasSearchApiKey(providerId: String): Boolean = withContext(Dispatchers.IO) { searchApiKeyStore(providerId).read()?.isNotBlank() == true }
    suspend fun clearSearchApiKey(providerId: String) { withContext(Dispatchers.IO) { searchApiKeyStore(providerId).clear() } }

    suspend fun updateImageSettings(provider: String, enabled: Boolean, size: String) {
        context.lightDataStore.edit { prefs ->
            prefs[imageProviderKey] = provider.trim().take(20)
            prefs[imageEnabledKey] = enabled
            prefs[imageSizeKey] = size.trim().take(20).ifBlank { "1024x1024" }
        }
    }

    suspend fun saveImageApiKey(providerId: String, apiKey: String) {
        withContext(Dispatchers.IO) { imageApiKeyStore(providerId).save(apiKey.trim()) }
    }

    suspend fun readImageApiKey(providerId: String): String? = withContext(Dispatchers.IO) { imageApiKeyStore(providerId).read() }
    suspend fun hasImageApiKey(providerId: String): Boolean = withContext(Dispatchers.IO) { imageApiKeyStore(providerId).read()?.isNotBlank() == true }
    suspend fun clearImageApiKey(providerId: String) { withContext(Dispatchers.IO) { imageApiKeyStore(providerId).clear() } }
}
