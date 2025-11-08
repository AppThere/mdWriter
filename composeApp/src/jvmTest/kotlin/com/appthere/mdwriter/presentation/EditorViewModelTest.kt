package com.appthere.mdwriter.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.appthere.mdwriter.data.model.*
import com.appthere.mdwriter.data.repository.InMemoryDocumentRepository
import com.appthere.mdwriter.domain.model.MarkdownFormat
import com.appthere.mdwriter.domain.model.Result
import com.appthere.mdwriter.domain.usecase.*
import com.appthere.mdwriter.presentation.editor.EditorIntent
import com.appthere.mdwriter.presentation.editor.EditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.Clock
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    private lateinit var repository: InMemoryDocumentRepository
    private lateinit var viewModel: EditorViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryDocumentRepository()

        viewModel = EditorViewModel(
            loadDocumentUseCase = LoadDocumentUseCase(repository),
            saveDocumentUseCase = SaveDocumentUseCase(repository),
            createDocumentUseCase = CreateDocumentUseCase(repository),
            applyFormatUseCase = ApplyFormatUseCase(),
            updateSectionContentUseCase = UpdateSectionContentUseCase()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.state.value

        assertNull(state.document)
        assertNull(state.documentPath)
        assertNull(state.currentSectionId)
        assertEquals(TextFieldValue(), state.editorContent)
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertTrue(state.undoStack.isEmpty())
        assertTrue(state.redoStack.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `create new document sets initial state`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertNotNull(state.document)
        assertEquals("Untitled Document", state.document?.metadata?.title)
        assertNull(state.documentPath) // Not saved yet
        assertNotNull(state.currentSectionId)
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `load document updates state correctly`() = runTest {
        // Setup: Create and save a document
        val now = Clock.System.now()
        val document = Document(
            metadata = Metadata(
                title = "Test Document",
                author = "Test Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf(
                "sec-1" to Section(
                    id = "sec-1",
                    content = "Test content",
                    order = 0
                )
            )
        )
        repository.saveDocument("test.json", document)

        // Test: Load the document
        viewModel.handleIntent(EditorIntent.LoadDocument("test.json"))
        advanceUntilIdle()

        val state = viewModel.state.value

        assertNotNull(state.document)
        assertEquals("Test Document", state.document?.metadata?.title)
        assertEquals("test.json", state.documentPath)
        assertEquals("sec-1", state.currentSectionId)
        assertEquals("Test content", state.editorContent.text)
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `text changed updates state and marks unsaved`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        val newText = TextFieldValue("New text content")
        viewModel.handleIntent(EditorIntent.TextChanged(newText))
        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals("New text content", state.editorContent.text)
        assertTrue(state.hasUnsavedChanges)
        assertEquals(1, state.undoStack.size) // Previous state added to undo
        assertTrue(state.redoStack.isEmpty())
    }

    @Test
    fun `apply bold format works correctly`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        // Set some text with selection
        val text = TextFieldValue(
            text = "Hello World",
            selection = TextRange(0, 5) // "Hello" selected
        )
        viewModel.handleIntent(EditorIntent.TextChanged(text))
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.ApplyFormat(MarkdownFormat.Bold))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("**Hello** World", state.editorContent.text)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `undo restores previous text`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        val text1 = TextFieldValue("First")
        viewModel.handleIntent(EditorIntent.TextChanged(text1))
        advanceUntilIdle()

        val text2 = TextFieldValue("Second")
        viewModel.handleIntent(EditorIntent.TextChanged(text2))
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.Undo)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("First", state.editorContent.text)
        assertTrue(state.canRedo)
    }

    @Test
    fun `redo restores undone text`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        val text1 = TextFieldValue("First")
        viewModel.handleIntent(EditorIntent.TextChanged(text1))
        advanceUntilIdle()

        val text2 = TextFieldValue("Second")
        viewModel.handleIntent(EditorIntent.TextChanged(text2))
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.Undo)
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.Redo)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Second", state.editorContent.text)
    }

    @Test
    fun `save document updates state`() = runTest {
        // Create document and save with path
        val now = Clock.System.now()
        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf(
                "sec-1" to Section(id = "sec-1", content = "", order = 0)
            )
        )
        repository.saveDocument("test.json", document)

        viewModel.handleIntent(EditorIntent.LoadDocument("test.json"))
        advanceUntilIdle()

        // Make changes
        viewModel.handleIntent(EditorIntent.TextChanged(TextFieldValue("Modified content")))
        advanceUntilIdle()

        // Save
        viewModel.handleIntent(EditorIntent.SaveDocument)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.hasUnsavedChanges)
        assertFalse(state.isSaving)
        assertNotNull(state.lastSavedTime)
    }

    @Test
    fun `update metadata changes document`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.UpdateMetadata(title = "New Title", author = "New Author"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("New Title", state.document?.metadata?.title)
        assertEquals("New Author", state.document?.metadata?.author)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `insert link formats correctly`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        val text = TextFieldValue(
            text = "Click here",
            selection = TextRange(6, 10) // "here" selected
        )
        viewModel.handleIntent(EditorIntent.TextChanged(text))
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.InsertLink("https://example.com", "Example"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.editorContent.text.contains("[here](https://example.com"))
    }

    @Test
    fun `dismiss error clears error state`() = runTest {
        // Force an error by trying to load non-existent document
        viewModel.handleIntent(EditorIntent.LoadDocument("non-existent.json"))
        advanceUntilIdle()

        var state = viewModel.state.value
        assertNotNull(state.error)

        viewModel.handleIntent(EditorIntent.DismissError)
        advanceUntilIdle()

        state = viewModel.state.value
        assertNull(state.error)
    }

    @Test
    fun `undo stack has maximum size`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        // Add more than max undo stack size (50)
        repeat(60) { i ->
            viewModel.handleIntent(EditorIntent.TextChanged(TextFieldValue("Text $i")))
            advanceUntilIdle()
        }

        val state = viewModel.state.value
        assertTrue(state.undoStack.size <= 50)
    }

    @Test
    fun `new changes clear redo stack`() = runTest {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.TextChanged(TextFieldValue("First")))
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.TextChanged(TextFieldValue("Second")))
        advanceUntilIdle()

        viewModel.handleIntent(EditorIntent.Undo)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.canRedo)

        // New change should clear redo stack
        viewModel.handleIntent(EditorIntent.TextChanged(TextFieldValue("Third")))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canRedo)
        assertTrue(viewModel.state.value.redoStack.isEmpty())
    }
}
