package com.example.reconix

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Web requires history manipulation, for now no-op for simplicity.
}
