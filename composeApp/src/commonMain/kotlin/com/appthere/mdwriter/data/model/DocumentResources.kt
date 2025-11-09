package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Font resource reference
 */
@Serializable
data class FontResource(
    val id: String,
    val name: String,
    val path: String,
    val format: String = "ttf" // ttf, otf, woff, woff2
)

/**
 * Image resource reference
 */
@Serializable
data class ImageResource(
    val id: String,
    val name: String,
    val path: String,
    val alt: String = "",
    val format: String = "png" // png, jpg, svg, etc.
)

/**
 * External resources referenced by the document
 */
@Serializable
data class DocumentResources(
    val fonts: List<FontResource> = emptyList(),
    val images: List<ImageResource> = emptyList()
)
