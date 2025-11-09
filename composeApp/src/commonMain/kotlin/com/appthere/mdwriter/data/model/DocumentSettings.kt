package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Document-level settings
 */
@Serializable
data class DocumentSettings(
    val defaultStylesheets: List<String> = emptyList() // Global stylesheet IDs
)
