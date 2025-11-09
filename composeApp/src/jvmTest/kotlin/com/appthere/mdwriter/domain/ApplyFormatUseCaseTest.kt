package com.appthere.mdwriter.domain

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.appthere.mdwriter.domain.model.MarkdownFormat
import com.appthere.mdwriter.domain.usecase.ApplyFormatUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplyFormatUseCaseTest {
    private val useCase = ApplyFormatUseCase()

    @Test
    fun `apply bold to selected text keeps selection`() {
        val input = TextFieldValue(
            text = "Hello World",
            selection = TextRange(0, 5) // "Hello" selected
        )

        val result = useCase(input, MarkdownFormat.Bold)

        assertEquals("**Hello** World", result.text)
        // Selection should now be "Hello" (between the **)
        assertEquals(2, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `apply bold with no selection places cursor between marks`() {
        val input = TextFieldValue(
            text = "Hello World",
            selection = TextRange(5, 5) // Cursor at position 5
        )

        val result = useCase(input, MarkdownFormat.Bold)

        assertEquals("Hello**** World", result.text)
        // Cursor should be between the **
        assertEquals(7, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `apply italic to selected text keeps selection`() {
        val input = TextFieldValue(
            text = "Hello World",
            selection = TextRange(6, 11) // "World" selected
        )

        val result = useCase(input, MarkdownFormat.Italic)

        assertEquals("Hello *World*", result.text)
        // Selection should now be "World" (between the *)
        assertEquals(7, result.selection.start)
        assertEquals(12, result.selection.end)
    }

    @Test
    fun `apply code to selected text keeps selection`() {
        val input = TextFieldValue(
            text = "var x = 10",
            selection = TextRange(4, 10) // "x = 10" selected
        )

        val result = useCase(input, MarkdownFormat.Code)

        assertEquals("var `x = 10`", result.text)
        // Selection should be "x = 10" (between the `)
        assertEquals(5, result.selection.start)
        assertEquals(11, result.selection.end)
    }

    @Test
    fun `apply strikethrough to selected text keeps selection`() {
        val input = TextFieldValue(
            text = "Remove this",
            selection = TextRange(7, 11) // "this" selected
        )

        val result = useCase(input, MarkdownFormat.Strikethrough)

        assertEquals("Remove ~~this~~", result.text)
        // Selection should be "this" (between the ~~)
        assertEquals(9, result.selection.start)
        assertEquals(13, result.selection.end)
    }

    @Test
    fun `apply heading1 to line with cursor`() {
        val input = TextFieldValue(
            text = "My Heading",
            selection = TextRange(3, 3) // Cursor in middle of line
        )

        val result = useCase(input, MarkdownFormat.Heading1)

        assertEquals("# My Heading", result.text)
        // Cursor should move to account for added "# "
        assertEquals(5, result.selection.start)
    }

    @Test
    fun `apply heading2 to selected text on single line`() {
        val input = TextFieldValue(
            text = "Subheading text here",
            selection = TextRange(0, 10) // "Subheading" selected
        )

        val result = useCase(input, MarkdownFormat.Heading2)

        assertEquals("## Subheading text here", result.text)
        // Selection adjusted for added "## "
        assertEquals(3, result.selection.start)
        assertEquals(13, result.selection.end)
    }

    @Test
    fun `apply heading to multiple lines`() {
        val input = TextFieldValue(
            text = "Line 1\nLine 2\nLine 3",
            selection = TextRange(0, 20) // Select all lines
        )

        val result = useCase(input, MarkdownFormat.Heading1)

        assertEquals("# Line 1\n# Line 2\n# Line 3", result.text)
    }

    @Test
    fun `apply heading skips empty lines`() {
        val input = TextFieldValue(
            text = "Line 1\n\nLine 2",
            selection = TextRange(0, 14)
        )

        val result = useCase(input, MarkdownFormat.Heading2)

        assertEquals("## Line 1\n\n## Line 2", result.text)
    }

    @Test
    fun `apply heading does not duplicate markers`() {
        val input = TextFieldValue(
            text = "# Already a heading",
            selection = TextRange(0, 19)
        )

        val result = useCase(input, MarkdownFormat.Heading1)

        // Should not add more # symbols
        assertEquals("# Already a heading", result.text)
    }

    @Test
    fun `apply blockquote to selected text keeps selection`() {
        val input = TextFieldValue(
            text = "Quote text",
            selection = TextRange(0, 10)
        )

        val result = useCase(input, MarkdownFormat.Blockquote)

        assertEquals("> Quote text\n", result.text)
        // Selection adjusted for "> "
        assertEquals(2, result.selection.start)
        assertEquals(12, result.selection.end)
    }

    @Test
    fun `apply bullet list to text keeps selection`() {
        val input = TextFieldValue(
            text = "Item 1",
            selection = TextRange(0, 6)
        )

        val result = useCase(input, MarkdownFormat.BulletList)

        assertEquals("- Item 1\n", result.text)
        assertEquals(2, result.selection.start)
        assertEquals(8, result.selection.end)
    }

    @Test
    fun `apply numbered list to text keeps selection`() {
        val input = TextFieldValue(
            text = "Item 1",
            selection = TextRange(0, 6)
        )

        val result = useCase(input, MarkdownFormat.NumberedList)

        assertEquals("1. Item 1\n", result.text)
        assertEquals(3, result.selection.start)
        assertEquals(9, result.selection.end)
    }

    @Test
    fun `apply task list to text keeps selection`() {
        val input = TextFieldValue(
            text = "Task",
            selection = TextRange(0, 4)
        )

        val result = useCase(input, MarkdownFormat.TaskList)

        assertEquals("- [ ] Task\n", result.text)
        assertEquals(6, result.selection.start)
        assertEquals(10, result.selection.end)
    }

    @Test
    fun `apply link format to selected text`() {
        val input = TextFieldValue(
            text = "Click here",
            selection = TextRange(6, 10) // "here" selected
        )

        val linkFormat = MarkdownFormat.Link("https://example.com", "Example")
        val result = useCase(input, linkFormat)

        assertEquals("Click [here](https://example.com \"Example\")", result.text)
        // Selection should be "here" (between the [])
        assertEquals(7, result.selection.start)
        assertEquals(11, result.selection.end)
    }

    @Test
    fun `apply link format without title to selected text`() {
        val input = TextFieldValue(
            text = "Click here",
            selection = TextRange(6, 10)
        )

        val linkFormat = MarkdownFormat.Link("https://example.com")
        val result = useCase(input, linkFormat)

        assertEquals("Click [here](https://example.com)", result.text)
        assertEquals(7, result.selection.start)
        assertEquals(11, result.selection.end)
    }

    @Test
    fun `apply link format with no selection places cursor between brackets`() {
        val input = TextFieldValue(
            text = "Click ",
            selection = TextRange(6, 6) // Cursor at end
        )

        val linkFormat = MarkdownFormat.Link("https://example.com")
        val result = useCase(input, linkFormat)

        assertEquals("Click [](https://example.com)", result.text)
        // Cursor should be between the []
        assertEquals(7, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `apply css class annotation to selected text`() {
        val input = TextFieldValue(
            text = "Styled text",
            selection = TextRange(7, 11) // "text" selected
        )

        val cssFormat = MarkdownFormat.CssClass("highlight")
        val result = useCase(input, cssFormat)

        assertEquals("Styled text {.highlight}", result.text)
        // CSS class is a suffix, so selection stays on "text"
        assertEquals(7, result.selection.start)
        assertEquals(11, result.selection.end)
    }

    @Test
    fun `apply code block format to selected text`() {
        val input = TextFieldValue(
            text = "function test() {}",
            selection = TextRange(0, 18)
        )

        val result = useCase(input, MarkdownFormat.CodeBlock)

        assertEquals("```\nfunction test() {}\n```\n", result.text)
        // Selection adjusted for code block markers
        assertEquals(4, result.selection.start)
        assertEquals(22, result.selection.end)
    }

    @Test
    fun `apply code block with no selection places cursor inside`() {
        val input = TextFieldValue(
            text = "Some text",
            selection = TextRange(9, 9) // Cursor at end
        )

        val result = useCase(input, MarkdownFormat.CodeBlock)

        assertEquals("Some text\n```\n\n```\n", result.text)
        // Cursor should be inside the code block
        assertEquals(14, result.selection.start)
        assertEquals(14, result.selection.end)
    }
}
