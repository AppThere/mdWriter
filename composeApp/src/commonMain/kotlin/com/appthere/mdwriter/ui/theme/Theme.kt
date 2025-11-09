package com.appthere.mdwriter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * E Ink optimized theme for Kaleido 3 displays
 */
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightOnSurface,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightOnSurface,

    tertiary = LightAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5E3),
    onTertiaryContainer = LightPrimary,

    error = LightError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    outline = LightOutline,
    outlineVariant = Color(0xFFE0E0E0),

    scrim = Color.Black.copy(alpha = 0.3f)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurface,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,

    tertiary = DarkAccent,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5D2C2A),
    onTertiaryContainer = DarkPrimary,

    error = DarkError,
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = DarkOutline,
    outlineVariant = Color(0xFF444444),

    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun MDWriterTheme(
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
        typography = Typography,
        content = content
    )
}
