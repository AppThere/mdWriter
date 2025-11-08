package com.appthere.mdwriter.data.repository

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.DocumentMetadata
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for document persistence operations
 *
 * Provides abstraction over document storage implementations.
 * Implementations must be thread-safe and handle concurrent access.
 */
interface DocumentRepository {
    /**
     * Load a document by ID
     *
     * @param id Document identifier
     * @return Flow emitting the document or null if not found
     */
    fun loadDocument(id: String): Flow<Document?>

    /**
     * Save a document
     *
     * Performs atomic write operation. If document exists, it will be overwritten.
     * Updates the document's modified timestamp automatically.
     *
     * @param id Document identifier
     * @param document Document to save
     * @return Result indicating success or failure with error details
     */
    suspend fun saveDocument(id: String, document: Document): Result<Unit>

    /**
     * Delete a document
     *
     * @param id Document identifier
     * @return Result indicating success or failure with error details
     */
    suspend fun deleteDocument(id: String): Result<Unit>

    /**
     * List all available documents
     *
     * Returns metadata only, not full document content.
     *
     * @return Flow emitting list of document metadata
     */
    fun listDocuments(): Flow<List<DocumentMetadata>>

    /**
     * Check if a document exists
     *
     * @param id Document identifier
     * @return true if document exists, false otherwise
     */
    suspend fun documentExists(id: String): Boolean

    /**
     * Get metadata for a specific document without loading full content
     *
     * @param id Document identifier
     * @return DocumentMetadata or null if not found
     */
    suspend fun getDocumentMetadata(id: String): DocumentMetadata?
}
