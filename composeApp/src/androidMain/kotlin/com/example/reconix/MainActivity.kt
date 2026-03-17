package com.example.reconix

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Enable transparent status bar
        setupTransparentStatusBar()

        setContent {
            // Pending callback: stored when the upload screen requests a file
            var pendingCallback by remember { mutableStateOf<((String, ByteArray) -> Unit)?>(null) }

            // Register the Android system file picker
            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: ByteArray(0)

                    // Step 1: query ContentResolver for the real display name
                    //         (e.g. "SUBAM_INVOICE.pdf" instead of "document:45045")
                    var fileName: String? = null
                    contentResolver.query(
                        uri,
                        arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                        null, null, null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            fileName = cursor.getString(0)
                        }
                    }

                    // Step 2: if display name is missing/blank, derive extension from MIME type
                    if (fileName.isNullOrBlank()) {
                        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                        val ext = when {
                            mimeType.contains("pdf")  -> ".pdf"
                            mimeType.contains("png")  -> ".png"
                            mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                            else -> ".pdf"   // safe default – server accepts PDF
                        }
                        fileName = "invoice_${System.currentTimeMillis()}$ext"
                    }

                    pendingCallback?.invoke(fileName!!, bytes)
                }
                pendingCallback = null
            }

            App(
                onRequestFilePick = { callback ->
                    pendingCallback = callback
                    filePickerLauncher.launch("*/*")
                }
            )
        }
    }

    private fun setupTransparentStatusBar() {
        // Make status bar transparent and draw content behind it
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // For older Android versions, ensure full screen layout
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // Make status bar icons dark on light background
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true // Dark icons for light background
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}