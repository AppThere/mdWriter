package com.appthere.mdwriter

import com.appthere.mdwriter.data.local.FileSystem
import com.appthere.mdwriter.data.local.JsonDocumentStore
import com.appthere.mdwriter.data.local.RecentDocumentsManager
import com.appthere.mdwriter.data.repository.DocumentRepository

/**
 * Container for application dependencies
 */
class AppDependencies(
    val fileSystem: FileSystem
) {
    val documentRepository: DocumentRepository by lazy {
        JsonDocumentStore(fileSystem)
    }

    val recentDocumentsManager: RecentDocumentsManager by lazy {
        RecentDocumentsManager(fileSystem)
    }
}

/**
 * Platform-specific factory for creating dependencies
 */
expect fun createAppDependencies(): AppDependencies
