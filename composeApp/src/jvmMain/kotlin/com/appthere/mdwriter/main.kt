package com.appthere.mdwriter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.OutputStream
import java.io.PrintStream

fun main() {
    // Suppress IntelliJ clipboard metadata errors when pasting
    // These occur when pasting from IntelliJ IDEA and don't affect functionality
    suppressClipboardErrors()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "mdWriter",
        ) {
            App()
        }
    }
}

/**
 * Filters out IntelliJ IDEA clipboard-related ClassNotFoundException errors
 * that appear when pasting content copied from IntelliJ.
 * These errors are harmless but clutter the debug output.
 */
private fun suppressClipboardErrors() {
    val originalErr = System.err
    val filteredErr = object : PrintStream(originalErr) {
        private val suppressPatterns = listOf(
            "EditorCopyPasteHelperImpl",
            "CopyPasteOptionsTransferableData",
            "DataFlavor"
        )

        override fun println(x: String?) {
            if (x != null && shouldSuppress(x)) {
                // Suppress this error
                return
            }
            originalErr.println(x)
        }

        override fun print(s: String?) {
            if (s != null && shouldSuppress(s)) {
                // Suppress this error
                return
            }
            originalErr.print(s)
        }

        private fun shouldSuppress(message: String): Boolean {
            val isClipboardError = message.contains("ClassNotFoundException") &&
                    suppressPatterns.any { message.contains(it) }
            return isClipboardError
        }
    }

    System.setErr(filteredErr)
}