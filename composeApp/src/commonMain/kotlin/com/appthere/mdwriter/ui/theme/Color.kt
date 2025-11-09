package com.appthere.mdwriter.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * E Ink optimized color palette for Kaleido 3 displays
 * Colors are chosen to render well with ~100 PPI and 4096 colors
 */

// Light Mode Colors
val LightBackground = Color(0xFFFFFFFF) // Pure white
val LightSurface = Color(0xFFF5F5F5) // Very light gray
val LightPrimary = Color(0xFF1A1A1A) // Near black
val LightSecondary = Color(0xFF4A4A4A) // Dark gray
val LightAccent = Color(0xFFC74440) // Muted red - renders well on Kaleido
val LightLinks = Color(0xFF3B5B8C) // Desaturated blue
val LightSuccess = Color(0xFF4A7C59) // Muted green
val LightWarning = Color(0xFFB8860B) // Dark goldenrod
val LightError = Color(0xFFB71C1C) // Deep red

val LightOnBackground = Color(0xFF1A1A1A)
val LightOnSurface = Color(0xFF1A1A1A)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnSecondary = Color(0xFFFFFFFF)

val LightOutline = Color(0xFFCCCCCC)
val LightSurfaceVariant = Color(0xFFEEEEEE)
val LightOnSurfaceVariant = Color(0xFF666666)

// Dark Mode Colors
val DarkBackground = Color(0xFF1A1A1A) // Very dark gray (not pure black)
val DarkSurface = Color(0xFF2D2D2D) // Dark gray
val DarkPrimary = Color(0xFFE5E5E5) // Light gray
val DarkSecondary = Color(0xFFB0B0B0) // Medium gray
val DarkAccent = Color(0xFFD96459) // Slightly lighter muted red
val DarkLinks = Color(0xFF6B8CC4) // Lighter desaturated blue
val DarkSuccess = Color(0xFF6BA87E) // Lighter muted green
val DarkWarning = Color(0xFFD4A817) // Lighter goldenrod
val DarkError = Color(0xFFE57373) // Lighter red

val DarkOnBackground = Color(0xFFE5E5E5)
val DarkOnSurface = Color(0xFFE5E5E5)
val DarkOnPrimary = Color(0xFF1A1A1A)
val DarkOnSecondary = Color(0xFF1A1A1A)

val DarkOutline = Color(0xFF555555)
val DarkSurfaceVariant = Color(0xFF3A3A3A)
val DarkOnSurfaceVariant = Color(0xFFB0B0B0)

// Syntax highlighting colors (E Ink friendly)
object SyntaxColors {
    // Light mode
    val LightHeading = Color(0xFF1A1A1A)
    val LightBold = Color(0xFF2D2D2D)
    val LightItalic = Color(0xFF4A4A4A)
    val LightCode = Color(0xFF3B5B8C)
    val LightLink = Color(0xFF3B5B8C)
    val LightQuote = Color(0xFF666666)

    // Dark mode
    val DarkHeading = Color(0xFFE5E5E5)
    val DarkBold = Color(0xFFD0D0D0)
    val DarkItalic = Color(0xFFB0B0B0)
    val DarkCode = Color(0xFF6B8CC4)
    val DarkLink = Color(0xFF6B8CC4)
    val DarkQuote = Color(0xFF999999)
}
