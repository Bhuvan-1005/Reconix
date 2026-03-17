package com.example.reconix.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** WasmJS (Browser/Wasm) stub — WebAuthn is not implemented in this target. */
@Composable
actual fun BiometricAuthButton(
    onAuthenticated: () -> Unit,
    modifier: Modifier,
    tint: Color
) {
    // No biometric in the WasmJS browser target; nothing rendered.
}
