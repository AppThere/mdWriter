package com.appthere.mdwriter.util

import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.system.measureTimeMillis

class SyntaxHighlighterPerformanceTest {

    private lateinit var parser: MarkdownParser
    private lateinit var highlighter: SyntaxHighlighter

    @BeforeTest
    fun setup() {
        parser = MarkdownParser()
        highlighter = SyntaxHighlighter(StyleConfig.light())
    }

    @Test
    fun `highlight should handle small document efficiently`() = runTest {
        val markdown = """
            # Small Document

            This is a small document with some **bold** and *italic* text.
        """.trimIndent()
        val parseResult = parser.parse(markdown)

        val time = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content)
        }

        // Small documents should be very fast (< 100ms)
        assertTrue(time < 100, "Small document took $time ms, expected < 100ms")
    }

    @Test
    fun `highlight should handle medium document efficiently`() = runTest {
        val markdown = buildMediumDocument()
        val parseResult = parser.parse(markdown)

        val time = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content)
        }

        // Medium documents should be fast (< 500ms)
        assertTrue(time < 500, "Medium document took $time ms, expected < 500ms")
    }

    @Test
    fun `highlight should handle large document`() = runTest {
        val markdown = buildLargeDocument()
        val parseResult = parser.parse(markdown)

        val time = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content)
        }

        // Large documents should complete (< 2000ms)
        assertTrue(time < 2000, "Large document took $time ms, expected < 2000ms")
    }

    @Test
    fun `cache should improve performance on repeated highlights`() = runTest {
        val markdown = buildMediumDocument()
        val parseResult = parser.parse(markdown)

        // First highlight (not cached)
        val time1 = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)
        }

        // Second highlight (should use cache)
        val time2 = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)
        }

        // Cached version should be faster or equal
        assertTrue(time2 <= time1, "Cached highlight ($time2 ms) should be <= first highlight ($time1 ms)")
    }

    @Test
    fun `incremental highlighter should handle large document changes efficiently`() = runTest {
        val incrementalHighlighter = IncrementalSyntaxHighlighter(StyleConfig.light())
        val markdown1 = buildLargeDocument()
        val parseResult1 = parser.parse(markdown1)

        // First highlight
        incrementalHighlighter.highlight(parseResult1.ast, parseResult1.content)

        // Small change
        val markdown2 = markdown1 + "\n\nAdditional paragraph."
        val parseResult2 = parser.parse(markdown2)

        val time = measureTimeMillis {
            incrementalHighlighter.highlight(parseResult2.ast, parseResult2.content)
        }

        // Incremental update should be reasonably fast
        assertTrue(time < 2000, "Incremental update took $time ms, expected < 2000ms")
    }

    @Test
    fun `highlight should handle document with many headings`() = runTest {
        val markdown = buildString {
            repeat(100) { i ->
                appendLine("# Heading $i")
                appendLine()
            }
        }
        val parseResult = parser.parse(markdown)

        val time = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content)
        }

        assertTrue(time < 1000, "100 headings took $time ms, expected < 1000ms")
    }

    @Test
    fun `highlight should handle document with many inline styles`() = runTest {
        val markdown = buildString {
            repeat(50) {
                append("This has **bold** and *italic* and `code` and [links](http://example.com). ")
            }
        }
        val parseResult = parser.parse(markdown)

        val time = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content)
        }

        assertTrue(time < 1000, "Many inline styles took $time ms, expected < 1000ms")
    }

    @Test
    fun `highlight should handle deeply nested lists`() = runTest {
        val markdown = buildString {
            repeat(20) { depth ->
                appendLine("${"  ".repeat(depth)}- Item at depth $depth")
            }
        }
        val parseResult = parser.parse(markdown)

        val time = measureTimeMillis {
            highlighter.highlight(parseResult.ast, parseResult.content)
        }

        assertTrue(time < 500, "Nested lists took $time ms, expected < 500ms")
    }

    @Test
    fun `clearCache should complete quickly`() = runTest {
        val markdown = buildLargeDocument()
        val parseResult = parser.parse(markdown)

        // Populate cache
        highlighter.highlight(parseResult.ast, parseResult.content, useCache = true)

        val time = measureTimeMillis {
            highlighter.clearCache()
        }

        assertTrue(time < 100, "Clear cache took $time ms, expected < 100ms")
    }

    // Helper functions to build test documents

    private fun buildMediumDocument(): String = buildString {
        appendLine("# Medium Document")
        appendLine()
        repeat(10) { section ->
            appendLine("## Section $section")
            appendLine()
            appendLine("This is a paragraph with **bold** and *italic* text.")
            appendLine()
            appendLine("- List item 1")
            appendLine("- List item 2")
            appendLine("- List item 3")
            appendLine()
            appendLine("```kotlin")
            appendLine("fun example() {")
            appendLine("    println(\"Hello\")")
            appendLine("}")
            appendLine("```")
            appendLine()
        }
    }

    private fun buildLargeDocument(): String = buildString {
        appendLine("# Large Document")
        appendLine()
        repeat(50) { section ->
            appendLine("## Section $section")
            appendLine()
            repeat(5) { para ->
                appendLine("This is paragraph $para with **bold** and *italic* and `code` text. ")
                appendLine("It has [links](http://example.com) and more content.")
                appendLine()
            }
            appendLine("- List item 1")
            appendLine("- List item 2 with **bold**")
            appendLine("- List item 3 with *italic*")
            appendLine()
            if (section % 5 == 0) {
                appendLine("```kotlin")
                appendLine("fun section$section() {")
                appendLine("    println(\"Section $section\")")
                appendLine("}")
                appendLine("```")
                appendLine()
            }
        }
    }
}
