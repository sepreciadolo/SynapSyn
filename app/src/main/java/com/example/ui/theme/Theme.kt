package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBg,
    surface = DarkSurface,
    error = DarkError,
    onBackground = androidx.compose.ui.graphics.Color(0xFFF9FAFB),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF9FAFB)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBg,
    surface = LightSurface,
    error = LightError,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1E293B),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1E293B)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom theme overrides dynamic color to preserve clinical brand
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
