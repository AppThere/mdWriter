package com.appthere.mdwriter.presentation.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.appthere.mdwriter.domain.model.MarkdownFormat

/**
 * User intents/actions for the editor
 */
sealed class EditorIntent {
    /**
     * Load a document from the specified path
     */
    data class LoadDocument(val path: String) : EditorIntent()

    /**
     * Create a new document
     */
    data object CreateNewDocument : EditorIntent()

    /**
     * Text content changed by user
     */
    data class TextChanged(val newText: TextFieldValue) : EditorIntent()

    /**
     * Apply Markdown formatting
     */
    data class ApplyFormat(val format: MarkdownFormat) : EditorIntent()

    /**
     * Manually trigger save
     */
    data object SaveDocument : EditorIntent()

    /**
     * Undo last change
     */
    data object Undo : EditorIntent()

    /**
     * Redo last undone change
     */
    data object Redo : EditorIntent()

    /**
     * Switch to a different section
     */
    data class SwitchSection(val sectionId: String) : EditorIntent()

    /**
     * Update document metadata
     */
    data class UpdateMetadata(
        val title: String? = null,
        val author: String? = null
    ) : EditorIntent()

    /**
     * Insert a link
     */
    data class InsertLink(val url: String, val title: String = "") : EditorIntent()

    /**
     * Insert an image
     */
    data class InsertImage(val url: String, val alt: String = "") : EditorIntent()

    /**
     * Add CSS class annotation
     */
    data class AddCssClass(val className: String) : EditorIntent()

    /**
     * Dismiss error
     */
    data object DismissError : EditorIntent()
}
