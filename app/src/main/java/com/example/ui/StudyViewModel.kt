package com.example.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.FocusAudioCatalog
import com.example.audio.FocusAudioEngine
import com.example.audio.FocusAudioTrack
import com.example.data.model.AdaptiveHabitTemplate
import com.example.data.model.AiStudyPlanResult
import com.example.data.model.Course
import com.example.data.model.FocusSession
import com.example.data.model.NotificationFrequency
import com.example.data.model.StudyCountdown
import com.example.data.model.StudyHabit
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.data.model.UiState
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptation
import com.example.data.preferences.StudyAppSettings
import com.example.data.preferences.ThemePreferencesStore
import com.example.data.repository.AgentChatMessage
import com.example.data.repository.AiAgentRepository
import com.example.data.repository.AiPlannerRepository
import com.example.data.repository.StudyAgentToolExecutor
import com.example.data.repository.StudyWorkspaceRepository
import com.example.notifications.LightNotificationHelper
import com.example.ui.theme.LightGradients
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val TAG = "StudyViewModel"

enum class FocusKind(val label: String) {
    POMODORO("Pomodoro"),
    STOPWATCH("Chronomètre")
}

enum class FocusMode(val id: String, val label: String, val defaultMinutes: Int) {
    POMODORO("pomodoro", "Pomodoro", 25),
    DEEP_CODE("deep_code", "Deep Code", 50),
    SHORT_BREAK("short_break", "Pause", 5),
    STOPWATCH("stopwatch", "Chrono", 0)
}

data class FocusTimerState(
    val kind: FocusKind = FocusKind.POMODORO,
    val mode: FocusMode = FocusMode.POMODORO,
    val totalSeconds: Int = FocusMode.POMODORO.defaultMinutes * 60,
    val remainingSeconds: Int = FocusMode.POMODORO.defaultMinutes * 60,
    val elapsedSeconds: Int = 0,
    val laps: List<Int> = emptyList(),
    val isRunning: Boolean = false,
    val selectedCourseId: String = "",
    val selectedTaskTitle: String = "Session de concentration"
)

data class FocusAudioUiState(
    val selectedTrack: FocusAudioTrack = FocusAudioCatalog.tracks.first(),
    val isPlaying: Boolean = false,
    val volume: Float = 0.65f,
    val autoPlayWithTimer: Boolean = false
)

sealed interface AiPlanUiState {
    data object Idle : AiPlanUiState
    data object Loading : AiPlanUiState
    data class Success(val result: AiStudyPlanResult) : AiPlanUiState
    data class Error(val message: String) : AiPlanUiState
}

class StudyViewModel(
    private val repository: StudyWorkspaceRepository,
    private val aiPlannerRepository: AiPlannerRepository,
    private val themePreferencesStore: ThemePreferencesStore,
    private val currentUserId: String,
    initialDisplayName: String,
    initialEmail: String,
    private val aiAgentRepository: AiAgentRepository = AiAgentRepository(),
    private val focusAudioEngine: FocusAudioEngine = FocusAudioEngine()
) : ViewModel() {

    private var timerJob: Job? = null

    val appSettings: StateFlow<StudyAppSettings> = themePreferencesStore.appSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, StudyAppSettings())

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Impossible de synchroniser le profil"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val coursesState: StateFlow<UiState<List<Course>>> = repository.observeCourses(currentUserId)
        .map<List<Course>, UiState<List<Course>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing courses", root)
            emit(UiState.Error(root.message ?: "Impossible de charger les matières"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val tasksState: StateFlow<UiState<List<StudyTask>>> = repository.observeTasks(currentUserId)
        .map<List<StudyTask>, UiState<List<StudyTask>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing tasks", root)
            emit(UiState.Error(root.message ?: "Impossible de charger les tâches"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val sessionsState: StateFlow<UiState<List<FocusSession>>> = repository.observeFocusSessions(currentUserId)
        .map<List<FocusSession>, UiState<List<FocusSession>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing focus sessions", root)
            emit(UiState.Error(root.message ?: "Impossible de charger les sessions Focus"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val habitsState: StateFlow<UiState<List<StudyHabit>>> = repository.observeHabits(currentUserId)
        .map<List<StudyHabit>, UiState<List<StudyHabit>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing habits", root)
            emit(UiState.Error(root.message ?: "Impossible de charger les habitudes"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val countdownsState: StateFlow<UiState<List<StudyCountdown>>> = repository.observeCountdowns(currentUserId)
        .map<List<StudyCountdown>, UiState<List<StudyCountdown>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing countdowns", root)
            emit(UiState.Error(root.message ?: "Impossible de charger les comptes à rebours"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val selectedGradientId: StateFlow<String> = combine(
        themePreferencesStore.selectedGradientId,
        profileState
    ) { localId, profState ->
        val remoteId = (profState as? UiState.Success)?.data?.selectedGradientId
        remoteId?.takeIf { it.isNotBlank() } ?: localId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "aurore_lavande")

    private val _selectedDateIso = MutableStateFlow(LocalDate.now().toString())
    val selectedDateIso: StateFlow<String> = _selectedDateIso.asStateFlow()

    private val _focusState = MutableStateFlow(FocusTimerState())
    val focusState: StateFlow<FocusTimerState> = _focusState.asStateFlow()

    private val _aiPlanState = MutableStateFlow<AiPlanUiState>(AiPlanUiState.Idle)
    val aiPlanState: StateFlow<AiPlanUiState> = _aiPlanState.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _agentMessages = MutableStateFlow(listOf(welcomeAgentMessage()))
    val agentMessages: StateFlow<List<AgentChatMessage>> = _agentMessages.asStateFlow()

    private val _agentBusy = MutableStateFlow(false)
    val agentBusy: StateFlow<Boolean> = _agentBusy.asStateFlow()

    private val _audioPlaying = MutableStateFlow(false)
    val audioState: StateFlow<FocusAudioUiState> = combine(
        appSettings,
        _audioPlaying
    ) { settings, playing ->
        FocusAudioUiState(
            selectedTrack = FocusAudioCatalog.byId(settings.selectedAudioTrackId),
            isPlaying = playing,
            volume = settings.audioVolume,
            autoPlayWithTimer = settings.autoPlayAudioWithFocus
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FocusAudioUiState())

    init {
        viewModelScope.launch {
            try {
                repository.ensureUserProfileAndSeedIfNew(
                    userId = currentUserId,
                    displayName = initialDisplayName,
                    email = initialEmail
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize user workspace", e)
                _bannerMessage.value = "Erreur d'initialisation cloud : ${e.localizedMessage ?: "réessayez"}"
            }
        }

        // Keep idle Pomodoro duration synced with DataStore preferences
        viewModelScope.launch {
            appSettings.collect { settings ->
                focusAudioEngine.setVolume(settings.audioVolume)
                val state = _focusState.value
                if (!state.isRunning && state.kind == FocusKind.POMODORO && state.remainingSeconds == state.totalSeconds) {
                    val mins = configuredMinutesFor(state.mode, settings)
                    val secs = mins * 60
                    _focusState.value = state.copy(totalSeconds = secs, remainingSeconds = secs)
                }
            }
        }
    }

    private fun configuredMinutesFor(mode: FocusMode, settings: StudyAppSettings): Int = when (mode) {
        FocusMode.POMODORO -> settings.pomodoroMinutes
        FocusMode.DEEP_CODE -> settings.deepCodeMinutes
        FocusMode.SHORT_BREAK -> settings.shortBreakMinutes
        FocusMode.STOPWATCH -> 0
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun selectDate(dateIso: String) {
        _selectedDateIso.value = dateIso
    }

    fun selectGradient(gradientId: String) {
        viewModelScope.launch {
            themePreferencesStore.saveSelectedGradient(gradientId)
            try {
                repository.updateSelectedGradient(currentUserId, gradientId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync gradient preference", e)
            }
        }
    }

    fun updateProfile(
        displayName: String,
        major: String,
        dailyGoalTasks: Long,
        gender: String? = null,
        birthYear: Long? = null,
        age: Long? = null,
        profession: String? = null,
        customProfession: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateProfile(
                    userId = currentUserId,
                    displayName = displayName,
                    major = major,
                    dailyGoalTasks = dailyGoalTasks,
                    gender = gender,
                    birthYear = birthYear,
                    age = age,
                    profession = profession,
                    customProfession = customProfession
                )
                aiAgentRepository.resetConversation()
                _bannerMessage.value = "Profil adapté et synchronisé dans le cloud."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Échec de la mise à jour du profil."
            }
        }
    }

    fun updateNotificationSettings(
        context: Context,
        enabled: Boolean,
        frequency: NotificationFrequency
    ) {
        viewModelScope.launch {
            themePreferencesStore.updateNotificationPreferences(enabled, frequency.key)
            try {
                repository.updateNotificationSettings(currentUserId, enabled, frequency)
                val profile = (profileState.value as? UiState.Success)?.data
                if (enabled && frequency != NotificationFrequency.OFF) {
                    val adapt = profile.adaptation()
                    LightNotificationHelper.sendOnboardingConfiguredNotification(
                        context = context,
                        displayName = profile?.displayName?.ifBlank { adapt.roleLabel } ?: adapt.roleLabel,
                        gender = adapt.gender,
                        profession = adapt.profession,
                        customProfession = adapt.customProfession,
                        frequency = frequency
                    )
                    _bannerMessage.value = "Notifications activées (${frequency.label})."
                } else {
                    _bannerMessage.value = "Notifications mises en pause."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Erreur lors de la sauvegarde des notifications."
            }
        }
    }

    fun completeOnboarding(
        context: Context,
        displayName: String,
        gender: UserGender,
        birthYear: Long,
        age: Long,
        profession: UserProfession,
        customProfession: String,
        major: String,
        selectedGradientId: String,
        dailyGoalTasks: Long,
        notificationsEnabled: Boolean,
        notificationFrequency: NotificationFrequency,
        hapticsEnabled: Boolean,
        autoPlayAudioWithFocus: Boolean,
        selectedHabits: List<AdaptiveHabitTemplate>,
        adaptModulesToProfession: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                themePreferencesStore.saveSelectedGradient(selectedGradientId)
                themePreferencesStore.updateHapticsEnabled(hapticsEnabled)
                themePreferencesStore.updateAutoPlayAudioWithFocus(autoPlayAudioWithFocus)
                themePreferencesStore.updateNotificationPreferences(notificationsEnabled, notificationFrequency.key)

                val recommendedPomo = when (profession) {
                    UserProfession.ENTREPRENEUR -> 45
                    UserProfession.SELF_TAUGHT -> 35
                    else -> 25
                }
                val currentSettings = appSettings.value
                themePreferencesStore.updateTimerDurations(
                    pomodoro = recommendedPomo,
                    deepCode = currentSettings.deepCodeMinutes,
                    shortBreak = currentSettings.shortBreakMinutes
                )

                repository.saveOnboardingConfiguration(
                    userId = currentUserId,
                    displayName = displayName,
                    gender = gender,
                    birthYear = birthYear,
                    age = age,
                    profession = profession,
                    customProfession = customProfession,
                    major = major,
                    selectedGradientId = selectedGradientId,
                    dailyGoalTasks = dailyGoalTasks,
                    notificationsEnabled = notificationsEnabled,
                    notificationFrequency = notificationFrequency,
                    selectedHabits = selectedHabits,
                    adaptModulesToProfession = adaptModulesToProfession
                )

                if (notificationsEnabled && notificationFrequency != NotificationFrequency.OFF) {
                    LightNotificationHelper.sendOnboardingConfiguredNotification(
                        context = context,
                        displayName = displayName.ifBlank { profession.labelFor(gender, customProfession) },
                        gender = gender,
                        profession = profession,
                        customProfession = customProfession,
                        frequency = notificationFrequency
                    )
                }

                aiAgentRepository.resetConversation()
                val roleLabel = profession.labelFor(gender, customProfession)
                val ready = if (gender == UserGender.FEMININE) "prête" else "prêt"
                _agentMessages.value = listOf(
                    AgentChatMessage(
                        isUser = false,
                        text = "Bienvenue ${displayName.ifBlank { roleLabel }} ! Votre profil $roleLabel ($age ans) est configuré. Je suis $ready à organiser vos tâches, habitudes et sessions Focus."
                    )
                )
                _bannerMessage.value = "Light est adapté à votre profil $roleLabel !"
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Erreur lors de la sauvegarde de l'onboarding."
            }
        }
    }

    fun updateTimerDurations(pomodoro: Int, deepCode: Int, shortBreak: Int) {
        viewModelScope.launch {
            themePreferencesStore.updateTimerDurations(pomodoro, deepCode, shortBreak)
            _bannerMessage.value = "Durées du minuteur enregistrées."
        }
    }

    fun updateDefaultCalendarView(viewKey: String) {
        viewModelScope.launch {
            themePreferencesStore.updateDefaultCalendarView(viewKey)
        }
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesStore.updateHapticsEnabled(enabled)
        }
    }

    fun updateAutoLogStopwatch(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesStore.updateAutoLogStopwatch(enabled)
        }
    }

    fun selectAudioTrack(trackId: String) {
        val track = FocusAudioCatalog.byId(trackId)
        viewModelScope.launch {
            themePreferencesStore.updateSelectedAudioTrack(track.id)
        }
        if (_audioPlaying.value) {
            focusAudioEngine.start(track.id, appSettings.value.audioVolume)
        }
    }

    fun toggleAudioPlayback() {
        if (_audioPlaying.value) {
            focusAudioEngine.stop()
            _audioPlaying.value = false
        } else {
            val settings = appSettings.value
            val started = focusAudioEngine.start(settings.selectedAudioTrackId, settings.audioVolume)
            _audioPlaying.value = started
        }
    }

    fun stopAudioPlayback() {
        focusAudioEngine.stop()
        _audioPlaying.value = false
    }

    fun setAudioVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        focusAudioEngine.setVolume(clamped)
        viewModelScope.launch {
            themePreferencesStore.updateAudioVolume(clamped)
        }
    }

    fun setAutoPlayAudioWithFocus(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesStore.updateAutoPlayAudioWithFocus(enabled)
        }
    }

    fun createCourse(
        code: String,
        title: String,
        professor: String,
        colorIndex: Long,
        targetHours: Int,
        grade: Double
    ) {
        viewModelScope.launch {
            try {
                repository.createCourse(
                    userId = currentUserId,
                    code = code,
                    title = title,
                    professor = professor,
                    colorIndex = colorIndex,
                    targetMinutes = (targetHours.coerceAtLeast(1) * 60L),
                    grade = grade
                )
                _bannerMessage.value = "Matière \"$code\" ajoutée."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible d'ajouter la matière."
            }
        }
    }

    fun updateCourseGrade(courseId: String, newGrade: Double) {
        viewModelScope.launch {
            try {
                repository.updateCourseGrade(currentUserId, courseId, newGrade)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Erreur lors de la mise à jour de la note."
            }
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCourse(currentUserId, courseId)
                _bannerMessage.value = "Matière supprimée."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Suppression impossible."
            }
        }
    }

    fun createTask(
        course: Course?,
        title: String,
        notes: String,
        dueDateIso: String,
        scheduledTime: String,
        priority: String,
        category: String = TaskCategory.REVISION.key,
        estimatedMinutes: Long,
        quadrant: Long? = null
    ) {
        viewModelScope.launch {
            try {
                repository.createTask(
                    userId = currentUserId,
                    courseId = course?.id ?: "",
                    courseTitle = course?.title ?: "Génie Logiciel",
                    title = title,
                    notes = notes,
                    dueDateIso = dueDateIso,
                    scheduledTime = scheduledTime,
                    priority = priority,
                    category = category,
                    estimatedMinutes = estimatedMinutes,
                    aiGenerated = false,
                    quadrant = quadrant
                )
                _bannerMessage.value = "Tâche ajoutée et synchronisée."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible d'enregistrer la tâche."
            }
        }
    }

    fun toggleTaskCompleted(task: StudyTask) {
        viewModelScope.launch {
            try {
                repository.toggleTaskCompleted(currentUserId, task)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible de modifier la tâche."
            }
        }
    }

    fun updateTaskQuadrant(taskId: String, quadrant: Int) {
        viewModelScope.launch {
            try {
                repository.updateTaskQuadrant(currentUserId, taskId, quadrant.toLong())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible de déplacer la tâche."
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(currentUserId, taskId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Suppression impossible."
            }
        }
    }

    fun createHabit(
        title: String,
        subtitle: String,
        category: String,
        iconKey: String,
        colorIndex: Long
    ) {
        viewModelScope.launch {
            try {
                repository.createHabit(
                    userId = currentUserId,
                    title = title,
                    subtitle = subtitle,
                    category = category,
                    iconKey = iconKey,
                    colorIndex = colorIndex
                )
                _bannerMessage.value = "Habitude \"$title\" ajoutée !"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible d'ajouter l'habitude."
            }
        }
    }

    fun toggleHabitToday(habit: StudyHabit, dateIso: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            try {
                repository.toggleHabitForDate(currentUserId, habit, dateIso)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible de valider l'habitude."
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(currentUserId, habitId)
                _bannerMessage.value = "Habitude supprimée."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Suppression impossible."
            }
        }
    }

    fun createCountdown(
        title: String,
        notes: String,
        targetDateIso: String,
        category: String,
        colorIndex: Long
    ) {
        viewModelScope.launch {
            try {
                repository.createCountdown(
                    userId = currentUserId,
                    title = title,
                    notes = notes,
                    targetDateIso = targetDateIso,
                    category = category,
                    colorIndex = colorIndex
                )
                _bannerMessage.value = "Compte à rebours ajouté."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible d'ajouter le compte à rebours."
            }
        }
    }

    fun deleteCountdown(countdownId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCountdown(currentUserId, countdownId)
                _bannerMessage.value = "Compte à rebours supprimé."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Suppression impossible."
            }
        }
    }

    fun generateAiStudyPlan(
        goalPrompt: String,
        targetDateIso: String,
        dailyMinutesAvailable: Int,
        courses: List<Course>
    ) {
        if (goalPrompt.isBlank()) {
            _aiPlanState.value = AiPlanUiState.Error("Décrivez d'abord votre objectif d'étude.")
            return
        }
        viewModelScope.launch {
            _aiPlanState.value = AiPlanUiState.Loading
            val persona = (profileState.value as? UiState.Success)?.data.adaptation().aiPersonaInstruction
            try {
                val result = aiPlannerRepository.generateSmartStudyPlan(
                    goalPrompt = goalPrompt,
                    targetDateIso = targetDateIso,
                    dailyMinutesAvailable = dailyMinutesAvailable,
                    courses = courses,
                    personaInstruction = persona
                )
                _aiPlanState.value = AiPlanUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiPlanState.value = AiPlanUiState.Error(
                    e.localizedMessage ?: "La génération du plan intelligent a échoué."
                )
            }
        }
    }

    fun applyAiPlan(plan: AiStudyPlanResult, courses: List<Course>) {
        viewModelScope.launch {
            try {
                val tasks = plan.tasks.map { aiTask ->
                    val matchedCourse = courses.firstOrNull {
                        it.code.equals(aiTask.courseCode, ignoreCase = true) ||
                            it.title.contains(aiTask.courseCode, ignoreCase = true)
                    }
                    StudyTask(
                        userId = currentUserId,
                        courseId = matchedCourse?.id ?: "",
                        courseTitle = matchedCourse?.title ?: aiTask.courseCode,
                        title = aiTask.title,
                        notes = aiTask.notes,
                        dueDateIso = aiTask.dueDateIso,
                        scheduledTime = aiTask.scheduledTime,
                        priority = aiTask.priority,
                        category = aiTask.category,
                        estimatedMinutes = aiTask.estimatedMinutes.toLong(),
                        completed = false,
                        aiGenerated = true
                    )
                }
                repository.createTasksBatch(currentUserId, tasks)
                _aiPlanState.value = AiPlanUiState.Idle
                _bannerMessage.value = "${tasks.size} sessions IA ajoutées à votre planning !"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Impossible d'ajouter les tâches IA."
            }
        }
    }

    fun resetAiPlan() {
        _aiPlanState.value = AiPlanUiState.Idle
    }

    fun setFocusKind(kind: FocusKind) {
        timerJob?.cancel()
        val settings = appSettings.value
        if (kind == FocusKind.STOPWATCH) {
            _focusState.value = _focusState.value.copy(
                kind = FocusKind.STOPWATCH,
                mode = FocusMode.STOPWATCH,
                totalSeconds = 60,
                remainingSeconds = 0,
                elapsedSeconds = 0,
                laps = emptyList(),
                isRunning = false
            )
        } else {
            val targetMode = if (_focusState.value.mode == FocusMode.STOPWATCH) FocusMode.POMODORO else _focusState.value.mode
            val secs = configuredMinutesFor(targetMode, settings) * 60
            _focusState.value = _focusState.value.copy(
                kind = FocusKind.POMODORO,
                mode = targetMode,
                totalSeconds = secs,
                remainingSeconds = secs,
                elapsedSeconds = 0,
                laps = emptyList(),
                isRunning = false
            )
        }
    }

    fun setFocusMode(mode: FocusMode) {
        timerJob?.cancel()
        val settings = appSettings.value
        if (mode == FocusMode.STOPWATCH) {
            setFocusKind(FocusKind.STOPWATCH)
            return
        }
        val secs = configuredMinutesFor(mode, settings) * 60
        _focusState.value = _focusState.value.copy(
            kind = FocusKind.POMODORO,
            mode = mode,
            totalSeconds = secs,
            remainingSeconds = secs,
            elapsedSeconds = 0,
            laps = emptyList(),
            isRunning = false
        )
    }

    fun setFocusTarget(courseId: String, taskTitle: String) {
        _focusState.value = _focusState.value.copy(
            selectedCourseId = courseId,
            selectedTaskTitle = taskTitle.ifBlank { "Session de concentration" }
        )
    }

    fun recordStopwatchLap() {
        val current = _focusState.value
        if (current.kind != FocusKind.STOPWATCH || current.elapsedSeconds <= 0) return
        _focusState.value = current.copy(
            laps = current.laps + current.elapsedSeconds
        )
    }

    fun toggleFocusTimer(courses: List<Course>) {
        val current = _focusState.value
        val settings = appSettings.value
        if (current.isRunning) {
            timerJob?.cancel()
            _focusState.value = current.copy(isRunning = false)
            if (settings.autoPlayAudioWithFocus && _audioPlaying.value) {
                stopAudioPlayback()
            }
        } else {
            _focusState.value = current.copy(isRunning = true)
            if (settings.autoPlayAudioWithFocus && !_audioPlaying.value) {
                val started = focusAudioEngine.start(settings.selectedAudioTrackId, settings.audioVolume)
                _audioPlaying.value = started
            }
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                if (current.kind == FocusKind.STOPWATCH) {
                    while (_focusState.value.isRunning) {
                        delay(1000L)
                        val latest = _focusState.value
                        if (!latest.isRunning || latest.kind != FocusKind.STOPWATCH) break
                        _focusState.value = latest.copy(elapsedSeconds = latest.elapsedSeconds + 1)
                    }
                } else {
                    while (_focusState.value.isRunning && _focusState.value.remainingSeconds > 0) {
                        delay(1000L)
                        val latest = _focusState.value
                        if (!latest.isRunning || latest.kind != FocusKind.POMODORO) break
                        val next = (latest.remainingSeconds - 1).coerceAtLeast(0)
                        _focusState.value = latest.copy(remainingSeconds = next)
                        if (next == 0) {
                            _focusState.value = _focusState.value.copy(isRunning = false)
                            if (appSettings.value.autoPlayAudioWithFocus) {
                                stopAudioPlayback()
                            }
                            completeCurrentFocusSession(courses, completedFull = true)
                            break
                        }
                    }
                }
            }
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        val current = _focusState.value
        _focusState.value = current.copy(
            remainingSeconds = if (current.kind == FocusKind.POMODORO) current.totalSeconds else 0,
            elapsedSeconds = 0,
            laps = emptyList(),
            isRunning = false
        )
        if (appSettings.value.autoPlayAudioWithFocus && _audioPlaying.value) {
            stopAudioPlayback()
        }
    }

    fun finishAndLogFocusNow(courses: List<Course>) {
        timerJob?.cancel()
        if (appSettings.value.autoPlayAudioWithFocus && _audioPlaying.value) {
            stopAudioPlayback()
        }
        completeCurrentFocusSession(courses, completedFull = false)
    }

    private fun completeCurrentFocusSession(courses: List<Course>, completedFull: Boolean) {
        val state = _focusState.value
        val settings = appSettings.value
        val elapsedMinutes = when {
            state.kind == FocusKind.STOPWATCH -> {
                val mins = (state.elapsedSeconds + 29L) / 60L
                mins.coerceAtLeast(1L)
            }
            completedFull -> configuredMinutesFor(state.mode, settings).toLong().coerceAtLeast(1L)
            else -> {
                val elapsedSec = (state.totalSeconds - state.remainingSeconds).coerceAtLeast(60)
                (elapsedSec / 60L).coerceAtLeast(1L)
            }
        }
        val selectedCourse = courses.firstOrNull { it.id == state.selectedCourseId }
        viewModelScope.launch {
            try {
                repository.logFocusSession(
                    userId = currentUserId,
                    courseId = selectedCourse?.id ?: "",
                    courseTitle = selectedCourse?.title ?: "Génie Logiciel",
                    taskTitle = state.selectedTaskTitle,
                    durationMinutes = elapsedMinutes,
                    mode = state.mode.id
                )
                val secs = configuredMinutesFor(state.mode, settings) * 60
                _focusState.value = state.copy(
                    remainingSeconds = if (state.kind == FocusKind.POMODORO) secs else 0,
                    totalSeconds = if (state.kind == FocusKind.POMODORO) secs else 60,
                    elapsedSeconds = 0,
                    laps = emptyList(),
                    isRunning = false
                )
                val modeLabel = if (state.kind == FocusKind.STOPWATCH) "Chronomètre" else state.mode.label
                _bannerMessage.value = "Bravo ! Session $modeLabel de ${elapsedMinutes} min enregistrée."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bannerMessage.value = e.localizedMessage ?: "Erreur lors de l'enregistrement de la session."
            }
        }
    }

    fun sendAgentMessage(prompt: String, onStartFocusRequested: () -> Unit) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _agentBusy.value) return

        val userMsg = AgentChatMessage(isUser = true, text = trimmed)
        _agentMessages.value = _agentMessages.value + userMsg
        _agentBusy.value = true

        viewModelScope.launch {
            val currentCourses = (coursesState.value as? UiState.Success)?.data.orEmpty()
            val currentTasks = (tasksState.value as? UiState.Success)?.data.orEmpty()
            val currentProfile = (profileState.value as? UiState.Success)?.data
            val adapt = currentProfile.adaptation()
            val courseNames = currentCourses.joinToString(", ") { "${it.code}:${it.title}" }.ifBlank { "Aucun" }
            val pendingCount = currentTasks.count { !it.completed }
            val snapshot = "${adapt.aiPersonaInstruction} Modules=[$courseNames], tâches en cours=$pendingCount, thème actif=${selectedGradientId.value}"

            val executor = object : StudyAgentToolExecutor {
                override suspend fun listTasks(filter: String, dateIso: String?): JsonObject {
                    val latestTasks = (tasksState.value as? UiState.Success)?.data ?: currentTasks
                    val filtered = latestTasks.filter { t ->
                        val statusOk = when (filter) {
                            "pending" -> !t.completed
                            "completed" -> t.completed
                            "high" -> t.priority == "high" && !t.completed
                            else -> true
                        }
                        val dateOk = dateIso == null || t.dueDateIso == dateIso
                        statusOk && dateOk
                    }
                    val arr = JsonArray(
                        filtered.take(15).map { t ->
                            JsonObject(
                                mapOf(
                                    "title" to JsonPrimitive(t.title),
                                    "course" to JsonPrimitive(t.courseTitle),
                                    "category" to JsonPrimitive(t.category),
                                    "priority" to JsonPrimitive(t.priority),
                                    "dueDateIso" to JsonPrimitive(t.dueDateIso),
                                    "scheduledTime" to JsonPrimitive(t.scheduledTime),
                                    "estimatedMinutes" to JsonPrimitive(t.estimatedMinutes),
                                    "completed" to JsonPrimitive(t.completed)
                                )
                            )
                        }
                    )
                    return JsonObject(
                        mapOf(
                            "count" to JsonPrimitive(filtered.size),
                            "tasks" to arr
                        )
                    )
                }

                override suspend fun createTask(
                    title: String,
                    courseCode: String,
                    category: String,
                    priority: String,
                    dueDateIso: String,
                    scheduledTime: String,
                    estimatedMinutes: Int,
                    notes: String
                ): JsonObject {
                    val latestCourses = (coursesState.value as? UiState.Success)?.data ?: currentCourses
                    val matched = latestCourses.firstOrNull {
                        it.code.equals(courseCode, ignoreCase = true) ||
                            it.title.contains(courseCode, ignoreCase = true)
                    }
                    val safeDate = if (Regex("""^\d{4}-\d{2}-\d{2}$""").matches(dueDateIso)) {
                        dueDateIso
                    } else {
                        LocalDate.now().toString()
                    }
                    val id = repository.createTask(
                        userId = currentUserId,
                        courseId = matched?.id ?: "",
                        courseTitle = matched?.title ?: courseCode,
                        title = title,
                        notes = notes,
                        dueDateIso = safeDate,
                        scheduledTime = scheduledTime,
                        priority = priority,
                        category = TaskCategory.sanitizeKey(category),
                        estimatedMinutes = estimatedMinutes.toLong().coerceIn(5L, 480L),
                        aiGenerated = true
                    )
                    return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "taskId" to JsonPrimitive(id),
                            "title" to JsonPrimitive(title),
                            "dueDateIso" to JsonPrimitive(safeDate)
                        )
                    )
                }

                override suspend fun completeTask(taskQuery: String): JsonObject {
                    val latestTasks = (tasksState.value as? UiState.Success)?.data ?: currentTasks
                    val match = latestTasks.firstOrNull {
                        !it.completed && it.title.contains(taskQuery, ignoreCase = true)
                    } ?: latestTasks.firstOrNull { it.title.contains(taskQuery, ignoreCase = true) }
                    if (match == null) {
                        return JsonObject(
                            mapOf(
                                "success" to JsonPrimitive(false),
                                "reason" to JsonPrimitive("Aucune tâche trouvée contenant '$taskQuery'.")
                            )
                        )
                    }
                    if (!match.completed) {
                        repository.toggleTaskCompleted(currentUserId, match)
                    }
                    return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "title" to JsonPrimitive(match.title)
                        )
                    )
                }

                override suspend fun deleteTask(taskQuery: String): JsonObject {
                    val latestTasks = (tasksState.value as? UiState.Success)?.data ?: currentTasks
                    val match = latestTasks.firstOrNull {
                        it.title.contains(taskQuery, ignoreCase = true)
                    } ?: return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(false),
                            "reason" to JsonPrimitive("Aucune tâche trouvée pour '$taskQuery'.")
                        )
                    )
                    repository.deleteTask(currentUserId, match.id)
                    return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "deletedTitle" to JsonPrimitive(match.title)
                        )
                    )
                }

                override suspend fun addCourse(
                    code: String,
                    title: String,
                    professor: String,
                    targetHours: Int
                ): JsonObject {
                    val latestCourses = (coursesState.value as? UiState.Success)?.data ?: currentCourses
                    val id = repository.createCourse(
                        userId = currentUserId,
                        code = code,
                        title = title,
                        professor = professor,
                        colorIndex = (latestCourses.size % 6).toLong(),
                        targetMinutes = (targetHours.coerceIn(1, 200) * 60L),
                        grade = 15.0
                    )
                    return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "courseId" to JsonPrimitive(id),
                            "code" to JsonPrimitive(code)
                        )
                    )
                }

                override suspend fun setGradientTheme(gradientId: String): JsonObject {
                    val preset = LightGradients.byId(gradientId)
                    selectGradient(preset.id)
                    return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "gradientName" to JsonPrimitive(preset.name),
                            "gradientId" to JsonPrimitive(preset.id)
                        )
                    )
                }

                override suspend fun startFocusTimer(mode: String, taskTitle: String?): JsonObject {
                    val focusMode = when (mode) {
                        "deep_code" -> FocusMode.DEEP_CODE
                        "short_break" -> FocusMode.SHORT_BREAK
                        "stopwatch" -> FocusMode.STOPWATCH
                        else -> FocusMode.POMODORO
                    }
                    setFocusMode(focusMode)
                    if (!taskTitle.isNullOrBlank()) {
                        setFocusTarget("", taskTitle)
                    }
                    toggleFocusTimer((coursesState.value as? UiState.Success)?.data ?: currentCourses)
                    onStartFocusRequested()
                    return JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "mode" to JsonPrimitive(focusMode.id),
                            "taskTitle" to JsonPrimitive(taskTitle ?: "Session de concentration")
                        )
                    )
                }
            }

            try {
                val result = aiAgentRepository.sendMessage(
                    userPrompt = trimmed,
                    contextSnapshot = snapshot,
                    executor = executor
                )
                _agentMessages.value = _agentMessages.value + AgentChatMessage(
                    isUser = false,
                    text = result.replyText,
                    actions = result.actions
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _agentMessages.value = _agentMessages.value + AgentChatMessage(
                    isUser = false,
                    text = e.localizedMessage ?: "Une erreur est survenue lors de l'appel à Gemini.",
                    isError = true
                )
            } finally {
                _agentBusy.value = false
            }
        }
    }

    fun clearAgentConversation() {
        aiAgentRepository.resetConversation()
        _agentMessages.value = listOf(welcomeAgentMessage())
    }

    private fun welcomeAgentMessage() = AgentChatMessage(
        isUser = false,
        text = "Bonjour ! Je suis Light Agent. Je peux consulter ou créer vos tâches, valider vos devoirs, ajouter une matière L3, changer le thème dégradé ou lancer votre minuteur Focus."
    )

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        focusAudioEngine.release()
    }
}
