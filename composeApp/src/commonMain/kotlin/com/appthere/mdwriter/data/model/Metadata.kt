package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Dublin Core metadata for documents
 * Using Long for timestamps (epoch milliseconds) to avoid kotlinx-datetime classpath issues
 */
@Serializable
data class Metadata(
    val title: String = "Untitled Document",
    val author: String = "",
    val created: Long, // epoch milliseconds
    val modified: Long, // epoch milliseconds
    val language: String = "en",
    val description: String = "",
    val subject: String = "",
    val keywords: List<String> = emptyList(),
    val publisher: String = "",
    val contributor: String = "",
    val rights: String = "",
    val identifier: String = ""
)
