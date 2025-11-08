package com.appthere.mdwriter.data.platform

/**
 * Fake FileSystem implementation for testing
 *
 * Provides in-memory file system simulation for unit tests.
 * Mimics the FileSystem interface without extending the expect class.
 */
class FakeFileSystem {
    private val files = mutableMapOf<String, String>()
    private val directories = mutableSetOf<String>()
    private val documentsDir = "/test/documents"

    init {
        directories.add(documentsDir)
    }

    fun getDocumentsDirectory(): String {
        return documentsDir
    }

    fun fileExists(path: String): Boolean {
        return files.containsKey(path)
    }

    fun createDirectories(path: String): Boolean {
        directories.add(path)
        return true
    }

    fun readFile(path: String): String? {
        return files[path]
    }

    fun writeFile(path: String, content: String): Boolean {
        // Create parent directory
        val parentPath = path.substringBeforeLast('/')
        if (parentPath.isNotEmpty()) {
            directories.add(parentPath)
        }
        files[path] = content
        return true
    }

    fun deleteFile(path: String): Boolean {
        files.remove(path)
        return true
    }

    fun listFiles(path: String): List<String> {
        return files.keys
            .filter { it.startsWith("$path/") }
            .map { it.substringAfterLast('/') }
    }

    fun getFileSize(path: String): Long? {
        return files[path]?.length?.toLong()
    }

    fun moveFile(sourcePath: String, destPath: String): Boolean {
        val content = files[sourcePath] ?: return false
        files.remove(sourcePath)
        files[destPath] = content
        return true
    }

    // Test utilities

    /**
     * Clear all files and directories
     */
    fun clear() {
        files.clear()
        directories.clear()
        directories.add(documentsDir)
    }

    /**
     * Get all file paths
     */
    fun getAllFiles(): Set<String> {
        return files.keys.toSet()
    }

    /**
     * Simulate file corruption by setting content to null
     */
    fun corruptFile(path: String) {
        if (files.containsKey(path)) {
            files[path] = ""
        }
    }
}
