package com.appthere.mdwriter.data.model

import com.appthere.mdwriter.data.serialization.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Individual content section of a document
 *
 * Sections contain Markdown content with optional YAML frontmatter,
 * can be linked to specific stylesheets, and maintain their own metadata.
 */
@Serializable
data class Section(
    /**
     * Unique identifier for the section
     * Must match pattern: [a-z0-9-_]+
     */
    val id: String,

    /**
     * Section title for navigation
     * Optional, can be derived from frontmatter or first heading
     */
    val title: String? = null,

    /**
     * Markdown content including optional frontmatter
     * Frontmatter must be at the start, delimited by ---
     */
    val content: String,

    /**
     * Display order hint
     * Note: Overridden by spine order in the document
     */
    val order: Int? = null,

    /**
     * Array of stylesheet IDs to apply to this section
     * Applied in order after document default stylesheets
     */
    val stylesheets: List<String> = emptyList(),

    /**
     * Section-specific metadata
     */
    val metadata: SectionMetadata? = null,

    /**
     * If true, section cannot be edited
     */
    val locked: Boolean = false,

    /**
     * If true, section is excluded from exports
     */
    val hidden: Boolean = false
) {
    /**
     * Validates the section
     * @return ValidationResult indicating whether the section is valid
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate ID format
        if (!id.matches(Regex("^[a-z0-9_-]+$"))) {
            errors.add("Invalid section ID format. Must match pattern: [a-z0-9_-]+")
        }

        if (id.isBlank()) {
            errors.add("Section ID is required and cannot be blank")
        }

        // Content is required but can be empty string
        // No validation needed for empty content

        // Validate stylesheet IDs if present
        stylesheets.forEach { stylesheetId ->
            if (!stylesheetId.matches(Regex("^[a-z0-9_-]+$"))) {
                errors.add("Invalid stylesheet ID '$stylesheetId' in section.stylesheets")
            }
        }

        // Validate section metadata if present
        metadata?.let {
            val metadataResult = it.validate()
            if (!metadataResult.isValid()) {
                errors.addAll(metadataResult.getErrors().map { error -> "Metadata: $error" })
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Extracts frontmatter from content if present
     * @return Pair of frontmatter (if present) and content without frontmatter
     */
    fun extractFrontmatter(): Pair<String?, String> {
        if (!content.startsWith("---")) {
            return null to content
        }

        val lines = content.lines()
        if (lines.size < 3) {
            return null to content
        }

        val endIndex = lines.drop(1).indexOfFirst { it.trim() == "---" }
        if (endIndex == -1) {
            return null to content
        }

        val frontmatterLines = lines.subList(1, endIndex + 1)
        val contentLines = lines.drop(endIndex + 2)

        return frontmatterLines.joinToString("\n") to contentLines.joinToString("\n")
    }

    /**
     * Get content without frontmatter
     */
    fun getContentWithoutFrontmatter(): String {
        return extractFrontmatter().second
    }

    /**
     * Get frontmatter if present
     */
    fun getFrontmatter(): String? {
        return extractFrontmatter().first
    }
}

/**
 * Section-specific metadata
 */
@Serializable
data class SectionMetadata(
    /**
     * Word count in this section
     */
    val wordCount: Int? = null,

    /**
     * Character count in this section
     */
    val charCount: Int? = null,

    /**
     * Date this section was created
     */
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val created: Instant? = null,

    /**
     * Date this section was last modified
     */
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val modified: Instant? = null,

    /**
     * Custom section metadata fields
     */
    val custom: Map<String, String> = emptyMap()
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (wordCount != null && wordCount < 0) {
            errors.add("Word count cannot be negative")
        }

        if (charCount != null && charCount < 0) {
            errors.add("Character count cannot be negative")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
