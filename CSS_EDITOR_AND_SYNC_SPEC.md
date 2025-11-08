# CSS Editor Enhancements & Cloud Sync with Resources

## CSS Editor Autocomplete

### Overview

The CSS editor should provide intelligent autocomplete suggestions to improve the editing experience and reduce syntax errors.

### Autocomplete Categories

#### 1. CSS Properties

Common properties to suggest:
```
Display & Layout:
- display, position, float, clear
- width, height, max-width, max-height, min-width, min-height
- margin, margin-top, margin-right, margin-bottom, margin-left
- padding, padding-top, padding-right, padding-bottom, padding-left
- box-sizing, overflow, overflow-x, overflow-y

Typography:
- font-family, font-size, font-weight, font-style
- line-height, letter-spacing, text-align, text-decoration
- text-transform, white-space, word-wrap, word-break
- color

Background & Borders:
- background, background-color, background-image, background-size
- background-position, background-repeat
- border, border-width, border-style, border-color
- border-radius, box-shadow

Flexbox:
- flex-direction, flex-wrap, justify-content, align-items
- align-content, flex-grow, flex-shrink, flex-basis

Grid:
- grid-template-columns, grid-template-rows, gap
- grid-column, grid-row

Animation & Transitions:
- transition, animation, transform
```

#### 2. CSS Selectors

```
Element selectors: h1, p, div, span, etc.
Class selectors: .class-name
ID selectors: #id-name
Pseudo-classes: :hover, :active, :focus, :first-child, :last-child
Pseudo-elements: ::before, ::after, ::first-line, ::first-letter
Attribute selectors: [attribute], [attribute="value"]
Combinators: space (descendant), > (child), + (adjacent), ~ (sibling)
```

#### 3. Property Values

Context-aware value suggestions based on property:

```kotlin
val propertyValues = mapOf(
    "display" to listOf("block", "inline", "inline-block", "flex", "grid", "none"),
    "position" to listOf("static", "relative", "absolute", "fixed", "sticky"),
    "font-weight" to listOf("normal", "bold", "lighter", "bolder", "100", "200", "300", "400", "500", "600", "700", "800", "900"),
    "font-style" to listOf("normal", "italic", "oblique"),
    "text-align" to listOf("left", "right", "center", "justify"),
    "text-decoration" to listOf("none", "underline", "overline", "line-through"),
    "overflow" to listOf("visible", "hidden", "scroll", "auto"),
    "cursor" to listOf("auto", "pointer", "default", "move", "text", "wait", "help"),
    // ... more mappings
)
```

#### 4. Font Family Suggestions

**Context**: When editing `font-family` property

**Sources**:
1. System fonts (platform-specific)
2. Embedded fonts (from `document.resources.fonts`)
3. Common web-safe fonts

```kotlin
class FontFamilySuggester(
    private val document: Document,
    private val systemFonts: List<String>
) {
    fun getSuggestions(): List<String> {
        val embeddedFonts = document.resources.fonts.map { it.family }
        val webSafeFonts = listOf(
            "Arial", "Helvetica", "Times New Roman", "Times",
            "Courier New", "Courier", "Verdana", "Georgia",
            "Palatino", "Garamond", "Comic Sans MS", "Trebuchet MS",
            "Arial Black", "Impact"
        )
        
        // Combine: embedded fonts first, then web-safe, then system
        return (embeddedFonts + webSafeFonts + systemFonts).distinct()
    }
    
    fun formatSuggestion(fontFamily: String): String {
        val isEmbedded = document.resources.fonts.any { it.family == fontFamily }
        return if (isEmbedded) {
            "$fontFamily (embedded)"
        } else {
            fontFamily
        }
    }
}
```

### Autocomplete UI

```kotlin
@Composable
fun CSSEditorWithAutocomplete(
    value: String,
    onValueChange: (String) -> Unit,
    document: Document,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var suggestions by remember { mutableStateOf<List<AutocompleteSuggestion>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val autocompleteEngine = remember { CSSAutocompleteEngine(document) }
    
    Box(modifier = modifier) {
        TextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onValueChange(newValue.text)
                
                // Get suggestions based on cursor position
                val cursorPos = newValue.selection.start
                suggestions = autocompleteEngine.getSuggestions(
                    text = newValue.text,
                    cursorPosition = cursorPos
                )
                showSuggestions = suggestions.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
        
        // Autocomplete dropdown
        if (showSuggestions) {
            AutocompleteDropdown(
                suggestions = suggestions,
                onSuggestionSelected = { suggestion ->
                    val newValue = autocompleteEngine.applySuggestion(
                        text = textFieldValue.text,
                        cursorPosition = textFieldValue.selection.start,
                        suggestion = suggestion
                    )
                    textFieldValue = TextFieldValue(
                        text = newValue,
                        selection = TextRange(textFieldValue.selection.start + suggestion.insertText.length)
                    )
                    onValueChange(newValue)
                    showSuggestions = false
                },
                onDismiss = { showSuggestions = false },
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
fun AutocompleteDropdown(
    suggestions: List<AutocompleteSuggestion>,
    onSuggestionSelected: (AutocompleteSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .heightIn(max = 200.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        LazyColumn {
            items(suggestions) { suggestion ->
                ListItem(
                    headlineContent = { 
                        Text(
                            suggestion.displayText,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    supportingContent = suggestion.description?.let {
                        { Text(it, style = MaterialTheme.typography.bodySmall) }
                    },
                    trailingContent = suggestion.type?.let {
                        { 
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    modifier = Modifier.clickable { 
                        onSuggestionSelected(suggestion)
                    }
                )
            }
        }
    }
}
```

### Autocomplete Engine

```kotlin
data class AutocompleteSuggestion(
    val displayText: String,
    val insertText: String,
    val description: String? = null,
    val type: String? = null // "property", "value", "selector", "font"
)

class CSSAutocompleteEngine(private val document: Document) {
    
    fun getSuggestions(text: String, cursorPosition: Int): List<AutocompleteSuggestion> {
        val context = analyzeCursorContext(text, cursorPosition)
        
        return when (context.type) {
            ContextType.PROPERTY -> getPropertySuggestions(context.prefix)
            ContextType.VALUE -> getValueSuggestions(context.property, context.prefix)
            ContextType.SELECTOR -> getSelectorSuggestions(context.prefix)
            ContextType.FONT_FAMILY -> getFontFamilySuggestions(context.prefix)
            else -> emptyList()
        }
    }
    
    private fun analyzeCursorContext(text: String, cursorPosition: Int): CursorContext {
        // Parse CSS to determine context
        val textBeforeCursor = text.substring(0, cursorPosition)
        
        // Check if inside a declaration block
        val insideDeclaration = textBeforeCursor.count { it == '{' } > textBeforeCursor.count { it == '}' }
        
        if (!insideDeclaration) {
            // We're in selector context
            val prefix = extractPrefix(textBeforeCursor)
            return CursorContext(ContextType.SELECTOR, prefix = prefix)
        }
        
        // Inside declaration - determine if property or value
        val lastColon = textBeforeCursor.lastIndexOf(':')
        val lastSemicolon = textBeforeCursor.lastIndexOf(';')
        
        if (lastColon > lastSemicolon) {
            // We're in value context
            val propertyStart = textBeforeCursor.lastIndexOf('\n', lastColon)
                .let { if (it == -1) 0 else it + 1 }
            val property = textBeforeCursor.substring(propertyStart, lastColon).trim()
            val prefix = textBeforeCursor.substring(lastColon + 1).trim()
            
            return if (property == "font-family") {
                CursorContext(ContextType.FONT_FAMILY, property = property, prefix = prefix)
            } else {
                CursorContext(ContextType.VALUE, property = property, prefix = prefix)
            }
        } else {
            // We're in property context
            val prefix = extractPrefix(textBeforeCursor)
            return CursorContext(ContextType.PROPERTY, prefix = prefix)
        }
    }
    
    private fun extractPrefix(text: String): String {
        return text.takeLastWhile { it.isLetterOrDigit() || it == '-' }
    }
    
    private fun getPropertySuggestions(prefix: String): List<AutocompleteSuggestion> {
        return CSS_PROPERTIES
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { property ->
                AutocompleteSuggestion(
                    displayText = property,
                    insertText = "$property: ",
                    type = "property"
                )
            }
    }
    
    private fun getValueSuggestions(property: String, prefix: String): List<AutocompleteSuggestion> {
        val values = PROPERTY_VALUES[property] ?: emptyList()
        return values
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { value ->
                AutocompleteSuggestion(
                    displayText = value,
                    insertText = "$value;",
                    type = "value"
                )
            }
    }
    
    private fun getFontFamilySuggestions(prefix: String): List<AutocompleteSuggestion> {
        val embeddedFonts = document.resources.fonts.map { font ->
            AutocompleteSuggestion(
                displayText = font.family,
                insertText = "'${font.family}', ",
                description = "Embedded font",
                type = "font"
            )
        }
        
        val systemFonts = getSystemFonts().map { font ->
            AutocompleteSuggestion(
                displayText = font,
                insertText = "'$font', ",
                type = "font"
            )
        }
        
        return (embeddedFonts + systemFonts)
            .filter { it.displayText.startsWith(prefix, ignoreCase = true) }
    }
    
    fun applySuggestion(text: String, cursorPosition: Int, suggestion: AutocompleteSuggestion): String {
        val context = analyzeCursorContext(text, cursorPosition)
        val prefixLength = context.prefix.length
        
        return text.replaceRange(
            cursorPosition - prefixLength,
            cursorPosition,
            suggestion.insertText
        )
    }
    
    companion object {
        val CSS_PROPERTIES = listOf(
            "align-content", "align-items", "align-self",
            "animation", "animation-delay", "animation-direction",
            "background", "background-color", "background-image",
            "border", "border-radius", "border-color",
            "box-shadow", "box-sizing",
            "color", "cursor",
            "display",
            "flex", "flex-direction", "flex-wrap",
            "font-family", "font-size", "font-style", "font-weight",
            "gap", "grid-template-columns", "grid-template-rows",
            "height", "width", "max-width", "max-height", "min-width", "min-height",
            "justify-content",
            "letter-spacing", "line-height",
            "margin", "margin-top", "margin-bottom", "margin-left", "margin-right",
            "opacity", "overflow", "overflow-x", "overflow-y",
            "padding", "padding-top", "padding-bottom", "padding-left", "padding-right",
            "position",
            "text-align", "text-decoration", "text-transform",
            "transform", "transition",
            "vertical-align",
            "z-index"
        ).sorted()
        
        val PROPERTY_VALUES = mapOf(
            "display" to listOf("block", "inline", "inline-block", "flex", "grid", "none"),
            "position" to listOf("static", "relative", "absolute", "fixed", "sticky"),
            "font-weight" to listOf("normal", "bold", "lighter", "bolder", "100", "200", "300", "400", "500", "600", "700", "800", "900"),
            "font-style" to listOf("normal", "italic", "oblique"),
            "text-align" to listOf("left", "right", "center", "justify"),
            "overflow" to listOf("visible", "hidden", "scroll", "auto"),
            // Add more as needed
        )
    }
}

data class CursorContext(
    val type: ContextType,
    val property: String? = null,
    val prefix: String = ""
)

enum class ContextType {
    PROPERTY,
    VALUE,
    SELECTOR,
    FONT_FAMILY,
    UNKNOWN
}
```

## Font Management

### Font Addition UI

```kotlin
@Composable
fun FontManagementDialog(
    document: Document,
    onFontAdded: (FontResource) -> Unit,
    onDismiss: () -> Unit
) {
    var showLicenseWarning by remember { mutableStateOf(true) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Font") },
        text = {
            Column {
                if (showLicenseWarning) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Font Licensing Warning",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                "Only embed fonts that you have the legal right to distribute. " +
                                "Many fonts require a license for embedding in documents. " +
                                "Check the font's license before embedding.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showLicenseWarning = false }) {
                                    Text("I Understand")
                                }
                            }
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = { /* Open file picker */ },
                    enabled = !showLicenseWarning
                ) {
                    Text("Select Font File (.ttf, .otf, .woff, .woff2)")
                }
                
                selectedFile?.let { file ->
                    Text(
                        "Selected: ${file.name}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedFile?.let { file ->
                        val fontResource = processFontFile(file)
                        onFontAdded(fontResource)
                    }
                    onDismiss()
                },
                enabled = selectedFile != null && !showLicenseWarning
            ) {
                Text("Add Font")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun processFontFile(file: File): FontResource {
    val fontAnalyzer = FontAnalyzer()
    val metadata = fontAnalyzer.extractMetadata(file)
    
    return FontResource(
        id = UUID.randomUUID().toString(),
        name = metadata.familyName,
        family = metadata.familyName,
        path = "fonts/${file.name}",
        format = detectFontFormat(file.extension),
        weight = metadata.weight,
        style = metadata.style
    )
}
```

## Cloud Sync with External Resources

### Enhanced Sync Architecture

When syncing to cloud storage, the application must also upload all external resources and maintain proper file references.

### Sync Process with Resources

```
1. Identify all external resources in document
   ↓
2. For each resource (font, image):
   a. Check if already uploaded
   b. Upload if not exists or modified
   c. Track cloud URI
   ↓
3. Update document with cloud resource references
   ↓
4. Upload updated document JSON
   ↓
5. Update sync metadata
```

### Resource Tracking

```kotlin
data class ResourceSyncMetadata(
    val resourceId: String,
    val localPath: String,
    val cloudPath: String,
    val cloudUri: String,
    val lastSyncTime: Instant,
    val hash: String
)

interface ResourceSyncRepository {
    suspend fun getResourceMetadata(resourceId: String): ResourceSyncMetadata?
    suspend fun updateResourceMetadata(metadata: ResourceSyncMetadata)
    suspend fun getAllResourcesForDocument(documentId: String): List<ResourceSyncMetadata>
}
```

### Enhanced Sync Provider

```kotlin
interface ResourceAwareSyncProvider : SyncProvider {
    /**
     * Upload a resource file and return its cloud URI
     */
    suspend fun uploadResource(
        localPath: String,
        remotePath: String,
        resourceType: ResourceType
    ): Result<String> // Returns cloud URI
    
    /**
     * Download a resource file
     */
    suspend fun downloadResource(
        cloudUri: String,
        localPath: String
    ): Result<Unit>
    
    /**
     * Check if resource exists in cloud
     */
    suspend fun resourceExists(remotePath: String): Boolean
}

enum class ResourceType {
    FONT,
    IMAGE,
    ATTACHMENT
}
```

### Resource Sync Orchestrator

```kotlin
class ResourceSyncOrchestrator(
    private val syncProvider: ResourceAwareSyncProvider,
    private val resourceSyncRepo: ResourceSyncRepository,
    private val documentRepo: DocumentRepository
) {
    
    suspend fun syncDocumentWithResources(documentId: String): SyncResult {
        val document = documentRepo.getDocument(documentId) 
            ?: return SyncResult.Error(SyncError.DOCUMENT_NOT_FOUND)
        
        try {
            // 1. Sync all fonts
            val fontResults = syncFonts(document)
            
            // 2. Sync all images
            val imageResults = syncImages(document)
            
            // 3. Update document with cloud URIs
            val updatedDocument = updateDocumentWithCloudUris(
                document,
                fontResults + imageResults
            )
            
            // 4. Upload updated document
            val documentResult = syncDocument(updatedDocument)
            
            return documentResult
        } catch (e: Exception) {
            return SyncResult.Error(SyncError.SYNC_FAILED, e.message)
        }
    }
    
    private suspend fun syncFonts(document: Document): List<ResourceSyncResult> {
        return document.resources.fonts.map { font ->
            syncResource(
                resourceId = font.id,
                localPath = font.path,
                remotePath = "fonts/${font.id}/${File(font.path).name}",
                resourceType = ResourceType.FONT
            )
        }
    }
    
    private suspend fun syncImages(document: Document): List<ResourceSyncResult> {
        return document.resources.images.map { image ->
            syncResource(
                resourceId = image.id,
                localPath = image.path,
                remotePath = "images/${image.id}/${File(image.path).name}",
                resourceType = ResourceType.IMAGE
            )
        }
    }
    
    private suspend fun syncResource(
        resourceId: String,
        localPath: String,
        remotePath: String,
        resourceType: ResourceType
    ): ResourceSyncResult {
        // Check if already synced
        val existingMetadata = resourceSyncRepo.getResourceMetadata(resourceId)
        val localFile = File(localPath)
        val currentHash = calculateHash(localFile)
        
        if (existingMetadata != null && existingMetadata.hash == currentHash) {
            // Already synced, no changes
            return ResourceSyncResult.AlreadySynced(resourceId, existingMetadata.cloudUri)
        }
        
        // Upload resource
        val uploadResult = syncProvider.uploadResource(localPath, remotePath, resourceType)
        
        return when {
            uploadResult.isSuccess -> {
                val cloudUri = uploadResult.getOrThrow()
                
                // Save metadata
                resourceSyncRepo.updateResourceMetadata(
                    ResourceSyncMetadata(
                        resourceId = resourceId,
                        localPath = localPath,
                        cloudPath = remotePath,
                        cloudUri = cloudUri,
                        lastSyncTime = Clock.System.now(),
                        hash = currentHash
                    )
                )
                
                ResourceSyncResult.Uploaded(resourceId, cloudUri)
            }
            else -> {
                ResourceSyncResult.Failed(resourceId, uploadResult.exceptionOrNull()?.message)
            }
        }
    }
    
    private fun updateDocumentWithCloudUris(
        document: Document,
        syncResults: List<ResourceSyncResult>
    ): Document {
        val cloudUriMap = syncResults
            .filterIsInstance<ResourceSyncResult.Uploaded>()
            .associate { it.resourceId to it.cloudUri }
        
        // Update font paths to cloud URIs
        val updatedFonts = document.resources.fonts.map { font ->
            cloudUriMap[font.id]?.let { uri ->
                font.copy(path = uri)
            } ?: font
        }
        
        // Update image paths to cloud URIs
        val updatedImages = document.resources.images.map { image ->
            cloudUriMap[image.id]?.let { uri ->
                image.copy(path = uri)
            } ?: image
        }
        
        return document.copy(
            resources = document.resources.copy(
                fonts = updatedFonts,
                images = updatedImages
            )
        )
    }
}

sealed class ResourceSyncResult {
    data class Uploaded(val resourceId: String, val cloudUri: String) : ResourceSyncResult()
    data class AlreadySynced(val resourceId: String, val cloudUri: String) : ResourceSyncResult()
    data class Failed(val resourceId: String, val error: String?) : ResourceSyncResult()
}
```

### Google Drive Resource Upload Example

```kotlin
class GoogleDriveResourceProvider : ResourceAwareSyncProvider {
    
    override suspend fun uploadResource(
        localPath: String,
        remotePath: String,
        resourceType: ResourceType
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val localFile = File(localPath)
            val mimeType = getMimeTypeForResource(resourceType, localFile.extension)
            
            // Create folder structure if needed
            val folderPath = remotePath.substringBeforeLast('/')
            val folderId = createFolderStructure(folderPath)
            
            // Upload file
            val fileMetadata = File().apply {
                name = localFile.name
                parents = listOf(folderId)
                mimeType = mimeType
            }
            
            val mediaContent = FileContent(mimeType, localFile)
            
            val uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, webContentLink")
                .execute()
            
            // Return cloud URI (webContentLink or custom URI)
            Result.success(uploadedFile.webContentLink ?: uploadedFile.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getMimeTypeForResource(type: ResourceType, extension: String): String {
        return when (type) {
            ResourceType.FONT -> when (extension.lowercase()) {
                "ttf" -> "font/ttf"
                "otf" -> "font/otf"
                "woff" -> "font/woff"
                "woff2" -> "font/woff2"
                else -> "application/octet-stream"
            }
            ResourceType.IMAGE -> when (extension.lowercase()) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "svg" -> "image/svg+xml"
                "webp" -> "image/webp"
                else -> "application/octet-stream"
            }
            ResourceType.ATTACHMENT -> "application/octet-stream"
        }
    }
}
```

### Sync UI with Resource Progress

```kotlin
@Composable
fun SyncProgressDialog(
    syncState: SyncState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Syncing Document") },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = syncState.progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(syncState.currentOperation)
                
                if (syncState.resourcesTotal > 0) {
                    Text(
                        "Resources: ${syncState.resourcesSynced}/${syncState.resourcesTotal}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (syncState.isComplete) {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    )
}

data class SyncState(
    val progress: Float,
    val currentOperation: String,
    val resourcesTotal: Int = 0,
    val resourcesSynced: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null
)
```

## Implementation Checklist

CSS Editor:
- [ ] Implement autocomplete engine
- [ ] Add property suggestions
- [ ] Add value suggestions
- [ ] Add selector suggestions
- [ ] Implement font-family autocomplete with embedded fonts
- [ ] Design autocomplete dropdown UI
- [ ] Handle keyboard navigation in dropdown
- [ ] Test autocomplete performance

Font Management:
- [ ] Implement font file picker
- [ ] Show licensing warning dialog
- [ ] Extract font metadata
- [ ] Generate @font-face CSS
- [ ] Add fonts to document resources
- [ ] Test with various font formats

Cloud Sync with Resources:
- [ ] Implement ResourceSyncOrchestrator
- [ ] Add resource upload to all sync providers
- [ ] Track resource sync metadata
- [ ] Update document with cloud URIs
- [ ] Handle resource conflicts
- [ ] Show progress for resource uploads
- [ ] Test with large font/image files
- [ ] Handle network interruptions
