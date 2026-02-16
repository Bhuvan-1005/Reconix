package com.example.reconix

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Enable transparent status bar
        setupTransparentStatusBar()

        setContent {
            App()
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