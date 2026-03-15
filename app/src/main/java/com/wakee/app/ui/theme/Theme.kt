package com.wakee.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WakeeDarkColorScheme = darkColorScheme(
    primary = Accent,
    secondary = AccentEnd,
    tertiary = Success,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onPrimary = PrimaryText,
    onSecondary = PrimaryText,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = SecondaryText,
    error = Danger,
    outline = Border
)

@Composable
fun WakeeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WakeeDarkColorScheme,
        typography = WakeeTypography,
        content = content
    )
}
