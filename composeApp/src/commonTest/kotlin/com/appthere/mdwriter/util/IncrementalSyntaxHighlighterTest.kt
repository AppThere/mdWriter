package com.appthere.mdwriter.util

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class IncrementalSyntaxHighlighterTest {

    private lateinit var parser: MarkdownParser
    private lateinit var highlighter: IncrementalSyntaxHighlighter

    @BeforeTest
    fun setup() {
        parser = MarkdownParser()
        highlighter = IncrementalSyntaxHighlighter(StyleConfig.light())
    }

    @Test
    fun `highlight should work for initial text`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)

        val result = highlighter.highlight(parseResult.ast, parseResult.content)

        assertNotNull(result)
        assertTrue(result.text.contains("Heading"))
    }

    @Test
    fun `highlight should handle text changes`() = runTest {
        val markdown1 = "# Heading 1"
        val parseResult1 = parser.parse(markdown1)
        val result1 = highlighter.highlight(parseResult1.ast, parseResult1.content)

        val markdown2 = "# Heading 2"
        val parseResult2 = parser.parse(markdown2)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content)

        assertNotNull(result1)
        assertNotNull(result2)
        assertTrue(result1.text.contains("Heading 1"))
        assertTrue(result2.text.contains("Heading 2"))
    }

    @Test
    fun `highlight should be efficient with similar text`() = runTest {
        val markdown1 = "This is a long paragraph with some text"
        val parseResult1 = parser.parse(markdown1)
        highlighter.highlight(parseResult1.ast, parseResult1.content)

        // Similar text (just one word changed)
        val markdown2 = "This is a long paragraph with more text"
        val parseResult2 = parser.parse(markdown2)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content)

        assertNotNull(result2)
        assertTrue(result2.text.contains("more text"))
    }

    @Test
    fun `highlight should handle completely different text`() = runTest {
        val markdown1 = "# Original"
        val parseResult1 = parser.parse(markdown1)
        highlighter.highlight(parseResult1.ast, parseResult1.content)

        val markdown2 = "Completely different text with no similarity"
        val parseResult2 = parser.parse(markdown2)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content)

        assertNotNull(result2)
        assertTrue(result2.text.contains("Completely different"))
    }

    @Test
    fun `clearCache should work`() = runTest {
        val markdown = "# Heading"
        val parseResult = parser.parse(markdown)
        highlighter.highlight(parseResult.ast, parseResult.content)

        highlighter.clearCache()

        val result = highlighter.highlight(parseResult.ast, parseResult.content)
        assertNotNull(result)
    }

    @Test
    fun `highlight should handle empty to non-empty transition`() = runTest {
        val markdown1 = ""
        val parseResult1 = parser.parse(markdown1)
        highlighter.highlight(parseResult1.ast, parseResult1.content)

        val markdown2 = "# New Content"
        val parseResult2 = parser.parse(markdown2)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content)

        assertNotNull(result2)
        assertTrue(result2.text.contains("New Content"))
    }

    @Test
    fun `highlight should handle non-empty to empty transition`() = runTest {
        val markdown1 = "# Some Content"
        val parseResult1 = parser.parse(markdown1)
        highlighter.highlight(parseResult1.ast, parseResult1.content)

        val markdown2 = ""
        val parseResult2 = parser.parse(markdown2)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content)

        assertNotNull(result2)
        assertEquals("", result2.text)
    }

    @Test
    fun `highlight should handle incremental additions`() = runTest {
        val markdown1 = "# Title"
        val parseResult1 = parser.parse(markdown1)
        highlighter.highlight(parseResult1.ast, parseResult1.content)

        val markdown2 = "# Title\n\nParagraph"
        val parseResult2 = parser.parse(markdown2)
        val result2 = highlighter.highlight(parseResult2.ast, parseResult2.content)

        val markdown3 = "# Title\n\nParagraph\n\n- List"
        val parseResult3 = parser.parse(markdown3)
        val result3 = highlighter.highlight(parseResult3.ast, parseResult3.content)

        assertNotNull(result2)
        assertNotNull(result3)
        assertTrue(result3.text.contains("List"))
    }
}
