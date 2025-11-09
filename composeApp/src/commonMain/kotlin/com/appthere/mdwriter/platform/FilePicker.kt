package com.appthere.mdwriter.platform

/**
 * Platform-specific file picker for import/export
 */
expect class FilePicker {
    /**
     * Pick a file to import
     * @return File path or null if cancelled
     */
    suspend fun pickFileForImport(): String?

    /**
     * Pick a location to export a file
     * @param suggestedFileName Suggested name for the export file
     * @return File path or null if cancelled
     */
    suspend fun pickFileForExport(suggestedFileName: String): String?

    /**
     * Pick multiple files to import
     * @return List of file paths or empty list if cancelled
     */
    suspend fun pickMultipleFiles(): List<String>
}
