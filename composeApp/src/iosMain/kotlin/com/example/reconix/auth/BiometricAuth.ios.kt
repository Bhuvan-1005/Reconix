package com.example.reconix.auth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

/**
 * iOS biometric actual — uses LocalAuthentication.LAContext.
 *
 * LAPolicyDeviceOwnerAuthentication covers both biometric sensors
 * (Touch ID / Face ID) AND the device passcode as fallback — this
 * matches the device's default lock behaviour.
 *
 * The reply block is guaranteed to be called on the main thread
 * for `evaluatePolicy`, so no dispatch is required.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BiometricAuthButton(
    onAuthenticated: () -> Unit,
    modifier: Modifier,
    tint: Color
) {
    val laContext = remember { LAContext() }

    // Check whether the device supports any owner authentication
    val canEvaluate = remember {
        laContext.canEvaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthentication,
            error  = null
        )
    }

    if (!canEvaluate) return

    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)

    IconButton(
        onClick = {
            laContext.evaluatePolicy(
                policy          = LAPolicyDeviceOwnerAuthentication,
                localizedReason = "Sign in to Invoice Validator"
            ) { success, _ ->
                if (success) currentOnAuthenticated()
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector        = Icons.Default.Fingerprint,
            contentDescription = "Sign in with biometrics",
            tint               = tint
        )
    }
}
