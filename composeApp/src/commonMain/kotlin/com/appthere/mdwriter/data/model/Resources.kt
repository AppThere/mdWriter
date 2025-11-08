package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Container for all document resources (fonts, images, attachments)
 */
@Serializable
data class Resources(
    /**
     * Font resources embedded or referenced by the document
     */
    val fonts: List<FontResource> = emptyList(),

    /**
     * Image resources referenced by the document
     */
    val images: List<ImageResource> = emptyList(),

    /**
     * Generic file attachments
     */
    val attachments: List<AttachmentResource> = emptyList()
) {
    /**
     * Validates all resources
     * @return ValidationResult indicating whether all resources are valid
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate all fonts
        fonts.forEach { font ->
            val result = font.validate()
            if (!result.isValid()) {
                errors.addAll(result.getErrorsOrEmpty().map { "Font '${font.id}': $it" })
            }
        }

        // Validate all images
        images.forEach { image ->
            val result = image.validate()
            if (!result.isValid()) {
                errors.addAll(result.getErrorsOrEmpty().map { "Image '${image.id}': $it" })
            }
        }

        // Validate all attachments
        attachments.forEach { attachment ->
            val result = attachment.validate()
            if (!result.isValid()) {
                errors.addAll(result.getErrorsOrEmpty().map { "Attachment '${attachment.id}': $it" })
            }
        }

        // Check for duplicate IDs across all resource types
        val allIds = fonts.map { it.id } + images.map { it.id } + attachments.map { it.id }
        val duplicateIds = allIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicateIds.isNotEmpty()) {
            errors.add("Duplicate resource IDs found: ${duplicateIds.joinToString(", ")}")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Font resource reference
 */
@Serializable
data class FontResource(
    /**
     * Unique identifier
     * Must match pattern: [a-z0-9-_]+
     */
    val id: String,

    /**
     * Display name for the font
     */
    val name: String,

    /**
     * Font family name
     */
    val family: String,

    /**
     * Relative path from document resource directory
     */
    val path: String,

    /**
     * Font format (truetype, opentype, woff, woff2)
     */
    val format: String,

    /**
     * Font weight (100-900)
     * Normal = 400, Bold = 700
     */
    val weight: Int = 400,

    /**
     * Font style (normal, italic, oblique)
     */
    val style: String = "normal"
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (!id.matches(Regex("^[a-z0-9_-]+$"))) {
            errors.add("Invalid font ID format")
        }

        if (name.isBlank()) {
            errors.add("Font name is required")
        }

        if (family.isBlank()) {
            errors.add("Font family is required")
        }

        if (path.isBlank()) {
            errors.add("Font path is required")
        }

        if (format !in listOf("truetype", "opentype", "woff", "woff2")) {
            errors.add("Invalid font format. Must be one of: truetype, opentype, woff, woff2")
        }

        if (weight !in 100..900) {
            errors.add("Font weight must be between 100 and 900")
        }

        if (style !in listOf("normal", "italic", "oblique")) {
            errors.add("Font style must be one of: normal, italic, oblique")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Image resource reference
 */
@Serializable
data class ImageResource(
    /**
     * Unique identifier
     * Must match pattern: [a-z0-9-_]+
     */
    val id: String,

    /**
     * Display name for the image
     */
    val name: String,

    /**
     * Relative path from document resource directory
     */
    val path: String,

    /**
     * Image format (png, jpg, jpeg, gif, webp, svg)
     */
    val format: String,

    /**
     * Image width in pixels
     */
    val width: Int? = null,

    /**
     * Image height in pixels
     */
    val height: Int? = null,

    /**
     * Alternative text for accessibility
     */
    val alt: String? = null,

    /**
     * Optional caption for figures
     */
    val caption: String? = null
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (!id.matches(Regex("^[a-z0-9_-]+$"))) {
            errors.add("Invalid image ID format")
        }

        if (name.isBlank()) {
            errors.add("Image name is required")
        }

        if (path.isBlank()) {
            errors.add("Image path is required")
        }

        if (format !in listOf("png", "jpg", "jpeg", "gif", "webp", "svg")) {
            errors.add("Invalid image format. Must be one of: png, jpg, jpeg, gif, webp, svg")
        }

        if (width != null && width <= 0) {
            errors.add("Image width must be positive")
        }

        if (height != null && height <= 0) {
            errors.add("Image height must be positive")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Generic file attachment reference
 */
@Serializable
data class AttachmentResource(
    /**
     * Unique identifier
     * Must match pattern: [a-z0-9-_]+
     */
    val id: String,

    /**
     * Display name for the attachment
     */
    val name: String,

    /**
     * Relative path from document resource directory
     */
    val path: String,

    /**
     * MIME type of the file
     */
    val mimeType: String,

    /**
     * File size in bytes
     */
    val size: Long? = null
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (!id.matches(Regex("^[a-z0-9_-]+$"))) {
            errors.add("Invalid attachment ID format")
        }

        if (name.isBlank()) {
            errors.add("Attachment name is required")
        }

        if (path.isBlank()) {
            errors.add("Attachment path is required")
        }

        if (mimeType.isBlank()) {
            errors.add("Attachment MIME type is required")
        }

        if (size != null && size < 0) {
            errors.add("Attachment size cannot be negative")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
