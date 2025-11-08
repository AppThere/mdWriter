package com.appthere.mdwriter.data.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * Dublin Core metadata for documents
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class Metadata(
    val title: String = "",
    val author: String = "",
    @Serializable(with = InstantSerializer::class)
    val created: Instant,
    @Serializable(with = InstantSerializer::class)
    val modified: Instant,
    val language: String = "en",
    val description: String = "",
    val subject: String = "",
    val publisher: String = "",
    val contributor: String = "",
    val rights: String = "",
    val identifier: String = ""
)
