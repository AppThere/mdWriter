package com.appthere.mdwriter.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Complete document with metadata, sections, stylesheets, and resources
 *
 * This is the root object for the mdoc file format.
 */
@Serializable
data class Document(
    /**
     * JSON Schema reference
     */
    @SerialName("\$schema")
    val schema: String = "https://example.com/mdoc-schema-v1.json",

    /**
     * Document format version
     */
    val version: String = "1.0",

    /**
     * Document metadata (Dublin Core)
     */
    val metadata: Metadata,

    /**
     * Ordered list of section IDs defining reading order
     * Must reference valid sections from the sections map
     */
    val spine: List<String>,

    /**
     * Map of section ID to Section object
     * All sections referenced in spine must be present here
     */
    val sections: Map<String, Section>,

    /**
     * Array of stylesheet objects
     */
    val stylesheets: List<Stylesheet> = emptyList(),

    /**
     * Document resources (fonts, images, attachments)
     */
    val resources: Resources = Resources(),

    /**
     * Document-specific settings
     */
    val settings: DocumentSettings = DocumentSettings()
) {
    /**
     * Comprehensive validation of the entire document
     * @return ValidationResult with all validation errors
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate version format (semantic versioning)
        if (!version.matches(Regex("^\\d+\\.\\d+(\\.\\d+)?$"))) {
            errors.add("Invalid version format. Expected semantic versioning (e.g., '1.0' or '1.0.0')")
        }

        // Validate metadata
        val metadataResult = metadata.validate()
        if (!metadataResult.isValid()) {
            errors.addAll(metadataResult.getErrorsOrEmpty().map { "Metadata: $it" })
        }

        // Validate spine
        if (spine.isEmpty()) {
            errors.add("Spine must contain at least one section ID")
        }

        // Check for duplicate IDs in spine
        val duplicateSpineIds = spine.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicateSpineIds.isNotEmpty()) {
            errors.add("Spine contains duplicate section IDs: ${duplicateSpineIds.joinToString(", ")}")
        }

        // Validate that all spine references exist in sections
        spine.forEach { spineId ->
            if (!sections.containsKey(spineId)) {
                errors.add("Spine references non-existent section: '$spineId'")
            }
        }

        // Validate that all sections are in spine
        val sectionsNotInSpine = sections.keys.filter { it !in spine }
        if (sectionsNotInSpine.isNotEmpty()) {
            errors.add("Sections exist that are not in spine: ${sectionsNotInSpine.joinToString(", ")}")
        }

        // Validate all sections
        sections.forEach { (id, section) ->
            // Verify section ID matches map key
            if (section.id != id) {
                errors.add("Section ID mismatch: map key '$id' does not match section.id '${section.id}'")
            }

            val sectionResult = section.validate()
            if (!sectionResult.isValid()) {
                errors.addAll(sectionResult.getErrorsOrEmpty().map { "Section '$id': $it" })
            }

            // Validate section stylesheet references
            section.stylesheets.forEach { stylesheetId ->
                if (stylesheets.none { it.id == stylesheetId }) {
                    errors.add("Section '$id' references non-existent stylesheet: '$stylesheetId'")
                }
            }
        }

        // Validate all stylesheets
        stylesheets.forEach { stylesheet ->
            val stylesheetResult = stylesheet.validate()
            if (!stylesheetResult.isValid()) {
                errors.addAll(stylesheetResult.getErrorsOrEmpty().map { "Stylesheet '${stylesheet.id}': $it" })
            }
        }

        // Check for duplicate stylesheet IDs
        val duplicateStylesheetIds = stylesheets.groupingBy { it.id }.eachCount().filter { it.value > 1 }.keys
        if (duplicateStylesheetIds.isNotEmpty()) {
            errors.add("Duplicate stylesheet IDs found: ${duplicateStylesheetIds.joinToString(", ")}")
        }

        // Validate resources
        val resourcesResult = resources.validate()
        if (!resourcesResult.isValid()) {
            errors.addAll(resourcesResult.getErrorsOrEmpty().map { "Resources: $it" })
        }

        // Validate settings
        val settingsResult = settings.validate()
        if (!settingsResult.isValid()) {
            errors.addAll(settingsResult.getErrorsOrEmpty().map { "Settings: $it" })
        }

        // Validate that settings.defaultStylesheets reference existing stylesheets
        settings.defaultStylesheets.forEach { stylesheetId ->
            if (stylesheets.none { it.id == stylesheetId }) {
                errors.add("Settings references non-existent default stylesheet: '$stylesheetId'")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Get sections in spine order
     */
    fun getSectionsInOrder(): List<Section> {
        return spine.mapNotNull { sections[it] }
    }

    /**
     * Get global stylesheets (those with global scope or in default settings)
     */
    fun getGlobalStylesheets(): List<Stylesheet> {
        val globalByScope = stylesheets.filter { it.scope == StylesheetScope.GLOBAL }
        val globalBySettings = stylesheets.filter { it.id in settings.defaultStylesheets }
        return (globalByScope + globalBySettings).distinctBy { it.id }.sortedBy { it.priority }
    }

    /**
     * Get all stylesheets that apply to a specific section
     * Includes global stylesheets and section-specific stylesheets in correct order
     */
    fun getStylesheetsForSection(sectionId: String): List<Stylesheet> {
        val section = sections[sectionId] ?: return getGlobalStylesheets()

        val global = getGlobalStylesheets()
        val sectionSpecific = section.stylesheets.mapNotNull { id ->
            stylesheets.find { it.id == id }
        }

        return (global + sectionSpecific).distinctBy { it.id }
    }

    companion object {
        /**
         * Create a new empty document with minimal required fields
         */
        fun createNew(title: String): Document {
            return Document(
                metadata = Metadata(title = title),
                spine = emptyList(),
                sections = emptyMap()
            )
        }
    }
}
