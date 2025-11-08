package com.appthere.mdwriter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * E Ink Optimized Color Schemes
 *
 * These color schemes are specifically designed for E Ink displays,
 * particularly Kaleido 3 color E Ink screens. The colors are chosen to:
 * - Minimize dithering artifacts
 * - Provide high contrast for readability
 * - Use muted, earthy tones that render well on limited color gamut
 * - Avoid gradients and high saturation colors
 */

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightPrimary,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightSecondary,

    tertiary = LightAccent,
    onTertiary = LightOnPrimary,
    tertiaryContainer = LightSurfaceVariant,
    onTertiaryContainer = LightAccent,

    error = LightError,
    onError = LightOnPrimary,
    errorContainer = LightSurfaceVariant,
    onErrorContainer = LightError,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightSecondary,

    outline = LightOutline,
    outlineVariant = LightSurfaceVariant,

    scrim = LightPrimary,

    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
    inversePrimary = DarkPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkPrimary,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkSecondary,

    tertiary = DarkAccent,
    onTertiary = DarkOnPrimary,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = DarkAccent,

    error = DarkError,
    onError = DarkOnPrimary,
    errorContainer = DarkSurfaceVariant,
    onErrorContainer = DarkError,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondary,

    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,

    scrim = DarkBackground,

    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
    inversePrimary = LightPrimary
)

/**
 * E Ink Optimized Theme
 *
 * Main theme composable that applies the E Ink optimized color scheme
 * and typography. Automatically switches between light and dark mode
 * based on system settings.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The content to theme.
 */
@Composable
fun MdWriterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
