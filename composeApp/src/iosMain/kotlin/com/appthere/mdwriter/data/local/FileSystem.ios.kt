package com.appthere.mdwriter.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*

/**
 * iOS implementation of FileSystem
 */
@OptIn(ExperimentalForeignApi::class)
actual class FileSystem {
    actual fun getDocumentsDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        ).firstOrNull() as? NSURL

        val docsDir = documentDirectory?.path?.let { "$it/documents" } ?: ""
        val docsDirFile = NSFileManager.defaultManager

        if (!docsDirFile.fileExistsAtPath(docsDir)) {
            docsDirFile.createDirectoryAtPath(
                path = docsDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }

        return docsDir
    }

    actual suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val content = NSString.stringWithContentsOfFile(
                path = path,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            if (content != null) {
                Result.success(content as String)
            } else {
                Result.failure(Exception("File not found or could not be read: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun writeFile(path: String, content: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val nsContent = content as NSString
            val success = nsContent.writeToFile(
                path = path,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to write file: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val fileManager = NSFileManager.defaultManager
            if (fileManager.fileExistsAtPath(path)) {
                fileManager.removeItemAtPath(path, error = null)
                Result.success(Unit)
            } else {
                Result.failure(Exception("File not found: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.Default) {
        NSFileManager.defaultManager.fileExistsAtPath(path)
    }

    actual suspend fun listDocumentFiles(): List<String> = withContext(Dispatchers.Default) {
        try {
            val docsDir = getDocumentsDirectory()
            val fileManager = NSFileManager.defaultManager
            val contents = fileManager.contentsOfDirectoryAtPath(docsDir, error = null) as? List<*>

            contents?.mapNotNull { fileName ->
                val name = fileName as? String
                if (name?.endsWith(".mddoc") == true) {
                    "$docsDir/$name"
                } else {
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual suspend fun ensureDocumentsDirectoryExists(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val docsDir = getDocumentsDirectory()
            val fileManager = NSFileManager.defaultManager

            if (!fileManager.fileExistsAtPath(docsDir)) {
                fileManager.createDirectoryAtPath(
                    path = docsDir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
