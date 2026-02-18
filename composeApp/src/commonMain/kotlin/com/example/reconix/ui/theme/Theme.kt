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

// ── Dark Color Scheme — Deep Navy Fintech ──────────────────
private val DarkColorScheme = darkColorScheme(
    // Primary brand: vivid royal blue (#4F7FFF)
    primary             = ElectricIndigo,
    onPrimary           = PureWhite,
    primaryContainer    = NavyLight,          // Elevated card surface
    onPrimaryContainer  = ElectricIndigoLight,

    // Secondary: Neon cyan for data highlights
    secondary             = NeonCyan,
    onSecondary           = DeepSlateBlue,
    secondaryContainer    = SlateBlue700,
    onSecondaryContainer  = NeonCyanLight,

    // Tertiary: Gold for pending/warning states
    tertiary             = GoldPending,
    onTertiary           = DeepSlateBlue,
    tertiaryContainer    = SlateBlue700,
    onTertiaryContainer  = AmberWarningLight,

    // Error: Crimson red (#FF2D5B)
    error             = CrimsonMismatch,
    onError           = PureWhite,
    errorContainer    = CrimsonMismatchDark,
    onErrorContainer  = VividRoseLight,

    // Background: True deep navy (#060E20)
    background   = DeepSlateBlue,
    onBackground = PureWhite,

    // Surface: Navy card (#0D1B3E)
    surface          = SlateBlue800,
    onSurface        = PureWhite,
    surfaceVariant   = SlateBlue700,
    onSurfaceVariant = SilverText,

    outline        = SlateBlue600,
    outlineVariant = SlateBlue700
)

// ── Light Color Scheme — Cloud White Fintech ───────────────
private val LightColorScheme = lightColorScheme(
    primary             = ElectricIndigoDark,
    onPrimary           = PureWhite,
    primaryContainer    = SoftGrey100,
    onPrimaryContainer  = NavyPrimary,

    secondary             = NeonCyanDark,
    onSecondary           = PureWhite,
    secondaryContainer    = SoftGrey100,
    onSecondaryContainer  = NeonCyanDark,

    tertiary             = GoldPendingDark,
    onTertiary           = PureWhite,
    tertiaryContainer    = SoftGrey100,
    onTertiaryContainer  = GoldPendingDark,

    error             = CrimsonMismatch,
    onError           = PureWhite,
    errorContainer    = SoftGrey100,
    onErrorContainer  = CrimsonMismatchDark,

    background   = SoftGrey50,
    onBackground = NavyPrimary,

    surface          = PureWhite,
    onSurface        = NavyPrimary,
    surfaceVariant   = SoftGrey100,
    onSurfaceVariant = SlateBlue600,

    outline        = SoftGrey300,
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
