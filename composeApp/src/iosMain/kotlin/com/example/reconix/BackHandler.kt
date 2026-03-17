package com.example.reconix

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS swipe-to-back is usually handled by UINavigationController, not explicit compose handlers 
    // unless using a complex library like Decompose. For simple states, this is a no-op.
}
