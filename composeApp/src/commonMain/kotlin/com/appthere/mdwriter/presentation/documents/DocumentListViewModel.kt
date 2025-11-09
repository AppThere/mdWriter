package com.appthere.mdwriter.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appthere.mdwriter.data.local.RecentDocumentsManager
import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.DocumentInfo
import com.appthere.mdwriter.data.repository.DocumentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for document list screen with search, sort, and CRUD operations
 */
class DocumentListViewModel(
    private val documentRepository: DocumentRepository,
    private val recentDocumentsManager: RecentDocumentsManager
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentListState())
    val state: StateFlow<DocumentListState> = _state.asStateFlow()

    init {
        observeDocuments()
        observeRecentDocuments()
    }

    /**
     * Handle user intents
     */
    fun handleIntent(intent: DocumentListIntent) {
        when (intent) {
            is DocumentListIntent.SearchDocuments -> searchDocuments(intent.query)
            is DocumentListIntent.ChangeSortOption -> changeSortOption(intent.sortOption)
            is DocumentListIntent.DeleteDocument -> deleteDocument(intent.documentInfo)
            is DocumentListIntent.RenameDocument -> renameDocument(intent.documentInfo, intent.newTitle)
            is DocumentListIntent.OpenDocument -> openDocument(intent.documentInfo)
            is DocumentListIntent.CreateDocument -> createDocument(intent.title, intent.author)
            is DocumentListIntent.ShowCreateDialog -> showCreateDialog()
            is DocumentListIntent.HideCreateDialog -> hideCreateDialog()
            is DocumentListIntent.ShowDeleteDialog -> showDeleteDialog(intent.documentInfo)
            is DocumentListIntent.HideDeleteDialog -> hideDeleteDialog()
            is DocumentListIntent.ShowRenameDialog -> showRenameDialog(intent.documentInfo)
            is DocumentListIntent.HideRenameDialog -> hideRenameDialog()
            is DocumentListIntent.ConfirmDelete -> confirmDelete()
            is DocumentListIntent.ImportDocument -> {
                // Will be handled by platform-specific file picker
            }
            is DocumentListIntent.ExportDocument -> exportDocument(intent.documentInfo)
            is DocumentListIntent.RefreshDocuments -> refreshDocuments()
        }
    }

    private fun observeDocuments() {
        viewModelScope.launch {
            println("DEBUG: observeDocuments started")
            documentRepository.getAllDocuments()
                .combine(_state.map { it.searchQuery }) { docs, query ->
                    println("DEBUG: getAllDocuments emitted ${docs.size} documents, query='$query'")
                    if (query.isBlank()) {
                        docs
                    } else {
                        docs.filter {
                            it.title.contains(query, ignoreCase = true) ||
                            it.author.contains(query, ignoreCase = true)
                        }
                    }
                }
                .combine(_state.map { it.sortOption }) { docs, sortOption ->
                    println("DEBUG: After filtering/sorting: ${docs.size} documents")
                    sortDocuments(docs, sortOption)
                }
                .catch { e ->
                    println("DEBUG: Error in observeDocuments: ${e.message}")
                    e.printStackTrace()
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { documents ->
                    println("DEBUG: Updating state with ${documents.size} documents")
                    _state.update { it.copy(documents = documents, isLoading = false) }
                }
        }
    }

    private fun observeRecentDocuments() {
        viewModelScope.launch {
            recentDocumentsManager.getRecentDocuments()
                .collect { recentDocs ->
                    _state.update { it.copy(recentDocuments = recentDocs) }
                }
        }
    }

    private fun searchDocuments(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun changeSortOption(sortOption: SortOption) {
        _state.update { it.copy(sortOption = sortOption) }
    }

    private fun sortDocuments(documents: List<DocumentInfo>, sortOption: SortOption): List<DocumentInfo> {
        return when (sortOption) {
            SortOption.TITLE_ASC -> documents.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> documents.sortedByDescending { it.title.lowercase() }
            SortOption.MODIFIED_DESC -> documents.sortedByDescending { it.modified }
            SortOption.MODIFIED_ASC -> documents.sortedBy { it.modified }
            SortOption.CREATED_DESC -> documents.sortedByDescending { it.created }
            SortOption.CREATED_ASC -> documents.sortedBy { it.created }
            SortOption.AUTHOR_ASC -> documents.sortedBy { it.author.lowercase() }
            SortOption.AUTHOR_DESC -> documents.sortedByDescending { it.author.lowercase() }
        }
    }

    private fun createDocument(title: String, author: String) {
        viewModelScope.launch {
            println("DEBUG: createDocument called with title='$title', author='$author'")
            _state.update { it.copy(isLoading = true, showCreateDialog = false) }
            try {
                val document = Document.create(
                    title = title.ifBlank { "Untitled Document" },
                    author = author
                )
                println("DEBUG: Created document with id=${document.metadata.identifier}")
                documentRepository.saveDocument(document).fold(
                    onSuccess = {
                        println("DEBUG: Document saved successfully")
                        _state.update { it.copy(isLoading = false, error = null) }
                    },
                    onFailure = { e ->
                        println("DEBUG: Document save failed: ${e.message}")
                        e.printStackTrace()
                        _state.update { it.copy(error = e.message, isLoading = false) }
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Exception creating document: ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun deleteDocument(documentInfo: DocumentInfo) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            documentRepository.deleteDocument(documentInfo.id).fold(
                onSuccess = {
                    recentDocumentsManager.removeRecentDocument(documentInfo.id)
                    _state.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            )
        }
    }

    private fun renameDocument(documentInfo: DocumentInfo, newTitle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showRenameDialog = false) }
            documentRepository.renameDocument(documentInfo.id, newTitle).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            )
        }
    }

    private fun openDocument(documentInfo: DocumentInfo) {
        viewModelScope.launch {
            recentDocumentsManager.addRecentDocument(documentInfo)
            // Navigation will be handled by the UI
        }
    }

    private fun exportDocument(documentInfo: DocumentInfo) {
        viewModelScope.launch {
            // Export will be handled by platform-specific file picker
            // This is a placeholder for the export logic
        }
    }

    private fun refreshDocuments() {
        _state.update { it.copy(isLoading = true) }
        // Documents will refresh automatically via the flow
    }

    private fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true) }
    }

    private fun hideCreateDialog() {
        _state.update { it.copy(showCreateDialog = false) }
    }

    private fun showDeleteDialog(documentInfo: DocumentInfo) {
        _state.update {
            it.copy(
                showDeleteDialog = true,
                documentToDelete = documentInfo
            )
        }
    }

    private fun hideDeleteDialog() {
        _state.update {
            it.copy(
                showDeleteDialog = false,
                documentToDelete = null
            )
        }
    }

    private fun showRenameDialog(documentInfo: DocumentInfo) {
        _state.update {
            it.copy(
                showRenameDialog = true,
                documentToRename = documentInfo
            )
        }
    }

    private fun hideRenameDialog() {
        _state.update {
            it.copy(
                showRenameDialog = false,
                documentToRename = null
            )
        }
    }

    private fun confirmDelete() {
        val documentToDelete = _state.value.documentToDelete
        if (documentToDelete != null) {
            hideDeleteDialog()
            deleteDocument(documentToDelete)
        }
    }
}
