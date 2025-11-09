package com.appthere.mdwriter.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * JVM (Desktop) implementation of FilePicker
 */
actual class FilePicker {
    actual suspend fun pickFileForImport(): String? = withContext(Dispatchers.IO) {
        val fileDialog = FileDialog(null as Frame?, "Select Document to Import", FileDialog.LOAD)
        fileDialog.setFilenameFilter { _, name -> name.endsWith(".mddoc") || name.endsWith(".json") }
        fileDialog.isVisible = true

        val directory = fileDialog.directory
        val file = fileDialog.file

        if (directory != null && file != null) {
            File(directory, file).absolutePath
        } else {
            null
        }
    }

    actual suspend fun pickFileForExport(suggestedFileName: String): String? = withContext(Dispatchers.IO) {
        val fileDialog = FileDialog(null as Frame?, "Export Document", FileDialog.SAVE)
        fileDialog.file = suggestedFileName
        fileDialog.isVisible = true

        val directory = fileDialog.directory
        val file = fileDialog.file

        if (directory != null && file != null) {
            val filePath = File(directory, file).absolutePath
            // Ensure .mddoc extension
            if (!filePath.endsWith(".mddoc")) {
                "$filePath.mddoc"
            } else {
                filePath
            }
        } else {
            null
        }
    }

    actual suspend fun pickMultipleFiles(): List<String> = withContext(Dispatchers.IO) {
        // FileDialog doesn't support multiple selection, so we'll just pick one
        val path = pickFileForImport()
        if (path != null) listOf(path) else emptyList()
    }
}
