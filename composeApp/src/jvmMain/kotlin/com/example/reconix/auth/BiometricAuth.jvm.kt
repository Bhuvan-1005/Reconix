package com.example.reconix.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** JVM (Desktop) stub — biometric hardware not accessible from the JVM desktop target. */
@Composable
actual fun BiometricAuthButton(
    onAuthenticated: () -> Unit,
    modifier: Modifier,
    tint: Color
) {
    // No biometric support on JVM Desktop; nothing rendered.
}
