package com.example.reconix

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop has no distinct hardware back button for compose apps.
}
