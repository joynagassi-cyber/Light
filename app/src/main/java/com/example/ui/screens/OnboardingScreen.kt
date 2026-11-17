package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AdaptiveHabitTemplate
import com.example.data.model.HabitCategory
import com.example.data.model.NotificationFrequency
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptiveHabitSuggestionsFor
import com.example.data.preferences.StudyAppSettings
import com.example.notifications.LightNotificationHelper
import com.example.ui.components.AppleCard
import com.example.ui.components.GradientSwatchCard
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.LightGradients
import com.example.ui.theme.GradientScreenSurface
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import com.example.ui.theme.appVerticalBrush
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    initialProfile: UserProfile?,
    appSettings: StudyAppSettings,
    selectedGradientId: String,
    canSkipOrDismiss: Boolean,
    onPreviewGradient: (String) -> Unit,
    onFinishOnboarding: (
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
        adaptModulesToProfession: Boolean
    ) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val gradient = LocalLightGradient.current
    val currentYear = remember { LocalDate.now().year }

    var step by rememberSaveable { mutableIntStateOf(0) } // 0..4 (5 steps)
    val totalSteps = 5

    var displayName by rememberSaveable {
        mutableStateOf(initialProfile?.displayName?.takeIf { it.isNotBlank() } ?: "")
    }
    var selectedGender by rememberSaveable {
        mutableStateOf(UserGender.fromKey(initialProfile?.gender))
    }
    var birthYear by rememberSaveable {
        mutableIntStateOf((initialProfile?.birthYear?.toInt() ?: (currentYear - 21)).coerceIn(1940, currentYear - 8))
    }
    var age by rememberSaveable {
        mutableIntStateOf((currentYear - birthYear).coerceIn(8, 99))
    }
    var ageInputText by rememberSaveable { mutableStateOf(age.toString()) }
    var birthYearInputText by rememberSaveable { mutableStateOf(birthYear.toString()) }

    var selectedProfession by rememberSaveable {
        mutableStateOf(UserProfession.fromKey(initialProfile?.profession))
    }
    var customProfession by rememberSaveable {
        mutableStateOf(initialProfile?.customProfession.orEmpty())
    }
    var majorDomain by rememberSaveable {
        mutableStateOf(
            initialProfile?.major?.takeIf { it.isNotBlank() }
                ?: selectedProfession.defaultFocusDomain()
        )
    }
    var majorEditedManually by rememberSaveable { mutableStateOf(false) }
    var dailyGoalTasks by rememberSaveable {
        mutableIntStateOf((initialProfile?.dailyGoalTasks?.toInt() ?: 5).coerceIn(1, 15))
    }
    var adaptModulesToProfession by rememberSaveable { mutableStateOf(true) }

    var chosenGradientId by rememberSaveable { mutableStateOf(selectedGradientId) }
    var hapticsEnabled by rememberSaveable { mutableStateOf(appSettings.hapticsEnabled) }
    var autoPlayAudioWithFocus by rememberSaveable { mutableStateOf(appSettings.autoPlayAudioWithFocus) }

    // Habits step
    val selectedHabitTitles = remember {
        mutableStateListOf<String>().apply {
            adaptiveHabitSuggestionsFor(selectedProfession).take(3).forEach { add(it.title) }
        }
    }
    val customHabits = remember { mutableStateListOf<AdaptiveHabitTemplate>() }
    var newHabitText by rememberSaveable { mutableStateOf("") }

    // Notifications step
    var notificationsEnabled by rememberSaveable {
        mutableStateOf(initialProfile?.notificationsEnabled ?: appSettings.notificationsEnabled)
    }
    var notificationFrequency by rememberSaveable {
        mutableStateOf(
            NotificationFrequency.fromKey(
                initialProfile?.notificationFrequency ?: appSettings.notificationFrequency
            )
        )
    }
    var systemNotifPermissionGranted by remember {
        mutableStateOf(LightNotificationHelper.hasNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        systemNotifPermissionGranted = granted
        if (granted) {
            notificationsEnabled = true
            if (notificationFrequency == NotificationFrequency.OFF) {
                notificationFrequency = NotificationFrequency.DAILY
            }
        }
    }

    fun syncFromBirthYear(newYear: Int) {
        val clampedYear = newYear.coerceIn(1940, currentYear - 8)
        birthYear = clampedYear
        age = (currentYear - clampedYear).coerceIn(8, 99)
        birthYearInputText = clampedYear.toString()
        ageInputText = age.toString()
    }

    fun syncFromAge(newAge: Int) {
        val clampedAge = newAge.coerceIn(8, 99)
        age = clampedAge
        birthYear = (currentYear - clampedAge).coerceIn(1940, currentYear - 8)
        ageInputText = clampedAge.toString()
        birthYearInputText = birthYear.toString()
    }

    BackHandler(enabled = step > 0 || canSkipOrDismiss) {
        if (step > 0) step-- else if (canSkipOrDismiss) onSkip()
    }

    val progressFraction by animateFloatAsState(
        targetValue = (step + 1).toFloat() / totalSteps.toFloat(),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "onboardingProgress"
    )

    val suggestionsForProfession = remember(selectedProfession) {
        adaptiveHabitSuggestionsFor(selectedProfession)
    }

    val canAdvanceCurrentStep = when (step) {
        0 -> displayName.isNotBlank() || true // allowed with fallback, though name encouraged
        1 -> selectedProfession != UserProfession.OTHER || customProfession.isNotBlank()
        else -> true
    }

    fun submitAll() {
        val allHabits = (suggestionsForProfession + customHabits)
            .filter { selectedHabitTitles.contains(it.title) }
            .distinctBy { it.title }
        val finalFreq = if (!notificationsEnabled) NotificationFrequency.OFF else notificationFrequency
        onFinishOnboarding(
            displayName.trim(),
            selectedGender,
            birthYear.toLong(),
            age.toLong(),
            selectedProfession,
            customProfession.trim(),
            majorDomain.trim().ifBlank { selectedProfession.defaultFocusDomain() },
            chosenGradientId,
            dailyGoalTasks.toLong(),
            notificationsEnabled && finalFreq != NotificationFrequency.OFF,
            finalFreq,
            hapticsEnabled,
            autoPlayAudioWithFocus,
            allHabits,
            adaptModulesToProfession
        )
    }

    GradientScreenSurface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Top progress & navigation bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step > 0 || canSkipOrDismiss) {
                        Surface(
                            onClick = { if (step > 0) step-- else onSkip() },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.90f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("onboarding_back_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Étape précédente",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Étape ${step + 1} sur $totalSteps",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = gradient.accentColor,
                            trackColor = Color.White.copy(alpha = 0.55f),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    if (canSkipOrDismiss) {
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text("Fermer", color = gradient.accentColor)
                        }
                    } else {
                        TextButton(
                            onClick = { submitAll() },
                            modifier = Modifier.testTag("onboarding_quick_finish_button")
                        ) {
                            Text("Passer", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Step Content Area
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val forward = targetState > initialState
                        (slideInHorizontally(
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        ) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(220)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                                ) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(150))
                            )
                    },
                    modifier = Modifier.weight(1f),
                    label = "onboardingStep"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OnboardingIllustration(
                            page = currentStep,
                            displayName = displayName,
                            profession = selectedProfession.labelFor(selectedGender, customProfession),
                            accent = gradient.accentColor,
                            activeHabitsCount = selectedHabitTitles.size,
                            notificationsEnabled = notificationsEnabled,
                            themeName = chosenGradientId.replace('_', ' ')
                        )
                        when (currentStep) {
                            0 -> StepIdentityGenderAge(
                                displayName = displayName,
                                onDisplayNameChange = { displayName = it },
                                selectedGender = selectedGender,
                                onSelectGender = { selectedGender = it },
                                age = age,
                                birthYear = birthYear,
                                ageInputText = ageInputText,
                                birthYearInputText = birthYearInputText,
                                currentYear = currentYear,
                                onAgeInput = { raw ->
                                    val digits = raw.filter { it.isDigit() }.take(2)
                                    ageInputText = digits
                                    digits.toIntOrNull()?.let { parsed ->
                                        if (parsed in 8..99) {
                                            age = parsed
                                            birthYear = (currentYear - parsed).coerceIn(1940, currentYear - 8)
                                            birthYearInputText = birthYear.toString()
                                        }
                                    }
                                },
                                onBirthYearInput = { raw ->
                                    val digits = raw.filter { it.isDigit() }.take(4)
                                    birthYearInputText = digits
                                    digits.toIntOrNull()?.let { parsed ->
                                        if (parsed in 1940..(currentYear - 8)) {
                                            birthYear = parsed
                                            age = (currentYear - parsed).coerceIn(8, 99)
                                            ageInputText = age.toString()
                                        }
                                    }
                                },
                                onStepAge = { delta -> syncFromAge(age + delta) },
                                onStepBirthYear = { delta -> syncFromBirthYear(birthYear + delta) },
                                onSelectBirthYearPreset = { syncFromBirthYear(it) }
                            )

                            1 -> StepProfessionAndGoals(
                                gender = selectedGender,
                                selectedProfession = selectedProfession,
                                customProfession = customProfession,
                                majorDomain = majorDomain,
                                dailyGoalTasks = dailyGoalTasks,
                                adaptModulesToProfession = adaptModulesToProfession,
                                onSelectProfession = { prof ->
                                    selectedProfession = prof
                                    if (!majorEditedManually) {
                                        majorDomain = prof.defaultFocusDomain()
                                    }
                                    // Refresh habit selection to match newly chosen profession
                                    selectedHabitTitles.clear()
                                    adaptiveHabitSuggestionsFor(prof).take(3).forEach {
                                        selectedHabitTitles.add(it.title)
                                    }
                                },
                                onCustomProfessionChange = { customProfession = it },
                                onMajorChange = {
                                    majorEditedManually = true
                                    majorDomain = it
                                },
                                onDailyGoalChange = { dailyGoalTasks = it },
                                onAdaptModulesChange = { adaptModulesToProfession = it }
                            )

                            2 -> StepDesignStudio(
                                selectedGradientId = chosenGradientId,
                                hapticsEnabled = hapticsEnabled,
                                autoPlayAudioWithFocus = autoPlayAudioWithFocus,
                                onSelectGradient = { id ->
                                    chosenGradientId = id
                                    onPreviewGradient(id)
                                },
                                onToggleHaptics = { hapticsEnabled = it },
                                onToggleAutoAudio = { autoPlayAudioWithFocus = it }
                            )

                            3 -> StepHabitsActivation(
                                profession = selectedProfession,
                                gender = selectedGender,
                                suggestions = suggestionsForProfession,
                                customHabits = customHabits,
                                selectedTitles = selectedHabitTitles,
                                newHabitText = newHabitText,
                                onNewHabitTextChange = { newHabitText = it },
                                onToggleHabit = { title ->
                                    if (selectedHabitTitles.contains(title)) {
                                        selectedHabitTitles.remove(title)
                                    } else {
                                        selectedHabitTitles.add(title)
                                    }
                                },
                                onAddCustomHabit = {
                                    val clean = newHabitText.trim()
                                    if (clean.isNotBlank()) {
                                        val created = AdaptiveHabitTemplate(
                                            title = clean,
                                            subtitle = "Habitude personnelle activée dès l'onboarding",
                                            category = HabitCategory.LIFE,
                                            iconKey = "sparkles",
                                            colorIndex = (customHabits.size % 6).toLong()
                                        )
                                        customHabits.add(created)
                                        if (!selectedHabitTitles.contains(clean)) {
                                            selectedHabitTitles.add(clean)
                                        }
                                        newHabitText = ""
                                    }
                                }
                            )

                            4 -> StepNotificationsAndSummary(
                                displayName = displayName,
                                gender = selectedGender,
                                age = age,
                                birthYear = birthYear,
                                profession = selectedProfession,
                                customProfession = customProfession,
                                majorDomain = majorDomain,
                                selectedGradientId = chosenGradientId,
                                activeHabitsCount = selectedHabitTitles.size,
                                notificationsEnabled = notificationsEnabled,
                                notificationFrequency = notificationFrequency,
                                systemPermissionGranted = systemNotifPermissionGranted,
                                onToggleNotifications = { enabled ->
                                    notificationsEnabled = enabled
                                    if (enabled) {
                                        if (notificationFrequency == NotificationFrequency.OFF) {
                                            notificationFrequency = NotificationFrequency.DAILY
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            !LightNotificationHelper.hasNotificationPermission(context)
                                        ) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                },
                                onRequestSystemPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                onSelectFrequency = { freq ->
                                    notificationFrequency = freq
                                    notificationsEnabled = freq != NotificationFrequency.OFF
                                }
                            )
                        }
                    }
                }

                // Bottom Action CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                ) {
                    val isLast = step == totalSteps - 1
                    Button(
                        onClick = {
                            if (isLast) submitAll() else step++
                        },
                        enabled = canAdvanceCurrentStep,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradient.accentColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag(if (isLast) "onboarding_finish_button" else "onboarding_next_button")
                    ) {
                        Text(
                            text = if (isLast) "Activer mon espace Light" else "Continuer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isLast) Icons.Rounded.RocketLaunch else Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(
    page: Int,
    displayName: String,
    profession: String,
    accent: Color,
    activeHabitsCount: Int,
    notificationsEnabled: Boolean,
    themeName: String
) {
    val pageTitle = when (page) {
        0 -> "Un espace à votre image"
        1 -> "Votre quotidien, clarifié"
        2 -> "Une ambiance qui vous ressemble"
        3 -> "Les petits pas font la différence"
        else -> "Au bon moment, jamais trop"
    }
    val pageIcon = when (page) {
        0 -> Icons.Rounded.Person
        1 -> Icons.Rounded.School
        2 -> Icons.Rounded.Palette
        3 -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.NotificationsActive
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(accent.copy(alpha = 0.075f))
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = accent.copy(alpha = 0.11f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.11f, size.height * 0.98f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = size.minDimension * 0.23f,
                center = Offset(size.width * 0.91f, size.height * 0.12f)
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.08f),
                topLeft = Offset(size.width * 0.79f, size.height * 0.69f),
                size = Size(size.width * 0.15f, size.height * 0.18f),
                cornerRadius = CornerRadius(14.dp.toPx())
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 300.dp)
                .fillMaxWidth(0.84f),
            shape = RoundedCornerShape(23.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            shadowElevation = 7.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(accent.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(pageIcon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pageTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "LIGHT · VOTRE ESPACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
                }

                when (page) {
                    0 -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = accent.copy(alpha = 0.13f), modifier = Modifier.size(35.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = displayName.trim().firstOrNull()?.uppercase() ?: "✦",
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(displayName.ifBlank { "Votre prénom" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Un profil pensé pour vous", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    1 -> Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(profession, style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.SemiBold)
                        MiniScheduleLine("08:30", "Session de travail", accent, 0.74f)
                        MiniScheduleLine("10:00", "Pause & respiration", accent, 0.48f)
                    }
                    2 -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
                            listOf(Color(0xFFFFAE63), Color(0xFFEC719F), accent, Color(0xFF61B8CE)).forEachIndexed { index, color ->
                                Box(
                                    Modifier
                                        .size(if (index == 2) 27.dp else 22.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(themeName.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text("Aperçu en direct", style = MaterialTheme.typography.labelSmall, color = accent)
                        }
                    }
                    3 -> Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("$activeHabitsCount habitudes sélectionnées", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.SemiBold)
                        MiniHabitLine("Préparer ma journée", true, accent)
                        MiniHabitLine("Faire une pause", activeHabitsCount > 1, accent)
                    }
                    else -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (notificationsEnabled) accent.copy(alpha = 0.13f) else Color(0xFFF0EFF4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notificationsEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = if (notificationsEnabled) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(if (notificationsEnabled) "Rappel activé · 18:00" else "Rappels en pause", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("Votre rythme, vos règles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniScheduleLine(time: String, label: String, accent: Color, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .width((48 * progress).dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.64f))
        )
    }
}

@Composable
private fun MiniHabitLine(title: String, checked: Boolean, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (checked) accent else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(7.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StepIdentityGenderAge(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    selectedGender: UserGender,
    onSelectGender: (UserGender) -> Unit,
    age: Int,
    birthYear: Int,
    ageInputText: String,
    birthYearInputText: String,
    currentYear: Int,
    onAgeInput: (String) -> Unit,
    onBirthYearInput: (String) -> Unit,
    onStepAge: (Int) -> Unit,
    onStepBirthYear: (Int) -> Unit,
    onSelectBirthYearPreset: (Int) -> Unit
) {
    val gradient = LocalLightGradient.current
    val greetingPreview = if (selectedGender == UserGender.FEMININE) {
        "Bienvenue${if (displayName.isNotBlank()) ", $displayName" else ""} ! Prête à organiser votre quotidien ?"
    } else {
        "Bienvenue${if (displayName.isNotBlank()) ", $displayName" else ""} ! Prêt à organiser votre quotidien ?"
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Faisons connaissance",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = greetingPreview,
            style = MaterialTheme.typography.bodyMedium,
            color = gradient.accentColor,
            fontWeight = FontWeight.Medium
        )

        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Votre prénom",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                placeholder = { Text("Ex : Camille, Lina, Thomas…") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = gradient.accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input")
            )
        }

        // Gender selector: Féminin / Masculin
        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Genre (pour accorder toute l'interface)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple(UserGender.FEMININE, Icons.Rounded.Female, "Féminin"),
                    Triple(UserGender.MASCULINE, Icons.Rounded.Male, "Masculin")
                ).forEach { (gender, icon, title) ->
                    val selected = selectedGender == gender
                    Surface(
                        onClick = { onSelectGender(gender) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(
                            if (selected) 1.5.dp else 0.8.dp,
                            if (selected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_gender_${gender.key}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Age & Birth Year synchronized interactive picker
        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cake,
                    contentDescription = null,
                    tint = gradient.accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Âge & année de naissance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Age input + stepper
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Âge ($age ans)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ageInputText,
                        onValueChange = onAgeInput,
                        singleLine = true,
                        suffix = { Text("ans") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_age_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { onStepAge(-1) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Diminuer l'âge")
                        }
                        IconButton(
                            onClick = { onStepAge(1) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Augmenter l'âge", tint = gradient.accentColor)
                        }
                    }
                }

                // Birth year input + stepper
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Année ($birthYear)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = birthYearInputText,
                        onValueChange = onBirthYearInput,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_birthyear_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { onStepBirthYear(-1) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Année précédente")
                        }
                        IconButton(
                            onClick = { onStepBirthYear(1) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Année suivante", tint = gradient.accentColor)
                        }
                    }
                }
            }

            Slider(
                value = birthYear.toFloat(),
                onValueChange = { onSelectBirthYearPreset(it.roundToInt()) },
                valueRange = 1960f..(currentYear - 10).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = gradient.accentColor,
                    activeTrackColor = gradient.accentColor
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1996, 2000, 2003, 2005, 2007).forEach { yr ->
                    val sel = birthYear == yr
                    FilterChip(
                        selected = sel,
                        onClick = { onSelectBirthYearPreset(yr) },
                        label = { Text("$yr (${currentYear - yr} ans)") },
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

@Composable
private fun StepProfessionAndGoals(
    gender: UserGender,
    selectedProfession: UserProfession,
    customProfession: String,
    majorDomain: String,
    dailyGoalTasks: Int,
    adaptModulesToProfession: Boolean,
    onSelectProfession: (UserProfession) -> Unit,
    onCustomProfessionChange: (String) -> Unit,
    onMajorChange: (String) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onAdaptModulesChange: (Boolean) -> Unit
) {
    val gradient = LocalLightGradient.current

    val options = listOf(
        Triple(
            UserProfession.STUDENT,
            Icons.Rounded.School,
            if (gender == UserGender.FEMININE) "Étudiante" to "Cours, examens, TPs, moyennes et révisions"
            else "Étudiant" to "Cours, examens, TPs, moyennes et révisions"
        ),
        Triple(
            UserProfession.ENTREPRENEUR,
            Icons.Rounded.RocketLaunch,
            if (gender == UserGender.FEMININE) "Entrepreneuse" to "Projets business, roadmap produit & Deep Work"
            else "Entrepreneur" to "Projets business, roadmap produit & Deep Work"
        ),
        Triple(
            UserProfession.SELF_TAUGHT,
            Icons.Rounded.Lightbulb,
            "Autodidacte" to "Auto-formation, défis pratiques & veille libre"
        ),
        Triple(
            UserProfession.INDIVIDUAL,
            Icons.Rounded.Person,
            if (gender == UserGender.FEMININE) "Particulière" to "Organisation perso, lectures & habitudes"
            else "Particulier" to "Organisation perso, lectures & habitudes"
        ),
        Triple(
            UserProfession.OTHER,
            Icons.Rounded.WorkOutline,
            "Autre (à préciser)" to "Indiquez votre métier ou activité exacte"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Votre activité principale",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Light adapte vos catégories, vos habitudes, vos modules et le ton de l'assistant IA à votre profil.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppleCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (prof, icon, copy) ->
                    val isSelected = selectedProfession == prof
                    Surface(
                        onClick = { onSelectProfession(prof) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.8.dp,
                            if (isSelected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_profession_${prof.key}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (isSelected) gradient.accentColor else Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = copy.first,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = copy.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = gradient.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = selectedProfession == UserProfession.OTHER) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        OutlinedTextField(
                            value = customProfession,
                            onValueChange = onCustomProfessionChange,
                            label = { Text("Précisez votre profession *") },
                            placeholder = { Text("Ex : Architecte, Designer UX, Chercheuse…") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Badge, contentDescription = null, tint = gradient.accentColor)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_custom_profession_input")
                        )
                    }
                }
            }
        }

        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = when (selectedProfession) {
                    UserProfession.STUDENT -> "Filière ou niveau d'étude"
                    UserProfession.ENTREPRENEUR -> "Projet ou secteur d'activité"
                    UserProfession.SELF_TAUGHT -> "Thème principal d'apprentissage"
                    else -> "Domaine ou objectif principal"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = majorDomain,
                onValueChange = onMajorChange,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_major_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Objectif quotidien",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$dailyGoalTasks tâches / jour",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = gradient.accentColor
                )
            }
            Slider(
                value = dailyGoalTasks.toFloat(),
                onValueChange = { onDailyGoalChange(it.roundToInt().coerceIn(1, 15)) },
                valueRange = 2f..10f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = gradient.accentColor,
                    activeTrackColor = gradient.accentColor
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "Adapter les modules de départ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Configure automatiquement vos pôles / matières initiaux",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = adaptModulesToProfession,
                    onCheckedChange = onAdaptModulesChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = gradient.accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun StepDesignStudio(
    selectedGradientId: String,
    hapticsEnabled: Boolean,
    autoPlayAudioWithFocus: Boolean,
    onSelectGradient: (String) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleAutoAudio: (Boolean) -> Unit
) {
    val gradient = LocalLightGradient.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient.appBrush()),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color.White)
            }
            Column {
                Text(
                    text = "Design & atmosphère",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Touchez un dégradé pour le tester en direct",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LightGradients.all.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { preset ->
                    GradientSwatchCard(
                        preset = preset,
                        isSelected = preset.id == selectedGradientId,
                        onClick = { onSelectGradient(preset.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "Signaux tactiles & transitions fluides",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Animations iOS douces à chaque interaction",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = gradient.accentColor
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "Musique Lo-Fi auto en Focus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lance l'ambiance sonore dès le démarrage du minuteur",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoPlayAudioWithFocus,
                    onCheckedChange = onToggleAutoAudio,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = gradient.accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun StepHabitsActivation(
    profession: UserProfession,
    gender: UserGender,
    suggestions: List<AdaptiveHabitTemplate>,
    customHabits: List<AdaptiveHabitTemplate>,
    selectedTitles: List<String>,
    newHabitText: String,
    onNewHabitTextChange: (String) -> Unit,
    onToggleHabit: (String) -> Unit,
    onAddCustomHabit: () -> Unit
) {
    val gradient = LocalLightGradient.current
    val combined = suggestions + customHabits

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleEmerald.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SelfImprovement, contentDescription = null, tint = AppleEmerald)
            }
            Column {
                Text(
                    text = "Activez vos habitudes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sélection suggérée pour ${profession.labelFor(gender)} (${selectedTitles.size} active(s))",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AppleCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                combined.forEach { habit ->
                    val checked = selectedTitles.contains(habit.title)
                    Surface(
                        onClick = { onToggleHabit(habit.title) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (checked) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(
                            if (checked) 1.3.dp else 0.7.dp,
                            if (checked) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_habit_${habit.title.take(12)}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (checked) "Habitude activée" else "Activer l'habitude",
                                tint = if (checked) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = habit.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Ajouter une habitude personnalisée",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newHabitText,
                    onValueChange = onNewHabitTextChange,
                    placeholder = { Text("Ex : Lire 10 pages, Méditation…") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = onAddCustomHabit,
                    shape = CircleShape,
                    color = gradient.accentColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, contentDescription = "Ajouter l'habitude", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNotificationsAndSummary(
    displayName: String,
    gender: UserGender,
    age: Int,
    birthYear: Int,
    profession: UserProfession,
    customProfession: String,
    majorDomain: String,
    selectedGradientId: String,
    activeHabitsCount: Int,
    notificationsEnabled: Boolean,
    notificationFrequency: NotificationFrequency,
    systemPermissionGranted: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onRequestSystemPermission: () -> Unit,
    onSelectFrequency: (NotificationFrequency) -> Unit
) {
    val gradient = LocalLightGradient.current
    val gradientPreset = LightGradients.byId(selectedGradientId)
    val role = profession.labelFor(gender, customProfession)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleIndigo.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (notificationsEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                    contentDescription = null,
                    tint = gradient.accentColor
                )
            }
            Column {
                Text(
                    text = "Notifications & rythme",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choisissez quand Light vous envoie des rappels",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "Activer les notifications",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (systemPermissionGranted) {
                            "Autorisation système active"
                        } else {
                            "Demande l'autorisation système Android"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = gradient.accentColor
                    ),
                    modifier = Modifier.testTag("onboarding_notifications_switch")
                )
            }

            if (notificationsEnabled && !systemPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onRequestSystemPermission,
                    shape = RoundedCornerShape(14.dp),
                    color = gradient.softContainer,
                    border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Autoriser les notifications sur cet appareil",
                        style = MaterialTheme.typography.labelLarge,
                        color = gradient.accentColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Fréquence des rappels",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationFrequency.entries.forEach { freq ->
                    val selected = if (!notificationsEnabled) freq == NotificationFrequency.OFF else notificationFrequency == freq
                    Surface(
                        onClick = { onSelectFrequency(freq) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) gradient.softContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(
                            if (selected) 1.3.dp else 0.7.dp,
                            if (selected) gradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_freq_${freq.key}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = freq.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = freq.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = gradient.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Personalized Adaptation Summary
        AppleCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = gradient.accentColor)
                Text(
                    text = "Votre espace Light sur-mesure",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow("Identité", "${displayName.ifBlank { role }} · ${gender.label}, $age ans ($birthYear)")
            SummaryRow("Profil", "$role · ${majorDomain.ifBlank { profession.defaultFocusDomain() }}")
            SummaryRow("Design", gradientPreset.name)
            SummaryRow("Habitudes", "$activeHabitsCount activée(s)")
            SummaryRow(
                "Notifications",
                if (notificationsEnabled && notificationFrequency != NotificationFrequency.OFF) {
                    "Actives (${notificationFrequency.label})"
                } else {
                    "Désactivées"
                }
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
