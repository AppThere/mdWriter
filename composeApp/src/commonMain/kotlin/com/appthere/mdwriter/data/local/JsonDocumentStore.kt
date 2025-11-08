package com.appthere.mdwriter.data.local

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.DocumentMetadata
import com.appthere.mdwriter.data.platform.FileSystem
import com.appthere.mdwriter.data.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JSON-based document storage implementation
 *
 * Stores documents as .mdoc JSON files in the application's documents directory.
 * Implements atomic writes using temp file + rename pattern.
 *
 * Thread-safe through coroutine context switching and atomic file operations.
 */
class JsonDocumentStore(
    private val fileSystem: FileSystem
) : DocumentRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    companion object {
        private const val DOCUMENT_EXTENSION = ".mdoc"
        private const val TEMP_EXTENSION = ".tmp"
        private const val DOCUMENTS_SUBDIR = "documents"
    }

    /**
     * Get the documents directory path
     */
    private fun getDocumentsPath(): String {
        val basePath = fileSystem.getDocumentsDirectory()
        val docsPath = "$basePath/$DOCUMENTS_SUBDIR"
        fileSystem.createDirectories(docsPath)
        return docsPath
    }

    /**
     * Get full file path for a document ID
     */
    private fun getDocumentPath(id: String): String {
        return "${getDocumentsPath()}/$id$DOCUMENT_EXTENSION"
    }

    /**
     * Get temporary file path for atomic writes
     */
    private fun getTempPath(id: String): String {
        return "${getDocumentsPath()}/$id$TEMP_EXTENSION"
    }

    override fun loadDocument(id: String): Flow<Document?> = flow {
        val documentPath = getDocumentPath(id)

        val content = fileSystem.readFile(documentPath)
        if (content == null) {
            emit(null)
            return@flow
        }

        try {
            val document = json.decodeFromString<Document>(content)
            emit(document)
        } catch (e: Exception) {
            // Log error in production
            emit(null)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun saveDocument(id: String, document: Document): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            // Validate document before saving
            val validationResult = document.validate()
            if (!validationResult.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Document validation failed: ${validationResult.getErrorsOrEmpty().joinToString(", ")}")
                )
            }

            // Update modified timestamp
            val updatedDocument = document.copy(
                metadata = document.metadata.copy(
                    modified = Clock.System.now()
                )
            )

            // Serialize to JSON
            val jsonContent = json.encodeToString(updatedDocument)

            // Write to temp file first (atomic write pattern)
            val tempPath = getTempPath(id)
            val documentPath = getDocumentPath(id)

            val writeSuccess = fileSystem.writeFile(tempPath, jsonContent)
            if (!writeSuccess) {
                return@withContext Result.failure(
                    Exception("Failed to write temporary file")
                )
            }

            // Atomically move temp file to final location
            val moveSuccess = fileSystem.moveFile(tempPath, documentPath)
            if (!moveSuccess) {
                // Clean up temp file on failure
                fileSystem.deleteFile(tempPath)
                return@withContext Result.failure(
                    Exception("Failed to move temporary file to final location")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocument(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val documentPath = getDocumentPath(id)
            val success = fileSystem.deleteFile(documentPath)

            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete document"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listDocuments(): Flow<List<DocumentMetadata>> = flow {
        val documentsPath = getDocumentsPath()
        val files = fileSystem.listFiles(documentsPath)
            .filter { it.endsWith(DOCUMENT_EXTENSION) }

        val metadataList = files.mapNotNull { filename ->
            try {
                val id = filename.removeSuffix(DOCUMENT_EXTENSION)
                getDocumentMetadata(id)
            } catch (e: Exception) {
                // Skip documents that fail to load
                null
            }
        }

        emit(metadataList)
    }.flowOn(Dispatchers.Default)

    override suspend fun documentExists(id: String): Boolean = withContext(Dispatchers.Default) {
        val documentPath = getDocumentPath(id)
        fileSystem.fileExists(documentPath)
    }

    override suspend fun getDocumentMetadata(id: String): DocumentMetadata? = withContext(Dispatchers.Default) {
        try {
            val documentPath = getDocumentPath(id)
            val content = fileSystem.readFile(documentPath) ?: return@withContext null

            val document = json.decodeFromString<Document>(content)
            val fileSize = fileSystem.getFileSize(documentPath)

            DocumentMetadata.fromDocument(id, document, fileSize)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recover from corrupted files
     *
     * Attempts to clean up temp files and corrupted documents.
     * Should be called on app startup.
     */
    suspend fun performMaintenance(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val documentsPath = getDocumentsPath()
            val files = fileSystem.listFiles(documentsPath)

            // Clean up orphaned temp files
            files.filter { it.endsWith(TEMP_EXTENSION) }.forEach { tempFile ->
                fileSystem.deleteFile("$documentsPath/$tempFile")
            }

            // Validate all document files
            files.filter { it.endsWith(DOCUMENT_EXTENSION) }.forEach { filename ->
                val path = "$documentsPath/$filename"
                val content = fileSystem.readFile(path)

                if (content == null) {
                    // File is unreadable, delete it
                    fileSystem.deleteFile(path)
                } else {
                    try {
                        // Try to parse to validate JSON
                        json.decodeFromString<Document>(content)
                    } catch (e: Exception) {
                        // Corrupted JSON, could backup before deleting
                        // For now, just delete
                        fileSystem.deleteFile(path)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
