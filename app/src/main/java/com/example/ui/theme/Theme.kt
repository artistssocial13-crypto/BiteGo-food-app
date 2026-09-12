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

private val DarkColorScheme = darkColorScheme(
    primary = WarmOrange,
    onPrimary = PureWhite,
    primaryContainer = DarkOrange,
    onPrimaryContainer = PureWhite,
    secondary = LightOrange,
    onSecondary = DeepBlack,
    secondaryContainer = Charcoal,
    onSecondaryContainer = OffWhite,
    tertiary = LightGray,
    background = DeepBlack,
    onBackground = PureWhite,
    surface = Charcoal,
    onSurface = PureWhite,
    surfaceVariant = LightCharcoal,
    onSurfaceVariant = OffWhite,
    error = ErrorRed,
    onError = PureWhite
)

private val LightColorScheme = DarkColorScheme // Force Dark Mode for this app

@Composable
fun CraveTheme(
  darkTheme: Boolean = true, // Always dark
  dynamicColor: Boolean = false, // Disable dynamic color to maintain premium branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
