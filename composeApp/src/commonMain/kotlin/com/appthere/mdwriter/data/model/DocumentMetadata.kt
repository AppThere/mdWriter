package com.appthere.mdwriter.data.model

import com.appthere.mdwriter.data.serialization.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Lightweight metadata for document listings
 *
 * Used when displaying lists of documents without loading full document content.
 * Contains essential information for browsing and selecting documents.
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class DocumentMetadata(
    /**
     * Unique document identifier
     * Typically derived from filename or UUID
     */
    val id: String,

    /**
     * Document title from metadata
     */
    val title: String,

    /**
     * Document creator/author
     */
    val creator: String? = null,

    /**
     * Document language
     */
    val language: String? = null,

    /**
     * Date of creation
     */
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val created: Instant? = null,

    /**
     * Date of last modification
     */
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val modified: Instant? = null,

    /**
     * File size in bytes
     */
    val fileSize: Long? = null,

    /**
     * Custom tags for categorization
     */
    val tags: List<String> = emptyList()
) {
    companion object {
        /**
         * Create DocumentMetadata from a full Document
         */
        fun fromDocument(id: String, document: Document, fileSize: Long? = null): DocumentMetadata {
            return DocumentMetadata(
                id = id,
                title = document.metadata.title,
                creator = document.metadata.creator,
                language = document.metadata.language,
                created = document.metadata.created,
                modified = document.metadata.modified,
                fileSize = fileSize,
                tags = document.metadata.custom["tags"]?.split(",")?.map { it.trim() } ?: emptyList()
            )
        }
    }
}
