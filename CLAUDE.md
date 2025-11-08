# Markdown Editor - Kotlin/Compose Multiplatform

## Project Overview

A cross-platform Markdown editing application built with Kotlin Multiplatform and Compose Multiplatform. The application edits multipart Markdown documents stored in a custom JSON format with metadata, supports Hugo-style frontmatter and CSS class annotations, provides real-time syntax highlighting, and syncs to cloud storage services.

**Target Platforms**: Android, iOS, Desktop (Windows, macOS, Linux)

## Core Architecture

### Document Format (JSON)

Documents are stored as JSON files containing:
- **Document Metadata**: Dublin Core metadata (title, author, date, etc.)
- **Spine**: Ordered list of content sections/chapters
- **Content Sections**: Individual Markdown content blocks with unique IDs
- **Stylesheets**: CSS stylesheet objects for document styling
- **Resources**: References to external fonts and images

Example structure:
```json
{
  "metadata": {
    "title": "Document Title",
    "author": "Author Name",
    "created": "2025-11-08T10:00:00Z",
    "modified": "2025-11-08T10:00:00Z",
    "language": "en"
  },
  "spine": ["section-1", "section-2", "section-3"],
  "sections": {
    "section-1": {
      "id": "section-1",
      "content": "---\ntitle: Chapter 1\n---\n\n# Chapter One\n\nContent here...",
      "order": 0,
      "stylesheets": ["base", "chapter-style"]
    }
  },
  "stylesheets": [
    {
      "id": "base",
      "name": "Base Styles",
      "scope": "global",
      "content": "body { font-family: serif; }"
    },
    {
      "id": "chapter-style",
      "name": "Chapter Styles",
      "scope": "manual",
      "content": ".chapter-heading { font-size: 2em; }"
    }
  ],
  "resources": {
    "fonts": [],
    "images": []
  },
  "settings": {
    "defaultStylesheets": ["base"]
  }
}
```

**Stylesheet Linking**:
- **Global stylesheets**: Apply to all sections (via `scope: "global"` or `settings.defaultStylesheets`)
- **Section-specific stylesheets**: Each section can reference zero or more stylesheets via `section.stylesheets` array
- **CSS class annotations**: Markdown blocks use `{.class-name}` to reference styles from active stylesheets
- **Application order**: Default stylesheets → Section-specific stylesheets (in order) → CSS class annotations

### Markdown Features

#### Hugo-Style Frontmatter
- YAML frontmatter at the beginning of sections
- Delimited by `---` markers
- Contains section-specific metadata

#### CSS Class Annotations
- Syntax: `{.css-class}` after block elements
- Example: `## Heading {.special-heading}`
- Applied to the preceding block element

### Application Architecture

**Pattern**: Clean Architecture with MVVM presentation layer

**Layers**:
1. **Domain Layer** (`commonMain`)
   - Document models (Document, Section, Metadata)
   - Use cases (LoadDocument, SaveDocument, ParseMarkdown, etc.)
   - Repository interfaces

2. **Data Layer** (`commonMain` + platform-specific)
   - Repository implementations
   - Local storage (file system access)
   - Cloud sync adapters (Google Drive, OneDrive, Dropbox)
   - JSON serialization/deserialization

3. **Presentation Layer** (`commonMain`)
   - ViewModels with state management
   - UI state models
   - Navigation logic

4. **UI Layer** (`commonMain` + platform-specific optimizations)
   - Compose UI components
   - Platform-specific file pickers, share sheets

## Key Technical Requirements

### 1. Markdown Parsing and Rendering

**Library Recommendation**: Use `intellij-markdown` (JetBrains) or create custom parser
- Parse Hugo frontmatter separately from content
- Identify block elements and their CSS class annotations
- Generate AST for rendering

**Real-time Syntax Highlighting**:
- TextFieldValue with AnnotatedString for styled text
- Apply SpanStyle based on Markdown syntax:
  - Headers: Larger font size, bold weight
  - Strong text: FontWeight.Bold
  - Emphasis: FontStyle.Italic
  - Links: Underline, blue color
  - Code: Monospace font, different background

**Implementation Approach**:
```kotlin
// Parse markdown and generate AnnotatedString
fun parseMarkdownWithStyles(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder(text)
    val ast = parseMarkdown(text)
    
    ast.children.forEach { node ->
        when (node.type) {
            ASTNodeType.HEADER -> {
                builder.addStyle(
                    SpanStyle(
                        fontSize = when(node.level) {
                            1 -> 32.sp
                            2 -> 28.sp
                            else -> 24.sp
                        },
                        fontWeight = FontWeight.Bold
                    ),
                    start = node.startOffset,
                    end = node.endOffset
                )
            }
            // ... other types
        }
    }
    
    return builder.toAnnotatedString()
}
```

### 2. CSS Editor with Syntax Highlighting

**Library**: Build on top of CodeEditText or create custom styled text field
- Highlight CSS selectors, properties, values
- Bracket matching
- Auto-indentation

**Color Scheme**: Use same E Ink-friendly palette as main UI

### 3. Responsive UI Design

**Breakpoints**:
- Compact: < 600dp (phone portrait)
- Medium: 600-840dp (phone landscape, small tablets)
- Expanded: > 840dp (tablets, desktop)

**Adaptive Layouts**:
- Compact: Single pane with navigation drawer
- Medium: Side-by-side with collapsible sidebar
- Expanded: Three-pane layout (file browser, editor, preview/styles)

**Implementation**:
```kotlin
@Composable
fun AdaptiveLayout() {
    val windowSizeClass = calculateWindowSizeClass()
    
    when (windowSizeClass) {
        WindowSizeClass.Compact -> CompactLayout()
        WindowSizeClass.Medium -> MediumLayout()
        WindowSizeClass.Expanded -> ExpandedLayout()
    }
}
```

### 4. E Ink Optimized Color Scheme

**Principle**: Use colors that render well on Kaleido 3 displays (4096 colors, ~100 PPI)

**Light Mode Palette**:
- Background: #FFFFFF (pure white)
- Surface: #F5F5F5 (very light gray)
- Primary: #1A1A1A (near black)
- Secondary: #4A4A4A (dark gray)
- Accent: #C74440 (muted red - renders well on Kaleido)
- Links: #3B5B8C (desaturated blue)
- Success: #4A7C59 (muted green)
- Warning: #B8860B (dark goldenrod)

**Dark Mode Palette**:
- Background: #1A1A1A (very dark gray, not pure black)
- Surface: #2D2D2D (dark gray)
- Primary: #E5E5E5 (light gray)
- Secondary: #B0B0B0 (medium gray)
- Accent: #D96459 (slightly lighter muted red)
- Links: #6B8CC4 (lighter desaturated blue)

**Guidelines**:
- Avoid pure saturated colors
- Prefer muted, earthy tones
- High contrast between text and background
- Avoid gradients (cause dithering)
- Use solid colors for backgrounds

### 5. Editor Toolbar

**Actions**:
- Block formatting: Paragraph, H1-H6, Blockquote, Code block
- Inline formatting: Bold, Italic, Code, Superscript, Subscript, Strikethrough
- Insert: Link, Image, Table, Horizontal rule
- List: Bullet list, Numbered list, Task list
- Special: Add CSS class, Edit frontmatter

**Implementation**:
```kotlin
@Composable
fun MarkdownToolbar(
    onFormatAction: (MarkdownFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        IconButton(onClick = { onFormatAction(MarkdownFormat.Bold) }) {
            Icon(Icons.Default.FormatBold, "Bold")
        }
        // ... more buttons
    }
}

// Apply formatting by inserting markdown syntax at cursor position
fun applyMarkdownFormat(
    text: TextFieldValue,
    format: MarkdownFormat
): TextFieldValue {
    val selection = text.selection
    val selectedText = text.text.substring(selection.start, selection.end)
    
    val (prefix, suffix) = when (format) {
        MarkdownFormat.Bold -> "**" to "**"
        MarkdownFormat.Italic -> "*" to "*"
        MarkdownFormat.Code -> "`" to "`"
        // ... others
    }
    
    val newText = text.text.replaceRange(
        selection.start,
        selection.end,
        "$prefix$selectedText$suffix"
    )
    
    return text.copy(
        text = newText,
        selection = TextRange(selection.start + prefix.length, selection.end + prefix.length)
    )
}
```

### 6. Cloud Sync Architecture

**Pattern**: Abstract sync adapter with provider-specific implementations

**Base Interface**:
```kotlin
interface SyncProvider {
    suspend fun authenticate(): AuthResult
    suspend fun listFiles(path: String): List<RemoteFile>
    suspend fun uploadFile(localPath: String, remotePath: String): SyncResult
    suspend fun downloadFile(remotePath: String, localPath: String): SyncResult
    suspend fun deleteFile(remotePath: String): SyncResult
    suspend fun getFileMetadata(remotePath: String): FileMetadata
}
```

**Conflict Resolution Strategy** (similar to Joplin):
1. Compare modification timestamps
2. If remote is newer, download and merge
3. If local is newer, upload
4. If conflict detected, create conflict copy with timestamp

**Providers to Implement**:
- Google Drive (using Google Drive API)
- OneDrive (using Microsoft Graph API)
- Dropbox (using Dropbox API)
- WebDAV (for self-hosted solutions)

**Sync Strategy**:
- On-demand sync (manual trigger)
- Background sync (periodic check)
- Conflict detection with user resolution UI
- Local-first approach (always work offline-capable)

### 7. Testing Strategy

**Unit Tests** (> 80% coverage target):
- Document model serialization/deserialization
- Markdown parsing logic
- Syntax highlighting logic
- CSS class extraction
- Frontmatter parsing
- All use cases and view models

**Instrumented Tests**:
- UI interactions (toolbar actions)
- File system operations
- Database operations (if using local DB)

**Integration Tests**:
- End-to-end document editing flow
- Sync provider integration (with mock servers)

**Testing Libraries**:
- JUnit 5 for unit tests
- Kotest for assertion DSL
- MockK for mocking
- Compose UI Test for UI testing

**Example Test Structure**:
```kotlin
class MarkdownParserTest {
    @Test
    fun `parse heading with CSS class annotation`() {
        val input = "## My Heading {.special}"
        val result = parseMarkdownLine(input)
        
        assertEquals("My Heading", result.content)
        assertEquals(HeadingLevel.H2, result.type)
        assertEquals("special", result.cssClass)
    }
}
```

### 8. Accessibility

**Requirements**:
- Semantic content descriptions for all interactive elements
- Keyboard navigation support (desktop)
- Screen reader compatibility
- Minimum touch target size: 48dp x 48dp
- Sufficient color contrast ratios (WCAG AA)
- Focus indicators
- Alternative text for images

**Implementation**:
```kotlin
@Composable
fun ToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp) // Minimum touch target
            .semantics { this.contentDescription = contentDescription }
    ) {
        Icon(icon, contentDescription)
    }
}
```

## Project Structure

```
project-root/
├── composeApp/
│   ├── src/
│   │   ├── androidMain/
│   │   │   ├── kotlin/
│   │   │   │   └── com/yourcompany/mdeditor/
│   │   │   │       ├── platform/
│   │   │   │       │   ├── FilePicker.android.kt
│   │   │   │       │   └── SyncProvider.android.kt
│   │   │   │       └── MainActivity.kt
│   │   │   └── AndroidManifest.xml
│   │   ├── commonMain/
│   │   │   ├── kotlin/
│   │   │   │   └── com/appthere/mdwriter/
│   │   │   │       ├── data/
│   │   │   │       │   ├── model/
│   │   │   │       │   │   ├── Document.kt
│   │   │   │       │   │   ├── Section.kt
│   │   │   │       │   │   ├── Metadata.kt
│   │   │   │       │   │   └── Stylesheet.kt
│   │   │   │       │   ├── repository/
│   │   │   │       │   │   ├── DocumentRepository.kt
│   │   │   │       │   │   └── SyncRepository.kt
│   │   │   │       │   ├── local/
│   │   │   │       │   │   └── JsonDocumentStore.kt
│   │   │   │       │   └── sync/
│   │   │   │       │       ├── SyncProvider.kt
│   │   │   │       │       ├── GoogleDriveSyncProvider.kt
│   │   │   │       │       ├── OneDriveSyncProvider.kt
│   │   │   │       │       └── DropboxSyncProvider.kt
│   │   │   │       ├── domain/
│   │   │   │       │   ├── usecase/
│   │   │   │       │   │   ├── LoadDocumentUseCase.kt
│   │   │   │       │   │   ├── SaveDocumentUseCase.kt
│   │   │   │       │   │   ├── ParseMarkdownUseCase.kt
│   │   │   │       │   │   ├── SyncDocumentUseCase.kt
│   │   │   │       │   │   └── ExportDocumentUseCase.kt
│   │   │   │       │   └── model/
│   │   │   │       │       └── MarkdownNode.kt
│   │   │   │       ├── presentation/
│   │   │   │       │   ├── editor/
│   │   │   │       │   │   ├── EditorViewModel.kt
│   │   │   │       │   │   ├── EditorState.kt
│   │   │   │       │   │   └── EditorIntent.kt
│   │   │   │       │   ├── documents/
│   │   │   │       │   │   ├── DocumentListViewModel.kt
│   │   │   │       │   │   └── DocumentListState.kt
│   │   │   │       │   └── settings/
│   │   │   │       │       ├── SettingsViewModel.kt
│   │   │   │       │       └── SettingsState.kt
│   │   │   │       ├── ui/
│   │   │   │       │   ├── theme/
│   │   │   │       │   │   ├── Color.kt
│   │   │   │       │   │   ├── Theme.kt
│   │   │   │       │   │   └── Typography.kt
│   │   │   │       │   ├── components/
│   │   │   │       │   │   ├── MarkdownEditor.kt
│   │   │   │       │   │   ├── MarkdownToolbar.kt
│   │   │   │       │   │   ├── CSSEditor.kt
│   │   │   │       │   │   ├── FrontmatterEditor.kt
│   │   │   │       │   │   └── SyntaxHighlighter.kt
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   ├── EditorScreen.kt
│   │   │   │       │   │   ├── DocumentListScreen.kt
│   │   │   │       │   │   └── SettingsScreen.kt
│   │   │   │       │   └── navigation/
│   │   │   │       │       └── Navigation.kt
│   │   │   │       └── util/
│   │   │   │           ├── MarkdownParser.kt
│   │   │   │           ├── SyntaxHighlighter.kt
│   │   │   │           └── Extensions.kt
│   │   │   └── composeResources/
│   │   ├── desktopMain/
│   │   │   └── kotlin/
│   │   │       └── com/yourcompany/mdeditor/
│   │   │           ├── platform/
│   │   │           │   ├── FilePicker.desktop.kt
│   │   │           │   └── SyncProvider.desktop.kt
│   │   │           └── main.kt
│   │   ├── iosMain/
│   │   │   └── kotlin/
│   │   │       └── com/yourcompany/mdeditor/
│   │   │           ├── platform/
│   │   │           │   ├── FilePicker.ios.kt
│   │   │           │   └── SyncProvider.ios.kt
│   │   │           └── MainViewController.kt
│   │   └── commonTest/
│   │       └── kotlin/
│   │           └── com/yourcompany/mdeditor/
│   │               ├── data/
│   │               │   └── DocumentSerializationTest.kt
│   │               ├── domain/
│   │               │   └── ParseMarkdownUseCaseTest.kt
│   │               └── util/
│   │                   ├── MarkdownParserTest.kt
│   │                   └── SyntaxHighlighterTest.kt
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Dependencies

**Essential Libraries** (add to `build.gradle.kts`):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            
            // Kotlin Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            
            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            
            // Navigation
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            
            // ViewModel
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
            
            // Markdown parsing (if using intellij-markdown)
            implementation("org.jetbrains:markdown:0.7.3")
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            implementation("io.kotest:kotest-assertions-core:5.9.0")
            implementation("io.mockk:mockk:1.13.11")
        }
        
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.9.0")
            implementation("com.google.android.gms:play-services-auth:21.2.0")
            implementation("com.google.apis:google-api-services-drive:v3-rev20240509-2.0.0")
        }
    }
}
```

## Development Phases

### Phase 1: Core Document Model (Week 1)
- Define data models (Document, Section, Metadata, Stylesheet)
- Implement JSON serialization/deserialization
- Create DocumentRepository with file system persistence
- Write comprehensive unit tests

**Deliverables**:
- `Document.kt`, `Section.kt`, `Metadata.kt`, `Stylesheet.kt`
- `JsonDocumentStore.kt`
- `DocumentRepository.kt`
- Test coverage > 90%

### Phase 2: Markdown Parser (Week 2)
- Implement Markdown parser with Hugo frontmatter support
- Add CSS class annotation parsing
- Create AST representation
- Build syntax highlighter (generate AnnotatedString)

**Deliverables**:
- `MarkdownParser.kt`
- `SyntaxHighlighter.kt`
- `MarkdownNode.kt` (AST models)
- Test coverage > 85%

### Phase 3: Basic Editor UI (Week 3)
- Create basic editor screen with text input
- Implement real-time syntax highlighting
- Add basic toolbar with formatting actions
- Implement E Ink optimized theme

**Deliverables**:
- `EditorScreen.kt`
- `MarkdownEditor.kt` (text field with highlighting)
- `MarkdownToolbar.kt`
- `Theme.kt` with E Ink color palette

### Phase 4: Advanced Editor Features (Week 4)
- CSS editor with syntax highlighting
- Frontmatter editor
- Insert link/image dialogs
- Document metadata editor

**Deliverables**:
- `CSSEditor.kt`
- `FrontmatterEditor.kt`
- Insert dialogs
- Metadata editor UI

### Phase 5: Document Management (Week 5)
- Document list screen
- Create/delete/rename documents
- File picker integration (platform-specific)
- Recent documents

**Deliverables**:
- `DocumentListScreen.kt`
- `FilePicker.kt` (expect/actual)
- Document CRUD operations

### Phase 6: Responsive Design (Week 6)
- Implement adaptive layouts for different screen sizes
- Navigation drawer for compact screens
- Multi-pane layout for tablets/desktop
- Test on various form factors

**Deliverables**:
- `AdaptiveLayout.kt`
- Responsive navigation
- Breakpoint handling

### Phase 7: Cloud Sync - Foundation (Week 7)
- Implement SyncProvider interface
- Create sync state management
- Add conflict resolution logic
- Implement one provider (Google Drive)

**Deliverables**:
- `SyncProvider.kt` interface
- `GoogleDriveSyncProvider.kt`
- `SyncRepository.kt`
- Conflict resolution UI

### Phase 8: Cloud Sync - Additional Providers (Week 8)
- OneDrive sync provider
- Dropbox sync provider
- WebDAV sync provider (optional)
- Sync settings UI

**Deliverables**:
- `OneDriveSyncProvider.kt`
- `DropboxSyncProvider.kt`
- Settings screen with sync configuration

### Phase 9: Accessibility & Testing (Week 9)
- Add semantic labels to all UI elements
- Implement keyboard navigation
- Ensure minimum touch target sizes
- Achieve >80% unit test coverage
- Write UI tests for critical flows

**Deliverables**:
- Accessibility annotations throughout UI
- Comprehensive test suite
- Testing documentation

### Phase 10: Polish & Optimization (Week 10)
- Performance optimization
- Animation polish
- Icon set
- Documentation
- Release preparation

**Deliverables**:
- Performance benchmarks
- User documentation
- Developer documentation
- Release builds

## Implementation Tips

### Starting the Project

1. **Create Project**: Use Kotlin Multiplatform Wizard or Android Studio
2. **Set Up Dependencies**: Add all required libraries to `build.gradle.kts`
3. **Configure Targets**: Enable Android, iOS, and Desktop targets
4. **Set Up Theme**: Create E Ink optimized color palette first

### Markdown Parser Strategy

**Option 1**: Use `intellij-markdown` library
- Pros: Mature, well-tested, supports CommonMark
- Cons: May need customization for Hugo features

**Option 2**: Build custom parser
- Pros: Full control, tailored to requirements
- Cons: More work, need to handle edge cases

**Recommendation**: Start with `intellij-markdown`, extend as needed

### Syntax Highlighting Performance

- Use `remember` to cache parsed AST
- Only re-parse when text changes
- Debounce parsing for large documents
- Consider parsing in background for very large files

### Sync Implementation Order

1. Google Drive (most common, good documentation)
2. OneDrive (Microsoft Graph API is well-documented)
3. Dropbox (straightforward API)
4. WebDAV (for advanced users, self-hosting)

### Platform-Specific Considerations

**Android**:
- Use `ActivityResultContracts` for file picker
- Request storage permissions
- Use WorkManager for background sync

**iOS**:
- Use `UIDocumentPickerViewController` for file picker
- Use `URLSession` for network operations
- Background sync via background fetch

**Desktop**:
- Use JFileChooser (Swing) or native file dialogs
- Platform-specific file watchers for auto-save

## Testing Checklist

- [ ] Document serialization/deserialization
- [ ] Markdown parsing with frontmatter
- [ ] CSS class annotation extraction
- [ ] Syntax highlighting accuracy
- [ ] Toolbar formatting actions
- [ ] File operations (create, save, delete)
- [ ] Sync provider authentication
- [ ] Sync conflict resolution
- [ ] Responsive layout breakpoints
- [ ] Accessibility (screen reader, keyboard navigation)
- [ ] Theme switching (light/dark)
- [ ] Cross-platform compatibility

## Performance Targets

- **Cold start time**: < 2 seconds
- **Document open time**: < 500ms for typical documents
- **Typing latency**: < 16ms (60fps)
- **Syntax highlighting**: Real-time with no visible lag
- **Sync operation**: < 5 seconds for typical document

## Security Considerations

- **OAuth Tokens**: Store securely using platform keychain/keystore
- **File Permissions**: Request minimal necessary permissions
- **Data Validation**: Validate JSON structure when loading documents
- **Sync Security**: Use HTTPS for all network operations
- **User Data**: Never log sensitive user content

## Accessibility Guidelines

- All interactive elements have content descriptions
- Minimum touch target: 48dp x 48dp
- Color contrast ratio: 4.5:1 for normal text, 3:1 for large text
- Support for screen readers (TalkBack, VoiceOver)
- Keyboard navigation support on desktop
- Focus indicators visible
- Text can be resized without breaking layout

## Common Challenges & Solutions

### Challenge: Text Field Performance with Large Documents
**Solution**: Virtualize text rendering, only render visible portion

### Challenge: Real-time Syntax Highlighting Lag
**Solution**: Debounce parsing, use background coroutine, cache AST

### Challenge: Sync Conflicts
**Solution**: Implement three-way merge, allow user to choose version, create backup copies

### Challenge: CSS Class Parsing Edge Cases
**Solution**: Use regex with proper escaping, handle multiple classes, validate class names

### Challenge: Platform-Specific File Pickers
**Solution**: Use expect/actual declarations, abstract file picker interface

### Challenge: E Ink Color Dithering
**Solution**: Stick to palette, avoid gradients, test on actual E Ink device

## Resources

- [Compose Multiplatform Docs](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Material Design 3](https://m3.material.io/)
- [CommonMark Spec](https://spec.commonmark.org/)
- [Hugo Documentation](https://gohugo.io/documentation/)
- [Google Drive API](https://developers.google.com/drive/api/guides/about-sdk)
- [Microsoft Graph API](https://docs.microsoft.com/en-us/graph/api/overview)
- [Dropbox API](https://www.dropbox.com/developers/documentation/http/overview)

## Notes for Claude Code

- When implementing, start with Phase 1 and work sequentially
- Each phase should be fully tested before moving to the next
- Use descriptive commit messages for each feature
- Keep functions small and focused (< 50 lines ideally)
- Write tests alongside implementation, not after
- Use Kotlin idioms (data classes, sealed classes, extension functions)
- Prefer immutability (val over var, immutable collections)
- Use coroutines for async operations
- Follow Clean Architecture principles strictly
- Platform-specific code should be minimal and isolated

## Success Criteria

The project is successful when:
1. ✅ User can create and edit Markdown documents with frontmatter
2. ✅ Syntax highlighting works in real-time without lag
3. ✅ CSS classes can be added to blocks using `{.class}` syntax
4. ✅ CSS editor allows styling documents
5. ✅ UI is responsive across phone, tablet, and desktop
6. ✅ Theme is optimized for E Ink displays
7. ✅ Toolbar makes Markdown formatting accessible
8. ✅ Documents sync to at least 2 cloud services
9. ✅ Conflict resolution works correctly
10. ✅ Unit test coverage > 80%
11. ✅ All accessibility requirements met
12. ✅ App works offline-first with sync when available
