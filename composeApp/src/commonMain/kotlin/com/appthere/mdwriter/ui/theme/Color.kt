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
    val SurfaceVariant = Color(0xFFEEEEEE)    // Slightly darker surface
    val Primary = Color(0xFF1A1A1A)           // Near black
    val Secondary = Color(0xFF4A4A4A)         // Dark gray
    val Tertiary = Color(0xFF6B6B6B)          // Medium gray
    val Accent = Color(0xFFC74440)            // Muted red (renders well on Kaleido)
    val Links = Color(0xFF3B5B8C)             // Desaturated blue
    val Success = Color(0xFF4A7C59)           // Muted green
    val Warning = Color(0xFFB8860B)           // Dark goldenrod
    val Error = Color(0xFFB71C1C)             // Dark red
    val OnBackground = Color(0xFF1A1A1A)      // Near black
    val OnSurface = Color(0xFF1A1A1A)         // Near black
    val OnPrimary = Color(0xFFFFFFFF)         // White
    val OnSecondary = Color(0xFFFFFFFF)       // White
    val OnAccent = Color(0xFFFFFFFF)          // White
    val Outline = Color(0xFFCCCCCC)           // Light gray border
}

// Dark Mode Colors
object DarkColors {
    val Background = Color(0xFF1A1A1A)        // Very dark gray (not pure black)
    val Surface = Color(0xFF2D2D2D)           // Dark gray
    val SurfaceVariant = Color(0xFF3A3A3A)    // Slightly lighter surface
    val Primary = Color(0xFFE5E5E5)           // Light gray
    val Secondary = Color(0xFFB0B0B0)         // Medium gray
    val Tertiary = Color(0xFF8A8A8A)          // Medium-light gray
    val Accent = Color(0xFFD96459)            // Slightly lighter muted red
    val Links = Color(0xFF6B8CC4)             // Lighter desaturated blue
    val Success = Color(0xFF5E9A6F)           // Lighter muted green
    val Warning = Color(0xFFD4A017)           // Lighter goldenrod
    val Error = Color(0xFFEF5350)             // Lighter red
    val OnBackground = Color(0xFFE5E5E5)      // Light gray
    val OnSurface = Color(0xFFE5E5E5)         // Light gray
    val OnPrimary = Color(0xFF1A1A1A)         // Very dark gray
    val OnSecondary = Color(0xFF1A1A1A)       // Very dark gray
    val OnAccent = Color(0xFF1A1A1A)          // Very dark gray
    val Outline = Color(0xFF4A4A4A)           // Dark gray border
}

// Markdown syntax highlighting colors (E Ink optimized)
object MarkdownSyntaxColors {
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

// CSS syntax highlighting colors (for CSS editor)
object CssSyntaxColors {
    // Light mode
    val LightKeyword = Color(0xFF8B4513)      // Saddle brown
    val LightString = Color(0xFF2F4F2F)       // Dark slate gray
    val LightComment = Color(0xFF696969)      // Dim gray
    val LightNumber = Color(0xFF483D8B)       // Dark slate blue
    val LightFunction = Color(0xFF8B4726)     // Burnt sienna
    val LightProperty = Color(0xFF2F4F4F)     // Dark slate gray
    val LightValue = Color(0xFF556B2F)        // Dark olive green
    val LightSelector = Color(0xFF8B4513)     // Saddle brown

    // Dark mode
    val DarkKeyword = Color(0xFFD2691E)       // Chocolate
    val DarkString = Color(0xFF90EE90)        // Light green
    val DarkComment = Color(0xFFA9A9A9)       // Dark gray
    val DarkNumber = Color(0xFF9370DB)        // Medium purple
    val DarkFunction = Color(0xFFDEB887)      // Burlywood
    val DarkProperty = Color(0xFFAFEEEE)      // Pale turquoise
    val DarkValue = Color(0xFFBDB76B)         // Dark khaki
    val DarkSelector = Color(0xFFD2691E)      // Chocolate
}

// Legacy color exports for compatibility
val LightBackground = LightColors.Background
val LightSurface = LightColors.Surface
val LightSurfaceVariant = LightColors.SurfaceVariant
val LightPrimary = LightColors.Primary
val LightSecondary = LightColors.Secondary
val LightTertiary = LightColors.Tertiary
val LightAccent = LightColors.Accent
val LightLink = LightColors.Links
val LightSuccess = LightColors.Success
val LightWarning = LightColors.Warning
val LightError = LightColors.Error
val LightOnPrimary = LightColors.OnPrimary
val LightOnBackground = LightColors.OnBackground
val LightOnSurface = LightColors.OnSurface
val LightOutline = LightColors.Outline

val DarkBackground = DarkColors.Background
val DarkSurface = DarkColors.Surface
val DarkSurfaceVariant = DarkColors.SurfaceVariant
val DarkPrimary = DarkColors.Primary
val DarkSecondary = DarkColors.Secondary
val DarkTertiary = DarkColors.Tertiary
val DarkAccent = DarkColors.Accent
val DarkLink = DarkColors.Links
val DarkSuccess = DarkColors.Success
val DarkWarning = DarkColors.Warning
val DarkError = DarkColors.Error
val DarkOnPrimary = DarkColors.OnPrimary
val DarkOnBackground = DarkColors.OnBackground
val DarkOnSurface = DarkColors.OnSurface
val DarkOutline = DarkColors.Outline

// CSS syntax color exports
val SyntaxKeyword = CssSyntaxColors.LightKeyword
val SyntaxString = CssSyntaxColors.LightString
val SyntaxComment = CssSyntaxColors.LightComment
val SyntaxNumber = CssSyntaxColors.LightNumber
val SyntaxFunction = CssSyntaxColors.LightFunction
val SyntaxProperty = CssSyntaxColors.LightProperty
val SyntaxValue = CssSyntaxColors.LightValue
val SyntaxSelector = CssSyntaxColors.LightSelector

val SyntaxKeywordDark = CssSyntaxColors.DarkKeyword
val SyntaxStringDark = CssSyntaxColors.DarkString
val SyntaxCommentDark = CssSyntaxColors.DarkComment
val SyntaxNumberDark = CssSyntaxColors.DarkNumber
val SyntaxFunctionDark = CssSyntaxColors.DarkFunction
val SyntaxPropertyDark = CssSyntaxColors.DarkProperty
val SyntaxValueDark = CssSyntaxColors.DarkValue
val SyntaxSelectorDark = CssSyntaxColors.DarkSelector
