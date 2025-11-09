package com.appthere.mdwriter.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of FileSystem
 */
actual class FileSystem(private val context: Context) {
    actual fun getDocumentsDirectory(): String {
        val docsDir = File(context.filesDir, "documents")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
        }
        return docsDir.absolutePath
    }

    actual suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                Result.success(file.readText())
            } else {
                Result.failure(Exception("File not found: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun writeFile(path: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                Result.success(Unit)
            } else {
                Result.failure(Exception("File not found: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        File(path).exists()
    }

    actual suspend fun listDocumentFiles(): List<String> = withContext(Dispatchers.IO) {
        val docsDir = File(getDocumentsDirectory())
        docsDir.listFiles { file -> file.extension == "mddoc" }
            ?.map { it.absolutePath }
            ?: emptyList()
    }

    actual suspend fun ensureDocumentsDirectoryExists(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docsDir = File(getDocumentsDirectory())
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
