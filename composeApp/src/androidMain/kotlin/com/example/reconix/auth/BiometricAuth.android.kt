package com.example.reconix.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Android biometric actual — uses androidx.biometric.BiometricPrompt.
 *
 * The button only renders when the device has a usable biometric sensor
 * (fingerprint, face, iris) OR device credential (PIN/pattern/password).
 * On devices without any of these the composable emits nothing.
 */
@Composable
actual fun BiometricAuthButton(
    onAuthenticated: () -> Unit,
    modifier: Modifier,
    tint: Color
) {
    val context  = LocalContext.current
    val activity = context as? FragmentActivity ?: return

    // ── Check availability ────────────────────────────────────
    val biometricManager = remember { BiometricManager.from(context) }
    val allowedAuthenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.BIOMETRIC_WEAK  or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val canAuthenticate = remember {
        biometricManager.canAuthenticate(allowedAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    if (!canAuthenticate) return

    // ── Build the prompt once ─────────────────────────────────
    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)
    val executor = remember { ContextCompat.getMainExecutor(context) }

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                currentOnAuthenticated()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // User cancelled or sensor unavailable; no action
            }
            override fun onAuthenticationFailed() {
                // Biometric not recognized; prompt stays open
            }
        }
    }

    val biometricPrompt = remember(activity) {
        BiometricPrompt(activity, executor, callback)
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sign In to Invoice Validator")
            .setSubtitle("Use your fingerprint, face, or device lock to continue")
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()
    }

    // ── Button UI ─────────────────────────────────────────────
    IconButton(
        onClick = { biometricPrompt.authenticate(promptInfo) },
        modifier = modifier
    ) {
        Icon(
            imageVector      = Icons.Default.Fingerprint,
            contentDescription = "Sign in with biometrics",
            tint             = tint
        )
    }
}
