package com.example.reconix

import androidx.compose.runtime.Composable

/** JS stub — browser has no native back-gesture API in this context. */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on JS browser target
}
