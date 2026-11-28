package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.ThemePreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Light", appName)
  }

  @Test
  fun `Google sign in is not paused by default (test mode removed)`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertFalse(ThemePreferencesStore(context).googleAuthPausedForTesting.first())
  }

  @Test
  fun `focus illustration and background selections persist`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = ThemePreferencesStore(context)

    store.updateFocusIllustration("group_study")
    store.updateFocusTheme("nebula")

    val settings = store.appSettings.first()
    assertEquals("group_study", settings.focusIllustrationId)
    assertEquals("nebula", settings.focusThemeId)
    assertEquals("gradient", settings.selectedThemeId)
  }
}
