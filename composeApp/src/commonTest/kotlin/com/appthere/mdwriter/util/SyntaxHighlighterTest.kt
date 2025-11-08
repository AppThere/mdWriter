package com.appthere.mdwriter.util

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.appthere.mdwriter.domain.model.MarkdownNode
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SyntaxHighlighterTest {

    private lateinit var parser: MarkdownParser
    private lateinit var highlighter: SyntaxHighlighter
    private lateinit var config: StyleConfig

    @BeforeTest
    fun setup() {
        parser = MarkdownParser()
        config = StyleConfig.light()
        highlighter = SyntaxHighlighter(config)
    }

    @Test
    fun `highlight should style headings`() = runTest {
        val markdown = "# Heading 1"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertTrue(result.spanStyles.isNotEmpty())

        // Find heading style
        val headingStyle = result.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        assertNotNull(headingStyle, "Heading should be bold")
    }

    @Test
    fun `highlight should style strong text`() = runTest {
        val markdown = "This is **bold** text"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val strongStyles = result.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertTrue(strongStyles.isNotEmpty(), "Should have bold styles")
    }

    @Test
    fun `highlight should style emphasis`() = runTest {
        val markdown = "This is *italic* text"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val emphasisStyles = result.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertTrue(emphasisStyles.isNotEmpty(), "Should have italic styles")
    }

    @Test
    fun `highlight should style inline code`() = runTest {
        val markdown = "Use the `code` function"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val codeStyles = result.spanStyles.filter { it.item.fontFamily != null }
        assertTrue(codeStyles.isNotEmpty(), "Should have monospace font for code")
    }

    @Test
    fun `highlight should style code blocks`() = runTest {
        val markdown = """
            ```kotlin
            fun main() {
                println("Hello")
            }
            ```
        """.trimIndent()
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val codeBlockStyles = result.spanStyles.filter { it.item.fontFamily != null }
        assertTrue(codeBlockStyles.isNotEmpty(), "Should have monospace font for code block")
    }

    @Test
    fun `highlight should style links`() = runTest {
        val markdown = "[Link](https://example.com)"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val linkStyles = result.spanStyles.filter {
            it.item.color == config.linkStyle.color
        }
        assertTrue(linkStyles.isNotEmpty(), "Should have link color")
    }

    @Test
    fun `highlight should handle nested styles`() = runTest {
        val markdown = "This is **bold with *italic* inside**"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val boldStyles = result.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        val italicStyles = result.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }

        assertTrue(boldStyles.isNotEmpty(), "Should have bold styles")
        assertTrue(italicStyles.isNotEmpty(), "Should have italic styles")
    }

    @Test
    fun `highlight should style blockquotes`() = runTest {
        val markdown = "> This is a quote"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val quoteStyles = result.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertTrue(quoteStyles.isNotEmpty(), "Should have italic style for blockquote")
    }

    @Test
    fun `highlight should style strikethrough`() = runTest {
        val markdown = "~~strikethrough~~"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val strikeStyles = result.spanStyles.filter { it.item.textDecoration != null }
        assertTrue(strikeStyles.isNotEmpty(), "Should have text decoration for strikethrough")
    }

    @Test
    fun `highlight should handle empty document`() = runTest {
        val markdown = ""
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertEquals("", result.text)
    }

    @Test
    fun `highlight should handle plain text`() = runTest {
        val markdown = "Just plain text"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertTrue(result.text.contains("plain text"))
    }

    @Test
    fun `highlight should use cache when enabled`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        val result1 = highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)
        val result2 = highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)

        assertEquals(result1.text, result2.text)
    }

    @Test
    fun `clearCache should invalidate cache`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)
        highlighter.clearCache()

        // Should still work after clearing cache
        val result = highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)
        assertNotNull(result)
    }

    @Test
    fun `highlight should handle complex nested document`() = runTest {
        val markdown = """
            # Title

            This is a paragraph with **bold** and *italic* text.

            ## Subsection

            - List item with `code`
            - Another item with [link](https://example.com)

            > Blockquote with **emphasis**
        """.trimIndent()
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertTrue(result.spanStyles.isNotEmpty(), "Should have styles applied")

        // Verify we have different types of styles
        val hasBold = result.spanStyles.any { it.item.fontWeight == FontWeight.Bold }
        val hasItalic = result.spanStyles.any { it.item.fontStyle == FontStyle.Italic }
        val hasCode = result.spanStyles.any { it.item.fontFamily != null }
        val hasLink = result.spanStyles.any { it.item.color == config.linkStyle.color }

        assertTrue(hasBold, "Should have bold text")
        assertTrue(hasItalic, "Should have italic text")
        assertTrue(hasCode, "Should have code")
        assertTrue(hasLink, "Should have links")
    }

    @Test
    fun `highlight should work with dark theme`() = runTest {
        val darkConfig = StyleConfig.dark()
        val darkHighlighter = SyntaxHighlighter(darkConfig)
        val markdown = "# Dark Theme"
        val parseResult = parser.parse(markdown)

        val result = darkHighlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        val headingStyle = result.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        assertNotNull(headingStyle, "Should have heading style")
    }

    @Test
    fun `highlight should handle lists`() = runTest {
        val markdown = """
            - Item 1
            - Item 2
            - Item 3
        """.trimIndent()
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertTrue(result.text.contains("Item"))
    }

    @Test
    fun `highlight should handle tables`() = runTest {
        val markdown = """
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
        """.trimIndent()
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertTrue(result.text.contains("Header"))
    }
}
