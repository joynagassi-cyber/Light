package com.example.data.model

sealed interface ProfileGate {
    data object Initializing : ProfileGate
    data class Error(val message: String) : ProfileGate
    data class NeedsOnboarding(val profile: UserProfile) : ProfileGate
    data class Ready(val profile: UserProfile) : ProfileGate
}

/**
 * Pure gate resolver — testable without Compose.
 * - Error takes precedence when profile is null and a message exists
 * - Initializing when Loading or Success(null) without error
 * - NeedsOnboarding when profile exists but onboardingCompleted == false
 * - Ready otherwise (profile with onboardingCompleted == true)
 */
fun resolveProfileGate(
    profileState: UiState<UserProfile?>,
    isCreatingProfile: Boolean = false,
    profileInitError: String? = null
): ProfileGate {
    val profile = (profileState as? UiState.Success)?.data
    val errorMsg = (profileState as? UiState.Error)?.message
        ?: profileInitError?.takeIf { profile == null }
    if (errorMsg != null) return ProfileGate.Error(errorMsg)
    if (profileState is UiState.Loading || profile == null) return ProfileGate.Initializing
    // profile is non-null here
    return if (!profile.onboardingCompleted) ProfileGate.NeedsOnboarding(profile) else ProfileGate.Ready(profile)
}
