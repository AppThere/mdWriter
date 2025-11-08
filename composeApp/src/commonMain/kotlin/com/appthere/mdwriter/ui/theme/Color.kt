package com.appthere.mdwriter.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * E Ink optimized color palette for Kaleido 3 displays
 *
 * Design principles:
 * - High contrast between text and background
 * - Muted, earthy tones that render well on E Ink
 * - No pure saturated colors
 * - Avoid gradients (cause dithering)
 * - Solid colors for backgrounds
 */

// Light Mode Colors
object LightColors {
    val Background = Color(0xFFFFFFFF)        // Pure white
    val Surface = Color(0xFFF5F5F5)           // Very light gray
    val Primary = Color(0xFF1A1A1A)           // Near black
    val Secondary = Color(0xFF4A4A4A)         // Dark gray
    val Accent = Color(0xFFC74440)            // Muted red (renders well on Kaleido)
    val Links = Color(0xFF3B5B8C)             // Desaturated blue
    val Success = Color(0xFF4A7C59)           // Muted green
    val Warning = Color(0xFFB8860B)           // Dark goldenrod
    val Error = Color(0xFFC74440)             // Same as accent for consistency
    val OnBackground = Color(0xFF1A1A1A)      // Near black
    val OnSurface = Color(0xFF1A1A1A)         // Near black
    val OnPrimary = Color(0xFFFFFFFF)         // White
    val OnSecondary = Color(0xFFFFFFFF)       // White
    val OnAccent = Color(0xFFFFFFFF)          // White
}

// Dark Mode Colors
object DarkColors {
    val Background = Color(0xFF1A1A1A)        // Very dark gray (not pure black)
    val Surface = Color(0xFF2D2D2D)           // Dark gray
    val Primary = Color(0xFFE5E5E5)           // Light gray
    val Secondary = Color(0xFFB0B0B0)         // Medium gray
    val Accent = Color(0xFFD96459)            // Slightly lighter muted red
    val Links = Color(0xFF6B8CC4)             // Lighter desaturated blue
    val Success = Color(0xFF6B9B7F)           // Lighter muted green
    val Warning = Color(0xFFD4A928)           // Lighter goldenrod
    val Error = Color(0xFFD96459)             // Same as accent for consistency
    val OnBackground = Color(0xFFE5E5E5)      // Light gray
    val OnSurface = Color(0xFFE5E5E5)         // Light gray
    val OnPrimary = Color(0xFF1A1A1A)         // Very dark gray
    val OnSecondary = Color(0xFF1A1A1A)       // Very dark gray
    val OnAccent = Color(0xFF1A1A1A)          // Very dark gray
}

// Syntax highlighting colors (E Ink optimized)
object SyntaxColors {
    // Light mode
    val LightHeading = Color(0xFF1A1A1A)      // Near black, bold weight
    val LightBold = Color(0xFF1A1A1A)         // Near black, bold weight
    val LightItalic = Color(0xFF1A1A1A)       // Near black, italic style
    val LightCode = Color(0xFF3B5B8C)         // Desaturated blue
    val LightCodeBackground = Color(0xFFF5F5F5) // Very light gray
    val LightLink = Color(0xFF3B5B8C)         // Desaturated blue
    val LightQuote = Color(0xFF4A4A4A)        // Dark gray
    val LightList = Color(0xFF1A1A1A)         // Near black

    // Dark mode
    val DarkHeading = Color(0xFFE5E5E5)       // Light gray, bold weight
    val DarkBold = Color(0xFFE5E5E5)          // Light gray, bold weight
    val DarkItalic = Color(0xFFE5E5E5)        // Light gray, italic style
    val DarkCode = Color(0xFF6B8CC4)          // Lighter desaturated blue
    val DarkCodeBackground = Color(0xFF2D2D2D) // Dark gray
    val DarkLink = Color(0xFF6B8CC4)          // Lighter desaturated blue
    val DarkQuote = Color(0xFFB0B0B0)         // Medium gray
    val DarkList = Color(0xFFE5E5E5)          // Light gray
}
