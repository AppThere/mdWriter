package com.appthere.mdwriter.domain.model

import kotlin.test.*

class MarkdownNodeTest {

    @Test
    fun `Heading should validate level range`() {
        assertFailsWith<IllegalArgumentException> {
            MarkdownNode.Heading(
                level = 0,
                text = "Invalid",
                startOffset = 0,
                endOffset = 10
            )
        }

        assertFailsWith<IllegalArgumentException> {
            MarkdownNode.Heading(
                level = 7,
                text = "Invalid",
                startOffset = 0,
                endOffset = 10
            )
        }
    }

    @Test
    fun `Heading should accept valid levels 1-6`() {
        for (level in 1..6) {
            val heading = MarkdownNode.Heading(
                level = level,
                text = "Heading $level",
                startOffset = 0,
                endOffset = 10
            )
            assertEquals(level, heading.level)
        }
    }

    @Test
    fun `extractText should extract text from Text node`() {
        val textNode = MarkdownNode.Text("Hello World", 0, 11)

        val result = MarkdownNodeUtil.extractText(textNode)

        assertEquals("Hello World", result)
    }

    @Test
    fun `extractText should extract text from Heading`() {
        val heading = MarkdownNode.Heading(
            level = 1,
            text = "Title",
            children = listOf(MarkdownNode.Text("Title", 0, 5)),
            startOffset = 0,
            endOffset = 7
        )

        val result = MarkdownNodeUtil.extractText(heading)

        assertEquals("Title", result)
    }

    @Test
    fun `extractText should extract text from Paragraph`() {
        val paragraph = MarkdownNode.Paragraph(
            children = listOf(
                MarkdownNode.Text("Hello ", 0, 6),
                MarkdownNode.Strong(
                    children = listOf(MarkdownNode.Text("world", 8, 13)),
                    startOffset = 6,
                    endOffset = 15
                )
            ),
            startOffset = 0,
            endOffset = 15
        )

        val result = MarkdownNodeUtil.extractText(paragraph)

        assertEquals("Hello world", result)
    }

    @Test
    fun `extractText should extract text from Code`() {
        val code = MarkdownNode.Code("println()", 0, 9)

        val result = MarkdownNodeUtil.extractText(code)

        assertEquals("println()", result)
    }

    @Test
    fun `extractText should extract text from CodeBlock`() {
        val codeBlock = MarkdownNode.CodeBlock(
            content = "fun main() { }",
            language = "kotlin",
            startOffset = 0,
            endOffset = 14
        )

        val result = MarkdownNodeUtil.extractText(codeBlock)

        assertEquals("fun main() { }", result)
    }

    @Test
    fun `extractText should extract alt text from Image`() {
        val image = MarkdownNode.Image(
            destination = "image.png",
            altText = "Description",
            startOffset = 0,
            endOffset = 20
        )

        val result = MarkdownNodeUtil.extractText(image)

        assertEquals("Description", result)
    }

    @Test
    fun `extractText should handle nested structures`() {
        val document = MarkdownNode.Document(
            children = listOf(
                MarkdownNode.Heading(
                    level = 1,
                    text = "Title",
                    children = listOf(MarkdownNode.Text("Title", 0, 5)),
                    startOffset = 0,
                    endOffset = 7
                ),
                MarkdownNode.Paragraph(
                    children = listOf(MarkdownNode.Text("Content", 8, 15)),
                    startOffset = 8,
                    endOffset = 15
                )
            ),
            startOffset = 0,
            endOffset = 15
        )

        val result = MarkdownNodeUtil.extractText(document)

        assertTrue(result.contains("Title"))
        assertTrue(result.contains("Content"))
    }

    @Test
    fun `extractText should handle lists`() {
        val list = MarkdownNode.BulletList(
            items = listOf(
                MarkdownNode.ListItem(
                    children = listOf(MarkdownNode.Text("Item 1", 0, 6)),
                    startOffset = 0,
                    endOffset = 6
                ),
                MarkdownNode.ListItem(
                    children = listOf(MarkdownNode.Text("Item 2", 7, 13)),
                    startOffset = 7,
                    endOffset = 13
                )
            ),
            startOffset = 0,
            endOffset = 13
        )

        val result = MarkdownNodeUtil.extractText(list)

        assertTrue(result.contains("Item 1"))
        assertTrue(result.contains("Item 2"))
    }

    @Test
    fun `extractText should return empty string for HorizontalRule`() {
        val rule = MarkdownNode.HorizontalRule(0, 3)

        val result = MarkdownNodeUtil.extractText(rule)

        assertEquals("", result)
    }

    @Test
    fun `findNodes should find all nodes of specific type`() {
        val document = MarkdownNode.Document(
            children = listOf(
                MarkdownNode.Heading(1, "Title", emptyList(), 0, 7),
                MarkdownNode.Paragraph(
                    children = listOf(
                        MarkdownNode.Text("Text with ", 0, 10),
                        MarkdownNode.Strong(
                            children = listOf(MarkdownNode.Text("bold", 10, 14)),
                            startOffset = 10,
                            endOffset = 14
                        )
                    ),
                    startOffset = 0,
                    endOffset = 14
                ),
                MarkdownNode.Heading(2, "Subtitle", emptyList(), 15, 25)
            ),
            startOffset = 0,
            endOffset = 25
        )

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(document)

        assertEquals(2, headings.size)
        assertEquals(1, headings[0].level)
        assertEquals(2, headings[1].level)
    }

    @Test
    fun `findNodes should find nested nodes`() {
        val document = MarkdownNode.Document(
            children = listOf(
                MarkdownNode.Paragraph(
                    children = listOf(
                        MarkdownNode.Text("Text with ", 0, 10),
                        MarkdownNode.Strong(
                            children = listOf(MarkdownNode.Text("bold", 10, 14)),
                            startOffset = 10,
                            endOffset = 14
                        ),
                        MarkdownNode.Text(" and ", 14, 19),
                        MarkdownNode.Emphasis(
                            children = listOf(MarkdownNode.Text("italic", 19, 25)),
                            startOffset = 19,
                            endOffset = 25
                        )
                    ),
                    startOffset = 0,
                    endOffset = 25
                )
            ),
            startOffset = 0,
            endOffset = 25
        )

        val strong = MarkdownNodeUtil.findNodes<MarkdownNode.Strong>(document)
        val emphasis = MarkdownNodeUtil.findNodes<MarkdownNode.Emphasis>(document)

        assertEquals(1, strong.size)
        assertEquals(1, emphasis.size)
    }

    @Test
    fun `findNodes should return empty list when no nodes match`() {
        val document = MarkdownNode.Document(
            children = listOf(
                MarkdownNode.Paragraph(
                    children = listOf(MarkdownNode.Text("Just text", 0, 9)),
                    startOffset = 0,
                    endOffset = 9
                )
            ),
            startOffset = 0,
            endOffset = 9
        )

        val headings = MarkdownNodeUtil.findNodes<MarkdownNode.Heading>(document)

        assertTrue(headings.isEmpty())
    }

    @Test
    fun `findNodes should include the root node if it matches`() {
        val document = MarkdownNode.Document(
            children = emptyList(),
            startOffset = 0,
            endOffset = 0
        )

        val documents = MarkdownNodeUtil.findNodes<MarkdownNode.Document>(document)

        assertEquals(1, documents.size)
        assertEquals(document, documents[0])
    }

    @Test
    fun `CSS classes should be stored correctly`() {
        val heading = MarkdownNode.Heading(
            level = 1,
            text = "Title",
            startOffset = 0,
            endOffset = 10,
            cssClasses = listOf("class1", "class2")
        )

        assertEquals(2, heading.cssClasses.size)
        assertTrue(heading.cssClasses.contains("class1"))
        assertTrue(heading.cssClasses.contains("class2"))
    }

    @Test
    fun `CellAlignment enum should have all values`() {
        val alignments = MarkdownNode.CellAlignment.values()

        assertEquals(4, alignments.size)
        assertTrue(alignments.contains(MarkdownNode.CellAlignment.NONE))
        assertTrue(alignments.contains(MarkdownNode.CellAlignment.LEFT))
        assertTrue(alignments.contains(MarkdownNode.CellAlignment.CENTER))
        assertTrue(alignments.contains(MarkdownNode.CellAlignment.RIGHT))
    }

    @Test
    fun `Table should support header and rows`() {
        val table = MarkdownNode.Table(
            header = MarkdownNode.TableRow(
                cells = listOf(
                    MarkdownNode.TableCell(
                        children = listOf(MarkdownNode.Text("Header", 0, 6)),
                        startOffset = 0,
                        endOffset = 6
                    )
                ),
                startOffset = 0,
                endOffset = 6
            ),
            rows = listOf(
                MarkdownNode.TableRow(
                    cells = listOf(
                        MarkdownNode.TableCell(
                            children = listOf(MarkdownNode.Text("Cell", 0, 4)),
                            startOffset = 0,
                            endOffset = 4
                        )
                    ),
                    startOffset = 0,
                    endOffset = 4
                )
            ),
            startOffset = 0,
            endOffset = 10
        )

        assertNotNull(table.header)
        assertEquals(1, table.rows.size)
    }

    @Test
    fun `TaskListItem should support checked state`() {
        val checkedItem = MarkdownNode.TaskListItem(
            checked = true,
            children = listOf(MarkdownNode.Text("Done", 0, 4)),
            startOffset = 0,
            endOffset = 4
        )

        val uncheckedItem = MarkdownNode.TaskListItem(
            checked = false,
            children = listOf(MarkdownNode.Text("Todo", 0, 4)),
            startOffset = 0,
            endOffset = 4
        )

        assertTrue(checkedItem.checked)
        assertFalse(uncheckedItem.checked)
    }
}
