package com.appthere.mdwriter.util

import com.appthere.mdwriter.domain.model.MarkdownNode
import com.appthere.mdwriter.domain.model.MarkdownNodeUtil
import kotlin.test.*

class MarkdownParserTest {

    private lateinit var parser: MarkdownParser

    @BeforeTest
    fun setup() {
        parser = MarkdownParser()
    }

    @Test
    fun `parse should handle simple paragraph`() {
        val markdown = "This is a simple paragraph."

        val result = parser.parse(markdown)

        assertTrue(result.ast is MarkdownNode.Document)
        val doc = result.ast as MarkdownNode.Document
        assertTrue(doc.children.isNotEmpty())
    }

    @Test
    fun `parse should extract headings`() {
        val markdown = """
            # Heading 1
            ## Heading 2
            ### Heading 3
        """.trimIndent()

        val result = parser.parse(markdown)

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(result.ast)
        assertTrue(headings.size >= 3)

        val h1 = headings.find { it.level == 1 }
        assertNotNull(h1)
        assertTrue(h1.text.contains("Heading 1"))

        val h2 = headings.find { it.level == 2 }
        assertNotNull(h2)

        val h3 = headings.find { it.level == 3 }
        assertNotNull(h3)
    }

    @Test
    fun `parse should handle CSS class annotations on headings`() {
        val markdown = "## My Heading {.special-heading}"

        val result = parser.parse(markdown)

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(result.ast)
        val heading = headings.firstOrNull()

        assertNotNull(heading)
        assertTrue(heading.cssClasses.contains("special-heading"))
        assertFalse(heading.text.contains("{.special-heading}"))
    }

    @Test
    fun `parse should handle multiple CSS classes`() {
        val markdown = "# Title {.class1 .class2 .class3}"

        val result = parser.parse(markdown)

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(result.ast)
        val heading = headings.firstOrNull()

        assertNotNull(heading)
        assertEquals(3, heading.cssClasses.size)
        assertTrue(heading.cssClasses.contains("class1"))
        assertTrue(heading.cssClasses.contains("class2"))
        assertTrue(heading.cssClasses.contains("class3"))
    }

    @Test
    fun `parse should handle strong emphasis`() {
        val markdown = "This is **bold text**."

        val result = parser.parse(markdown)

        val strong = MarkdownNodeUtil.findNodes<MarkdownNode.Strong>(result.ast)
        assertTrue(strong.isNotEmpty())
    }

    @Test
    fun `parse should handle emphasis`() {
        val markdown = "This is *italic text*."

        val result = parser.parse(markdown)

        val emphasis = MarkdownNodeUtil.findNodes<MarkdownNode.Emphasis>(result.ast)
        assertTrue(emphasis.isNotEmpty())
    }

    @Test
    fun `parse should handle inline code`() {
        val markdown = "Use the `code` function."

        val result = parser.parse(markdown)

        val code = MarkdownNodeUtil.findNodes<MarkdownNode.Code>(result.ast)
        assertTrue(code.isNotEmpty())
        assertEquals("code", code.first().content)
    }

    @Test
    fun `parse should handle code blocks`() {
        val markdown = """
            ```kotlin
            fun main() {
                println("Hello")
            }
            ```
        """.trimIndent()

        val result = parser.parse(markdown)

        val codeBlocks = MarkdownNodeUtil.findNodes<MarkdownNode.CodeBlock>(result.ast)
        assertTrue(codeBlocks.isNotEmpty())

        val codeBlock = codeBlocks.first()
        assertEquals("kotlin", codeBlock.language)
        assertTrue(codeBlock.content.contains("main"))
    }

    @Test
    fun `parse should handle code blocks without language`() {
        val markdown = """
            ```
            plain code
            ```
        """.trimIndent()

        val result = parser.parse(markdown)

        val codeBlocks = MarkdownNodeUtil.findNodes<MarkdownNode.CodeBlock>(result.ast)
        assertTrue(codeBlocks.isNotEmpty())

        val codeBlock = codeBlocks.first()
        assertNull(codeBlock.language)
    }

    @Test
    fun `parse should handle links`() {
        val markdown = "[Link text](https://example.com)"

        val result = parser.parse(markdown)

        val links = MarkdownNodeUtil.findNodes<MarkdownNode.Link>(result.ast)
        assertTrue(links.isNotEmpty())

        val link = links.first()
        assertEquals("https://example.com", link.destination)
    }

    @Test
    fun `parse should handle images`() {
        val markdown = "![Alt text](image.png)"

        val result = parser.parse(markdown)

        val images = MarkdownNodeUtil.findNodes<MarkdownNode.Image>(result.ast)
        assertTrue(images.isNotEmpty())

        val image = images.first()
        assertEquals("image.png", image.destination)
        assertEquals("Alt text", image.altText)
    }

    @Test
    fun `parse should handle unordered lists`() {
        val markdown = """
            - Item 1
            - Item 2
            - Item 3
        """.trimIndent()

        val result = parser.parse(markdown)

        val lists = MarkdownNodeUtil.findNodes<MarkdownNode.BulletList>(result.ast)
        assertTrue(lists.isNotEmpty())

        val list = lists.first()
        assertEquals(3, list.items.size)
    }

    @Test
    fun `parse should handle ordered lists`() {
        val markdown = """
            1. First
            2. Second
            3. Third
        """.trimIndent()

        val result = parser.parse(markdown)

        val lists = MarkdownNodeUtil.findNodes<MarkdownNode.OrderedList>(result.ast)
        assertTrue(lists.isNotEmpty())

        val list = lists.first()
        assertEquals(3, list.items.size)
    }

    @Test
    fun `parse should handle blockquotes`() {
        val markdown = "> This is a quote"

        val result = parser.parse(markdown)

        val blockquotes = MarkdownNodeUtil.findNodes<MarkdownNode.Blockquote>(result.ast)
        assertTrue(blockquotes.isNotEmpty())
    }

    @Test
    fun `parse should handle horizontal rules`() {
        val markdown = """
            Text above

            ---

            Text below
        """.trimIndent()

        val result = parser.parse(markdown)

        val rules = MarkdownNodeUtil.findNodes<MarkdownNode.HorizontalRule>(result.ast)
        assertTrue(rules.isNotEmpty())
    }

    @Test
    fun `parse should handle strikethrough`() {
        val markdown = "~~strikethrough text~~"

        val result = parser.parse(markdown)

        val strikethrough = MarkdownNodeUtil.findNodes<MarkdownNode.Strikethrough>(result.ast)
        assertTrue(strikethrough.isNotEmpty())
    }

    @Test
    fun `parse should handle tables`() {
        val markdown = """
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
        """.trimIndent()

        val result = parser.parse(markdown)

        val tables = MarkdownNodeUtil.findNodes<MarkdownNode.Table>(result.ast)
        assertTrue(tables.isNotEmpty())

        val table = tables.first()
        assertNotNull(table.header)
        assertTrue(table.rows.isNotEmpty())
    }

    @Test
    fun `parse should extract frontmatter`() {
        val markdown = """
            ---
            title: Test Document
            author: John Doe
            ---

            # Content
        """.trimIndent()

        val result = parser.parse(markdown)

        assertEquals("Test Document", result.frontmatter["title"])
        assertEquals("John Doe", result.frontmatter["author"])
        assertFalse(result.content.contains("---"))
    }

    @Test
    fun `parse should handle markdown without frontmatter`() {
        val markdown = "# Just Content"

        val result = parser.parse(markdown)

        assertTrue(result.frontmatter.isEmpty())
        assertEquals(markdown, result.content)
    }

    @Test
    fun `parse should handle complex nested structures`() {
        val markdown = """
            # Main Title

            This is a paragraph with **bold** and *italic* text.

            ## Subsection

            - List item with `code`
            - Another item with [a link](https://example.com)

            > A blockquote with **emphasis**
        """.trimIndent()

        val result = parser.parse(markdown)

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(result.ast)
        assertTrue(headings.size >= 2)

        val lists = MarkdownNodeUtil.findNodes<MarkdownNode.BulletList>(result.ast)
        assertTrue(lists.isNotEmpty())

        val blockquotes = MarkdownNodeUtil.findNodes<MarkdownNode.Blockquote>(result.ast)
        assertTrue(blockquotes.isNotEmpty())
    }

    @Test
    fun `extractText should extract all text from node tree`() {
        val markdown = "# Heading with **bold** text"

        val result = parser.parse(markdown)

        val text = MarkdownNodeUtil.extractText(result.ast)
        assertTrue(text.contains("Heading"))
        assertTrue(text.contains("bold"))
    }

    @Test
    fun `parse should handle CSS classes on paragraphs`() {
        val markdown = """
            This is a paragraph. {.highlight}
        """.trimIndent()

        val result = parser.parse(markdown)

        val paragraphs = MarkdownNodeUtil.findNodes<MarkdownNode.Paragraph>(result.ast)
        val paragraph = paragraphs.firstOrNull()

        assertNotNull(paragraph)
        assertTrue(paragraph.cssClasses.contains("highlight"))
    }

    @Test
    fun `parse should handle CSS classes on blockquotes`() {
        val markdown = "> Important quote {.callout .warning}"

        val result = parser.parse(markdown)

        val blockquotes = MarkdownNodeUtil.findNodes<MarkdownNode.Blockquote>(result.ast)
        val blockquote = blockquotes.firstOrNull()

        assertNotNull(blockquote)
        assertTrue(blockquote.cssClasses.contains("callout"))
        assertTrue(blockquote.cssClasses.contains("warning"))
    }

    @Test
    fun `parse should handle empty document`() {
        val markdown = ""

        val result = parser.parse(markdown)

        assertTrue(result.ast is MarkdownNode.Document)
        assertTrue(result.frontmatter.isEmpty())
    }

    @Test
    fun `parse should preserve source positions`() {
        val markdown = "# Heading"

        val result = parser.parse(markdown)

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(result.ast)
        val heading = headings.firstOrNull()

        assertNotNull(heading)
        assertTrue(heading.startOffset >= 0)
        assertTrue(heading.endOffset > heading.startOffset)
    }
}
