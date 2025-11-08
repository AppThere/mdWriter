package com.appthere.mdwriter.data.repository

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for document operations
 */
interface DocumentRepository {
    /**
     * Load a document by its path
     */
    suspend fun loadDocument(path: String): Result<Document>

    /**
     * Save a document to the specified path
     */
    suspend fun saveDocument(path: String, document: Document): Result<Unit>

    /**
     * Create a new empty document
     */
    suspend fun createDocument(): Result<Document>

    /**
     * Delete a document
     */
    suspend fun deleteDocument(path: String): Result<Unit>

    /**
     * List all available documents
     */
    suspend fun listDocuments(): Result<List<String>>

    /**
     * Observe changes to a document
     */
    fun observeDocument(path: String): Flow<Result<Document>>
}
