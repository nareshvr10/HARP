package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.vrntechnology.harpedge.data.repository.AuthRepository
import com.vrntechnology.harpedge.ui.auth.AuthViewModel
import com.vrntechnology.harpedge.ui.auth.LoginScreen
import com.vrntechnology.harpedge.ui.theme.HarpEdgeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val authRepo = AuthRepository()
    val authVm = AuthViewModel(authRepo)
    composeTestRule.setContent {
      HarpEdgeTheme(darkTheme = true) {
        LoginScreen(
          authViewModel = authVm,
          onNavigateToDashboard = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

