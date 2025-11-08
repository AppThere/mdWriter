package com.appthere.mdwriter.util

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.test.*

class StyleConfigTest {

    @Test
    fun `light theme should use light colors`() {
        val config = StyleConfig.light()

        assertFalse(config.isDarkTheme)
        assertEquals(EInkColors.Light.primary, config.h1Style.color)
        assertEquals(EInkColors.Light.links, config.linkStyle.color)
        assertEquals(EInkColors.Light.surface, config.codeStyle.background)
    }

    @Test
    fun `dark theme should use dark colors`() {
        val config = StyleConfig.dark()

        assertTrue(config.isDarkTheme)
        assertEquals(EInkColors.Dark.primary, config.h1Style.color)
        assertEquals(EInkColors.Dark.links, config.linkStyle.color)
        assertEquals(EInkColors.Dark.surface, config.codeStyle.background)
    }

    @Test
    fun `heading styles should have correct sizes`() {
        val config = StyleConfig.light()

        assertEquals(32.sp, config.h1Style.fontSize)
        assertEquals(28.sp, config.h2Style.fontSize)
        assertEquals(24.sp, config.h3Style.fontSize)
        assertEquals(20.sp, config.h4Style.fontSize)
        assertEquals(18.sp, config.h5Style.fontSize)
        assertEquals(16.sp, config.h6Style.fontSize)
    }

    @Test
    fun `heading styles should be bold`() {
        val config = StyleConfig.light()

        assertEquals(FontWeight.Bold, config.h1Style.fontWeight)
        assertEquals(FontWeight.Bold, config.h2Style.fontWeight)
        assertEquals(FontWeight.Bold, config.h3Style.fontWeight)
        assertEquals(FontWeight.Bold, config.h4Style.fontWeight)
        assertEquals(FontWeight.Bold, config.h5Style.fontWeight)
        assertEquals(FontWeight.Bold, config.h6Style.fontWeight)
    }

    @Test
    fun `getHeadingStyle should return correct style for each level`() {
        val config = StyleConfig.light()

        assertEquals(config.h1Style, config.getHeadingStyle(1))
        assertEquals(config.h2Style, config.getHeadingStyle(2))
        assertEquals(config.h3Style, config.getHeadingStyle(3))
        assertEquals(config.h4Style, config.getHeadingStyle(4))
        assertEquals(config.h5Style, config.getHeadingStyle(5))
        assertEquals(config.h6Style, config.getHeadingStyle(6))
    }

    @Test
    fun `getHeadingStyle should return h6 for invalid levels`() {
        val config = StyleConfig.light()

        assertEquals(config.h6Style, config.getHeadingStyle(0))
        assertEquals(config.h6Style, config.getHeadingStyle(7))
        assertEquals(config.h6Style, config.getHeadingStyle(-1))
    }

    @Test
    fun `strong style should be bold`() {
        val config = StyleConfig.light()

        assertEquals(FontWeight.Bold, config.strongStyle.fontWeight)
    }

    @Test
    fun `emphasis style should be italic`() {
        val config = StyleConfig.light()

        assertEquals(FontStyle.Italic, config.emphasisStyle.fontStyle)
    }

    @Test
    fun `code style should have monospace font`() {
        val config = StyleConfig.light()

        assertNotNull(config.codeStyle.fontFamily)
        assertNotNull(config.codeStyle.background)
    }

    @Test
    fun `link style should have underline`() {
        val config = StyleConfig.light()

        assertNotNull(config.linkStyle.textDecoration)
        assertEquals(EInkColors.Light.links, config.linkStyle.color)
    }

    @Test
    fun `strikethrough style should have line through`() {
        val config = StyleConfig.light()

        assertNotNull(config.strikethroughStyle.textDecoration)
    }

    @Test
    fun `blockquote style should be italic`() {
        val config = StyleConfig.light()

        assertEquals(FontStyle.Italic, config.blockquoteStyle.fontStyle)
    }

    @Test
    fun `code block style should have monospace font and smaller size`() {
        val config = StyleConfig.light()

        assertNotNull(config.codeBlockStyle.fontFamily)
        assertEquals(14.sp, config.codeBlockStyle.fontSize)
        assertNotNull(config.codeBlockStyle.background)
    }

    @Test
    fun `base text size should be configurable`() {
        val config = StyleConfig(baseTextSize = 18.sp)

        assertEquals(18.sp, config.baseTextSize)
    }

    @Test
    fun `E Ink light colors should be defined`() {
        assertNotNull(EInkColors.Light.background)
        assertNotNull(EInkColors.Light.surface)
        assertNotNull(EInkColors.Light.primary)
        assertNotNull(EInkColors.Light.secondary)
        assertNotNull(EInkColors.Light.accent)
        assertNotNull(EInkColors.Light.links)
        assertNotNull(EInkColors.Light.success)
        assertNotNull(EInkColors.Light.warning)
    }

    @Test
    fun `E Ink dark colors should be defined`() {
        assertNotNull(EInkColors.Dark.background)
        assertNotNull(EInkColors.Dark.surface)
        assertNotNull(EInkColors.Dark.primary)
        assertNotNull(EInkColors.Dark.secondary)
        assertNotNull(EInkColors.Dark.accent)
        assertNotNull(EInkColors.Dark.links)
        assertNotNull(EInkColors.Dark.success)
        assertNotNull(EInkColors.Dark.warning)
    }
}
