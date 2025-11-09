package com.appthere.mdwriter.data.local

/**
 * Platform-specific file system operations
 */
expect class FileSystem {
    /**
     * Get the documents directory path
     */
    fun getDocumentsDirectory(): String

    /**
     * Read file content as string
     */
    suspend fun readFile(path: String): Result<String>

    /**
     * Write string content to file
     */
    suspend fun writeFile(path: String, content: String): Result<Unit>

    /**
     * Delete a file
     */
    suspend fun deleteFile(path: String): Result<Unit>

    /**
     * Check if file exists
     */
    suspend fun fileExists(path: String): Boolean

    /**
     * List all .mddoc files in documents directory
     */
    suspend fun listDocumentFiles(): List<String>

    /**
     * Create documents directory if it doesn't exist
     */
    suspend fun ensureDocumentsDirectoryExists(): Result<Unit>
}
