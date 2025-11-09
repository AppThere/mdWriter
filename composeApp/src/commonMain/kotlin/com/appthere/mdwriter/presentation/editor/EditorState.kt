package com.appthere.mdwriter.presentation.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.appthere.mdwriter.data.model.Document

/**
 * State for the editor screen
 */
data class EditorState(
    // Current document being edited
    val document: Document? = null,

    // Current document path
    val documentPath: String? = null,

    // Current section being edited
    val currentSectionId: String? = null,

    // Editor text field value
    val editorContent: TextFieldValue = TextFieldValue(),

    // Loading states
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    // Undo/Redo stacks
    val undoStack: List<TextFieldValue> = emptyList(),
    val redoStack: List<TextFieldValue> = emptyList(),

    // Error state
    val error: String? = null,

    // Auto-save state
    val lastSavedTime: Long? = null,
    val hasUnsavedChanges: Boolean = false
) {
    /**
     * Check if undo is available
     */
    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    /**
     * Check if redo is available
     */
    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    /**
     * Get current section
     */
    fun getCurrentSection() = currentSectionId?.let { document?.getSection(it) }

    /**
     * Get all sections in order
     */
    fun getSectionsInOrder() = document?.getSectionsInOrder() ?: emptyList()
}
