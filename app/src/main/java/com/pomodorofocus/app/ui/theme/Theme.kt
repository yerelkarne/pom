package com.pomodorofocus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkDark,
    background = GraySurface,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = PinkPrimary,
    secondary = PinkDark,
    background = GraySurfaceDark,
    surface = Color(0xFF2C2C2E),
    onPrimary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

@Composable
fun PomodoroFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
