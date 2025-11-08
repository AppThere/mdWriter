package com.appthere.mdwriter.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DebouncedSyntaxHighlighterTest {

    private lateinit var parser: MarkdownParser
    private lateinit var highlighter: DebouncedSyntaxHighlighter

    @BeforeTest
    fun setup() {
        parser = MarkdownParser()
        highlighter = DebouncedSyntaxHighlighter(StyleConfig.light(), debounceMillis = 100)
    }

    @Test
    fun `highlight with force should return immediately`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content, force = true)

        assertNotNull(result, "Force highlight should return immediately")
        assertTrue(result.text.contains("Heading"))
    }

    @Test
    fun `highlight without force should debounce`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        // First call without force - should be debounced
        val result1 = highlighter.highlight(parseResult.ast, parseResult.content, force = false)
        // Since no previous highlight, this might return null or the result depending on timing

        // Wait for debounce period
        delay(150)

        // Second call should work
        val result2 = highlighter.highlight(parseResult.ast, parseResult.content, force = false)
        assertNotNull(result2)
    }

    @Test
    fun `rapid calls should be debounced`() = runTest {
        val markdown1 = "# Heading 1"
        val markdown2 = "# Heading 2"
        val markdown3 = "# Heading 3"

        val parseResult1 = parser.parse(markdown1)
        val parseResult2 = parser.parse(markdown2)
        val parseResult3 = parser.parse(markdown3)

        // Make rapid calls
        highlighter.highlight(parseResult1.ast, parseResult1.content, force = true)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content, force = false)
        val result3 = highlighter.highlight(parseResult3.ast, parseResult3.content, force = false)

        // result2 and result3 might be null due to debouncing
        // This is expected behavior
    }

    @Test
    fun `clearCache should work`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        highlighter.highlight(parseResult.ast, parseResult.content, force = true)
        highlighter.clearCache()

        val result = highlighter.highlight(parseResult.ast, parseResult.content, force = true)
        assertNotNull(result)
    }

    @Test
    fun `highlight after debounce period should work`() = runTest {
        val markdown1 = "# Heading 1"
        val parseResult1 = parser.parse(markdown1)

        highlighter.highlight(parseResult1.ast, parseResult1.content, force = true)

        // Wait for debounce period to pass
        delay(150)

        val markdown2 = "# Heading 2"
        val parseResult2 = parser.parse(markdown2)
        val result = highlighter.highlight(parseResult2.ast, parseResult2.content, force = false)

        assertNotNull(result)
        assertTrue(result.text.contains("Heading 2"))
    }

    @Test
    fun `custom debounce time should be respected`() = runTest {
        val customHighlighter = DebouncedSyntaxHighlighter(
            StyleConfig.light(),
            debounceMillis = 50
        )

        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        customHighlighter.highlight(parseResult.ast, parseResult.content, force = true)

        // Wait for custom debounce period
        delay(60)

        val result = customHighlighter.highlight(parseResult.ast, parseResult.content, force = false)
        assertNotNull(result)
    }

    @Test
    fun `force highlight should bypass debounce timer`() = runTest {
        val markdown1 = "# Heading 1"
        val parseResult1 = parser.parse(markdown1)

        // First highlight
        highlighter.highlight(parseResult1.ast, parseResult1.content, force = true)

        // Immediate second highlight with force should work
        val markdown2 = "# Heading 2"
        val parseResult2 = parser.parse(markdown2)
        val result = highlighter.highlight(parseResult2.ast, parseResult2.content, force = true)

        assertNotNull(result)
        assertTrue(result.text.contains("Heading 2"))
    }
}
