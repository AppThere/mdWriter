package com.appthere.mdwriter.data.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

/**
 * iOS implementation of FileSystem
 */
@OptIn(ExperimentalForeignApi::class)
actual class FileSystem {
    actual fun getDocumentsDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        return paths.first() as String
    }

    actual fun fileExists(path: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }

    actual fun createDirectories(path: String): Boolean {
        return try {
            NSFileManager.defaultManager.createDirectoryAtPath(
                path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        } catch (e: Exception) {
            false
        }
    }

    actual fun readFile(path: String): String? {
        return try {
            NSString.stringWithContentsOfFile(
                path,
                encoding = NSUTF8StringEncoding,
                error = null
            )
        } catch (e: Exception) {
            null
        }
    }

    actual fun writeFile(path: String, content: String): Boolean {
        return try {
            // Create parent directories if needed
            val parentPath = (path as NSString).stringByDeletingLastPathComponent
            createDirectories(parentPath)

            (content as NSString).writeToFile(
                path,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
        } catch (e: Exception) {
            false
        }
    }

    actual fun deleteFile(path: String): Boolean {
        return try {
            NSFileManager.defaultManager.removeItemAtPath(path, error = null) || !fileExists(path)
        } catch (e: Exception) {
            false
        }
    }

    actual fun listFiles(path: String): List<String> {
        return try {
            val contents = NSFileManager.defaultManager.contentsOfDirectoryAtPath(path, error = null)
            contents?.map { it.toString() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual fun getFileSize(path: String): Long? {
        return try {
            val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
            attrs?.get(NSFileSize) as? Long
        } catch (e: Exception) {
            null
        }
    }

    actual fun moveFile(sourcePath: String, destPath: String): Boolean {
        return try {
            NSFileManager.defaultManager.moveItemAtPath(sourcePath, toPath = destPath, error = null)
        } catch (e: Exception) {
            false
        }
    }
}
