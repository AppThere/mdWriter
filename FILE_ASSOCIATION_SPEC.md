# File Association & Landing Page Specification

## File Extension

**Extension**: `.mdoc` (Markdown Document)

**MIME Type**: `application/vnd.appthere.mdwriter+json`

**UTI (iOS)**: `com.appthere.mdwriter.document`

## Landing Page

### When to Show Landing Page

Display landing page when:
- App launched with no arguments
- App launched without a file to open
- User navigates "Home" from editor

### Landing Page Layout

```
┌─────────────────────────────────────────────────────┐
│  [☰ Menu]              MDWriter           [Settings]│
├─────────────────────────────────────────────────────┤
│                                                     │
│  Welcome back!                                      │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │  [+] Create New Document                     │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  Recent Documents                                   │
│  ┌──────────────────────────────────────────────┐ │
│  │ 📄 Project Proposal                          │ │
│  │    Edited 2 hours ago · 1,234 words          │ │
│  ├──────────────────────────────────────────────┤ │
│  │ 📄 Meeting Notes                             │ │
│  │    Edited yesterday · 567 words              │ │
│  ├──────────────────────────────────────────────┤ │
│  │ 📄 Technical Documentation                   │ │
│  │    Edited 3 days ago · 5,432 words           │ │
│  ├──────────────────────────────────────────────┤ │
│  │ 📄 Blog Post Draft                           │ │
│  │    Edited 1 week ago · 890 words             │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  [View All Documents]                               │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Responsive Layouts

**Compact (< 600dp)**:
- Single column
- Recent documents list
- Floating action button for new document

**Medium (600-840dp)**:
- Two columns: Recent + Actions sidebar
- Grid view for recent documents

**Expanded (> 840dp)**:
- Three columns: Navigation, Recent, Preview
- Grid view with larger cards
- Quick preview pane

### Recent Documents Data

Track for each document:
```kotlin
data class RecentDocument(
    val id: String,
    val title: String,
    val path: String,
    val lastOpened: Instant,
    val lastModified: Instant,
    val wordCount: Int,
    val preview: String? = null // First 100 characters
)
```

**Storage**: Store in preferences/database, limit to 20 most recent

**Sorting**: By `lastOpened` descending (most recent first)

## Platform-Specific File Association

### Android Implementation

#### 1. AndroidManifest.xml Configuration

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.appthere.mdwriter">
    
    <application>
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            
            <!-- Default launcher intent (landing page) -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- File association for .mdoc files -->
            <intent-filter android:label="@string/app_name">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                
                <!-- File scheme -->
                <data android:scheme="file" />
                <data android:mimeType="application/vnd.appthere.mdwriter+json" />
                <data android:pathPattern=".*\\.mdoc" />
                <data android:host="*" />
            </intent-filter>
            
            <!-- Content provider scheme (for shared files) -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                
                <data android:scheme="content" />
                <data android:mimeType="application/vnd.appthere.mdwriter+json" />
            </intent-filter>
            
            <!-- Open from other apps -->
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/vnd.appthere.mdwriter+json" />
            </intent-filter>
        </activity>
        
        <!-- File provider for sharing -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.appthere.mdwriter.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
```

#### 2. MainActivity Intent Handling

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle intent when app is launched
        val fileToOpen = handleIntent(intent)
        
        setContent {
            MDWriterApp(initialFile = fileToOpen)
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            // Handle intent when app is already running
            val fileToOpen = handleIntent(it)
            fileToOpen?.let { file ->
                // Navigate to editor with file
                navigateToEditor(file)
            }
        }
    }
    
    private fun handleIntent(intent: Intent): String? {
        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                // User opened a .mdoc file
                intent.data?.let { uri ->
                    copyFileToInternalStorage(uri)
                }
            }
            Intent.ACTION_SEND -> {
                // File shared from another app
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    copyFileToInternalStorage(uri)
                }
            }
            else -> null // Show landing page
        }
    }
    
    private fun copyFileToInternalStorage(uri: Uri): String {
        // Copy file from URI to app's internal storage
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = getFileName(uri)
        val destFile = File(filesDir, fileName)
        
        inputStream?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return destFile.absolutePath
    }
    
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        return result ?: uri.lastPathSegment ?: "document.mdoc"
    }
}
```

#### 3. File Paths Configuration

`res/xml/file_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="documents" path="documents/" />
    <cache-path name="cache" path="cache/" />
    <external-files-path name="external_files" path="." />
</paths>
```

### iOS Implementation

#### 1. Info.plist Configuration

```xml
<key>CFBundleDocumentTypes</key>
<array>
    <dict>
        <key>CFBundleTypeName</key>
        <string>MDWriter Document</string>
        
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        
        <key>LSHandlerRank</key>
        <string>Owner</string>
        
        <key>LSItemContentTypes</key>
        <array>
            <string>com.appthere.mdwriter.document</string>
        </array>
    </dict>
</array>

<key>UTExportedTypeDeclarations</key>
<array>
    <dict>
        <key>UTTypeIdentifier</key>
        <string>com.appthere.mdwriter.document</string>
        
        <key>UTTypeConformsTo</key>
        <array>
            <string>public.data</string>
            <string>public.content</string>
        </array>
        
        <key>UTTypeDescription</key>
        <string>MDWriter Document</string>
        
        <key>UTTypeIconFiles</key>
        <array/>
        
        <key>UTTypeTagSpecification</key>
        <dict>
            <key>public.filename-extension</key>
            <array>
                <string>mdoc</string>
            </array>
            <key>public.mime-type</key>
            <array>
                <string>application/vnd.appthere.mdwriter+json</string>
            </array>
        </dict>
    </dict>
</array>

<!-- Allow opening files from other apps -->
<key>CFBundleAllowMixedLocalizations</key>
<true/>

<key>LSSupportsOpeningDocumentsInPlace</key>
<true/>

<key>UIFileSharingEnabled</key>
<true/>

<key>UISupportsDocumentBrowser</key>
<true/>
```

#### 2. SceneDelegate Handling

```swift
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    
    var window: UIWindow?
    
    func scene(_ scene: UIScene, 
               willConnectTo session: UISceneSession, 
               options connectionOptions: UIScene.ConnectionOptions) {
        
        guard let windowScene = (scene as? UIWindowScene) else { return }
        
        // Check for file URL in connection options
        if let urlContext = connectionOptions.urlContexts.first {
            handleIncomingURL(urlContext.url)
        } else {
            // Show landing page
            showLandingPage()
        }
    }
    
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        // Handle file opened while app is running
        if let urlContext = URLContexts.first {
            handleIncomingURL(urlContext.url)
        }
    }
    
    private func handleIncomingURL(_ url: URL) {
        // Copy file to app container if needed
        let fileManager = FileManager.default
        let documentsPath = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let destURL = documentsPath.appendingPathComponent(url.lastPathComponent)
        
        // Start accessing security-scoped resource
        let shouldStopAccessing = url.startAccessingSecurityScopedResource()
        
        defer {
            if shouldStopAccessing {
                url.stopAccessingSecurityScopedResource()
            }
        }
        
        do {
            if fileManager.fileExists(atPath: destURL.path) {
                try fileManager.removeItem(at: destURL)
            }
            try fileManager.copyItem(at: url, to: destURL)
            
            // Navigate to editor with file
            navigateToEditor(filePath: destURL.path)
        } catch {
            print("Error copying file: \(error)")
        }
    }
}
```

#### 3. Kotlin Multiplatform iOS Integration

```kotlin
// iosMain/kotlin/com/appthere/mdwriter/IOSAppDelegate.kt
@OptIn(ExperimentalForeignApi::class)
class IOSAppDelegate : UIResponder, UIApplicationDelegateProtocol {
    
    companion object : UIApplicationDelegateProtocolMeta
    
    @OverrideInit
    constructor() : super()
    
    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) { _window = window }
    
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?
    ): Boolean {
        return true
    }
}

// Expect/actual for file opening
expect class FileOpener {
    fun openFile(path: String)
}

actual class FileOpener {
    actual fun openFile(path: String) {
        // Notify Compose UI to navigate to editor
        navigateToEditor(path)
    }
}
```

### Desktop Implementation (JVM)

#### 1. File Association (Platform-Specific)

**Windows**: Create registry entries or use installer

**macOS**: Configure in `Info.plist` of app bundle

**Linux**: Create `.desktop` file

#### 2. Command Line Arguments

```kotlin
// desktopMain/kotlin/com/appthere/mdwriter/Main.kt
fun main(args: Array<String>) = application {
    val fileToOpen = if (args.isNotEmpty()) {
        File(args[0]).takeIf { it.exists() && it.extension == "mdoc" }?.absolutePath
    } else {
        null
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "MDWriter"
    ) {
        MDWriterApp(initialFile = fileToOpen)
    }
}
```

#### 3. Drag & Drop Support

```kotlin
@Composable
fun MDWriterApp(initialFile: String? = null) {
    var currentFile by remember { mutableStateOf(initialFile) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onExternalDrag(
                onDragStart = { state ->
                    state is DragData.FilesList
                },
                onDrag = { },
                onDrop = { state ->
                    val files = (state as? DragData.FilesList)?.readFiles() ?: return@onExternalDrag
                    val mdocFile = files.firstOrNull { it.endsWith(".mdoc") }
                    mdocFile?.let { currentFile = it }
                }
            )
    ) {
        if (currentFile != null) {
            EditorScreen(filePath = currentFile)
        } else {
            LandingPage(
                onFileSelected = { currentFile = it }
            )
        }
    }
}
```

## App Navigation State

```kotlin
sealed class AppDestination {
    object Landing : AppDestination()
    data class Editor(val filePath: String) : AppDestination()
    object DocumentList : AppDestination()
    object Settings : AppDestination()
}

class AppNavigator {
    private val _destination = MutableStateFlow<AppDestination>(AppDestination.Landing)
    val destination: StateFlow<AppDestination> = _destination.asStateFlow()
    
    fun navigateTo(destination: AppDestination) {
        _destination.value = destination
    }
    
    fun openFile(path: String) {
        _destination.value = AppDestination.Editor(path)
        addToRecent(path)
    }
    
    fun navigateHome() {
        _destination.value = AppDestination.Landing
    }
    
    private fun addToRecent(path: String) {
        // Add to recent documents list
    }
}
```

## Recent Documents Management

```kotlin
interface RecentDocumentsRepository {
    suspend fun getRecentDocuments(limit: Int = 20): List<RecentDocument>
    suspend fun addRecentDocument(document: RecentDocument)
    suspend fun removeRecentDocument(id: String)
    suspend fun clearRecentDocuments()
}

class RecentDocumentsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : RecentDocumentsRepository {
    
    override suspend fun getRecentDocuments(limit: Int): List<RecentDocument> {
        return dataStore.data.map { prefs ->
            val json = prefs[RECENT_DOCS_KEY] ?: "[]"
            Json.decodeFromString<List<RecentDocument>>(json)
                .sortedByDescending { it.lastOpened }
                .take(limit)
        }.first()
    }
    
    override suspend fun addRecentDocument(document: RecentDocument) {
        dataStore.edit { prefs ->
            val current = Json.decodeFromString<List<RecentDocument>>(
                prefs[RECENT_DOCS_KEY] ?: "[]"
            )
            
            // Remove if already exists, then add to front
            val updated = (listOf(document) + current.filter { it.id != document.id })
                .take(20)
            
            prefs[RECENT_DOCS_KEY] = Json.encodeToString(updated)
        }
    }
    
    companion object {
        private val RECENT_DOCS_KEY = stringPreferencesKey("recent_documents")
    }
}
```

## Landing Page Implementation

```kotlin
@Composable
fun LandingPage(
    onFileSelected: (String) -> Unit,
    onCreateNew: () -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: LandingViewModel = viewModel()
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val windowSizeClass = calculateWindowSizeClass()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MDWriter") },
                navigationIcon = {
                    IconButton(onClick = { /* Open drawer */ }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = onViewAll) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (windowSizeClass == WindowSizeClass.COMPACT) {
                FloatingActionButton(onClick = onCreateNew) {
                    Icon(Icons.Default.Add, "Create new document")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Welcome message
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Create new button (if not compact)
            if (windowSizeClass != WindowSizeClass.COMPACT) {
                OutlinedButton(
                    onClick = onCreateNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create New Document")
                }
            }
            
            // Recent documents section
            Text(
                text = "Recent Documents",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            when {
                recentDocs.isEmpty() -> {
                    EmptyRecentState(
                        onCreateNew = onCreateNew,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    RecentDocumentsList(
                        documents = recentDocs,
                        onDocumentClick = onFileSelected,
                        windowSizeClass = windowSizeClass,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // View all button
            TextButton(
                onClick = onViewAll,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            ) {
                Text("View All Documents")
            }
        }
    }
}

@Composable
fun RecentDocumentCard(
    document: RecentDocument,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = buildString {
                        append(formatRelativeTime(document.lastOpened))
                        append(" · ")
                        append("${document.wordCount} words")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                document.preview?.let { preview ->
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

## Testing File Association

### Android Testing
1. Create test `.mdoc` file
2. Use `adb push` to transfer to device
3. Open file manager and tap `.mdoc` file
4. Verify app opens with file loaded

### iOS Testing
1. Create test `.mdoc` file
2. AirDrop to device or use Files app
3. Tap file in Files app
4. Verify app opens with file loaded

### Desktop Testing
1. Create test `.mdoc` file
2. Double-click file
3. Verify app launches and opens file

## File Association Icon

Create custom document icon for `.mdoc` files:
- **Android**: `res/mipmap/ic_document.xml`
- **iOS**: Add to asset catalog
- **Desktop**: Platform-specific icon files

## Error Handling

Handle edge cases:
- File doesn't exist
- File is corrupted
- Permission denied
- File is too large
- Invalid JSON format
- Network file (cloud storage)

Show appropriate error messages and fallback to landing page.
