package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Stylesheet scope determines how it's applied
 */
@Serializable
enum class StylesheetScope {
    GLOBAL,  // Applied to all sections
    MANUAL   // Applied only when explicitly referenced
}

/**
 * CSS stylesheet for document styling
 */
@Serializable
data class Stylesheet(
    val id: String,
    val name: String,
    val scope: StylesheetScope = StylesheetScope.MANUAL,
    val content: String = ""
)
