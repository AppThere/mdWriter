package com.appthere.mdwriter.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of FilePicker
 */
actual class FilePicker {
    private var activity: ComponentActivity? = null

    fun setActivity(activity: ComponentActivity) {
        this.activity = activity
    }

    actual suspend fun pickFileForImport(): String? {
        return suspendCancellableCoroutine { continuation ->
            val activity = this.activity ?: run {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val launcher = activity.activityResultRegistry.register(
                "file_picker_import",
                ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    // Convert URI to file path
                    val path = uri.toString()
                    continuation.resume(path)
                } else {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                launcher.unregister()
            }

            launcher.launch("*/*")
        }
    }

    actual suspend fun pickFileForExport(suggestedFileName: String): String? {
        return suspendCancellableCoroutine { continuation ->
            val activity = this.activity ?: run {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val launcher = activity.activityResultRegistry.register(
                "file_picker_export",
                ActivityResultContracts.CreateDocument("application/json")
            ) { uri: Uri? ->
                if (uri != null) {
                    val path = uri.toString()
                    continuation.resume(path)
                } else {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                launcher.unregister()
            }

            launcher.launch(suggestedFileName)
        }
    }

    actual suspend fun pickMultipleFiles(): List<String> {
        return suspendCancellableCoroutine { continuation ->
            val activity = this.activity ?: run {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            val launcher = activity.activityResultRegistry.register(
                "file_picker_multiple",
                ActivityResultContracts.GetMultipleContents()
            ) { uris: List<Uri> ->
                val paths = uris.map { it.toString() }
                continuation.resume(paths)
            }

            continuation.invokeOnCancellation {
                launcher.unregister()
            }

            launcher.launch("*/*")
        }
    }
}
