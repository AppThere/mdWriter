package com.appthere.mdwriter.util

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar

/**
 * Parser for YAML frontmatter in Markdown documents
 *
 * Supports Hugo-style frontmatter delimited by --- markers.
 */
class FrontmatterParser {

    /**
     * Parse YAML frontmatter into a Map
     *
     * @param yamlContent YAML content (without --- delimiters)
     * @return Map of key-value pairs, or empty map if parsing fails
     */
    fun parse(yamlContent: String): Map<String, Any> {
        if (yamlContent.isBlank()) {
            return emptyMap()
        }

        return try {
            val yamlNode = Yaml.default.parseToYamlNode(yamlContent)
            convertYamlNode(yamlNode)
        } catch (e: Exception) {
            // Log error in production
            emptyMap()
        }
    }

    /**
     * Parse YAML frontmatter and return result with error information
     *
     * @param yamlContent YAML content (without --- delimiters)
     * @return Result containing parsed map or error
     */
    fun parseWithResult(yamlContent: String): Result<Map<String, Any>> {
        if (yamlContent.isBlank()) {
            return Result.success(emptyMap())
        }

        return try {
            val yamlNode = Yaml.default.parseToYamlNode(yamlContent)
            Result.success(convertYamlNode(yamlNode))
        } catch (e: Exception) {
            Result.failure(YamlParseException("Failed to parse YAML frontmatter: ${e.message}", e))
        }
    }

    /**
     * Extract frontmatter from full Markdown content
     *
     * @param markdownContent Full Markdown content including frontmatter
     * @return Pair of (frontmatter map, content without frontmatter)
     */
    fun extractFrontmatter(markdownContent: String): Pair<Map<String, Any>, String> {
        if (!markdownContent.startsWith("---")) {
            return emptyMap<String, Any>() to markdownContent
        }

        val lines = markdownContent.lines()
        if (lines.size < 3) {
            return emptyMap<String, Any>() to markdownContent
        }

        // Find the closing --- delimiter
        val endIndex = lines.drop(1).indexOfFirst { it.trim() == "---" }
        if (endIndex == -1) {
            return emptyMap<String, Any>() to markdownContent
        }

        // Extract frontmatter content (between the two --- markers)
        val frontmatterLines = lines.subList(1, endIndex + 1)
        val frontmatterContent = frontmatterLines.joinToString("\n")

        // Extract content after frontmatter
        val contentLines = lines.drop(endIndex + 2)
        val content = contentLines.joinToString("\n")

        val frontmatter = parse(frontmatterContent)
        return frontmatter to content
    }

    /**
     * Convert YAML node to Kotlin types
     */
    private fun convertYamlNode(node: com.charleskorn.kaml.YamlNode): Map<String, Any> {
        return when (node) {
            is YamlMap -> {
                buildMap {
                    node.entries.forEach { (key, value) ->
                        val keyStr = key.content
                        put(keyStr, convertYamlValue(value))
                    }
                }
            }
            else -> emptyMap()
        }
    }

    /**
     * Convert YAML value node to Kotlin type
     */
    private fun convertYamlValue(node: com.charleskorn.kaml.YamlNode): Any {
        return when (node) {
            is YamlScalar -> {
                // Try to convert to appropriate type
                val content = node.content
                when {
                    content == "true" || content == "false" -> content.toBoolean()
                    content.toIntOrNull() != null -> content.toInt()
                    content.toDoubleOrNull() != null -> content.toDouble()
                    else -> content
                }
            }
            is YamlMap -> convertYamlNode(node)
            is com.charleskorn.kaml.YamlList -> {
                node.items.map { convertYamlValue(it) }
            }
            else -> node.toString()
        }
    }

    /**
     * Validate frontmatter structure
     *
     * @param frontmatter Parsed frontmatter map
     * @return List of validation errors (empty if valid)
     */
    fun validate(frontmatter: Map<String, Any>): List<String> {
        val errors = mutableListOf<String>()

        // Could add specific validation rules here
        // For now, just basic type checking

        return errors
    }
}

/**
 * Exception thrown when YAML parsing fails
 */
class YamlParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
