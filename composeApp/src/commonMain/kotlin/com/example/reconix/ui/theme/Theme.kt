package com.example.reconix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * ═══════════════════════════════════════════════════════════════
 *  Reconix Theme — Futuristic Corporate Aesthetic
 *  Dark: Bloomberg Terminal  ·  Light: Clean Glassmorphism
 *
 *  NOTE: Transparent status bar is platform-specific.
 *  On Android, set it in MainActivity via:
 *    WindowCompat.setDecorFitsSystemWindows(window, false)
 *    window.statusBarColor = Color.TRANSPARENT
 * ═══════════════════════════════════════════════════════════════
 */

// ── Dark Color Scheme ───────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = PureWhite,
    primaryContainer = SlateBlue800,
    onPrimaryContainer = ElectricIndigoLight,

    secondary = NeonCyan,
    onSecondary = DeepSlateBlue,
    secondaryContainer = SlateBlue700,
    onSecondaryContainer = NeonCyanLight,

    tertiary = AmberWarning,
    onTertiary = DeepSlateBlue,
    tertiaryContainer = SlateBlue700,
    onTertiaryContainer = AmberWarningLight,

    error = VividRose,
    onError = PureWhite,
    errorContainer = VividRoseDark,
    onErrorContainer = VividRoseLight,

    background = DeepSlateBlue,
    onBackground = SoftGrey100,

    surface = SlateBlue800,
    onSurface = SoftGrey100,
    surfaceVariant = SlateBlue700,
    onSurfaceVariant = SoftGrey200,

    outline = SlateBlue600,
    outlineVariant = SlateBlue700
)

// ── Light Color Scheme ──────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = PureWhite,
    primaryContainer = SoftGrey100,
    onPrimaryContainer = ElectricIndigoDark,

    secondary = NeonCyan,
    onSecondary = PureWhite,
    secondaryContainer = SoftGrey100,
    onSecondaryContainer = NeonCyanDark,

    tertiary = AmberWarning,
    onTertiary = PureWhite,
    tertiaryContainer = SoftGrey100,
    onTertiaryContainer = AmberWarningDark,

    error = VividRose,
    onError = PureWhite,
    errorContainer = SoftGrey100,
    onErrorContainer = VividRoseDark,

    background = SoftGrey50,
    onBackground = DeepSlateBlue,

    surface = PureWhite,
    onSurface = DeepSlateBlue,
    surfaceVariant = SoftGrey100,
    onSurfaceVariant = SlateBlue600,

    outline = SoftGrey300,
    outlineVariant = SoftGrey200
)

// ── Theme Composable ────────────────────────────────────────

@Composable
fun ReconixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppMaterialShapes,
        content = content
    )
}

/** Convenience alias — use either name */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = ReconixTheme(darkTheme = darkTheme, content = content)
