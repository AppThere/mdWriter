package com.appthere.mdwriter.data.model

/**
 * Represents the result of a validation operation
 */
sealed class ValidationResult {
    /**
     * Validation passed successfully
     */
    data object Valid : ValidationResult()

    /**
     * Validation failed with one or more errors
     * @property errors List of error messages describing validation failures
     */
    data class Invalid(val errors: List<String>) : ValidationResult()

    /**
     * Check if the validation result is valid
     */
    fun isValid(): Boolean = this is Valid

    /**
     * Get error messages if invalid, or empty list if valid
     */
    fun getErrors(): List<String> = when (this) {
        is Valid -> emptyList()
        is Invalid -> errors
    }
}
