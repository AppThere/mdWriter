package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * CSS stylesheet object that can be applied to document sections
 */
@Serializable
data class Stylesheet(
    /**
     * Unique identifier for the stylesheet
     * Must match pattern: [a-z0-9-_]+
     */
    val id: String,

    /**
     * Human-readable name for the stylesheet
     */
    val name: String,

    /**
     * CSS content
     */
    val content: String,

    /**
     * Whether the stylesheet is currently active
     */
    val enabled: Boolean = true,

    /**
     * Load order priority
     * Lower values are loaded earlier
     */
    val priority: Int = 0,

    /**
     * Scope of the stylesheet
     * - "global": Applied to all sections automatically
     * - "manual": Only applied when explicitly referenced by a section
     */
    val scope: StylesheetScope = StylesheetScope.MANUAL
) {
    /**
     * Validates the stylesheet
     * @return ValidationResult indicating whether the stylesheet is valid
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate ID format
        if (!id.matches(Regex("^[a-z0-9_-]+$"))) {
            errors.add("Invalid stylesheet ID format. Must match pattern: [a-z0-9_-]+")
        }

        if (id.isBlank()) {
            errors.add("Stylesheet ID is required and cannot be blank")
        }

        // Validate name
        if (name.isBlank()) {
            errors.add("Stylesheet name is required and cannot be blank")
        }

        // Content can be empty (valid empty stylesheet), but should be a string

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Scope of stylesheet application
 */
@Serializable
enum class StylesheetScope {
    /**
     * Applied to all sections automatically
     */
    GLOBAL,

    /**
     * Only applied when explicitly referenced by a section
     */
    MANUAL;

    companion object {
        /**
         * Parse scope from string value
         */
        fun fromString(value: String): StylesheetScope {
            return when (value.lowercase()) {
                "global" -> GLOBAL
                "manual" -> MANUAL
                else -> MANUAL // Default to manual if unknown
            }
        }
    }
}
