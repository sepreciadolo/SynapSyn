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
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1A0A38),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF2A1C49),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFEADBFF),
    secondary = DarkSecondary,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF003730),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF034E45),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF99F6E4),
    background = DarkBg,
    onBackground = androidx.compose.ui.graphics.Color(0xFFF1EDF9),
    surface = DarkSurface,
    onSurface = androidx.compose.ui.graphics.Color(0xFFF1EDF9),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1D182B),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFD3CBE0),
    error = DarkError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightPrimary,
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFEADBFF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF1F005E),
    secondary = LightSecondary,
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFCCFBF1),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF002B25),
    background = LightBg,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1A24),
    surface = LightSurface,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1A24),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFEADBFF).copy(alpha = 0.5f),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4C4556),
    error = LightError
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
