package com.appthere.mdwriter.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Dublin Core metadata for documents
 */
@Serializable
data class Metadata(
    val title: String = "Untitled Document",
    val author: String = "",
    val created: Instant,
    val modified: Instant,
    val language: String = "en",
    val description: String = "",
    val subject: String = "",
    val keywords: List<String> = emptyList()
)
