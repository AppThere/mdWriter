package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Document-specific settings
 */
@Serializable
data class DocumentSettings(
    /**
     * Array of stylesheet IDs to apply to all sections by default
     */
    val defaultStylesheets: List<String> = emptyList(),

    /**
     * Editor theme preference (light/dark)
     */
    val theme: String? = "light",

    /**
     * Base font size for rendering
     */
    val fontSize: Int? = 16,

    /**
     * Default font family
     */
    val fontFamily: String? = "Georgia",

    /**
     * Line height for text rendering
     */
    val lineHeight: Double? = 1.6,

    /**
     * Maximum width for rendered content
     */
    val maxWidth: Int? = 800,

    /**
     * Rendering options
     */
    val renderOptions: RenderOptions? = null,

    /**
     * Export options
     */
    val exportOptions: ExportOptions? = null
) {
    /**
     * Validates the settings
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate default stylesheet IDs
        defaultStylesheets.forEach { stylesheetId ->
            if (!stylesheetId.matches(Regex("^[a-z0-9_-]+$"))) {
                errors.add("Invalid stylesheet ID '$stylesheetId' in defaultStylesheets")
            }
        }

        // Validate theme
        theme?.let {
            if (it !in listOf("light", "dark")) {
                errors.add("Theme must be either 'light' or 'dark'")
            }
        }

        // Validate font size
        fontSize?.let {
            if (it <= 0) {
                errors.add("Font size must be positive")
            }
        }

        // Validate line height
        lineHeight?.let {
            if (it <= 0) {
                errors.add("Line height must be positive")
            }
        }

        // Validate max width
        maxWidth?.let {
            if (it <= 0) {
                errors.add("Max width must be positive")
            }
        }

        // Validate nested options
        renderOptions?.let {
            val result = it.validate()
            if (!result.isValid()) {
                errors.addAll(result.getErrors().map { error -> "RenderOptions: $error" })
            }
        }

        exportOptions?.let {
            val result = it.validate()
            if (!result.isValid()) {
                errors.addAll(result.getErrors().map { error -> "ExportOptions: $error" })
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Options for rendering Markdown content
 */
@Serializable
data class RenderOptions(
    /**
     * Enable smart quotes conversion
     */
    val smartQuotes: Boolean = true,

    /**
     * Enable smart dashes conversion
     */
    val smartDashes: Boolean = true,

    /**
     * Line break handling (soft, hard)
     */
    val lineBreaks: String = "hard",

    /**
     * Generate IDs for headings
     */
    val headingIds: Boolean = true
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (lineBreaks !in listOf("soft", "hard")) {
            errors.add("Line breaks must be either 'soft' or 'hard'")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Options for exporting documents
 */
@Serializable
data class ExportOptions(
    /**
     * Include table of contents in exports
     */
    val includeToc: Boolean = true,

    /**
     * Maximum heading depth for table of contents
     */
    val tocDepth: Int = 3,

    /**
     * Number headings in exports
     */
    val numberHeadings: Boolean = false,

    /**
     * Where to insert page breaks (none, section, chapter)
     */
    val pageBreaks: String = "chapter"
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (tocDepth < 1 || tocDepth > 6) {
            errors.add("TOC depth must be between 1 and 6")
        }

        if (pageBreaks !in listOf("none", "section", "chapter")) {
            errors.add("Page breaks must be one of: none, section, chapter")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
