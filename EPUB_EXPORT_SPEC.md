# EPUB 3.3 Export Specification

## Overview

This document specifies how to export documents from the custom JSON/Markdown format to valid EPUB 3.3 publications.

## EPUB 3.3 Structure

An EPUB 3.3 file is a ZIP archive with the following structure:

```
publication.epub (ZIP archive)
├── mimetype                          (must be first, uncompressed)
├── META-INF/
│   ├── container.xml                 (points to package document)
│   └── com.apple.ibooks.display-options.xml (optional)
├── EPUB/
│   ├── package.opf                   (package document)
│   ├── nav.xhtml                     (navigation document)
│   ├── toc.ncx                       (NCX for EPUB 2 compatibility, optional)
│   ├── content/
│   │   ├── section-001.xhtml         (content documents)
│   │   ├── section-002.xhtml
│   │   └── ...
│   ├── styles/
│   │   ├── stylesheet-001.css
│   │   ├── stylesheet-002.css
│   │   └── ...
│   ├── fonts/
│   │   ├── font-001.ttf
│   │   └── ...
│   └── images/
│       ├── image-001.png
│       └── ...
```

## File Extension

**Document File Extension**: `.mdoc`

**Purpose**: 
- OS file association with the application
- Double-click to open in editor
- Distinct from standard markdown files

**MIME Type**: `application/vnd.appthere.mdwriter+json`

**Platform Registration**:
- **Android**: Declare in AndroidManifest.xml
- **iOS**: Register in Info.plist
- **Desktop**: Platform-specific file association

## Export Process Flow

```
1. Parse JSON document
   ↓
2. Validate structure
   ↓
3. Convert Markdown sections to XHTML
   ↓
4. Generate package.opf manifest
   ↓
5. Generate nav.xhtml navigation
   ↓
6. Process stylesheets and fonts
   ↓
7. Copy/process resources (images, fonts)
   ↓
8. Create ZIP archive with correct structure
   ↓
9. Validate EPUB 3.3 conformance
   ↓
10. Save .epub file
```

## Required Files

### 1. mimetype

Must be the first file in the ZIP, stored uncompressed.

```
application/epub+zip
```

**Requirements**:
- No newline at end
- No BOM
- ASCII encoding
- Must be first file in ZIP
- Must be uncompressed (STORE method)

### 2. META-INF/container.xml

Points to the package document (package.opf).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="EPUB/package.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>
```

### 3. EPUB/package.opf (Package Document)

Central manifest listing all resources.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" 
         version="3.0" 
         unique-identifier="pub-id"
         xml:lang="en">
    
    <!-- Metadata -->
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:identifier id="pub-id">urn:uuid:${UUID}</dc:identifier>
        <dc:title>${document.metadata.title}</dc:title>
        <dc:creator>${document.metadata.creator}</dc:creator>
        <dc:language>${document.metadata.language}</dc:language>
        <dc:date>${document.metadata.date}</dc:date>
        <meta property="dcterms:modified">${ISO_8601_timestamp}</meta>
        
        <!-- Optional metadata -->
        <dc:subject>${document.metadata.subject}</dc:subject>
        <dc:description>${document.metadata.description}</dc:description>
        <dc:publisher>${document.metadata.publisher}</dc:publisher>
        <dc:rights>${document.metadata.rights}</dc:rights>
    </metadata>
    
    <!-- Manifest: All resources -->
    <manifest>
        <!-- Navigation document (required) -->
        <item id="nav" href="nav.xhtml" 
              media-type="application/xhtml+xml" 
              properties="nav"/>
        
        <!-- NCX for EPUB 2 compatibility (optional but recommended) -->
        <item id="ncx" href="toc.ncx" 
              media-type="application/x-dtbncx+xml"/>
        
        <!-- Content documents -->
        <item id="section-001" href="content/section-001.xhtml" 
              media-type="application/xhtml+xml"/>
        <item id="section-002" href="content/section-002.xhtml" 
              media-type="application/xhtml+xml"/>
        <!-- ... more sections -->
        
        <!-- Stylesheets -->
        <item id="style-001" href="styles/stylesheet-001.css" 
              media-type="text/css"/>
        <item id="fonts-css" href="styles/fonts.css" 
              media-type="text/css"/>
        
        <!-- Fonts -->
        <item id="font-001" href="fonts/font-001.ttf" 
              media-type="font/ttf"/>
        <item id="font-002" href="fonts/font-002.otf" 
              media-type="font/otf"/>
        
        <!-- Images -->
        <item id="image-001" href="images/image-001.png" 
              media-type="image/png"/>
        <item id="image-002" href="images/image-002.jpg" 
              media-type="image/jpeg"/>
    </manifest>
    
    <!-- Spine: Reading order -->
    <spine toc="ncx">
        <itemref idref="section-001"/>
        <itemref idref="section-002"/>
        <!-- ... in document.spine order -->
    </spine>
    
    <!-- Guide (optional) -->
    <guide>
        <reference type="toc" title="Table of Contents" href="nav.xhtml"/>
    </guide>
</package>
```

### 4. EPUB/nav.xhtml (Navigation Document)

EPUB 3 navigation document (replaces NCX as primary navigation).

```html
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" 
      xmlns:epub="http://www.idpf.org/2007/ops" 
      xml:lang="en" lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Table of Contents</title>
    <link rel="stylesheet" type="text/css" href="styles/nav.css"/>
</head>
<body>
    <nav epub:type="toc" id="toc">
        <h1>Table of Contents</h1>
        <ol>
            <li>
                <a href="content/section-001.xhtml">Chapter 1: Introduction</a>
                <ol>
                    <li><a href="content/section-001.xhtml#heading-1">Subsection 1.1</a></li>
                </ol>
            </li>
            <li>
                <a href="content/section-002.xhtml">Chapter 2: Getting Started</a>
            </li>
            <!-- ... more sections from document.spine -->
        </ol>
    </nav>
    
    <!-- Optional landmarks navigation -->
    <nav epub:type="landmarks" id="landmarks" hidden="">
        <h2>Landmarks</h2>
        <ol>
            <li><a epub:type="toc" href="#toc">Table of Contents</a></li>
            <li><a epub:type="bodymatter" href="content/section-001.xhtml">Start of Content</a></li>
        </ol>
    </nav>
</body>
</html>
```

### 5. EPUB/toc.ncx (NCX for EPUB 2 Compatibility)

Optional but recommended for backward compatibility.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
    <head>
        <meta name="dtb:uid" content="urn:uuid:${UUID}"/>
        <meta name="dtb:depth" content="2"/>
        <meta name="dtb:totalPageCount" content="0"/>
        <meta name="dtb:maxPageNumber" content="0"/>
    </head>
    <docTitle>
        <text>${document.metadata.title}</text>
    </docTitle>
    <navMap>
        <navPoint id="navpoint-1" playOrder="1">
            <navLabel>
                <text>Chapter 1: Introduction</text>
            </navLabel>
            <content src="content/section-001.xhtml"/>
        </navPoint>
        <!-- ... more navPoints -->
    </navMap>
</ncx>
```

## Content Document Template

Each section becomes an XHTML document:

```html
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" 
      xmlns:epub="http://www.idpf.org/2007/ops" 
      xml:lang="${language}" lang="${language}">
<head>
    <meta charset="UTF-8"/>
    <title>${section.title || section.frontmatter.title}</title>
    
    <!-- Link to stylesheets based on section.stylesheets -->
    <link rel="stylesheet" type="text/css" href="../styles/fonts.css"/>
    <link rel="stylesheet" type="text/css" href="../styles/stylesheet-001.css"/>
    <!-- ... more stylesheets as needed -->
    
    <!-- Frontmatter metadata -->
    <meta name="author" content="${section.frontmatter.author}"/>
    <meta name="date" content="${section.frontmatter.date}"/>
    <!-- ... other frontmatter fields -->
</head>
<body epub:type="bodymatter">
    <section>
        <!-- Converted Markdown content -->
        ${convertedHTML}
    </section>
</body>
</html>
```

## Markdown to XHTML Conversion

### Conversion Rules

```kotlin
// Markdown elements to EPUB 3.3 XHTML
Heading 1 → <h1>
Heading 2 → <h2>
Heading 3 → <h3>
Heading 4 → <h4>
Heading 5 → <h5>
Heading 6 → <h6>

Paragraph → <p>
Bold/Strong → <strong> or <b>
Italic/Emphasis → <em> or <i>
Code inline → <code>
Strikethrough → <del> or <s>
Superscript → <sup>
Subscript → <sub>

Link → <a href="...">
Image → <img src="..." alt="..."/>

Unordered list → <ul><li>...</li></ul>
Ordered list → <ol><li>...</li></ol>
Task list → <ul><li><input type="checkbox"/>...</li></ul>

Blockquote → <blockquote>
Code block → <pre><code>...</code></pre>
Horizontal rule → <hr/>

Table → <table><thead><tr><th>...</table>
```

### CSS Class Annotations

CSS class annotations `{.class-name}` should be converted to class attributes:

```markdown
# Heading {.special}
→ <h1 class="special">Heading</h1>

> Blockquote {.callout .warning}
→ <blockquote class="callout warning">Blockquote</blockquote>
```

### Special Handling

**Line breaks**: Use `<br/>` for hard line breaks

**HTML entities**: Escape `&`, `<`, `>`, `"`, `'`

**IDs for headings**: Generate IDs for internal linking
```html
<h2 id="section-2-1">Subsection 2.1</h2>
```

**Images**: Convert relative paths
```markdown
![Alt text](images/diagram.png)
→ <img src="../images/diagram.png" alt="Alt text"/>
```

## Font Management

### Font Declaration CSS

Generate `fonts.css` with all embedded fonts:

```css
/* Generated from document.resources.fonts */

@font-face {
    font-family: '${font.family}';
    src: url('../fonts/${font.filename}') format('${font.format}');
    font-weight: ${font.weight};
    font-style: ${font.style};
}

@font-face {
    font-family: 'CustomSerif';
    src: url('../fonts/CustomSerif-Regular.ttf') format('truetype');
    font-weight: 400;
    font-style: normal;
}

@font-face {
    font-family: 'CustomSerif';
    src: url('../fonts/CustomSerif-Bold.ttf') format('truetype');
    font-weight: 700;
    font-style: normal;
}

@font-face {
    font-family: 'CustomSerif';
    src: url('../fonts/CustomSerif-Italic.ttf') format('truetype');
    font-weight: 400;
    font-style: italic;
}
```

### Font Format Detection

Map file extensions to formats:
- `.ttf` → `truetype`
- `.otf` → `opentype`
- `.woff` → `woff`
- `.woff2` → `woff2`

### MIME Types for Fonts

- TrueType: `font/ttf` or `application/x-font-ttf`
- OpenType: `font/otf` or `application/x-font-otf`
- WOFF: `font/woff` or `application/font-woff`
- WOFF2: `font/woff2` or `application/font-woff2`

## Resource Processing

### Images

1. Copy images to `EPUB/images/`
2. Update image references in HTML
3. Add to manifest with correct MIME type
4. Preserve original format (no conversion)

**MIME Types**:
- PNG: `image/png`
- JPEG: `image/jpeg`
- GIF: `image/gif`
- SVG: `image/svg+xml`
- WebP: `image/webp`

### Fonts

1. Copy font files to `EPUB/fonts/`
2. Generate `fonts.css` with @font-face declarations
3. Add to manifest with correct MIME type
4. Reference `fonts.css` in all content documents

### Stylesheets

1. Process each stylesheet from `document.stylesheets`
2. Save to `EPUB/styles/stylesheet-{id}.css`
3. Add to manifest
4. Link in content documents based on section.stylesheets

## ZIP Archive Creation

### Requirements

1. **mimetype**: MUST be first file, MUST be uncompressed (STORE)
2. **All other files**: Can be compressed (DEFLATE)
3. **Directory structure**: Must match EPUB specification
4. **File ordering**: mimetype first, then any order

### Implementation (Kotlin)

```kotlin
fun createEpubZip(outputPath: String, epubContent: EpubContent) {
    ZipOutputStream(FileOutputStream(outputPath)).use { zip ->
        // 1. Add mimetype (uncompressed, must be first)
        zip.setMethod(ZipOutputStream.STORED)
        val mimetypeEntry = ZipEntry("mimetype")
        val mimetypeBytes = "application/epub+zip".toByteArray()
        mimetypeEntry.size = mimetypeBytes.size.toLong()
        mimetypeEntry.compressedSize = mimetypeBytes.size.toLong()
        mimetypeEntry.crc = calculateCRC32(mimetypeBytes)
        zip.putNextEntry(mimetypeEntry)
        zip.write(mimetypeBytes)
        zip.closeEntry()
        
        // 2. Switch to compression for remaining files
        zip.setMethod(ZipOutputStream.DEFLATED)
        
        // 3. Add META-INF/container.xml
        addToZip(zip, "META-INF/container.xml", containerXml)
        
        // 4. Add package.opf
        addToZip(zip, "EPUB/package.opf", packageOpf)
        
        // 5. Add navigation
        addToZip(zip, "EPUB/nav.xhtml", navXhtml)
        addToZip(zip, "EPUB/toc.ncx", tocNcx)
        
        // 6. Add content documents
        epubContent.sections.forEach { section ->
            addToZip(zip, "EPUB/content/${section.id}.xhtml", section.xhtml)
        }
        
        // 7. Add stylesheets
        addToZip(zip, "EPUB/styles/fonts.css", fontsCss)
        epubContent.stylesheets.forEach { stylesheet ->
            addToZip(zip, "EPUB/styles/${stylesheet.id}.css", stylesheet.content)
        }
        
        // 8. Add fonts
        epubContent.fonts.forEach { font ->
            addFileToZip(zip, "EPUB/fonts/${font.filename}", font.file)
        }
        
        // 9. Add images
        epubContent.images.forEach { image ->
            addFileToZip(zip, "EPUB/images/${image.filename}", image.file)
        }
    }
}
```

## EPUB 3.3 Validation

### Validation Steps

1. **Structure validation**:
   - mimetype file present and correct
   - META-INF/container.xml present
   - Package document exists at specified path
   - All referenced files in manifest exist

2. **Content validation**:
   - All XHTML documents are well-formed
   - All required metadata present
   - Spine references valid manifest items
   - Navigation document valid

3. **Resource validation**:
   - All images/fonts referenced in content exist in manifest
   - MIME types correct
   - File paths relative and correct

### Validation Tools

**Recommended**: EPUBCheck (official validator)
```bash
java -jar epubcheck.jar publication.epub
```

**Integration**: Consider embedding validation in the app or providing external validation

### Common Validation Errors

- Missing mimetype file
- mimetype compressed or not first
- Invalid package.opf XML
- Missing required metadata (identifier, title, language, modified date)
- Spine references non-existent items
- Broken internal links
- Invalid XHTML
- Missing manifest entries

## Export Implementation

### EpubExporter Class

```kotlin
class EpubExporter(
    private val document: Document,
    private val styleResolver: StylesheetResolver,
    private val markdownConverter: MarkdownToHtmlConverter
) {
    
    suspend fun export(outputPath: String): Result<Unit> {
        return try {
            // 1. Prepare EPUB structure
            val epubContent = prepareEpubContent()
            
            // 2. Convert all sections
            val sections = convertSections()
            
            // 3. Generate package.opf
            val packageOpf = generatePackageOpf(sections)
            
            // 4. Generate navigation
            val navXhtml = generateNavigation(sections)
            val tocNcx = generateTocNcx(sections)
            
            // 5. Process resources
            val fonts = processFonts()
            val images = processImages()
            val stylesheets = processStylesheets()
            
            // 6. Create ZIP archive
            createEpubZip(outputPath, EpubContent(
                sections = sections,
                packageOpf = packageOpf,
                navXhtml = navXhtml,
                tocNcx = tocNcx,
                fonts = fonts,
                images = images,
                stylesheets = stylesheets
            ))
            
            // 7. Validate (optional but recommended)
            validateEpub(outputPath)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun convertSections(): List<ConvertedSection> {
        return document.spine.map { sectionId ->
            val section = document.sections[sectionId]!!
            val stylesheets = styleResolver.getStylesheetsForSection(sectionId)
            
            ConvertedSection(
                id = sectionId,
                title = section.title ?: extractTitle(section.content),
                xhtml = generateSectionXhtml(section, stylesheets),
                headings = extractHeadings(section.content)
            )
        }
    }
    
    private fun generateSectionXhtml(
        section: Section, 
        stylesheets: List<Stylesheet>
    ): String {
        val (frontmatter, content) = extractFrontmatter(section.content)
        val html = markdownConverter.convertToHtml(content)
        
        return buildXhtml {
            xmlDeclaration()
            html(xmlns = "http://www.w3.org/1999/xhtml", 
                 epubNamespace = true) {
                head {
                    meta(charset = "UTF-8")
                    title(section.title ?: frontmatter["title"] as? String ?: "")
                    
                    // Link fonts stylesheet
                    link(rel = "stylesheet", 
                         type = "text/css", 
                         href = "../styles/fonts.css")
                    
                    // Link section stylesheets
                    stylesheets.forEach { stylesheet ->
                        link(rel = "stylesheet", 
                             type = "text/css", 
                             href = "../styles/${stylesheet.id}.css")
                    }
                }
                body(epubType = "bodymatter") {
                    section {
                        raw(html)
                    }
                }
            }
        }
    }
}
```

## Export UI Flow

```
User clicks "Export to EPUB"
↓
Show export dialog:
- Output filename
- Include TOC? (checkbox)
- Validate after export? (checkbox)
- [Cancel] [Export]
↓
User clicks Export
↓
Show progress dialog:
- Converting sections... (progress bar)
- Processing resources...
- Creating archive...
- Validating...
↓
Export complete:
- Success: "EPUB created successfully"
- Option to share/open file
↓
OR
↓
Export failed:
- Show error message
- Option to retry
```

## Testing Checklist

- [ ] EPUB structure correct
- [ ] mimetype file first and uncompressed
- [ ] All manifest items present in ZIP
- [ ] Navigation document valid
- [ ] All sections convert correctly
- [ ] CSS classes preserved
- [ ] Fonts embedded and working
- [ ] Images display correctly
- [ ] Internal links work
- [ ] Passes EPUBCheck validation
- [ ] Opens in major EPUB readers (Calibre, Apple Books, Google Play Books)

## EPUB Reader Compatibility

Test in these readers:
- **Calibre** (Desktop)
- **Apple Books** (iOS/macOS)
- **Google Play Books** (Android/Web)
- **Kobo** (e-ink readers)
- **Adobe Digital Editions** (Desktop)

## Resources

- [EPUB 3.3 Specification](https://www.w3.org/TR/epub-33/)
- [EPUBCheck Validator](https://github.com/w3c/epubcheck)
- [EPUB Content Documents 3.3](https://www.w3.org/TR/epub-33/#sec-content-docs)
- [EPUB Packages 3.3](https://www.w3.org/TR/epub-33/#sec-package-doc)
