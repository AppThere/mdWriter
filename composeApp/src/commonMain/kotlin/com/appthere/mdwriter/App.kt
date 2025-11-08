package com.appthere.mdwriter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.appthere.mdwriter.ui.screen.EditorScreen
import com.appthere.mdwriter.ui.theme.MdWriterTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MdWriterTheme {
        EditorScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}