package com.appthere.mdwriter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    error = LightError,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnPrimary,
    onTertiary = LightOnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onError = LightOnPrimary,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    error = DarkError,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnPrimary,
    onTertiary = DarkOnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onError = DarkOnPrimary,
    outline = DarkOutline
)

/**
 * E Ink optimized theme for the Markdown Editor
 * Designed for Kaleido 3 displays with 4096 colors
 */
@Composable
fun MdWriterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
