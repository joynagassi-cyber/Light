package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.firebase.configureAppCheck
import com.example.data.model.UiState
import com.example.data.network.NetworkMonitor
import com.example.data.preferences.AgentThreadsStore
import com.example.data.preferences.StudyAppSettings
import com.example.data.preferences.ThemePreferencesStore
import com.example.data.repository.AiPlannerRepository
import com.example.data.repository.LocalTestStudyRepository
import com.example.data.repository.StudyRepository
import com.example.data.repository.StudyWorkspaceRepository
import com.example.audio.FocusAudioTrack
import com.example.ui.FocusKind
import com.example.ui.StudyViewModel
import com.example.ui.components.AgentChatTopBar
import com.example.ui.components.AgentThreadsDrawerSheet
import com.example.ui.components.GradientPickerSheet
import com.example.ui.components.OfflineBanner
import com.example.ui.theme.GradientScreenSurface
import com.example.ui.screens.AgentChatScreen
import com.example.ui.screens.AgentCapabilitiesScreen
import com.example.ui.screens.NavigationSettingsScreen
import com.example.ui.screens.SettingsPageHeader
import com.example.ui.screens.WorkspaceProfilePage
import com.example.ui.screens.navigationIcon
import com.example.ui.theme.LightSpacing
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CountdownScreen
import com.example.ui.screens.EisenhowerScreen
import com.example.ui.screens.FocusAnalyticsScreen
import com.example.ui.screens.FocusProfilesScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.ImmersiveFocusSession
import com.example.ui.screens.GanttScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.MoreSubPage
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TaskCreationScreen
import com.example.ui.theme.LightThemeProvider
import com.example.ui.theme.LocalLightGradient
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private enum class LightTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home),
    PLANNER("planner", R.string.nav_planner, Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    FOCUS("focus", R.string.nav_focus, Icons.Rounded.Timer, Icons.Outlined.Timer),
    MATRIX("matrix", R.string.nav_matrix, Icons.Rounded.GridView, Icons.Outlined.GridView),
    MORE("more", R.string.nav_more, Icons.Rounded.Explore, Icons.Rounded.Explore)
}

private enum class SettingsPage { NAVIGATION, CAPABILITIES, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // App Check MUST be initialized with launch intent before setContent or ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            LightAppRoot()
        }
    }
}

@Composable
private fun LightAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeStore = remember(context) { ThemePreferencesStore(context.applicationContext) }
    val localGradientId by themeStore.selectedGradientId.collectAsStateWithLifecycle(initialValue = "aurore_lavande")
    val themeSettings by themeStore.appSettings.collectAsStateWithLifecycle(initialValue = StudyAppSettings())
    val googleAuthPaused by themeStore.googleAuthPausedForTesting.collectAsStateWithLifecycle(initialValue = null)
    val localTestRepository = remember(context) { LocalTestStudyRepository(context.applicationContext) }
    val networkMonitor = remember(context) { NetworkMonitor(context.applicationContext) }
    DisposableEffect(networkMonitor) {
        networkMonitor.start()
        onDispose { networkMonitor.stop() }
    }
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

    // Local mode is the default. Avoid touching Firebase Auth until the user explicitly opts in.
    // This lets the app launch and be explored even without google-services.json or OAuth setup.
    val auth = remember(googleAuthPaused) {
        if (googleAuthPaused == false) {
            runCatching {
                val app = FirebaseApp.getApps(context).firstOrNull()
                    ?: FirebaseApp.initializeApp(context)
                app?.let { FirebaseAuth.getInstance(it) }
            }.getOrNull()
        } else {
            null
        }
    }
    val authState = remember(auth) { auth?.authStateFlow() ?: flowOf(auth?.currentUser) }
    val currentUser by authState.collectAsStateWithLifecycle(initialValue = auth?.currentUser)
    var startupChecked by remember { mutableStateOf(false) }

    LaunchedEffect(googleAuthPaused, currentUser, auth) {
        val paused = googleAuthPaused ?: return@LaunchedEffect
        when {
            paused -> startupChecked = true
            auth == null -> startupChecked = true
            currentUser != null -> startupChecked = true
            else -> {
                val credentialManager = CredentialManager.create(context)
                attemptAutoSignIn(
                    context = context,
                    credentialManager = credentialManager,
                    onAuthSuccess = { startupChecked = true },
                    onUnauthenticated = { startupChecked = true },
                    scope = scope
                )
            }
        }
    }

    val user = currentUser
    val paused = googleAuthPaused
    if (paused == null || !startupChecked) {
        LightThemeProvider(
            selectedGradientId = localGradientId,
            selectedThemeId = themeSettings.selectedThemeId,
            appearanceModeKey = themeSettings.appearanceMode,
            glassEnabled = themeSettings.glassEnabled,
            wrapWithGradientSurface = true
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LocalLightGradient.current.accentColor)
            }
        }
    } else if (user != null && auth != null) {
        val activeAuth = auth
        val cloudRepository = remember(context, user.uid, activeAuth) {
            val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId).also { firestore ->
                runCatching {
                    firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                        .build()
                }
            }
            StudyRepository(db, activeAuth)
        }
        LaunchedEffect(user.uid, isOnline, cloudRepository) {
            if (isOnline) {
                runCatching {
                    localTestRepository.syncToCloudIfNeeded(
                        cloudRepo = cloudRepository,
                        cloudUserId = user.uid,
                        displayName = user.displayName.orEmpty(),
                        email = user.email.orEmpty()
                    )
                }
            }
        }
        key(user.uid) {
            AuthenticatedStudyShell(
                userId = user.uid,
                initialDisplayName = user.displayName ?: "Étudiante",
                initialEmail = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                isGoogleAuthPaused = false,
                isOnlineFlow = networkMonitor.isOnline,
                themeStore = themeStore,
                repositoryFactory = { cloudRepository },
                onTogglePauseGoogleAuth = { pause ->
                    scope.launch {
                        // Persist the local-mode choice before Firebase emits its signed-out event,
                        // so the auth observer cannot immediately trigger another Google prompt.
                        themeStore.updateGoogleAuthPausedForTesting(pause)
                        if (pause) {
                            val cm = CredentialManager.create(context)
                            signOut(credentialManager = cm, scope = scope)
                        }
                    }
                }
            )
        }
    } else if (paused) {
        key("local_test_mode") {
            AuthenticatedStudyShell(
                userId = "local_test_user",
                initialDisplayName = "Mode Test",
                initialEmail = "mode.test@local.light",
                photoUrl = null,
                isGoogleAuthPaused = true,
                isOnlineFlow = networkMonitor.isOnline,
                themeStore = themeStore,
                repositoryFactory = { localTestRepository },
                onTogglePauseGoogleAuth = { pause ->
                    scope.launch {
                        themeStore.updateGoogleAuthPausedForTesting(pause)
                    }
                }
            )
        }
    } else {
        LightThemeProvider(
            selectedGradientId = localGradientId,
            selectedThemeId = themeSettings.selectedThemeId,
            appearanceModeKey = themeSettings.appearanceMode,
            glassEnabled = themeSettings.glassEnabled
        ) {
            AuthScreen(
                onAuthSuccess = { startupChecked = true },
                onEnterTestMode = {
                    scope.launch {
                        themeStore.updateGoogleAuthPausedForTesting(true)
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedStudyShell(
    userId: String,
    initialDisplayName: String,
    initialEmail: String,
    photoUrl: String?,
    isGoogleAuthPaused: Boolean,
    isOnlineFlow: StateFlow<Boolean>,
    themeStore: ThemePreferencesStore,
    repositoryFactory: (android.app.Application) -> StudyWorkspaceRepository,
    onTogglePauseGoogleAuth: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: StudyViewModel = viewModel(
        key = "${userId}_${if (isGoogleAuthPaused) "test" else "cloud"}",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                StudyViewModel(
                    repository = repositoryFactory(app),
                    aiPlannerRepository = AiPlannerRepository(),
                    themePreferencesStore = themeStore,
                    currentUserId = userId,
                    initialDisplayName = initialDisplayName,
                    initialEmail = initialEmail,
                    isOnline = isOnlineFlow,
                    isLocalMode = isGoogleAuthPaused,
                    agentThreadsStore = AgentThreadsStore(app, userId)
                )
            }
        }
    )

    val selectedGradientId by viewModel.selectedGradientId.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val coursesState by viewModel.coursesState.collectAsStateWithLifecycle()
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val projectsState by viewModel.projectsState.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()
    val focusProfilesState by viewModel.focusProfilesState.collectAsStateWithLifecycle()
    val habitsState by viewModel.habitsState.collectAsStateWithLifecycle()
    val countdownsState by viewModel.countdownsState.collectAsStateWithLifecycle()
    val selectedDateIso by viewModel.selectedDateIso.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val navigationLayout by viewModel.navigationLayout.collectAsStateWithLifecycle()
    val featureTabs = LightTab.entries.filterNot { it == LightTab.MORE }
    val navTabByRoute = featureTabs.associateBy { it.route }
    val navTabs = navigationLayout.visible.mapNotNull(navTabByRoute::get) + LightTab.MORE
    val aiPlanState by viewModel.aiPlanState.collectAsStateWithLifecycle()
    val agentMessages by viewModel.agentMessages.collectAsStateWithLifecycle()
    val agentThreads by viewModel.agentThreads.collectAsStateWithLifecycle()
    val activeAgentThreadId by viewModel.activeAgentThreadId.collectAsStateWithLifecycle()
    val agentBusy by viewModel.agentBusy.collectAsStateWithLifecycle()
    val agentProgress by viewModel.agentProgress.collectAsStateWithLifecycle()
    val pendingAgentApproval by viewModel.pendingAgentApproval.collectAsStateWithLifecycle()
    val agentApiKeyConfigured by viewModel.agentApiKeyConfigured.collectAsStateWithLifecycle()
    val agentModels by viewModel.agentModels.collectAsStateWithLifecycle()
    val agentModelsLoading by viewModel.agentModelsLoading.collectAsStateWithLifecycle()
    val agentModelsError by viewModel.agentModelsError.collectAsStateWithLifecycle()
    val agentUsage by viewModel.agentUsage.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isCreatingProfile by viewModel.isCreatingProfile.collectAsStateWithLifecycle()
    val profileInitError by viewModel.profileInitError.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data
    val profileErrorMessage = (profileState as? UiState.Error)?.message
        ?: profileInitError?.takeIf { profile == null }
    val courses = (coursesState as? UiState.Success)?.data.orEmpty()
    val tasks = (tasksState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val sessions = (sessionsState as? UiState.Success)?.data.orEmpty()
    val focusProfiles = (focusProfilesState as? UiState.Success)?.data.orEmpty()
    val habits = (habitsState as? UiState.Success)?.data.orEmpty()
    val countdowns = (countdownsState as? UiState.Success)?.data.orEmpty()

    var currentTab by rememberSaveable { mutableStateOf(LightTab.HOME) }
    var activeMorePage by rememberSaveable { mutableStateOf<MoreSubPage?>(null) }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var focusProfileCreateTrigger by remember { mutableIntStateOf(0) }
    var initialFocusProfileDetailId by rememberSaveable { mutableStateOf("") }
    var habitCreateTrigger by remember { mutableIntStateOf(0) }
    var countdownCreateTrigger by remember { mutableIntStateOf(0) }
    var ganttCreateTrigger by remember { mutableIntStateOf(0) }
    var agentConfigTrigger by remember { mutableIntStateOf(0) }
    val agentDrawerState = rememberDrawerState(DrawerValue.Closed)
    var pendingTaskQuadrant by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingProjectId by rememberSaveable { mutableStateOf("") }
    var pendingFocusProfileId by rememberSaveable { mutableStateOf("") }
    var showGradientSheet by remember { mutableStateOf(false) }
    var showTaskCreation by rememberSaveable { mutableStateOf(false) }
    var showSettingsOverlay by rememberSaveable { mutableStateOf(false) }
    var settingsPage by rememberSaveable { mutableStateOf<SettingsPage?>(null) }
    var profileOpenedFromAvatar by rememberSaveable { mutableStateOf(false) }
    val backFromSettingsPage: () -> Unit = {
        if (settingsPage == SettingsPage.PROFILE && profileOpenedFromAvatar) showSettingsOverlay = false
        profileOpenedFromAvatar = false
        settingsPage = null
    }
    var showOnboardingOverlay by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val startFocusForTask: (com.example.data.model.StudyTask) -> Unit = { task ->
        val matchedFp = focusProfiles.firstOrNull { it.id == task.focusProfileId }
        viewModel.setFocusTarget(
            courseId = task.courseId,
            taskTitle = task.title,
            taskId = task.id,
            focusProfileId = matchedFp?.id ?: task.focusProfileId,
            focusProfileTitle = matchedFp?.title ?: task.focusProfileTitle
        )
        currentTab = LightTab.FOCUS
    }

    val handleSelectAudioTrack: (FocusAudioTrack) -> Unit = { track ->
        val wasSameAndPlaying = audioState.selectedTrack.id == track.id && audioState.isPlaying
        viewModel.selectAudioTrack(track.id)
        if (!audioState.isPlaying || wasSameAndPlaying) {
            viewModel.toggleAudioPlayback()
        }
    }

    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    val isProfileError = profileErrorMessage != null
    val isInitializingProfile = !isProfileError && (profileState is UiState.Loading || profile == null)
    val needsOnboarding = !isInitializingProfile && !isProfileError && ((profile != null && !profile.onboardingCompleted) || showOnboardingOverlay)
    var immersiveDismissed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(focusState.isRunning) {
        if (!focusState.isRunning) immersiveDismissed = false
    }
    val showImmersiveFocus = currentTab == LightTab.FOCUS && focusState.isRunning && !immersiveDismissed &&
        !showSettingsOverlay && !showTaskCreation && !needsOnboarding && !isInitializingProfile && !isProfileError
    val isOnAgentChat = !needsOnboarding && !isInitializingProfile && !isProfileError &&
        currentTab == LightTab.MORE && activeMorePage == MoreSubPage.AGENT &&
        !showSettingsOverlay && !showTaskCreation

    if (!needsOnboarding && !isInitializingProfile && !isProfileError) {
        if (showImmersiveFocus) {
            BackHandler { immersiveDismissed = true }
        } else if (isOnAgentChat && agentDrawerState.isOpen) {
            BackHandler {
                scope.launch { agentDrawerState.close() }
            }
        } else if (showSettingsOverlay && !showTaskCreation) {
            BackHandler {
                if (settingsPage != null) backFromSettingsPage() else showSettingsOverlay = false
            }
        } else if (currentTab == LightTab.MORE && activeMorePage != null && !showTaskCreation) {
            BackHandler {
                initialFocusProfileDetailId = ""
                activeMorePage = null
            }
        } else if (currentTab != LightTab.HOME && !showTaskCreation) {
            BackHandler {
                currentTab = LightTab.HOME
            }
        }
    }

    LightThemeProvider(
        selectedGradientId = selectedGradientId,
        selectedThemeId = appSettings.selectedThemeId,
        appearanceModeKey = appSettings.appearanceMode,
        glassEnabled = appSettings.glassEnabled,
        onSelectGradient = viewModel::selectGradient,
        wrapWithGradientSurface = !needsOnboarding
    ) {
        val gradient = LocalLightGradient.current

        if (isInitializingProfile) {
            ProfileInitializingScreen(accentColor = gradient.accentColor, isCreating = isCreatingProfile)
        } else if (isProfileError) {
            ProfileErrorScreen(
                message = profileErrorMessage ?: "Impossible de charger le profil.",
                onRetry = viewModel::retryProfileInit,
                accentColor = gradient.accentColor,
                isOnline = isOnline,
                isCreating = isCreatingProfile
            )
        } else if (needsOnboarding) {
            OnboardingScreen(
                initialProfile = profile,
                appSettings = appSettings,
                selectedGradientId = selectedGradientId,
                canSkipOrDismiss = profile?.onboardingCompleted == true,
                onPreviewGradient = viewModel::selectGradient,
                onFinishOnboarding = { name, gender, birthYear, age, profession, customProf, major, gradId, dailyGoal, notifEnabled, notifFreq, haptics, autoAudio, habitsList, adaptModules ->
                    viewModel.completeOnboarding(
                        context = context,
                        displayName = name,
                        gender = gender,
                        birthYear = birthYear,
                        age = age,
                        profession = profession,
                        customProfession = customProf,
                        major = major,
                        selectedGradientId = gradId,
                        dailyGoalTasks = dailyGoal,
                        notificationsEnabled = notifEnabled,
                        notificationFrequency = notifFreq,
                        hapticsEnabled = haptics,
                        autoPlayAudioWithFocus = autoAudio,
                        selectedHabits = habitsList,
                        adaptModulesToProfession = adaptModules,
                        onComplete = { showOnboardingOverlay = false }
                    )
                },
                onSkip = { showOnboardingOverlay = false }
            )
        } else if (showImmersiveFocus) {
            ImmersiveFocusSession(focusState, onExit = { immersiveDismissed = true })
        } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 700.dp

            val overflowPages = listOf(
                MoreSubPage.AGENT to Icons.Rounded.AutoAwesome,
                MoreSubPage.FOCUS_PROFILES to Icons.Rounded.School,
                MoreSubPage.FOCUS_ANALYTICS to Icons.Rounded.Insights,
                MoreSubPage.PROGRESS to Icons.Rounded.BarChart,
                MoreSubPage.HABITS to Icons.Rounded.SelfImprovement,
                MoreSubPage.COUNTDOWN to Icons.Rounded.Event,
                MoreSubPage.SEARCH to Icons.Rounded.Search,
                MoreSubPage.PROJECTS to Icons.Rounded.DateRange
            )
            val pendingCount = tasks.count { !it.completed }

            val showContextFab = !showSettingsOverlay && !showTaskCreation && when {
                currentTab == LightTab.MORE -> activeMorePage in setOf(
                    MoreSubPage.FOCUS_PROFILES,
                    MoreSubPage.FOCUS_ANALYTICS,
                    MoreSubPage.PROGRESS,
                    MoreSubPage.HABITS,
                    MoreSubPage.COUNTDOWN,
                    MoreSubPage.SEARCH,
                    MoreSubPage.PROJECTS
                )
                else -> true
            }

            @Composable
            fun MoreOverflowDropdown(expanded: Boolean, onDismiss: () -> Unit) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onDismiss,
                    modifier = Modifier.testTag("more_overflow_menu")
                ) {
                    featureTabs.filter { it.route in navigationLayout.hidden }.forEach { tab ->
                        DropdownMenuItem(
                            text = { Text(stringResource(tab.labelRes)) },
                            leadingIcon = { Icon(navigationIcon(navigationLayout.icons[tab.route]) ?: tab.unselectedIcon, null) },
                            onClick = { onDismiss(); currentTab = tab },
                            modifier = Modifier.testTag("more_hidden_${tab.route}"),
                        )
                    }
                    overflowPages.forEach { (page, icon) ->
                        val badgeText = when (page) {
                            MoreSubPage.AGENT -> "IA"
                            MoreSubPage.FOCUS_PROFILES -> focusProfiles.size.takeIf { it > 0 }?.toString()
                            MoreSubPage.HABITS -> habits.size.takeIf { it > 0 }?.toString()
                            MoreSubPage.COUNTDOWN -> countdowns.size.takeIf { it > 0 }?.toString()
                            MoreSubPage.SEARCH, MoreSubPage.PROJECTS -> pendingCount.takeIf { it > 0 }?.toString()
                            else -> null
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (badgeText != null) "${page.title} · $badgeText" else page.title,
                                    fontWeight = if (currentTab == LightTab.MORE && activeMorePage == page) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == LightTab.MORE && activeMorePage == page) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (currentTab == LightTab.MORE && activeMorePage == page) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                onDismiss()
                                if (page != MoreSubPage.FOCUS_PROFILES) {
                                    initialFocusProfileDetailId = ""
                                }
                                activeMorePage = page
                                currentTab = LightTab.MORE
                            },
                            modifier = Modifier.testTag("more_menu_${page.key}")
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Toutes les pages (Explorer)") },
                        leadingIcon = { Icon(Icons.Rounded.Explore, contentDescription = null) },
                        onClick = {
                            onDismiss()
                            initialFocusProfileDetailId = ""
                            activeMorePage = null
                            currentTab = LightTab.MORE
                        },
                        modifier = Modifier.testTag("more_menu_explorer")
                    )

                }
            }

            val activeThreadTitle = remember(agentThreads, activeAgentThreadId) {
                agentThreads.firstOrNull { it.id == activeAgentThreadId }?.title ?: "Nouvelle discussion"
            }

            ModalNavigationDrawer(
                drawerState = agentDrawerState,
                gesturesEnabled = isOnAgentChat,
                drawerContent = {
                    AgentThreadsDrawerSheet(
                        threads = agentThreads,
                        activeThreadId = activeAgentThreadId,
                        onSelectThread = viewModel::selectAgentThread,
                        onNewThread = viewModel::startNewAgentThread,
                        onDeleteThread = viewModel::deleteAgentThread,
                        onCloseDrawer = { scope.launch { agentDrawerState.close() } },
                        drawerOpen = agentDrawerState.isOpen
                    )
                }
            ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (isOnAgentChat) {
                        AgentChatTopBar(
                            activeThreadTitle = activeThreadTitle,
                            onOpenDrawer = { scope.launch { agentDrawerState.open() } },
                            onClearConversation = viewModel::clearAgentConversation,
                            onOpenAgentConfig = { agentConfigTrigger++ },
                            onOpenSettings = { showSettingsOverlay = true }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                floatingActionButton = {
                    if (showContextFab) {
                        val (fabIcon, fabLabel, fabContentDesc) = when {
                            currentTab == LightTab.FOCUS -> if (focusState.isRunning) {
                                Triple(Icons.Rounded.Pause, "Pause", "Mettre en pause Focus")
                            } else {
                                Triple(Icons.Rounded.PlayArrow, "Focus", "Démarrer le minuteur Focus")
                            }
                            currentTab == LightTab.MORE && activeMorePage == MoreSubPage.FOCUS_PROFILES ->
                                Triple(Icons.Rounded.Add, "Profil", "Nouveau profil de Focus")
                            currentTab == LightTab.MORE && activeMorePage == MoreSubPage.FOCUS_ANALYTICS ->
                                Triple(Icons.Rounded.PlayArrow, "Focus", "Ouvrir le minuteur Focus")
                            currentTab == LightTab.MORE && activeMorePage == MoreSubPage.HABITS ->
                                Triple(Icons.Rounded.Add, "Habitude", "Nouvelle habitude")
                            currentTab == LightTab.MORE && activeMorePage == MoreSubPage.COUNTDOWN ->
                                Triple(Icons.Rounded.Add, "Échéance", "Nouvelle échéance")
                            currentTab == LightTab.MORE && activeMorePage == MoreSubPage.PROJECTS ->
                                Triple(Icons.Rounded.Add, "Ajouter", "Ajouter une tâche ou un projet")
                            else -> Triple(Icons.Rounded.Add, "Tâche", "Ajouter une tâche")
                        }
                        ExtendedFloatingActionButton(
                            onClick = {
                                when {
                                    currentTab == LightTab.FOCUS -> viewModel.toggleFocusTimer(courses)
                                    currentTab == LightTab.MORE && activeMorePage == MoreSubPage.FOCUS_PROFILES ->
                                        focusProfileCreateTrigger++
                                    currentTab == LightTab.MORE && activeMorePage == MoreSubPage.FOCUS_ANALYTICS ->
                                        currentTab = LightTab.FOCUS
                                    currentTab == LightTab.MORE && activeMorePage == MoreSubPage.HABITS -> habitCreateTrigger++
                                    currentTab == LightTab.MORE && activeMorePage == MoreSubPage.COUNTDOWN -> countdownCreateTrigger++
                                    currentTab == LightTab.MORE && activeMorePage == MoreSubPage.PROJECTS -> ganttCreateTrigger++
                                    currentTab == LightTab.MATRIX -> {
                                        pendingTaskQuadrant = 1
                                        pendingProjectId = ""
                                        pendingFocusProfileId = ""
                                        showTaskCreation = true
                                    }
                                    else -> {
                                        pendingTaskQuadrant = null
                                        pendingProjectId = ""
                                        pendingFocusProfileId = ""
                                        showTaskCreation = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = fabIcon, contentDescription = fabContentDesc) },
                            text = { Text(text = fabLabel, fontWeight = FontWeight.SemiBold) },
                            containerColor = gradient.accentColor,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.testTag("context_fab")
                        )
                    }
                },
                bottomBar = {
                    if (!useRail) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 6.dp
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                windowInsets = WindowInsets.navigationBars,
                                tonalElevation = 0.dp
                            ) {
                                navTabs.forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (tab == LightTab.MORE) {
                                                moreMenuExpanded = true
                                            } else {
                                                currentTab = tab
                                            }
                                        },
                                        icon = {
                                            Box {
                                                Icon(
                                                    imageVector = navigationIcon(navigationLayout.icons[tab.route]) ?: if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = stringResource(tab.labelRes),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                if (tab == LightTab.MORE) {
                                                    MoreOverflowDropdown(
                                                        expanded = moreMenuExpanded,
                                                        onDismiss = { moreMenuExpanded = false }
                                                    )
                                                }
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(tab.labelRes),
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = gradient.accentColor,
                                            selectedTextColor = gradient.accentColor,
                                            indicatorColor = gradient.softContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .then(if (isOnAgentChat) Modifier else Modifier.statusBarsPadding())
                        ) {
                            navTabs.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        if (tab == LightTab.MORE) {
                                            moreMenuExpanded = true
                                        } else {
                                            currentTab = tab
                                        }
                                    },
                                    icon = {
                                        Box {
                                            Icon(
                                                imageVector = navigationIcon(navigationLayout.icons[tab.route]) ?: if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = stringResource(tab.labelRes)
                                            )
                                            if (tab == LightTab.MORE) {
                                                MoreOverflowDropdown(
                                                    expanded = moreMenuExpanded,
                                                    onDismiss = { moreMenuExpanded = false }
                                                )
                                            }
                                        }
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = gradient.accentColor,
                                        selectedTextColor = gradient.accentColor,
                                        indicatorColor = gradient.softContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_rail_${tab.route}")
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(if (isOnAgentChat) Modifier else Modifier.statusBarsPadding()),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.98f, animationSpec = tween(240, easing = FastOutSlowInEasing)))
                                    .togetherWith(fadeOut(tween(160)))
                            },
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxSize(),
                            label = "tabTransition"
                        ) { tab ->
                            val openSettingsAction: () -> Unit = {
                                showSettingsOverlay = true
                            }

                            when (tab) {
                                LightTab.HOME -> Column(modifier = Modifier.fillMaxSize()) {
                                    OfflineBanner(isOnline = isOnline || isGoogleAuthPaused)
                                    HomeDashboardScreen(
                                    profile = profile,
                                    photoUrl = photoUrl,
                                    courses = courses,
                                    tasks = tasks,
                                    sessions = sessions,
                                    onToggleTask = viewModel::toggleTaskCompleted,
                                    onStartFocusForTask = startFocusForTask,
                                    onNavigateToPlanner = { currentTab = LightTab.PLANNER },
                                    onNavigateToProgress = {
                                        activeMorePage = MoreSubPage.PROGRESS
                                        currentTab = LightTab.MORE
                                    },
                                    onNavigateToAgent = {
                                        activeMorePage = MoreSubPage.AGENT
                                        currentTab = LightTab.MORE
                                    },
                                    onOpenStopwatch = {
                                        viewModel.setFocusKind(FocusKind.STOPWATCH)
                                        currentTab = LightTab.FOCUS
                                    },
                                    onOpenSettings = openSettingsAction,
                                    onOpenProfile = {
                                        profileOpenedFromAvatar = true
                                        settingsPage = SettingsPage.PROFILE
                                        showSettingsOverlay = true
                                    },
                                    onOpenGradientPicker = { showGradientSheet = true },
                                    onOpenCreateTask = {
                                        pendingTaskQuadrant = null
                                        pendingProjectId = ""
                                        pendingFocusProfileId = ""
                                        showTaskCreation = true
                                    }
                                    )
                                }

                                LightTab.PLANNER -> PlannerScreen(
                                    selectedDateIso = selectedDateIso,
                                    defaultCalendarViewKey = appSettings.defaultCalendarView,
                                    onSelectDate = viewModel::selectDate,
                                    courses = courses,
                                    tasks = tasks,
                                    aiPlanState = aiPlanState,
                                    onGenerateAiPlan = { goal, targetIso, dailyMins ->
                                        viewModel.generateAiStudyPlan(goal, targetIso, dailyMins, courses)
                                    },
                                    onApplyAiPlan = { plan ->
                                        viewModel.applyAiPlan(plan, courses)
                                    },
                                    onResetAiPlan = viewModel::resetAiPlan,
                                    onOpenCreateTask = {
                                        pendingTaskQuadrant = null
                                        pendingProjectId = ""
                                        pendingFocusProfileId = ""
                                        showTaskCreation = true
                                    },
                                    onOpenSettings = openSettingsAction,
                                    onOpenGantt = {
                                        activeMorePage = MoreSubPage.PROJECTS
                                        currentTab = LightTab.MORE
                                    },
                                    onToggleTask = viewModel::toggleTaskCompleted,
                                    onDeleteTask = viewModel::deleteTask,
                                    onStartFocusForTask = startFocusForTask
                                )

                                LightTab.FOCUS -> FocusScreen(
                                    focusState = focusState,
                                    audioState = audioState,
                                    appSettings = appSettings,
                                    courses = courses,
                                    sessions = sessions,
                                    focusProfiles = focusProfiles,
                                    onSelectKind = viewModel::setFocusKind,
                                    onSelectMode = viewModel::setFocusMode,
                                    onSelectCourse = { course ->
                                        viewModel.setFocusTarget(
                                            courseId = course?.id ?: "",
                                            taskTitle = course?.title ?: "Session de concentration",
                                            focusProfileId = focusState.selectedFocusProfileId,
                                            focusProfileTitle = focusState.selectedFocusProfileTitle
                                        )
                                    },
                                    onSelectFocusProfile = viewModel::selectFocusProfileForTimer,
                                    onOpenFocusProfiles = {
                                        initialFocusProfileDetailId = focusState.selectedFocusProfileId
                                        activeMorePage = MoreSubPage.FOCUS_PROFILES
                                        currentTab = LightTab.MORE
                                    },
                                    onTogglePlayPause = { viewModel.toggleFocusTimer(courses) },
                                    onRecordLap = viewModel::recordStopwatchLap,
                                    onReset = viewModel::resetFocusTimer,
                                    onFinishAndLog = { viewModel.finishAndLogFocusNow(courses) },
                                    onSelectAudioTrack = handleSelectAudioTrack,
                                    onToggleAudioPlayPause = viewModel::toggleAudioPlayback,
                                    onAudioVolumeChange = viewModel::setAudioVolume,
                                    onToggleAutoPlayAudio = viewModel::setAutoPlayAudioWithFocus,
                                    onSelectFocusIllustration = viewModel::selectFocusIllustration,
                                    onSelectFocusTheme = viewModel::selectFocusTheme,
                                    onUpdateTimerDurations = viewModel::updateTimerDurations,
                                    onUpdateAutoLogStopwatch = viewModel::updateAutoLogStopwatch,
                                    onOpenSettings = openSettingsAction
                                )

                                LightTab.MATRIX -> EisenhowerScreen(
                                    tasks = tasks,
                                    onToggleTask = viewModel::toggleTaskCompleted,
                                    onMoveTaskQuadrant = viewModel::updateTaskQuadrant,
                                    onStartFocusForTask = startFocusForTask,
                                    onOpenCreateTaskForQuadrant = { quadrant ->
                                        pendingTaskQuadrant = quadrant
                                        pendingProjectId = ""
                                        pendingFocusProfileId = ""
                                        showTaskCreation = true
                                    },
                                    onOpenSettings = openSettingsAction
                                )

                                LightTab.MORE -> {
                                    AnimatedContent(
                                        targetState = activeMorePage,
                                        transitionSpec = {
                                            (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                                                scaleIn(initialScale = 0.985f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                                .togetherWith(fadeOut(tween(140)))
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        label = "moreSubPageTransition"
                                    ) { subPage ->
                                        if (subPage == null) {
                                            MoreHubScreen(
                                                habitsCount = habits.size,
                                                countdownsCount = countdowns.size,
                                                pendingTasksCount = tasks.count { !it.completed },
                                                focusProfilesCount = focusProfiles.size,
                                                onOpenPage = { page ->
                                                    if (page != MoreSubPage.FOCUS_PROFILES) initialFocusProfileDetailId = ""
                                                    activeMorePage = page
                                                },
                                                onOpenGradientPicker = { showGradientSheet = true }
                                            )
                                        } else if (subPage == MoreSubPage.AGENT) {
                                            AgentChatScreen(
                                                messages = agentMessages,
                                                threads = agentThreads,
                                                activeThreadId = activeAgentThreadId,
                                                isBusy = agentBusy,
                                                liveProgress = agentProgress,
                                                pendingAction = pendingAgentApproval,
                                                onResolveAgentAction = viewModel::resolveAgentApproval,
                                                onSendMessage = { prompt, visibleMessage, skillKey ->
                                                    viewModel.sendAgentMessage(
                                                        prompt = prompt,
                                                        displayPrompt = visibleMessage,
                                                        skillKey = skillKey,
                                                        onStartFocusRequested = { currentTab = LightTab.FOCUS }
                                                    )
                                                },
                                                onClearConversation = viewModel::clearAgentConversation,
                                                onSelectThread = viewModel::selectAgentThread,
                                                onNewThread = viewModel::startNewAgentThread,
                                                onDeleteThread = viewModel::deleteAgentThread,
                                                onOpenSettings = openSettingsAction,
                                                settings = appSettings,
                                                apiKeyConfigured = agentApiKeyConfigured,
                                                agentModels = agentModels,
                                                agentModelsLoading = agentModelsLoading,
                                                agentModelsError = agentModelsError,
                                                agentUsage = agentUsage,
                                                onClearAgentUsage = viewModel::clearAgentUsage,
                                                onLoadAgentModels = viewModel::loadAgentModels,
                                                onSaveAgentSettings = viewModel::updateAgentPreferences,
                                                showEmbeddedNavigationChrome = false,
                                                externalConfigTrigger = agentConfigTrigger
                                            )
                                        } else {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                // Clean iOS sub-page back bar to return to indexed directory
                                                run {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Surface(
                                                            onClick = {
                                                                initialFocusProfileDetailId = ""
                                                                activeMorePage = null
                                                            },
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.surface,
                                                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                                            shadowElevation = 2.dp,
                                                            modifier = Modifier
                                                                .size(48.dp)
                                                                .testTag("more_subpage_back_button")
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                                    contentDescription = "Retour aux pages indexées",
                                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }
                                                        }
                                                        Column {
                                                            Text(
                                                                text = "Explorer",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = gradient.accentColor
                                                            )
                                                            Text(
                                                                text = subPage.title,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    when (subPage) {
                                                        MoreSubPage.AGENT -> Unit

                                                        MoreSubPage.FOCUS_PROFILES -> FocusProfilesScreen(
                                                            profiles = focusProfiles,
                                                            tasks = tasks,
                                                            sessions = sessions,
                                                            focusState = focusState,
                                                            initialSelectedProfileId = initialFocusProfileDetailId,
                                                            externalCreateTrigger = focusProfileCreateTrigger,
                                                            onCreateProfile = viewModel::createFocusProfile,
                                                            onDeleteProfile = viewModel::deleteFocusProfile,
                                                            onAssignTaskToProfile = viewModel::assignTaskToFocusProfile,
                                                            onToggleTaskCompleted = viewModel::toggleTaskCompleted,
                                                            onStartFocusForTaskInProfile = { fp, task ->
                                                                viewModel.startFocusForProfileTask(fp, task, courses)
                                                            },
                                                            onToggleTimerPlayPause = { viewModel.toggleFocusTimer(courses) },
                                                            onOpenTimerScreen = { currentTab = LightTab.FOCUS },
                                                            onOpenCreateTaskForProfile = { fp ->
                                                                pendingTaskQuadrant = null
                                                                pendingProjectId = ""
                                                                pendingFocusProfileId = fp.id
                                                                showTaskCreation = true
                                                            }
                                                        )

                                                        MoreSubPage.FOCUS_ANALYTICS -> FocusAnalyticsScreen(
                                                            sessions = sessions,
                                                            courses = courses,
                                                            onStartFocusSession = { currentTab = LightTab.FOCUS },
                                                            onOpenSettings = openSettingsAction,
                                                            focusProfiles = focusProfiles
                                                        )

                                                        MoreSubPage.PROGRESS -> ProgressScreen(
                                                            profile = profile,
                                                            courses = courses,
                                                            tasks = tasks,
                                                            sessions = sessions,
                                                            onUpdateCourseGrade = viewModel::updateCourseGrade,
                                                            onOpenSettings = openSettingsAction,
                                                            habits = habits,
                                                            focusProfiles = focusProfiles
                                                        )

                                                        MoreSubPage.HABITS -> HabitsScreen(
                                                            habits = habits,
                                                            onToggleHabitToday = viewModel::toggleHabitToday,
                                                            onCreateHabit = viewModel::createHabit,
                                                            onDeleteHabit = viewModel::deleteHabit,
                                                            externalCreateTrigger = habitCreateTrigger
                                                        )

                                                        MoreSubPage.COUNTDOWN -> CountdownScreen(
                                                            countdowns = countdowns,
                                                            onCreateCountdown = viewModel::createCountdown,
                                                            onDeleteCountdown = viewModel::deleteCountdown,
                                                            externalCreateTrigger = countdownCreateTrigger
                                                        )

                                                        MoreSubPage.SEARCH -> SearchScreen(
                                                            tasks = tasks,
                                                            courses = courses,
                                                            habits = habits,
                                                            countdowns = countdowns,
                                                            onToggleTask = viewModel::toggleTaskCompleted,
                                                            onStartFocusForTask = startFocusForTask,
                                                            onToggleHabitToday = viewModel::toggleHabitToday
                                                        )

                                                        MoreSubPage.PROJECTS -> GanttScreen(
                                                            tasks = tasks,
                                                            courses = courses,
                                                            projects = projects,
                                                            onCreateProject = viewModel::createProject,
                                                            onUpdateProject = viewModel::updateProject,
                                                            onDeleteProject = viewModel::deleteProject,
                                                            onCreateTask = { projectId ->
                                                                pendingTaskQuadrant = null
                                                                pendingProjectId = projectId
                                                                pendingFocusProfileId = ""
                                                                showTaskCreation = true
                                                            },
                                                            onToggleTask = viewModel::toggleTaskCompleted,
                                                            onDeleteTask = viewModel::deleteTask,
                                                            onMoveTask = viewModel::moveTask,
                                                            onSetPredecessors = viewModel::setTaskPredecessors,
                                                            externalCreateTrigger = ganttCreateTrigger
                                                        )


                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        if (showGradientSheet) {
            GradientPickerSheet(
                selectedGradientId = selectedGradientId,
                onSelectGradient = { id ->
                    viewModel.selectGradient(id)
                },
                onDismiss = { showGradientSheet = false }
            )
        }

        // Slide-in Settings overlay on handheld screens
        AnimatedVisibility(
            visible = showSettingsOverlay,
            enter = slideInVertically(
                animationSpec = tween(260, easing = FastOutSlowInEasing)
            ) { fullHeight -> fullHeight } + fadeIn(tween(200)),
            exit = slideOutVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ) { fullHeight -> fullHeight } + fadeOut(tween(160))
        ) {
            GradientScreenSurface {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val pageModifier = Modifier.widthIn(max = 760.dp).fillMaxSize()
                    when (settingsPage) {
                        SettingsPage.NAVIGATION -> NavigationSettingsScreen(
                            layout = navigationLayout,
                            labels = featureTabs.associate { it.route to stringResource(it.labelRes) },
                            defaultIcons = featureTabs.associate { it.route to it.selectedIcon },
                            onSave = { viewModel.saveNavigationLayout(it); backFromSettingsPage() },
                            onBack = backFromSettingsPage,
                            modifier = pageModifier,
                        )
                        SettingsPage.CAPABILITIES -> Column(pageModifier.padding(LightSpacing.two)) {
                            SettingsPageHeader("Capacités de l’agent", backFromSettingsPage)
                            AgentCapabilitiesScreen(appSettings.agentCapabilities, viewModel::updateAgentCapabilities, Modifier.weight(1f))
                        }
                        SettingsPage.PROFILE -> WorkspaceProfilePage(profile, photoUrl, viewModel::updateProfile, backFromSettingsPage, pageModifier)
                        null -> SettingsScreen(
                            profile = profile,
                            photoUrl = photoUrl,
                            selectedGradientId = selectedGradientId,
                            appSettings = appSettings,
                            courses = courses,
                            onSelectGradient = viewModel::selectGradient,
                            onUpdateProfile = viewModel::updateProfile,
                            onUpdateNotifications = { enabled, freq ->
                                viewModel.updateNotificationSettings(context, enabled, freq)
                            },
                            onReopenOnboarding = {
                                showSettingsOverlay = false
                                showOnboardingOverlay = true
                            },
                            onUpdateTimerDurations = viewModel::updateTimerDurations,
                            onUpdateDefaultCalendarView = viewModel::updateDefaultCalendarView,
                            onUpdateHapticsEnabled = viewModel::updateHapticsEnabled,
                            onUpdateAutoLogStopwatch = viewModel::updateAutoLogStopwatch,
                            onUpdateAutoPlayAudioWithFocus = viewModel::setAutoPlayAudioWithFocus,
                            audioState = audioState,
                            onSelectAudioTrack = handleSelectAudioTrack,
                            onToggleAudioPlayPause = viewModel::toggleAudioPlayback,
                            onAudioVolumeChange = viewModel::setAudioVolume,
                            onSelectFocusIllustration = viewModel::selectFocusIllustration,
                            onSelectFocusTheme = viewModel::selectFocusTheme,
                            onSelectTheme = viewModel::selectTheme,
                            onSelectAppearanceMode = viewModel::selectAppearanceMode,
                            onSetGlassEnabled = viewModel::setGlassEnabled,
                            onCreateCourse = viewModel::createCourse,
                            onDeleteCourse = viewModel::deleteCourse,
                            isGoogleAuthPaused = isGoogleAuthPaused,
                            onResumeGoogleSignIn = {
                                showSettingsOverlay = false
                                onTogglePauseGoogleAuth(false)
                            },
                            onPauseGoogleSignIn = {
                                showSettingsOverlay = false
                                onTogglePauseGoogleAuth(true)
                            },
                            onSignOut = {
                                showSettingsOverlay = false
                                scope.launch {
                                    themeStore.updateGoogleAuthPausedForTesting(true)
                                    signOut(credentialManager = credentialManager, scope = scope)
                                }
                            },
                            onOpenNavigationLayout = { settingsPage = SettingsPage.NAVIGATION },
                            onOpenCapabilities = { settingsPage = SettingsPage.CAPABILITIES },
                            onOpenProfile = { settingsPage = SettingsPage.PROFILE },
                            onClose = { showSettingsOverlay = false },
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }

        if (showTaskCreation) {
            TaskCreationScreen(
                courses = courses,
                projects = projects,
                focusProfiles = focusProfiles,
                initialDateIso = selectedDateIso,
                initialQuadrant = pendingTaskQuadrant,
                initialProjectId = pendingProjectId,
                initialFocusProfileId = pendingFocusProfileId,
                onCancel = {
                    pendingTaskQuadrant = null
                    pendingProjectId = ""
                    pendingFocusProfileId = ""
                    showTaskCreation = false
                },
                onCreateTask = { course, title, notes, startDateIso, dueDateIso, scheduledTime, priority, category, estimatedMinutes, quadrant, projectId, focusProfileId, focusProfileTitle, subtasks ->
                    viewModel.createTask(
                        course = course,
                        title = title,
                        notes = notes,
                        startDateIso = startDateIso,
                        dueDateIso = dueDateIso,
                        scheduledTime = scheduledTime,
                        priority = priority,
                        category = category,
                        estimatedMinutes = estimatedMinutes,
                        quadrant = quadrant,
                        projectId = projectId,
                        focusProfileId = focusProfileId,
                        focusProfileTitle = focusProfileTitle,
                        subtasks = subtasks
                    )
                    viewModel.selectDate(dueDateIso)
                    pendingTaskQuadrant = null
                    pendingProjectId = ""
                    pendingFocusProfileId = ""
                    showTaskCreation = false
                }
            )
        }
        }
    }
}
