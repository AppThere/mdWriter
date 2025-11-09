package com.appthere.mdwriter.presentation.documents

import com.appthere.mdwriter.data.local.RecentDocument
import com.appthere.mdwriter.data.model.DocumentInfo

/**
 * UI state for document list screen
 */
data class DocumentListState(
    val documents: List<DocumentInfo> = emptyList(),
    val recentDocuments: List<RecentDocument> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.MODIFIED_DESC,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val documentToDelete: DocumentInfo? = null,
    val showRenameDialog: Boolean = false,
    val documentToRename: DocumentInfo? = null
)

/**
 * Sort options for document list
 */
enum class SortOption(val label: String) {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    MODIFIED_DESC("Recently Modified"),
    MODIFIED_ASC("Oldest Modified"),
    CREATED_DESC("Recently Created"),
    CREATED_ASC("Oldest Created"),
    AUTHOR_ASC("Author (A-Z)"),
    AUTHOR_DESC("Author (Z-A)")
}

/**
 * User intents/actions for document list
 */
sealed class DocumentListIntent {
    data class SearchDocuments(val query: String) : DocumentListIntent()
    data class ChangeSortOption(val sortOption: SortOption) : DocumentListIntent()
    data class DeleteDocument(val documentInfo: DocumentInfo) : DocumentListIntent()
    data class RenameDocument(val documentInfo: DocumentInfo, val newTitle: String) : DocumentListIntent()
    data class OpenDocument(val documentInfo: DocumentInfo) : DocumentListIntent()
    data class CreateDocument(val title: String, val author: String) : DocumentListIntent()
    data object ShowCreateDialog : DocumentListIntent()
    data object HideCreateDialog : DocumentListIntent()
    data class ShowDeleteDialog(val documentInfo: DocumentInfo) : DocumentListIntent()
    data object HideDeleteDialog : DocumentListIntent()
    data class ShowRenameDialog(val documentInfo: DocumentInfo) : DocumentListIntent()
    data object HideRenameDialog : DocumentListIntent()
    data object ConfirmDelete : DocumentListIntent()
    data object ImportDocument : DocumentListIntent()
    data class ExportDocument(val documentInfo: DocumentInfo) : DocumentListIntent()
    data object RefreshDocuments : DocumentListIntent()
}
