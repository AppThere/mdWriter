package com.appthere.mdwriter.data.platform

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * JVM (Desktop) implementation of FileSystem
 */
actual class FileSystem {
    actual fun getDocumentsDirectory(): String {
        // Use user.home for desktop applications
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".mdwriter/documents")
        appDir.mkdirs()
        return appDir.absolutePath
    }

    actual fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    actual fun createDirectories(path: String): Boolean {
        return try {
            File(path).mkdirs() || File(path).exists()
        } catch (e: Exception) {
            false
        }
    }

    actual fun readFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            null
        }
    }

    actual fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun deleteFile(path: String): Boolean {
        return try {
            File(path).delete() || !File(path).exists()
        } catch (e: Exception) {
            false
        }
    }

    actual fun listFiles(path: String): List<String> {
        return try {
            File(path).listFiles()?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual fun getFileSize(path: String): Long? {
        return try {
            val file = File(path)
            if (file.exists()) file.length() else null
        } catch (e: Exception) {
            null
        }
    }

    actual fun moveFile(sourcePath: String, destPath: String): Boolean {
        return try {
            val source = File(sourcePath).toPath()
            val dest = File(destPath).toPath()
            Files.move(source, dest, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (e: Exception) {
            // Fallback to non-atomic move
            try {
                File(sourcePath).renameTo(File(destPath))
            } catch (e: Exception) {
                false
            }
        }
    }
}
