package com.appthere.mdwriter.data.model

import com.appthere.mdwriter.data.serialization.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Document metadata based on Dublin Core Metadata Initiative (DCMI) terms.
 *
 * Provides comprehensive metadata fields for document identification,
 * attribution, and cataloging.
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class Metadata(
    /**
     * The name given to the resource (Dublin Core: title)
     * Required field for document identification
     */
    val title: String,

    /**
     * An entity primarily responsible for making the resource (Dublin Core: creator)
     * Typically the author's name
     */
    val creator: String? = null,

    /**
     * The topic of the resource (Dublin Core: subject)
     */
    val subject: String? = null,

    /**
     * An account of the resource (Dublin Core: description)
     */
    val description: String? = null,

    /**
     * An entity responsible for making the resource available (Dublin Core: publisher)
     */
    val publisher: String? = null,

    /**
     * An entity responsible for making contributions to the resource (Dublin Core: contributor)
     */
    val contributor: List<String> = emptyList(),

    /**
     * A point or period of time associated with an event in the lifecycle of the resource
     * (Dublin Core: date)
     * Format: YYYY-MM-DD
     */
    val date: String? = null,

    /**
     * The nature or genre of the resource (Dublin Core: type)
     * Example: "Text", "Article", "Book"
     */
    val type: String? = null,

    /**
     * The file format, physical medium, or dimensions of the resource (Dublin Core: format)
     * Default: "text/markdown"
     */
    val format: String? = "text/markdown",

    /**
     * An unambiguous reference to the resource within a given context (Dublin Core: identifier)
     * Example: UUID, DOI, ISBN
     */
    val identifier: String? = null,

    /**
     * A related resource from which the described resource is derived (Dublin Core: source)
     */
    val source: String? = null,

    /**
     * A language of the resource (Dublin Core: language)
     * ISO 639-1 language code (e.g., "en", "es", "fr")
     */
    val language: String? = null,

    /**
     * A related resource (Dublin Core: relation)
     */
    val relation: String? = null,

    /**
     * The spatial or temporal topic of the resource (Dublin Core: coverage)
     */
    val coverage: String? = null,

    /**
     * Information about rights held in and over the resource (Dublin Core: rights)
     * Copyright information
     */
    val rights: String? = null,

    /**
     * Date of creation of the resource
     * ISO 8601 timestamp
     */
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val created: Instant? = null,

    /**
     * Date on which the resource was changed
     * ISO 8601 timestamp
     */
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val modified: Instant? = null,

    /**
     * Custom metadata fields
     * Application-specific fields that don't fit Dublin Core schema
     */
    val custom: Map<String, String> = emptyMap()
) {
    /**
     * Validates the metadata
     * @return ValidationResult indicating whether metadata is valid
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Title is required and must not be blank
        if (title.isBlank()) {
            errors.add("Title is required and cannot be blank")
        }

        // Validate language code format if present
        language?.let {
            if (!it.matches(Regex("^[a-z]{2}(-[A-Z]{2})?$"))) {
                errors.add("Invalid language code format. Expected ISO 639-1 format (e.g., 'en', 'en-US')")
            }
        }

        // Validate date format if present
        date?.let {
            if (!it.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                errors.add("Invalid date format. Expected YYYY-MM-DD")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
