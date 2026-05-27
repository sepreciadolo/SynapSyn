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
    onPrimary = androidx.compose.ui.graphics.Color(0xFF2C194D),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF3F2B66),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFEDE7F6),
    secondary = DarkSecondary,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF2B203C),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF332946),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFE8E1F5),
    background = DarkBg,
    onBackground = androidx.compose.ui.graphics.Color(0xFFF3EDFA),
    surface = DarkSurface,
    onSurface = androidx.compose.ui.graphics.Color(0xFFF3EDFA),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2B263C),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFD6CEDE),
    error = DarkError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightPrimary,
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFEDE7F6),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF25134A),
    secondary = LightSecondary,
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFF3ECFC),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF4C1D95),
    background = LightBg,
    onBackground = androidx.compose.ui.graphics.Color(0xFF201A29),
    surface = LightSurface,
    onSurface = androidx.compose.ui.graphics.Color(0xFF201A29),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFECE6F4),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF49454E),
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
