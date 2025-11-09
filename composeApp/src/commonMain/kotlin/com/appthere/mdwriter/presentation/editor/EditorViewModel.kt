package com.appthere.mdwriter.presentation.editor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.domain.model.MarkdownFormat
import com.appthere.mdwriter.domain.usecase.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.appthere.mdwriter.util.now

/**
 * ViewModel for the editor screen using MVI pattern
 */
class EditorViewModel(
    private val loadDocumentUseCase: LoadDocumentUseCase,
    private val saveDocumentUseCase: SaveDocumentUseCase,
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val applyFormatUseCase: ApplyFormatUseCase,
    private val updateSectionContentUseCase: UpdateSectionContentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var autoSaveJob: Job? = null
    private val undoStackMaxSize = 50

    companion object {
        private const val AUTO_SAVE_DELAY_MS = 2000L // 2 seconds
    }

    /**
     * Process user intents
     */
    fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadDocument -> loadDocument(intent.path)
            is EditorIntent.CreateNewDocument -> createNewDocument()
            is EditorIntent.TextChanged -> onTextChanged(intent.newText)
            is EditorIntent.ApplyFormat -> applyFormat(intent.format)
            is EditorIntent.SaveDocument -> saveDocument()
            is EditorIntent.Undo -> undo()
            is EditorIntent.Redo -> redo()
            is EditorIntent.SwitchSection -> switchSection(intent.sectionId)
            is EditorIntent.UpdateMetadata -> updateMetadata(intent.title, intent.author)
            is EditorIntent.InsertLink -> insertLink(intent.url, intent.title)
            is EditorIntent.InsertImage -> insertImage(intent.url, intent.alt)
            is EditorIntent.AddCssClass -> addCssClass(intent.className)
            is EditorIntent.DismissError -> dismissError()
        }
    }

    /**
     * Load a document from storage
     */
    private fun loadDocument(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            loadDocumentUseCase(path).fold(
                onSuccess = { document ->
                    val firstSectionId = document.spine.firstOrNull()
                    val firstSection = firstSectionId?.let { document.getSection(it) }

                    _state.update {
                        it.copy(
                            document = document,
                            documentPath = path,
                            currentSectionId = firstSectionId,
                            editorContent = TextFieldValue(firstSection?.content ?: ""),
                            isLoading = false,
                            hasUnsavedChanges = false,
                            undoStack = emptyList(),
                            redoStack = emptyList()
                        )
                    }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load document: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new document
     */
    private fun createNewDocument() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            createDocumentUseCase().fold(
                onSuccess = { document ->
                    val firstSectionId = document.spine.firstOrNull()
                    val firstSection = firstSectionId?.let { document.getSection(it) }

                    _state.update {
                        it.copy(
                            document = document,
                            documentPath = null, // New document not yet saved
                            currentSectionId = firstSectionId,
                            editorContent = TextFieldValue(firstSection?.content ?: ""),
                            isLoading = false,
                            hasUnsavedChanges = false,
                            undoStack = emptyList(),
                            redoStack = emptyList()
                        )
                    }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to create document: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Handle text changes with undo stack management and auto-save
     */
    private fun onTextChanged(newText: TextFieldValue) {
        val currentState = _state.value
        val currentText = currentState.editorContent

        // Add to undo stack
        val newUndoStack = (currentState.undoStack + currentText)
            .takeLast(undoStackMaxSize)

        _state.update {
            it.copy(
                editorContent = newText,
                undoStack = newUndoStack,
                redoStack = emptyList(), // Clear redo stack on new changes
                hasUnsavedChanges = true
            )
        }

        // Schedule auto-save
        scheduleAutoSave()
    }

    /**
     * Apply Markdown formatting
     */
    private fun applyFormat(format: MarkdownFormat) {
        val currentText = _state.value.editorContent
        val formattedText = applyFormatUseCase(currentText, format)

        onTextChanged(formattedText)
    }

    /**
     * Insert a link at cursor position
     */
    private fun insertLink(url: String, title: String) {
        applyFormat(MarkdownFormat.Link(url, title))
    }

    /**
     * Insert an image at cursor position
     */
    private fun insertImage(url: String, alt: String) {
        applyFormat(MarkdownFormat.Image(url, alt))
    }

    /**
     * Add CSS class annotation
     */
    private fun addCssClass(className: String) {
        applyFormat(MarkdownFormat.CssClass(className))
    }

    /**
     * Save the current document
     */
    private fun saveDocument() {
        val currentState = _state.value
        val document = currentState.document ?: return
        val path = currentState.documentPath ?: return
        val sectionId = currentState.currentSectionId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            // Update section content in document
            val updatedDocument = updateSectionContentUseCase(
                document,
                sectionId,
                currentState.editorContent.text
            )

            saveDocumentUseCase(path, updatedDocument).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            document = updatedDocument,
                            isSaving = false,
                            hasUnsavedChanges = false,
                            lastSavedTime = now().toEpochMilliseconds()
                        )
                    }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = "Failed to save document: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Schedule auto-save with debouncing
     */
    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            if (_state.value.hasUnsavedChanges && _state.value.documentPath != null) {
                saveDocument()
            }
        }
    }

    /**
     * Undo last change
     */
    private fun undo() {
        val currentState = _state.value
        if (!currentState.canUndo) return

        val previousText = currentState.undoStack.last()
        val newUndoStack = currentState.undoStack.dropLast(1)
        val newRedoStack = currentState.redoStack + currentState.editorContent

        _state.update {
            it.copy(
                editorContent = previousText,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                hasUnsavedChanges = true
            )
        }

        scheduleAutoSave()
    }

    /**
     * Redo last undone change
     */
    private fun redo() {
        val currentState = _state.value
        if (!currentState.canRedo) return

        val nextText = currentState.redoStack.last()
        val newRedoStack = currentState.redoStack.dropLast(1)
        val newUndoStack = currentState.undoStack + currentState.editorContent

        _state.update {
            it.copy(
                editorContent = nextText,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                hasUnsavedChanges = true
            )
        }

        scheduleAutoSave()
    }

    /**
     * Switch to a different section
     */
    private fun switchSection(sectionId: String) {
        val currentState = _state.value
        val document = currentState.document ?: return

        // Save current section content before switching
        if (currentState.currentSectionId != null) {
            val updatedDocument = updateSectionContentUseCase(
                document,
                currentState.currentSectionId,
                currentState.editorContent.text
            )

            _state.update { it.copy(document = updatedDocument) }
        }

        // Load new section content
        val newSection = document.getSection(sectionId)
        if (newSection != null) {
            _state.update {
                it.copy(
                    currentSectionId = sectionId,
                    editorContent = TextFieldValue(newSection.content),
                    undoStack = emptyList(),
                    redoStack = emptyList()
                )
            }
        }
    }

    /**
     * Update document metadata
     */
    private fun updateMetadata(title: String?, author: String?) {
        val currentState = _state.value
        val document = currentState.document ?: return

        val updatedMetadata = document.metadata.copy(
            title = title ?: document.metadata.title,
            author = author ?: document.metadata.author
        )

        _state.update {
            it.copy(
                document = document.copy(metadata = updatedMetadata),
                hasUnsavedChanges = true
            )
        }

        scheduleAutoSave()
    }

    /**
     * Dismiss error message
     */
    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
