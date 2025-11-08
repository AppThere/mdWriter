package com.appthere.mdwriter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light color scheme optimized for E Ink displays
 */
private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    onPrimary = LightColors.OnPrimary,
    secondary = LightColors.Secondary,
    onSecondary = LightColors.OnSecondary,
    tertiary = LightColors.Accent,
    onTertiary = LightColors.OnAccent,
    background = LightColors.Background,
    onBackground = LightColors.OnBackground,
    surface = LightColors.Surface,
    onSurface = LightColors.OnSurface,
    error = LightColors.Error,
    onError = Color(0xFFFFFFFF)
)

/**
 * Dark color scheme optimized for E Ink displays
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.Primary,
    onPrimary = DarkColors.OnPrimary,
    secondary = DarkColors.Secondary,
    onSecondary = DarkColors.OnSecondary,
    tertiary = DarkColors.Accent,
    onTertiary = DarkColors.OnAccent,
    background = DarkColors.Background,
    onBackground = DarkColors.OnBackground,
    surface = DarkColors.Surface,
    onSurface = DarkColors.OnSurface,
    error = DarkColors.Error,
    onError = Color(0xFF1A1A1A)
)

/**
 * E Ink optimized Material Theme
 *
 * Designed for Kaleido 3 displays with:
 * - High contrast colors
 * - Muted, earthy tones
 * - No gradients
 * - Solid backgrounds
 *
 * @param darkTheme Whether to use dark theme
 * @param content The composable content
 */
@Composable
fun MdWriterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
