package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
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
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        MainScreen(darkTheme = true, onToggleTheme = {})
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun test_nav_tab_1_calculadoras() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        MainScreen(darkTheme = true, onToggleTheme = {})
      }
    }
    composeTestRule.onNodeWithTag("nav_tab_1").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun test_nav_tab_2_protocolos() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        MainScreen(darkTheme = true, onToggleTheme = {})
      }
    }
    composeTestRule.onNodeWithTag("nav_tab_2").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun test_nav_tab_3_farmacos() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        MainScreen(darkTheme = true, onToggleTheme = {})
      }
    }
    composeTestRule.onNodeWithTag("nav_tab_3").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun test_nav_tab_4_exploracion() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        MainScreen(darkTheme = true, onToggleTheme = {})
      }
    }
    composeTestRule.onNodeWithTag("nav_tab_4").performClick()
    composeTestRule.waitForIdle()
  }
}
