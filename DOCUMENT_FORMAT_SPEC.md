# Document Format Specification

## Overview

This document defines the JSON-based format for multipart Markdown documents with metadata, stylesheets, and resource references.

## File Extension

`.mdoc` (Markdown Document)

## JSON Schema

### Root Document Object

```json
{
  "$schema": "https://example.com/mdoc-schema-v1.json",
  "version": "1.0",
  "metadata": { /* Metadata Object */ },
  "spine": [ /* Array of Section IDs */ ],
  "sections": { /* Map of Section Objects */ },
  "stylesheets": [ /* Array of Stylesheet Objects */ ],
  "resources": { /* Resources Object */ },
  "settings": { /* Document Settings Object */ }
}
```

### Metadata Object

Based on Dublin Core Metadata Initiative (DCMI) terms:

```json
{
  "title": "Document Title",
  "creator": "Author Name",
  "subject": "Document subject/topic",
  "description": "Brief description of the document",
  "publisher": "Publisher name",
  "contributor": ["Contributor 1", "Contributor 2"],
  "date": "2025-11-08",
  "type": "Text",
  "format": "text/markdown",
  "identifier": "uuid:550e8400-e29b-41d4-a716-446655440000",
  "source": "Original source if applicable",
  "language": "en",
  "relation": "Related resource",
  "coverage": "Spatial or temporal coverage",
  "rights": "Copyright information",
  "created": "2025-11-08T10:00:00Z",
  "modified": "2025-11-08T15:30:00Z",
  "custom": {
    "wordCount": 5432,
    "readingTime": "27 minutes",
    "tags": ["tag1", "tag2"],
    "category": "article"
  }
}
```

### Spine Array

Defines the reading order of sections:

```json
[
  "section-001",
  "section-002",
  "section-003"
]
```

**Rules**:
- Section IDs must be unique
- Order determines document sequence
- All sections in `sections` must appear in spine
- Spine order overrides any ordering in section metadata

### Section Object

```json
{
  "id": "section-001",
  "title": "Chapter One",
  "content": "---\ntitle: Chapter One\nauthor: John Doe\ndate: 2025-11-08\n---\n\n# Chapter One {.chapter-heading}\n\nThis is the first paragraph.",
  "order": 0,
  "stylesheets": ["main-style", "chapter-style"],
  "metadata": {
    "wordCount": 250,
    "charCount": 1420,
    "created": "2025-11-08T10:00:00Z",
    "modified": "2025-11-08T10:15:00Z"
  },
  "locked": false,
  "hidden": false
}
```

**Fields**:
- `id` (required): Unique identifier (string)
- `title` (optional): Section title for navigation
- `content` (required): Markdown content including frontmatter
- `order` (optional): Display order hint (overridden by spine)
- `stylesheets` (optional): Array of stylesheet IDs to apply to this section (in order)
- `metadata` (optional): Section-specific metadata
- `locked` (optional): If true, section cannot be edited
- `hidden` (optional): If true, section excluded from exports

**Stylesheet Application**:
When rendering a section, stylesheets are applied in this order:
1. Document-level default stylesheets (from `settings.defaultStylesheet`)
2. Section-specific stylesheets (from `section.stylesheets` array, in order)
3. CSS classes from `{.class}` annotations reference definitions in these stylesheets

### Markdown Content Format

#### Frontmatter

YAML frontmatter at the start of content:

```markdown
---
title: Section Title
author: Author Name
date: 2025-11-08
draft: false
custom_field: value
---

Content starts here...
```

**Rules**:
- Delimited by `---` markers
- Must be at the very start of content
- Uses YAML syntax
- All fields are optional
- Can contain any valid YAML

#### CSS Class Annotations

Add CSS classes to block elements:

```markdown
# Heading {.special-heading}

Paragraph text {.highlight .important}

> Blockquote {.callout}
```

**Syntax**: `{.class-name}` or `{.class1 .class2 .class3}`

**Rules**:
- Must immediately follow the block element
- Multiple classes separated by spaces
- Each class prefixed with `.`
- Applied to the entire block, not inline
- Valid class names: `[a-zA-Z][a-zA-Z0-9_-]*`

#### Supported Markdown Extensions

Based on CommonMark with extensions:
- **Tables**: GitHub Flavored Markdown style
- **Task Lists**: `- [ ]` unchecked, `- [x]` checked
- **Strikethrough**: `~~text~~`
- **Footnotes**: `[^1]` reference, `[^1]: note` definition
- **Definition Lists**: CommonMark extension
- **Superscript**: `text^super^`
- **Subscript**: `text~sub~`

### Stylesheet Object

```json
{
  "id": "main-style",
  "name": "Main Stylesheet",
  "content": "body {\n  font-family: 'Georgia', serif;\n  font-size: 16px;\n  line-height: 1.6;\n}\n\n.chapter-heading {\n  color: #2c3e50;\n  border-bottom: 2px solid #3498db;\n}",
  "enabled": true,
  "priority": 0,
  "scope": "global"
}
```

**Fields**:
- `id` (required): Unique identifier
- `name` (required): Display name
- `content` (required): CSS content
- `enabled` (optional): Whether stylesheet is active (default: true)
- `priority` (optional): Load order (lower = earlier, default: 0)
- `scope` (optional): `"global"` (all sections) or `"manual"` (only when explicitly linked)

**CSS Scope**:
- Styles apply to rendered preview only
- Scoped to document namespace to avoid conflicts
- Can reference custom classes from `{.class}` annotations

### Stylesheet Linking & Application

There are three ways to apply stylesheets to content:

#### 1. Global Stylesheets
Stylesheets with `scope: "global"` or those listed in `settings.defaultStylesheets` apply to all sections:

```json
{
  "stylesheets": [
    {
      "id": "base",
      "scope": "global",
      "content": "body { font-family: serif; }"
    }
  ],
  "settings": {
    "defaultStylesheets": ["base"]
  }
}
```

#### 2. Section-Specific Stylesheets
Sections can reference specific stylesheets via the `stylesheets` array:

```json
{
  "sections": {
    "chapter-1": {
      "id": "chapter-1",
      "stylesheets": ["base", "chapter-style", "special-format"],
      "content": "# Chapter 1\n\nContent..."
    }
  }
}
```

Stylesheets are applied in order: `base` → `chapter-style` → `special-format`

#### 3. CSS Class References
Markdown blocks use `{.class-name}` syntax to reference CSS classes defined in any active stylesheet:

```markdown
# Main Heading {.chapter-heading .fancy}

This paragraph has custom styling {.highlight}

> A blockquote {.callout .warning}
```

The CSS classes must be defined in stylesheets that are active for that section (either global or section-specific).

**Complete Example**:

```json
{
  "stylesheets": [
    {
      "id": "base",
      "name": "Base Styles",
      "scope": "global",
      "content": "body { font-family: serif; line-height: 1.6; }\n.highlight { background: yellow; }",
      "priority": 0
    },
    {
      "id": "chapter-style",
      "name": "Chapter Styles",
      "scope": "manual",
      "content": ".chapter-heading { font-size: 2em; color: navy; }\n.fancy { font-variant: small-caps; }",
      "priority": 10
    },
    {
      "id": "callout-style",
      "name": "Callout Styles",
      "scope": "manual",
      "content": ".callout { border-left: 4px solid blue; padding-left: 1em; }\n.warning { border-color: red; }",
      "priority": 20
    }
  ],
  "sections": {
    "intro": {
      "id": "intro",
      "stylesheets": [],
      "content": "# Introduction\n\nThis uses only base styles."
    },
    "chapter-1": {
      "id": "chapter-1",
      "stylesheets": ["chapter-style"],
      "content": "# Chapter One {.chapter-heading .fancy}\n\nImportant text {.highlight}"
    },
    "appendix": {
      "id": "appendix",
      "stylesheets": ["callout-style"],
      "content": "> Warning: Handle with care {.callout .warning}"
    }
  },
  "settings": {
    "defaultStylesheets": ["base"]
  }
}
```

**Rendering Logic**:
1. For `intro` section: Apply `base` only (from default)
2. For `chapter-1` section: Apply `base` (default) → `chapter-style` (section-specific)
   - `.chapter-heading` and `.fancy` are available from `chapter-style`
   - `.highlight` is available from `base`
3. For `appendix` section: Apply `base` (default) → `callout-style` (section-specific)
   - `.callout` and `.warning` are available from `callout-style`

### Resources Object

```json
{
  "fonts": [
    {
      "id": "font-001",
      "name": "Georgia",
      "family": "Georgia",
      "path": "fonts/georgia.ttf",
      "format": "truetype",
      "weight": 400,
      "style": "normal"
    }
  ],
  "images": [
    {
      "id": "img-001",
      "name": "Diagram 1",
      "path": "images/diagram1.png",
      "format": "png",
      "width": 1200,
      "height": 800,
      "alt": "System architecture diagram",
      "caption": "Figure 1: System Architecture"
    }
  ],
  "attachments": [
    {
      "id": "attach-001",
      "name": "dataset.csv",
      "path": "attachments/dataset.csv",
      "mimeType": "text/csv",
      "size": 524288
    }
  ]
}
```

**Font Resource**:
- `path`: Relative path from document root
- `format`: `truetype`, `opentype`, `woff`, `woff2`
- `weight`: 100-900 (normal = 400, bold = 700)
- `style`: `normal`, `italic`, `oblique`

**Image Resource**:
- `path`: Relative path from document root
- `format`: `png`, `jpg`, `jpeg`, `gif`, `webp`, `svg`
- `width`/`height`: Dimensions in pixels
- `alt`: Alternative text for accessibility
- `caption`: Optional caption for figures

**Attachment Resource**:
- Generic file references
- `size`: File size in bytes
- `mimeType`: MIME type of file

### Settings Object

Document-specific settings:

```json
{
  "defaultStylesheets": ["main-style"],
  "theme": "light",
  "fontSize": 16,
  "fontFamily": "Georgia",
  "lineHeight": 1.6,
  "maxWidth": 800,
  "renderOptions": {
    "smartQuotes": true,
    "smartDashes": true,
    "lineBreaks": "hard",
    "headingIds": true
  },
  "exportOptions": {
    "includeToc": true,
    "tocDepth": 3,
    "numberHeadings": false,
    "pageBreaks": "chapter"
  }
}
```

**Fields**:
- `defaultStylesheets` (optional): Array of stylesheet IDs to apply to all sections by default
- `theme`: Editor theme (light/dark)
- `fontSize`: Base font size
- `fontFamily`: Default font family
- Other rendering and export options

## File Structure

For documents with external resources:

```
document-name.mdoc           # Main document file
document-name-files/         # Resource directory
  ├── images/
  │   ├── diagram1.png
  │   └── photo.jpg
  ├── fonts/
  │   └── custom-font.ttf
  └── attachments/
      └── data.csv
```

**Rules**:
- Resource directory name: `{document-name}-files/`
- Paths in JSON are relative to resource directory
- Resource directory is optional (if no external resources)

## Version History

Embedded version history for documents:

```json
{
  "history": [
    {
      "version": 1,
      "timestamp": "2025-11-08T10:00:00Z",
      "author": "John Doe",
      "message": "Initial version",
      "changes": {
        "added": ["section-001"],
        "modified": [],
        "deleted": []
      }
    }
  ]
}
```

**Optional Feature**: Can be enabled per document

## Validation Rules

### Required Fields
- `version`: Must be present and valid semantic version
- `metadata.title`: Required for identification
- `spine`: Must be array with at least one section ID
- `sections`: Must contain all sections referenced in spine
- `section.id`: Required and must be unique
- `section.content`: Required, can be empty string

### Constraints
- Section IDs: `[a-z0-9-_]+` (lowercase, numbers, hyphens, underscores)
- Stylesheet IDs: Same as section IDs
- Resource IDs: Same as section IDs
- Maximum section content: 1MB (recommended)
- Maximum total document size: 50MB (recommended)

### Validation Errors
- `MISSING_REQUIRED_FIELD`: Required field is missing
- `INVALID_SECTION_ID`: Section referenced in spine not found
- `DUPLICATE_ID`: Multiple sections/stylesheets with same ID
- `INVALID_YAML_FRONTMATTER`: Frontmatter is not valid YAML
- `INVALID_CSS`: Stylesheet contains invalid CSS
- `RESOURCE_NOT_FOUND`: Referenced resource file doesn't exist
- `INVALID_SPINE_ORDER`: Spine contains duplicate or invalid IDs

## Backwards Compatibility

Version 1.0 is the initial version. Future versions must:
- Support reading older format versions
- Provide migration path for deprecated features
- Maintain critical field compatibility

## Example Complete Document

```json
{
  "$schema": "https://example.com/mdoc-schema-v1.json",
  "version": "1.0",
  "metadata": {
    "title": "Technical Guide",
    "creator": "Jane Smith",
    "date": "2025-11-08",
    "language": "en",
    "created": "2025-11-08T09:00:00Z",
    "modified": "2025-11-08T16:30:00Z",
    "custom": {
      "tags": ["tutorial", "technical"],
      "category": "documentation"
    }
  },
  "spine": [
    "intro",
    "chapter-1",
    "chapter-2",
    "conclusion"
  ],
  "sections": {
    "intro": {
      "id": "intro",
      "title": "Introduction",
      "content": "---\ntitle: Introduction\n---\n\n# Introduction {.chapter-heading}\n\nWelcome to this guide.",
      "order": 0,
      "stylesheets": [],
      "metadata": {
        "wordCount": 50
      }
    },
    "chapter-1": {
      "id": "chapter-1",
      "title": "Chapter 1: Getting Started",
      "content": "---\ntitle: Getting Started\n---\n\n# Chapter 1: Getting Started {.chapter-heading}\n\n> Important: Read this carefully {.callout .info}\n\nFirst steps...",
      "order": 1,
      "stylesheets": ["chapter-style", "callout-style"]
    },
    "chapter-2": {
      "id": "chapter-2",
      "title": "Chapter 2: Advanced Topics",
      "content": "# Chapter 2: Advanced Topics\n\nAdvanced content here.",
      "order": 2,
      "stylesheets": ["chapter-style"]
    },
    "conclusion": {
      "id": "conclusion",
      "title": "Conclusion",
      "content": "# Conclusion\n\nSummary and next steps.",
      "order": 3,
      "stylesheets": []
    }
  },
  "stylesheets": [
    {
      "id": "base",
      "name": "Base Stylesheet",
      "content": "body { font-family: 'Georgia', serif; font-size: 16px; line-height: 1.6; }",
      "enabled": true,
      "priority": 0,
      "scope": "global"
    },
    {
      "id": "chapter-style",
      "name": "Chapter Stylesheet",
      "content": ".chapter-heading {\n  color: #2c3e50;\n  border-bottom: 2px solid #3498db;\n  padding-bottom: 0.5em;\n}",
      "enabled": true,
      "priority": 10,
      "scope": "manual"
    },
    {
      "id": "callout-style",
      "name": "Callout Stylesheet",
      "content": ".callout { border-left: 4px solid #ccc; padding-left: 1em; margin: 1em 0; }\n.callout.info { border-color: #3498db; }\n.callout.warning { border-color: #e74c3c; }",
      "enabled": true,
      "priority": 20,
      "scope": "manual"
    }
  ],
  "resources": {
    "fonts": [],
    "images": [],
    "attachments": []
  },
  "settings": {
    "defaultStylesheets": ["base"],
    "theme": "light",
    "fontSize": 16
  }
}
```

## Extensibility

### Custom Metadata Fields

Applications can add custom fields to:
- `metadata.custom`: Document-level custom fields
- `section.metadata`: Section-level custom fields
- `settings`: Application-specific settings

### Custom Block Types

Future extensions may add:
- Math blocks (LaTeX)
- Diagram blocks (Mermaid, PlantUML)
- Embedded media blocks
- Interactive elements

Use namespaced class annotations:
```markdown
```math {.katex}
E = mc^2
```
```

## Implementation Notes

### Parsing Strategy

1. Parse JSON to object model
2. Validate structure and required fields
3. Parse each section's frontmatter separately
4. Parse Markdown content to AST
5. Extract CSS class annotations from blocks

### Serialization Strategy

1. Update `modified` timestamp
2. Recalculate metadata (word count, etc.)
3. Serialize to JSON with pretty printing
4. Validate before writing
5. Write atomically (temp file + rename)

### Performance Considerations

- Lazy-load section content for large documents
- Cache parsed AST for active sections
- Debounce auto-save operations
- Use streaming for very large documents
- Index sections for fast search

## Security Considerations

- Sanitize CSS to prevent XSS in preview
- Validate paths to prevent directory traversal
- Limit document/section size to prevent DoS
- Validate YAML frontmatter structure
- Escape HTML in Markdown preview if rendering as HTML
