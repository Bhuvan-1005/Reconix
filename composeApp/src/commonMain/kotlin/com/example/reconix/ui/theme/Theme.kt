package com.example.reconix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

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
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF1A1A1A),
    onPrimaryContainer  = ElectricIndigoLight,

    // Secondary: Neon cyan for data highlights
    secondary             = ElectricIndigo,
    onSecondary           = Color.Black,
    secondaryContainer    = Color(0xFF1A1A1A),
    onSecondaryContainer  = Color(0xFFE0E0E0),

    // Tertiary: Gold for pending/warning states
    tertiary             = GoldPending,
    onTertiary           = Color.Black,
    tertiaryContainer    = Color(0xFF1A1A1A),
    onTertiaryContainer  = AmberWarningLight,

    // Error: Crimson red (#FF2D5B)
    error             = CrimsonMismatch,
    onError           = Color.White,
    errorContainer    = CrimsonMismatchDark,
    onErrorContainer  = VividRoseLight,

    // Background: True black
    background   = Color.Black,
    onBackground = Color.White,

    // Surface: Dark neutral (no blue)
    surface          = Color(0xFF121212),
    onSurface        = Color.White,
    surfaceVariant   = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFF9CA3AF),

    outline        = Color(0xFF333333),
    outlineVariant = Color(0xFF2A2A2A)
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
    darkTheme: Boolean = ThemeManager.isDarkMode,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppMaterialShapes,
            content = content
        )
    }
}

/**
 * Composition local that provides the current dark-theme flag.
 * Wrap your root composable with `CompositionLocalProvider(LocalIsDarkTheme provides isDark)`
 * or simply read it from AppShell / ReconixTheme wrapper.
 */
val LocalIsDarkTheme = compositionLocalOf { true }

/**
 * Simple singleton for programmatic theme overrides.
 * Read `ThemeManager.isDarkMode` anywhere; toggle it to re-theme.
 */
object ThemeManager {
    var isDarkMode: Boolean by mutableStateOf(true)
}

/** Convenience alias — use either name */
@Composable
fun AppTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode,
    content: @Composable () -> Unit
) = ReconixTheme(darkTheme = darkTheme, content = content)
