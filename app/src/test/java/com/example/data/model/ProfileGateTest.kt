package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileGateTest {

    private fun profile(onboardingCompleted: Boolean = true) = UserProfile(
        userId = "u1",
        displayName = "Lina",
        onboardingCompleted = onboardingCompleted
    )

    @Test
    fun loadingIsInitializing() {
        val gate = resolveProfileGate(UiState.Loading)
        assertTrue(gate is ProfileGate.Initializing)
    }

    @Test
    fun successNullWithoutErrorIsInitializing() {
        val gate = resolveProfileGate(UiState.Success(null))
        assertTrue(gate is ProfileGate.Initializing)
    }

    @Test
    fun successNullWithCreatingStillInitializing() {
        val gate = resolveProfileGate(UiState.Success(null), isCreatingProfile = true)
        assertTrue(gate is ProfileGate.Initializing)
    }

    @Test
    fun successNullWithInitErrorIsError() {
        val gate = resolveProfileGate(UiState.Success(null), profileInitError = "Erreur d'initialisation : PERMISSION_DENIED")
        assertTrue(gate is ProfileGate.Error)
        assertEquals("Erreur d'initialisation : PERMISSION_DENIED", (gate as ProfileGate.Error).message)
    }

    @Test
    fun errorStateIsErrorEvenWithProfileNull() {
        val gate = resolveProfileGate(UiState.Error("Impossible de synchroniser le profil"))
        assertTrue(gate is ProfileGate.Error)
    }

    @Test
    fun needsOnboardingWhenFlagFalse() {
        val gate = resolveProfileGate(UiState.Success(profile(onboardingCompleted = false)))
        assertTrue(gate is ProfileGate.NeedsOnboarding)
    }

    @Test
    fun readyWhenOnboardingCompleted() {
        val gate = resolveProfileGate(UiState.Success(profile(onboardingCompleted = true)))
        assertTrue(gate is ProfileGate.Ready)
    }

    @Test
    fun errorTakesPrecedenceOverInitializing() {
        // Simulate race: Success(null) but observe already emitted Error via initError
        val gate = resolveProfileGate(UiState.Success(null), profileInitError = "offline")
        assertTrue(gate is ProfileGate.Error)
    }

    @Test
    fun successNullDuringOfflineCreationShowsInitializingNotHome() {
        // Regression: previously showed home (Ready) incorrectly
        val gate = resolveProfileGate(UiState.Success(null))
        assertTrue("Gate must not be Ready when profile is null", gate !is ProfileGate.Ready)
        assertTrue(gate is ProfileGate.Initializing)
    }
}
