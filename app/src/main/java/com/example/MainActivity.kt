package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.example.data.preferences.ThemePreferencesStore
import com.example.data.repository.AiPlannerRepository
import com.example.data.repository.LocalTestStudyRepository
import com.example.data.repository.StudyRepository
import com.example.data.repository.StudyWorkspaceRepository
import com.example.ui.FocusKind
import com.example.ui.StudyViewModel
import com.example.ui.components.GradientPickerSheet
import com.example.ui.theme.GradientScreenSurface
import com.example.ui.screens.AgentChatScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CountdownScreen
import com.example.ui.screens.EisenhowerScreen
import com.example.ui.screens.FocusScreen
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
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.ElanThemeProvider
import com.example.ui.theme.LocalElanGradient
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private enum class ElanTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home),
    PLANNER("planner", R.string.nav_planner, Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    FOCUS("focus", R.string.nav_focus, Icons.Rounded.Timer, Icons.Outlined.Timer),
    MATRIX("matrix", R.string.nav_matrix, Icons.Rounded.GridView, Icons.Outlined.GridView),
    MORE("more", R.string.nav_more, Icons.Rounded.MoreHoriz, Icons.Outlined.MoreHoriz)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Check MUST be initialized with launch intent before setContent or ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            ElanAppRoot()
        }
    }
}

@Composable
private fun ElanAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeStore = remember(context) { ThemePreferencesStore(context.applicationContext) }
    val localGradientId by themeStore.selectedGradientId.collectAsStateWithLifecycle(initialValue = "aurore_lavande")
    val googleAuthPaused by themeStore.googleAuthPausedForTesting.collectAsStateWithLifecycle(initialValue = null)
    val localTestRepository = remember { LocalTestStudyRepository() }

    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var startupChecked by remember { mutableStateOf(auth.currentUser != null) }

    LaunchedEffect(googleAuthPaused, currentUser) {
        val paused = googleAuthPaused ?: return@LaunchedEffect
        if (currentUser != null) {
            startupChecked = true
            if (paused) {
                themeStore.updateGoogleAuthPausedForTesting(false)
            }
        } else if (paused) {
            startupChecked = true
        } else {
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

    val user = currentUser
    val paused = googleAuthPaused
    if (paused == null || (!startupChecked && user == null && !paused)) {
        ElanThemeProvider(
            selectedGradientId = localGradientId,
            wrapWithGradientSurface = true
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LocalElanGradient.current.accentColor)
            }
        }
    } else if (user != null) {
        key(user.uid) {
            AuthenticatedStudyShell(
                userId = user.uid,
                initialDisplayName = user.displayName ?: "Étudiante",
                initialEmail = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                isGoogleAuthPaused = false,
                themeStore = themeStore,
                repositoryFactory = { app ->
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    StudyRepository(db, Firebase.auth)
                },
                onTogglePauseGoogleAuth = { pause ->
                    scope.launch {
                        val cm = CredentialManager.create(context)
                        signOut(credentialManager = cm, scope = scope) {
                            scope.launch {
                                themeStore.updateGoogleAuthPausedForTesting(pause)
                            }
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
                initialEmail = "mode.test@local.elan",
                photoUrl = null,
                isGoogleAuthPaused = true,
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
        ElanThemeProvider(selectedGradientId = localGradientId) {
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
                    initialEmail = initialEmail
                )
            }
        }
    )

    val selectedGradientId by viewModel.selectedGradientId.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val coursesState by viewModel.coursesState.collectAsStateWithLifecycle()
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()
    val habitsState by viewModel.habitsState.collectAsStateWithLifecycle()
    val countdownsState by viewModel.countdownsState.collectAsStateWithLifecycle()
    val selectedDateIso by viewModel.selectedDateIso.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val aiPlanState by viewModel.aiPlanState.collectAsStateWithLifecycle()
    val agentMessages by viewModel.agentMessages.collectAsStateWithLifecycle()
    val agentBusy by viewModel.agentBusy.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data
    val courses = (coursesState as? UiState.Success)?.data.orEmpty()
    val tasks = (tasksState as? UiState.Success)?.data.orEmpty()
    val sessions = (sessionsState as? UiState.Success)?.data.orEmpty()
    val habits = (habitsState as? UiState.Success)?.data.orEmpty()
    val countdowns = (countdownsState as? UiState.Success)?.data.orEmpty()

    var currentTab by rememberSaveable { mutableStateOf(ElanTab.HOME) }
    var activeMorePage by rememberSaveable { mutableStateOf<MoreSubPage?>(null) }
    var pendingTaskQuadrant by rememberSaveable { mutableStateOf<Int?>(null) }
    var showGradientSheet by remember { mutableStateOf(false) }
    var showTaskCreation by rememberSaveable { mutableStateOf(false) }
    var showSettingsOverlay by rememberSaveable { mutableStateOf(false) }
    var showOnboardingOverlay by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    val isLoadingInitialProfile = profileState is UiState.Loading
    val needsOnboarding = (profile != null && !profile.onboardingCompleted) || showOnboardingOverlay

    if (!needsOnboarding && !isLoadingInitialProfile) {
        if (showSettingsOverlay && !showTaskCreation) {
            BackHandler {
                showSettingsOverlay = false
            }
        } else if (currentTab == ElanTab.MORE && activeMorePage != null && !showTaskCreation) {
            BackHandler {
                activeMorePage = null
            }
        } else if (currentTab != ElanTab.HOME && !showTaskCreation) {
            BackHandler {
                currentTab = ElanTab.HOME
            }
        }
    }

    ElanThemeProvider(
        selectedGradientId = selectedGradientId,
        onSelectGradient = viewModel::selectGradient,
        wrapWithGradientSurface = !needsOnboarding
    ) {
        val gradient = LocalElanGradient.current

        if (isLoadingInitialProfile) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = gradient.accentColor)
            }
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
        } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 700.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail) {
                        Surface(
                            color = Color.White.copy(alpha = 0.88f),
                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.9f)),
                            shadowElevation = 6.dp
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                windowInsets = WindowInsets.navigationBars,
                                tonalElevation = 0.dp
                            ) {
                                ElanTab.entries.forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (tab == ElanTab.MORE && currentTab == ElanTab.MORE) {
                                                activeMorePage = null
                                            }
                                            currentTab = tab
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = stringResource(tab.labelRes),
                                                modifier = Modifier.size(22.dp)
                                            )
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
                                            unselectedIconColor = AppleTextSecondary,
                                            unselectedTextColor = AppleTextSecondary
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
                            containerColor = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                        ) {
                            ElanTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        if (tab == ElanTab.MORE && currentTab == ElanTab.MORE) {
                                            activeMorePage = null
                                        }
                                        currentTab = tab
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = gradient.accentColor,
                                        selectedTextColor = gradient.accentColor,
                                        indicatorColor = gradient.softContainer,
                                        unselectedIconColor = AppleTextSecondary,
                                        unselectedTextColor = AppleTextSecondary
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
                            .statusBarsPadding(),
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
                                ElanTab.HOME -> HomeDashboardScreen(
                                    profile = profile,
                                    photoUrl = photoUrl,
                                    courses = courses,
                                    tasks = tasks,
                                    sessions = sessions,
                                    onToggleTask = viewModel::toggleTaskCompleted,
                                    onStartFocusForTask = { task ->
                                        viewModel.setFocusTarget(task.courseId, task.title)
                                        currentTab = ElanTab.FOCUS
                                    },
                                    onNavigateToPlanner = { currentTab = ElanTab.PLANNER },
                                    onNavigateToProgress = {
                                        activeMorePage = MoreSubPage.PROGRESS
                                        currentTab = ElanTab.MORE
                                    },
                                    onNavigateToAgent = {
                                        activeMorePage = MoreSubPage.AGENT
                                        currentTab = ElanTab.MORE
                                    },
                                    onOpenStopwatch = {
                                        viewModel.setFocusKind(FocusKind.STOPWATCH)
                                        currentTab = ElanTab.FOCUS
                                    },
                                    onOpenSettings = openSettingsAction,
                                    onOpenGradientPicker = { showGradientSheet = true },
                                    onOpenCreateTask = {
                                        pendingTaskQuadrant = null
                                        showTaskCreation = true
                                    }
                                )

                                ElanTab.PLANNER -> PlannerScreen(
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
                                        showTaskCreation = true
                                    },
                                    onOpenSettings = openSettingsAction,
                                    onToggleTask = viewModel::toggleTaskCompleted,
                                    onDeleteTask = viewModel::deleteTask,
                                    onStartFocusForTask = { task ->
                                        viewModel.setFocusTarget(task.courseId, task.title)
                                        currentTab = ElanTab.FOCUS
                                    }
                                )

                                ElanTab.FOCUS -> FocusScreen(
                                    focusState = focusState,
                                    audioState = audioState,
                                    appSettings = appSettings,
                                    courses = courses,
                                    sessions = sessions,
                                    onSelectKind = viewModel::setFocusKind,
                                    onSelectMode = viewModel::setFocusMode,
                                    onSelectCourse = { course ->
                                        viewModel.setFocusTarget(
                                            courseId = course?.id ?: "",
                                            taskTitle = course?.title ?: "Session de concentration"
                                        )
                                    },
                                    onTogglePlayPause = { viewModel.toggleFocusTimer(courses) },
                                    onRecordLap = viewModel::recordStopwatchLap,
                                    onReset = viewModel::resetFocusTimer,
                                    onFinishAndLog = { viewModel.finishAndLogFocusNow(courses) },
                                    onSelectAudioTrack = { track ->
                                        val wasSameAndPlaying = audioState.selectedTrack.id == track.id && audioState.isPlaying
                                        viewModel.selectAudioTrack(track.id)
                                        if (!audioState.isPlaying) {
                                            viewModel.toggleAudioPlayback()
                                        } else if (wasSameAndPlaying) {
                                            viewModel.toggleAudioPlayback()
                                        }
                                    },
                                    onToggleAudioPlayPause = viewModel::toggleAudioPlayback,
                                    onAudioVolumeChange = viewModel::setAudioVolume,
                                    onToggleAutoPlayAudio = viewModel::setAutoPlayAudioWithFocus,
                                    onOpenSettings = openSettingsAction
                                )

                                ElanTab.MATRIX -> EisenhowerScreen(
                                    tasks = tasks,
                                    onToggleTask = viewModel::toggleTaskCompleted,
                                    onMoveTaskQuadrant = viewModel::updateTaskQuadrant,
                                    onStartFocusForTask = { task ->
                                        viewModel.setFocusTarget(task.courseId, task.title)
                                        currentTab = ElanTab.FOCUS
                                    },
                                    onOpenCreateTaskForQuadrant = { quadrant ->
                                        pendingTaskQuadrant = quadrant
                                        showTaskCreation = true
                                    },
                                    onOpenSettings = openSettingsAction
                                )

                                ElanTab.MORE -> {
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
                                                onOpenPage = { page -> activeMorePage = page },
                                                onOpenGradientPicker = { showGradientSheet = true }
                                            )
                                        } else {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                // Clean iOS sub-page back bar to return to indexed directory
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Surface(
                                                        onClick = { activeMorePage = null },
                                                        shape = CircleShape,
                                                        color = Color.White.copy(alpha = 0.92f),
                                                        border = BorderStroke(0.8.dp, AppleDivider),
                                                        shadowElevation = 2.dp,
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .testTag("more_subpage_back_button")
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                                contentDescription = "Retour aux pages indexées",
                                                                tint = AppleTextPrimary,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                    Column {
                                                        Text(
                                                            text = "Plus • Index",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = gradient.accentColor
                                                        )
                                                        Text(
                                                            text = subPage.title,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = AppleTextPrimary
                                                        )
                                                    }
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    when (subPage) {
                                                        MoreSubPage.AGENT -> AgentChatScreen(
                                                            messages = agentMessages,
                                                            isBusy = agentBusy,
                                                            onSendMessage = { prompt ->
                                                                viewModel.sendAgentMessage(
                                                                    prompt = prompt,
                                                                    onStartFocusRequested = { currentTab = ElanTab.FOCUS }
                                                                )
                                                            },
                                                            onClearConversation = viewModel::clearAgentConversation,
                                                            onOpenSettings = { activeMorePage = MoreSubPage.SETTINGS }
                                                        )

                                                        MoreSubPage.PROGRESS -> ProgressScreen(
                                                            profile = profile,
                                                            courses = courses,
                                                            tasks = tasks,
                                                            sessions = sessions,
                                                            onUpdateCourseGrade = viewModel::updateCourseGrade,
                                                            onOpenSettings = { activeMorePage = MoreSubPage.SETTINGS }
                                                        )

                                                        MoreSubPage.HABITS -> HabitsScreen(
                                                            habits = habits,
                                                            onToggleHabitToday = viewModel::toggleHabitToday,
                                                            onCreateHabit = viewModel::createHabit,
                                                            onDeleteHabit = viewModel::deleteHabit
                                                        )

                                                        MoreSubPage.COUNTDOWN -> CountdownScreen(
                                                            countdowns = countdowns,
                                                            onCreateCountdown = viewModel::createCountdown,
                                                            onDeleteCountdown = viewModel::deleteCountdown
                                                        )

                                                        MoreSubPage.SEARCH -> SearchScreen(
                                                            tasks = tasks,
                                                            courses = courses,
                                                            habits = habits,
                                                            countdowns = countdowns,
                                                            onToggleTask = viewModel::toggleTaskCompleted,
                                                            onStartFocusForTask = { task ->
                                                                viewModel.setFocusTarget(task.courseId, task.title)
                                                                currentTab = ElanTab.FOCUS
                                                            },
                                                            onToggleHabitToday = viewModel::toggleHabitToday
                                                        )

                                                        MoreSubPage.SETTINGS -> SettingsScreen(
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
                                                                showOnboardingOverlay = true
                                                            },
                                                            onUpdateTimerDurations = viewModel::updateTimerDurations,
                                                            onUpdateDefaultCalendarView = viewModel::updateDefaultCalendarView,
                                                            onUpdateHapticsEnabled = viewModel::updateHapticsEnabled,
                                                            onUpdateAutoLogStopwatch = viewModel::updateAutoLogStopwatch,
                                                            onUpdateAutoPlayAudioWithFocus = viewModel::setAutoPlayAudioWithFocus,
                                                            onCreateCourse = viewModel::createCourse,
                                                            onDeleteCourse = viewModel::deleteCourse,
                                                            isGoogleAuthPaused = isGoogleAuthPaused,
                                                            onResumeGoogleSignIn = { onTogglePauseGoogleAuth(false) },
                                                            onPauseGoogleSignIn = { onTogglePauseGoogleAuth(true) },
                                                            onSignOut = {
                                                                signOut(credentialManager = credentialManager, scope = scope)
                                                            },
                                                            onClose = { activeMorePage = null }
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
                        .statusBarsPadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    SettingsScreen(
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
                            signOut(credentialManager = credentialManager, scope = scope)
                        },
                        onClose = { showSettingsOverlay = false },
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxSize()
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showTaskCreation,
            enter = slideInVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            ) { fullHeight -> fullHeight } + fadeIn(tween(220)),
            exit = slideOutVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing)
            ) { fullHeight -> fullHeight } + fadeOut(tween(180))
        ) {
            TaskCreationScreen(
                courses = courses,
                initialDateIso = selectedDateIso,
                initialQuadrant = pendingTaskQuadrant,
                profile = profile,
                onCancel = {
                    pendingTaskQuadrant = null
                    showTaskCreation = false
                },
                onCreateTask = { course, title, notes, dueDateIso, scheduledTime, priority, category, estimatedMinutes, quadrant ->
                    viewModel.createTask(
                        course = course,
                        title = title,
                        notes = notes,
                        dueDateIso = dueDateIso,
                        scheduledTime = scheduledTime,
                        priority = priority,
                        category = category,
                        estimatedMinutes = estimatedMinutes,
                        quadrant = quadrant
                    )
                    viewModel.selectDate(dueDateIso)
                    pendingTaskQuadrant = null
                    showTaskCreation = false
                }
            )
        }
        }
    }
}
