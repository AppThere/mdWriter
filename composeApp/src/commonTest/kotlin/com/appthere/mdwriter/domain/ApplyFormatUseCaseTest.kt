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
    fun `apply bold to selected text`() {
        val input = TextFieldValue(
            text = "Hello World",
            selection = TextRange(0, 5) // "Hello" selected
        )

        val result = useCase(input, MarkdownFormat.Bold)

        assertEquals("**Hello** World", result.text)
        assertEquals(7, result.selection.start) // Cursor after "Hello"
    }

    @Test
    fun `apply italic to selected text`() {
        val input = TextFieldValue(
            text = "Hello World",
            selection = TextRange(6, 11) // "World" selected
        )

        val result = useCase(input, MarkdownFormat.Italic)

        assertEquals("Hello *World*", result.text)
        assertEquals(12, result.selection.start) // Cursor after "World"
    }

    @Test
    fun `apply code to selected text`() {
        val input = TextFieldValue(
            text = "var x = 10",
            selection = TextRange(4, 10) // "x = 10" selected
        )

        val result = useCase(input, MarkdownFormat.Code)

        assertEquals("var `x = 10`", result.text)
    }

    @Test
    fun `apply heading1 to text`() {
        val input = TextFieldValue(
            text = "My Heading",
            selection = TextRange(0, 10)
        )

        val result = useCase(input, MarkdownFormat.Heading1)

        assertEquals("# My Heading\n", result.text)
    }

    @Test
    fun `apply heading2 to text`() {
        val input = TextFieldValue(
            text = "Subheading",
            selection = TextRange(0, 10)
        )

        val result = useCase(input, MarkdownFormat.Heading2)

        assertEquals("## Subheading\n", result.text)
    }

    @Test
    fun `apply blockquote to text`() {
        val input = TextFieldValue(
            text = "Quote text",
            selection = TextRange(0, 10)
        )

        val result = useCase(input, MarkdownFormat.Blockquote)

        assertEquals("> Quote text\n", result.text)
    }

    @Test
    fun `apply bullet list to text`() {
        val input = TextFieldValue(
            text = "Item 1",
            selection = TextRange(0, 6)
        )

        val result = useCase(input, MarkdownFormat.BulletList)

        assertEquals("- Item 1\n", result.text)
    }

    @Test
    fun `apply numbered list to text`() {
        val input = TextFieldValue(
            text = "Item 1",
            selection = TextRange(0, 6)
        )

        val result = useCase(input, MarkdownFormat.NumberedList)

        assertEquals("1. Item 1\n", result.text)
    }

    @Test
    fun `apply task list to text`() {
        val input = TextFieldValue(
            text = "Task",
            selection = TextRange(0, 4)
        )

        val result = useCase(input, MarkdownFormat.TaskList)

        assertEquals("- [ ] Task\n", result.text)
    }

    @Test
    fun `apply strikethrough to selected text`() {
        val input = TextFieldValue(
            text = "Remove this",
            selection = TextRange(7, 11) // "this" selected
        )

        val result = useCase(input, MarkdownFormat.Strikethrough)

        assertEquals("Remove ~~this~~", result.text)
    }

    @Test
    fun `apply link format`() {
        val input = TextFieldValue(
            text = "Click here",
            selection = TextRange(6, 10) // "here" selected
        )

        val linkFormat = MarkdownFormat.Link("https://example.com", "Example")
        val result = useCase(input, linkFormat)

        assertEquals("Click [here](https://example.com \"Example\")", result.text)
    }

    @Test
    fun `apply link format without title`() {
        val input = TextFieldValue(
            text = "Click here",
            selection = TextRange(6, 10)
        )

        val linkFormat = MarkdownFormat.Link("https://example.com")
        val result = useCase(input, linkFormat)

        assertEquals("Click [here](https://example.com)", result.text)
    }

    @Test
    fun `apply css class annotation`() {
        val input = TextFieldValue(
            text = "Styled text",
            selection = TextRange(7, 11) // "text" selected
        )

        val cssFormat = MarkdownFormat.CssClass("highlight")
        val result = useCase(input, cssFormat)

        assertEquals("Styled text {.highlight}", result.text)
    }

    @Test
    fun `apply format with empty selection`() {
        val input = TextFieldValue(
            text = "Some text",
            selection = TextRange(5, 5) // Cursor at position 5
        )

        val result = useCase(input, MarkdownFormat.Bold)

        assertEquals("Some ****text", result.text)
    }

    @Test
    fun `apply code block format`() {
        val input = TextFieldValue(
            text = "function test() {}",
            selection = TextRange(0, 18)
        )

        val result = useCase(input, MarkdownFormat.CodeBlock)

        assertEquals("```\nfunction test() {}\n```\n", result.text)
    }
}
