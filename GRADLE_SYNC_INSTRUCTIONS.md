# Gradle Sync Instructions

## The Issue
You're getting `ClassNotFoundException: kotlinx.datetime.Clock$System` because Gradle hasn't synced the new dependency yet.

## The Fix

The dependency `kotlinx-datetime` is already in `composeApp/build.gradle.kts` (line 58), but Gradle needs to sync it.

### Option 1: IntelliJ IDEA / Android Studio
1. Click on the "Gradle" tab (usually on the right side)
2. Click the "Reload All Gradle Projects" button (circular arrows icon)
3. Wait for sync to complete
4. Try running again

### Option 2: Command Line
```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew :composeApp:jvmRun
```

### Option 3: IntelliJ File Menu
1. File → Invalidate Caches / Restart
2. Select "Invalidate and Restart"
3. After IDE restarts, sync Gradle
4. Try running again

## Verification
After syncing, the dependency should be resolved and the app should start without the `ClassNotFoundException`.

## What Was Added
In `composeApp/build.gradle.kts`:
```kotlin
jvmMain.dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.kotlinx.datetime)  // ← This was added
}
```

## Expected Result
After syncing, running `./gradlew :composeApp:jvmRun` should:
- ✅ Start without ClassNotFoundException
- ✅ Show the editor window
- ✅ Display "Untitled Document" in the title bar
- ✅ Show the formatting toolbar
- ✅ Allow text editing
