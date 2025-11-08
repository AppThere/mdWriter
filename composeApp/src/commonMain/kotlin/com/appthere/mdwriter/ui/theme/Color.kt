package com.appthere.mdwriter.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * E Ink Optimized Color Palette
 *
 * These colors are optimized for Kaleido 3 displays (4096 colors, ~100 PPI).
 * Design principles:
 * - Use muted, earthy tones instead of pure saturated colors
 * - High contrast between text and background
 * - Avoid gradients (causes dithering on E Ink)
 * - Use solid colors for backgrounds
 */

// Light Mode Palette
val LightBackground = Color(0xFFFFFFFF)        // Pure white
val LightSurface = Color(0xFFF5F5F5)           // Very light gray
val LightPrimary = Color(0xFF1A1A1A)           // Near black
val LightSecondary = Color(0xFF4A4A4A)         // Dark gray
val LightAccent = Color(0xFFC74440)            // Muted red (renders well on Kaleido)
val LightLinks = Color(0xFF3B5B8C)             // Desaturated blue
val LightSuccess = Color(0xFF4A7C59)           // Muted green
val LightWarning = Color(0xFFB8860B)           // Dark goldenrod
val LightError = Color(0xFFC74440)             // Muted red (same as accent)
val LightOnBackground = Color(0xFF1A1A1A)      // Near black
val LightOnSurface = Color(0xFF1A1A1A)         // Near black
val LightOnPrimary = Color(0xFFFFFFFF)         // White
val LightOnSecondary = Color(0xFFFFFFFF)       // White
val LightOutline = Color(0xFF4A4A4A)           // Dark gray
val LightSurfaceVariant = Color(0xFFE5E5E5)    // Light gray variant

// Dark Mode Palette
val DarkBackground = Color(0xFF1A1A1A)         // Very dark gray (not pure black)
val DarkSurface = Color(0xFF2D2D2D)            // Dark gray
val DarkPrimary = Color(0xFFE5E5E5)            // Light gray
val DarkSecondary = Color(0xFFB0B0B0)          // Medium gray
val DarkAccent = Color(0xFFD96459)             // Slightly lighter muted red
val DarkLinks = Color(0xFF6B8CC4)              // Lighter desaturated blue
val DarkSuccess = Color(0xFF6B9B7A)            // Lighter muted green
val DarkWarning = Color(0xFFD4A120)            // Lighter dark goldenrod
val DarkError = Color(0xFFD96459)              // Slightly lighter muted red
val DarkOnBackground = Color(0xFFE5E5E5)       // Light gray
val DarkOnSurface = Color(0xFFE5E5E5)          // Light gray
val DarkOnPrimary = Color(0xFF1A1A1A)          // Very dark gray
val DarkOnSecondary = Color(0xFF1A1A1A)        // Very dark gray
val DarkOutline = Color(0xFFB0B0B0)            // Medium gray
val DarkSurfaceVariant = Color(0xFF3A3A3A)     // Dark gray variant

// Code editor specific colors (work well on E Ink)
val CodeBackground = Color(0xFFF5F5F5)         // Light background for code blocks
val CodeBackgroundDark = Color(0xFF2D2D2D)     // Dark background for code blocks
val CodeKeyword = Color(0xFF3B5B8C)            // Desaturated blue
val CodeKeywordDark = Color(0xFF6B8CC4)        // Lighter desaturated blue
val CodeString = Color(0xFF4A7C59)             // Muted green
val CodeStringDark = Color(0xFF6B9B7A)         // Lighter muted green
val CodeComment = Color(0xFF7A7A7A)            // Medium gray
val CodeCommentDark = Color(0xFF9A9A9A)        // Lighter medium gray
