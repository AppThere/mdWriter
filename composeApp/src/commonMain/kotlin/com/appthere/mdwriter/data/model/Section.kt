package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Individual content section/chapter in a document
 */
@Serializable
data class Section(
    val id: String,
    val content: String = "",
    val order: Int = 0,
    val stylesheets: List<String> = emptyList() // IDs of stylesheets to apply
)
