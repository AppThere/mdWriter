package com.appthere.mdwriter.data.repository

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.DocumentInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for document persistence and operations
 * Supports both ID-based and path-based operations for flexibility
 */
interface DocumentRepository {
    /**
     * Get all documents as a flow
     */
    fun getAllDocuments(): Flow<List<DocumentInfo>>

    /**
     * Get a specific document by ID
     */
    suspend fun getDocument(id: String): Result<Document>

    /**
     * Load a document by its file path
     */
    suspend fun loadDocument(path: String): Result<Document>

    /**
     * Save a document (ID-based)
     */
    suspend fun saveDocument(document: Document): Result<Unit>

    /**
     * Save a document to a specific path
     */
    suspend fun saveDocument(path: String, document: Document): Result<Unit>

    /**
     * Create a new empty document
     */
    suspend fun createDocument(): Result<Document>

    /**
     * Delete a document by ID
     */
    suspend fun deleteDocument(id: String): Result<Unit>

    /**
     * Delete a document by path
     */
    suspend fun deleteDocument(path: String): Result<Unit>

    /**
     * Rename a document (updates metadata title)
     */
    suspend fun renameDocument(id: String, newTitle: String): Result<Unit>

    /**
     * Export document to a specific path
     */
    suspend fun exportDocument(document: Document, exportPath: String): Result<Unit>

    /**
     * Import document from a specific path
     */
    suspend fun importDocument(importPath: String): Result<Document>

    /**
     * Search documents by title or content
     */
    fun searchDocuments(query: String): Flow<List<DocumentInfo>>

    /**
     * List all available documents
     */
    suspend fun listDocuments(): Result<List<String>>
}
