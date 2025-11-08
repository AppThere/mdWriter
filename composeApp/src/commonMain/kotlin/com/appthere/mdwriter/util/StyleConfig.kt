package com.appthere.mdwriter.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Configuration for Markdown syntax highlighting styles
 *
 * Uses E Ink friendly color palette optimized for Kaleido 3 displays.
 */
data class StyleConfig(
    val isDarkTheme: Boolean = false,

    // Base text style
    val baseTextSize: TextUnit = 16.sp,

    // Heading styles
    val h1Style: SpanStyle = SpanStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),
    val h2Style: SpanStyle = SpanStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),
    val h3Style: SpanStyle = SpanStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),
    val h4Style: SpanStyle = SpanStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),
    val h5Style: SpanStyle = SpanStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),
    val h6Style: SpanStyle = SpanStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),

    // Inline formatting styles
    val strongStyle: SpanStyle = SpanStyle(
        fontWeight = FontWeight.Bold
    ),
    val emphasisStyle: SpanStyle = SpanStyle(
        fontStyle = FontStyle.Italic
    ),
    val codeStyle: SpanStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = if (isDarkTheme) EInkColors.Dark.surface else EInkColors.Light.surface,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary
    ),
    val strikethroughStyle: SpanStyle = SpanStyle(
        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
    ),

    // Link styles
    val linkStyle: SpanStyle = SpanStyle(
        color = if (isDarkTheme) EInkColors.Dark.links else EInkColors.Light.links,
        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
    ),

    // Code block style
    val codeBlockStyle: SpanStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = if (isDarkTheme) EInkColors.Dark.surface else EInkColors.Light.surface,
        color = if (isDarkTheme) EInkColors.Dark.primary else EInkColors.Light.primary,
        fontSize = 14.sp
    ),

    // Blockquote style
    val blockquoteStyle: SpanStyle = SpanStyle(
        fontStyle = FontStyle.Italic,
        color = if (isDarkTheme) EInkColors.Dark.secondary else EInkColors.Light.secondary
    )
) {
    /**
     * Get heading style by level (1-6)
     */
    fun getHeadingStyle(level: Int): SpanStyle {
        return when (level) {
            1 -> h1Style
            2 -> h2Style
            3 -> h3Style
            4 -> h4Style
            5 -> h5Style
            6 -> h6Style
            else -> h6Style
        }
    }

    companion object {
        /**
         * Create default light theme configuration
         */
        fun light(): StyleConfig = StyleConfig(isDarkTheme = false)

        /**
         * Create default dark theme configuration
         */
        fun dark(): StyleConfig = StyleConfig(isDarkTheme = true)
    }
}

/**
 * E Ink friendly color palette optimized for Kaleido 3 displays
 *
 * Uses muted, earthy tones that render well on E Ink screens.
 * Avoids pure saturated colors and provides high contrast.
 */
object EInkColors {
    object Light {
        val background = Color(0xFFFFFFFF)      // Pure white
        val surface = Color(0xFFF5F5F5)         // Very light gray
        val primary = Color(0xFF1A1A1A)         // Near black
        val secondary = Color(0xFF4A4A4A)       // Dark gray
        val accent = Color(0xFFC74440)          // Muted red
        val links = Color(0xFF3B5B8C)           // Desaturated blue
        val success = Color(0xFF4A7C59)         // Muted green
        val warning = Color(0xFFB8860B)         // Dark goldenrod
    }

    object Dark {
        val background = Color(0xFF1A1A1A)      // Very dark gray
        val surface = Color(0xFF2D2D2D)         // Dark gray
        val primary = Color(0xFFE5E5E5)         // Light gray
        val secondary = Color(0xFFB0B0B0)       // Medium gray
        val accent = Color(0xFFD96459)          // Slightly lighter muted red
        val links = Color(0xFF6B8CC4)           // Lighter desaturated blue
        val success = Color(0xFF5A8C69)         // Lighter muted green
        val warning = Color(0xFFC8960B)         // Lighter dark goldenrod
    }
}
