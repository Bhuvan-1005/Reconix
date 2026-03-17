package com.example.reconix.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  BiometricAuthButton — Platform-agnostic composable
 *
 *  Renders a fingerprint / face-id icon button that triggers the
 *  device's native biometric prompt (fingerprint, Face ID, or the
 *  device lock screen if no biometric sensor is enrolled).
 *
 *  • Android  → androidx.biometric.BiometricPrompt
 *  • iOS      → LocalAuthentication.LAContext
 *  • JVM/JS/Wasm → renders nothing (biometric not supported)
 *
 *  Usage:
 *    BiometricAuthButton(onAuthenticated = { /* proceed */ })
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Composable
expect fun BiometricAuthButton(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF00C896)
)
