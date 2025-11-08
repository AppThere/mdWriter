package com.appthere.mdwriter.data.platform

/**
 * Platform-specific file system operations
 *
 * Use expect/actual pattern to provide platform-specific implementations
 * for file operations that differ across Android, iOS, and Desktop.
 */
expect class FileSystem() {
    /**
     * Get the application's documents directory path
     *
     * @return Absolute path to documents directory
     */
    fun getDocumentsDirectory(): String

    /**
     * Check if a file exists
     *
     * @param path Absolute file path
     * @return true if file exists, false otherwise
     */
    fun fileExists(path: String): Boolean

    /**
     * Create directory and all parent directories if they don't exist
     *
     * @param path Directory path
     * @return true if directory was created or already exists, false on error
     */
    fun createDirectories(path: String): Boolean

    /**
     * Read file contents as string
     *
     * @param path Absolute file path
     * @return File contents or null if file doesn't exist or error occurs
     */
    fun readFile(path: String): String?

    /**
     * Write string content to file
     *
     * Creates parent directories if needed.
     *
     * @param path Absolute file path
     * @param content Content to write
     * @return true if write succeeded, false otherwise
     */
    fun writeFile(path: String, content: String): Boolean

    /**
     * Delete a file
     *
     * @param path Absolute file path
     * @return true if file was deleted or doesn't exist, false on error
     */
    fun deleteFile(path: String): Boolean

    /**
     * List all files in a directory
     *
     * @param path Directory path
     * @return List of file names (not full paths) in the directory, empty if directory doesn't exist
     */
    fun listFiles(path: String): List<String>

    /**
     * Get file size in bytes
     *
     * @param path Absolute file path
     * @return File size in bytes or null if file doesn't exist
     */
    fun getFileSize(path: String): Long?

    /**
     * Move/rename a file atomically
     *
     * @param sourcePath Source file path
     * @param destPath Destination file path
     * @return true if move succeeded, false otherwise
     */
    fun moveFile(sourcePath: String, destPath: String): Boolean
}
