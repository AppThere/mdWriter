package com.appthere.mdwriter.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Dublin Core metadata for documents
 */
@Serializable
data class Metadata(
    val title: String = "",
    val author: String = "",
    val created: Instant,
    val modified: Instant,
    val language: String = "en",
    val description: String = "",
    val subject: String = "",
    val publisher: String = "",
    val contributor: String = "",
    val rights: String = "",
    val identifier: String = ""
)
