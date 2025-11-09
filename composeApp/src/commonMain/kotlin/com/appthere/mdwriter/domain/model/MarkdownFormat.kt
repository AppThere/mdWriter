package com.appthere.mdwriter.domain.model

/**
 * Markdown formatting options available in the toolbar
 */
sealed class MarkdownFormat(
    val prefix: String,
    val suffix: String = prefix,
    val blockPrefix: String = "",
    val blockSuffix: String = ""
) {
    // Inline formats
    data object Bold : MarkdownFormat("**")
    data object Italic : MarkdownFormat("*")
    data object Code : MarkdownFormat("`")
    data object Strikethrough : MarkdownFormat("~~")
    data object Superscript : MarkdownFormat("<sup>", "</sup>")
    data object Subscript : MarkdownFormat("<sub>", "</sub>")

    // Block formats
    data object Heading1 : MarkdownFormat("", "", "# ", "\n")
    data object Heading2 : MarkdownFormat("", "", "## ", "\n")
    data object Heading3 : MarkdownFormat("", "", "### ", "\n")
    data object Heading4 : MarkdownFormat("", "", "#### ", "\n")
    data object Heading5 : MarkdownFormat("", "", "##### ", "\n")
    data object Heading6 : MarkdownFormat("", "", "###### ", "\n")
    data object Blockquote : MarkdownFormat("", "", "> ", "\n")
    data object CodeBlock : MarkdownFormat("", "", "```\n", "\n```\n")
    data object BulletList : MarkdownFormat("", "", "- ", "\n")
    data object NumberedList : MarkdownFormat("", "", "1. ", "\n")
    data object TaskList : MarkdownFormat("", "", "- [ ] ", "\n")
    data object HorizontalRule : MarkdownFormat("", "", "---\n", "")

    // Special formats
    data class Link(val url: String, val title: String = "") : MarkdownFormat("[", "]($url${if (title.isNotEmpty()) " \"$title\"" else ""})")
    data class Image(val url: String, val alt: String = "") : MarkdownFormat("", "", "![$alt]($url)\n", "")
    data class CssClass(val className: String) : MarkdownFormat("", " {.$className}")
}
