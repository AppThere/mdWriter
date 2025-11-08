# Additional Implementation Prompts for New Features

These prompts extend the original IMPLEMENTATION_PROMPTS.md with the new features: EPUB export, file association, landing page, enhanced CSS editor, and resource sync.

## Prerequisites

Before using these prompts:
- [ ] Complete Prompts 1-10 from IMPLEMENTATION_PROMPTS.md
- [ ] Have basic editor, document management, and sync working
- [ ] Read EPUB_EXPORT_SPEC.md
- [ ] Read FILE_ASSOCIATION_SPEC.md  
- [ ] Read CSS_EDITOR_AND_SYNC_SPEC.md

## Prompt 11A: Landing Page & File Association

```
Implement the landing page and file association features for MDWriter.

Package: com.appthere.mdwriter
File Extension: .mdoc

Please implement:

1. Landing Page (ui/screen/LandingPage.kt):
   - Display recent documents (sorted by last opened)
   - Show document metadata (title, word count, last edited)
   - "Create New Document" action
   - Responsive layout following CLAUDE.md breakpoints
   - Empty state when no recent documents

2. Recent Documents Tracking:
   - RecentDocumentsRepository to store/retrieve recent docs
   - Store in DataStore (max 20 documents)
   - Track: id, title, path, lastOpened, lastModified, wordCount, preview
   - Add document to recent when opened
   - Sort by lastOpened descending

3. App Navigation:
   - AppNavigator class with destinations (Landing, Editor, DocumentList, Settings)
   - Handle deep linking to editor with file path
   - Navigate home from editor

4. Platform-Specific File Association:

   Android (androidMain):
   - Update AndroidManifest.xml with intent filters for .mdoc files
   - MIME type: application/vnd.appthere.mdwriter+json
   - Handle ACTION_VIEW intents in MainActivity
   - Copy files from content:// URI to internal storage
   - Handle files opened while app is running (onNewIntent)
   
   iOS (iosMain):
   - Configure Info.plist with UTI declaration
   - UTI: com.appthere.mdwriter.document
   - Handle file opening in SceneDelegate/AppDelegate
   - Copy security-scoped resources to app container
   
   Desktop (desktopMain):
   - Accept file path as command-line argument
   - Implement drag-and-drop for .mdoc files
   - Open file if provided, else show landing page

5. Testing:
   - Test landing page with 0, 1, and 20 recent documents
   - Test file opening from external sources
   - Test navigation flows

Reference FILE_ASSOCIATION_SPEC.md for complete specifications.
Target: Full landing page and file association working on all platforms.
```

## Prompt 12: Enhanced CSS Editor with Autocomplete

```
Implement CSS editor enhancements with intelligent autocomplete.

Please implement:

1. CSS Autocomplete Engine (util/CSSAutocompleteEngine.kt):
   - Analyze cursor context (property, value, selector, font-family)
   - Generate suggestions based on context
   - Property suggestions (display, margin, padding, font-*, etc.)
   - Value suggestions (context-aware per property)
   - Selector suggestions
   - Font-family suggestions (embedded fonts + web-safe fonts)
   
2. Autocomplete Data:
   - List of 50+ common CSS properties
   - Property-to-values mapping
   - System font detection (platform-specific)
   - Embedded font detection from document.resources.fonts

3. Enhanced CSS Editor UI (ui/components/CSSEditorWithAutocomplete.kt):
   - TextField with monospace font
   - Autocomplete dropdown positioned below cursor
   - Show suggestions as user types
   - Keyboard navigation (up/down arrows, Enter to accept, Esc to dismiss)
   - Click to select suggestion
   - Display suggestion type (property/value/font)
   
4. Font-Family Special Handling:
   - Detect when editing font-family property
   - Show embedded fonts first with "(embedded)" label
   - Show web-safe fonts
   - Show system fonts
   - Format suggestion: 'Font Name',

5. Suggestion Application:
   - Insert suggestion at cursor position
   - Replace current partial word
   - Move cursor after insertion
   - Continue typing after selection

6. Testing:
   - Test property autocomplete
   - Test value autocomplete for various properties
   - Test font-family with embedded fonts
   - Test keyboard navigation
   - Test performance with large CSS files

Reference CSS_EDITOR_AND_SYNC_SPEC.md for implementation details.
Target: Fully functional CSS autocomplete system.
```

## Prompt 13: Font Management

```
Implement font management features with licensing awareness.

Please implement:

1. Font Addition Dialog (ui/components/FontManagementDialog.kt):
   - Show licensing warning (prominent, must acknowledge)
   - File picker for font files (.ttf, .otf, .woff, .woff2)
   - Display selected file name
   - Extract font metadata (family name, weight, style)
   - Add to document.resources.fonts

2. Font File Processing (util/FontProcessor.kt):
   - Detect font format from extension
   - Extract metadata (use platform-specific APIs or parser)
   - Generate unique ID
   - Copy font file to document resources directory
   - Create FontResource object

3. Font Metadata Extraction:
   - Family name
   - Weight (100-900)
   - Style (normal, italic, oblique)
   - Format (truetype, opentype, woff, woff2)

4. Font List UI (ui/screen/FontManagementScreen.kt):
   - List all fonts in document.resources.fonts
   - Show family, weight, style, format
   - Preview text in font (if possible)
   - Delete font option
   - Add new font button

5. CSS Generation:
   - Generate @font-face declarations for all fonts
   - Create fonts.css stylesheet
   - Apply globally to all sections
   - Include in EPUB export

6. Testing:
   - Test with various font formats
   - Test font metadata extraction
   - Test CSS generation
   - Verify fonts work in preview
   - Test licensing warning flow

Reference CSS_EDITOR_AND_SYNC_SPEC.md and EPUB_EXPORT_SPEC.md.
Target: Complete font management system with proper licensing warnings.
```

## Prompt 14: EPUB 3.3 Export Foundation

```
Phase 1 of EPUB export: Implement core EPUB structure and file generation.

Please implement:

1. EPUB Structure Models (data/model/epub/):
   - EpubManifestItem (id, href, mediaType, properties)
   - EpubSpineItem (idref, linear)
   - EpubMetadata (from Document.metadata)
   - EpubPackage (metadata, manifest, spine)
   - EpubNavPoint (label, content, children)

2. Package Document Generator (domain/epub/PackageGenerator.kt):
   - Generate package.opf XML from Document
   - Include all metadata (Dublin Core)
   - Create manifest with all resources
   - Generate spine in document.spine order
   - Assign proper MIME types

3. Container File Generator:
   - Generate META-INF/container.xml
   - Point to EPUB/package.opf

4. Mimetype File:
   - Create mimetype file with correct content
   - Ensure no newline, no BOM, ASCII encoding

5. Testing:
   - Test package.opf generation
   - Validate XML structure
   - Test with various document configurations
   - Test spine ordering

Reference EPUB_EXPORT_SPEC.md sections on "Required Files" and "Package Document".
Target: Generate valid EPUB package structure (no content yet).
```

## Prompt 15: EPUB Navigation & Content Conversion

```
Phase 2 of EPUB export: Implement navigation and Markdown to XHTML conversion.

Please implement:

1. Navigation Generator (domain/epub/NavigationGenerator.kt):
   - Generate nav.xhtml (EPUB 3 navigation)
   - Extract headings from sections
   - Build hierarchical TOC
   - Generate landmarks navigation
   - Include proper XHTML structure and namespaces

2. NCX Generator (domain/epub/NcxGenerator.kt):
   - Generate toc.ncx for EPUB 2 compatibility
   - Create navMap from sections
   - Assign playOrder
   - Include metadata

3. Markdown to XHTML Converter (domain/epub/MarkdownToXhtmlConverter.kt):
   - Convert all Markdown elements to XHTML
   - Preserve CSS class annotations as class attributes
   - Generate heading IDs for linking
   - Convert image paths to EPUB-relative paths
   - Escape HTML entities
   - Ensure well-formed XHTML

4. Content Document Generator (domain/epub/ContentGenerator.kt):
   - Generate XHTML for each section
   - Include proper DOCTYPE and namespaces
   - Link stylesheets (fonts.css + section stylesheets)
   - Include frontmatter as meta tags
   - Apply section.title or extract from content

5. Testing:
   - Test nav.xhtml generation
   - Test NCX generation
   - Test Markdown conversion for all elements
   - Test CSS class preservation
   - Validate XHTML structure

Reference EPUB_EXPORT_SPEC.md sections on navigation and content documents.
Target: Generate valid navigation and convert all content to XHTML.
```

## Prompt 16: EPUB Resource Processing & ZIP Creation

```
Phase 3 of EPUB export: Complete EPUB export with resources and ZIP archive.

Please implement:

1. Font CSS Generator (domain/epub/FontCssGenerator.kt):
   - Generate fonts.css with all @font-face declarations
   - Include all fonts from document.resources.fonts
   - Map file extensions to CSS formats
   - Use correct font-family names

2. Resource Processor (domain/epub/ResourceProcessor.kt):
   - Copy images to EPUB/images/
   - Copy fonts to EPUB/fonts/
   - Copy stylesheets to EPUB/styles/
   - Update paths in manifest
   - Assign correct MIME types

3. EPUB ZIP Creator (domain/epub/EpubZipCreator.kt):
   - Create ZIP archive with proper structure
   - Add mimetype FIRST, UNCOMPRESSED (STORED)
   - Add all other files compressed (DEFLATE)
   - Maintain directory structure
   - Calculate CRC32 for mimetype

4. EPUB Exporter (domain/epub/EpubExporter.kt):
   - Orchestrate complete export process
   - Prepare EPUB structure
   - Convert sections
   - Generate package.opf, nav.xhtml, toc.ncx
   - Process resources
   - Create ZIP archive
   - Return Result with path or error

5. Export UI (ui/components/ExportEpubDialog.kt):
   - File name input
   - Options (include TOC, validate)
   - Progress dialog with steps
   - Success message with option to share/open
   - Error handling and display

6. Optional: EPUB Validation:
   - Run EPUBCheck if available
   - Display validation results
   - Handle validation errors

7. Testing:
   - Test complete EPUB export
   - Verify ZIP structure
   - Test mimetype is first and uncompressed
   - Verify fonts are embedded correctly
   - Test in EPUB readers (Calibre, Apple Books)
   - Validate with EPUBCheck

Reference EPUB_EXPORT_SPEC.md for complete specifications.
Target: Fully functional EPUB 3.3 export that passes validation.
```

## Prompt 17: Enhanced Cloud Sync with Resources

```
Extend cloud sync to handle external resources (fonts, images).

Please implement:

1. Resource Sync Models (data/sync/):
   - ResourceSyncMetadata (resourceId, localPath, cloudPath, cloudUri, hash, lastSyncTime)
   - ResourceSyncRepository interface
   - ResourceSyncResult sealed class

2. Enhanced Sync Provider Interface (data/sync/ResourceAwareSyncProvider.kt):
   - Extend SyncProvider with resource methods
   - uploadResource(localPath, remotePath, resourceType) → cloudUri
   - downloadResource(cloudUri, localPath)
   - resourceExists(remotePath) → Boolean
   - ResourceType enum (FONT, IMAGE, ATTACHMENT)

3. Resource Sync Orchestrator (domain/sync/ResourceSyncOrchestrator.kt):
   - syncDocumentWithResources(documentId)
   - syncFonts(document) → List<ResourceSyncResult>
   - syncImages(document) → List<ResourceSyncResult>
   - Check existing sync metadata
   - Calculate file hashes
   - Upload only changed resources
   - Update document with cloud URIs
   - Save resource sync metadata

4. Implement for Google Drive (data/sync/GoogleDriveResourceProvider.kt):
   - uploadResource implementation
   - Create folder structure in Drive
   - Upload with correct MIME types
   - Return webContentLink or file ID as cloud URI
   - Handle large files

5. Implement for OneDrive & Dropbox:
   - Similar implementations for each provider
   - Provider-specific URI formats

6. Update Document with Cloud URIs:
   - Replace local paths with cloud URIs in resources
   - Maintain backwards compatibility
   - Handle URI resolution for preview

7. Sync Progress UI:
   - Show resource sync progress
   - Display: "Resources: 3/5"
   - Show current operation
   - Handle long uploads

8. Testing:
   - Test font upload to each provider
   - Test image upload
   - Test hash-based skipping
   - Test document update with cloud URIs
   - Test large file uploads
   - Test network interruptions

Reference CSS_EDITOR_AND_SYNC_SPEC.md and SYNC_ARCHITECTURE_SPEC.md.
Target: Cloud sync that handles all document resources automatically.
```

## Prompt 18: Integration & Polish

```
Final integration and polish for all new features.

Please implement:

1. Deep Linking Integration:
   - Connect file association to app navigation
   - Open document directly in editor when launched with file
   - Add to recent documents
   - Handle errors (file not found, corrupted)

2. Export Integration:
   - Add "Export to EPUB" option to editor menu
   - Export to EPUB from document list
   - Share EPUB file (platform-specific)
   - Open in default EPUB reader

3. Font Integration:
   - Ensure embedded fonts work in preview
   - Fonts available in CSS autocomplete
   - Fonts included in EPUB export
   - Fonts sync to cloud storage

4. Settings Screen:
   - Font management section
   - Export preferences
   - File associations info
   - Cloud sync with resources toggle

5. Error Handling:
   - Handle EPUB export errors gracefully
   - Handle font file errors
   - Handle sync resource errors
   - User-friendly error messages

6. Performance Optimization:
   - Lazy load fonts
   - Cache EPUB conversion
   - Optimize resource uploads
   - Background sync for resources

7. Documentation:
   - Update README with new features
   - Document EPUB export process
   - Document font management
   - Document file association setup

8. Testing:
   - End-to-end test: Create document → Add fonts → Export EPUB → Verify
   - End-to-end test: Create document → Add images → Sync → Download → Verify
   - Test all platforms for file association
   - Test EPUB in multiple readers

Target: All new features integrated and polished for release.
```

## Feature Testing Checklist

### Landing Page & File Association
- [ ] Landing page displays on launch with no arguments
- [ ] Recent documents sorted correctly
- [ ] Create new document works
- [ ] Double-click .mdoc file opens in app (all platforms)
- [ ] File opened from external app works (Android/iOS)
- [ ] Document added to recent when opened
- [ ] Responsive layout works on all screen sizes

### CSS Autocomplete
- [ ] Property suggestions appear
- [ ] Value suggestions contextual to property
- [ ] Font-family shows embedded fonts
- [ ] Keyboard navigation works
- [ ] Click selection works
- [ ] Autocomplete doesn't lag on typing

### Font Management
- [ ] Licensing warning shown and must be acknowledged
- [ ] Font file picker works
- [ ] Font metadata extracted correctly
- [ ] Font added to resources
- [ ] Font appears in autocomplete
- [ ] Font preview works (if implemented)
- [ ] Fonts can be deleted

### EPUB Export
- [ ] Export generates valid .epub file
- [ ] mimetype is first and uncompressed
- [ ] All sections converted to XHTML
- [ ] Navigation working (TOC)
- [ ] Stylesheets included and working
- [ ] Fonts embedded and working
- [ ] Images included and displaying
- [ ] CSS classes preserved
- [ ] Opens in Calibre
- [ ] Opens in Apple Books
- [ ] Passes EPUBCheck validation

### Resource Sync
- [ ] Fonts upload to cloud storage
- [ ] Images upload to cloud storage
- [ ] Resources only upload when changed
- [ ] Document updated with cloud URIs
- [ ] Progress shows resource uploads
- [ ] Large files handle correctly
- [ ] Network errors handled gracefully
- [ ] Downloaded documents include resources

## Budget Allocation

**Estimated time for new features**: 12-15 hours

High Priority (8-10 hours):
- ✅ Landing page & file association (2-3 hours)
- ✅ EPUB export foundation (3-4 hours)
- ✅ EPUB content & ZIP creation (2-3 hours)

Medium Priority (3-4 hours):
- CSS autocomplete (1-2 hours)
- Font management (1-2 hours)

Lower Priority (1-2 hours):
- Enhanced resource sync (2-3 hours)
- Integration & polish (1-2 hours)

**Total Project Estimate**: 22-25 hours of Claude Code time
(Original 10-15 hours + New features 12-15 hours)

## Implementation Order

**Recommended sequence**:

1. **Landing Page & File Association** (Prompt 11A)
   - Provides good user experience foundation
   - Required for full app flow

2. **Font Management** (Prompt 13)
   - Smaller feature, can be done early
   - Needed for CSS autocomplete and EPUB

3. **CSS Autocomplete** (Prompt 12)
   - Builds on font management
   - Improves editor experience

4. **EPUB Export** (Prompts 14-16)
   - Core value-add feature
   - Requires multiple sessions
   - Build incrementally

5. **Enhanced Resource Sync** (Prompt 17)
   - Extends existing sync
   - Depends on resource tracking

6. **Integration & Polish** (Prompt 18)
   - Final touches
   - Bug fixes
   - Prepare for release

## Notes for Claude Code

- Work through EPUB export in phases (Prompts 14, 15, 16)
- Test EPUB output frequently in real readers
- Reference specifications for exact XML/file formats
- Use existing parsers/generators where possible
- Prioritize EPUB validation compliance
- Test file association on actual devices
- Keep font licensing warnings prominent
