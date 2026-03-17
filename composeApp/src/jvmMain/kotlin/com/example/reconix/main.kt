package com.example.reconix

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Reconix",
    ) {
        App(
            onRequestFilePick = { callback ->
                // Open Swing file chooser on the EDT without blocking the Compose thread
                SwingUtilities.invokeLater {
                    val chooser = JFileChooser()
                    chooser.dialogTitle = "Select Invoice File"
                    val result = chooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        val file: File = chooser.selectedFile
                        callback(file.name, file.readBytes())
                    }
                }
            }
        )
    }
}