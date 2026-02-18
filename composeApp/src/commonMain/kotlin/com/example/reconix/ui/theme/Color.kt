package com.example.reconix.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ═══════════════════════════════════════════════════════════════
 *  Reconix Design System — "Premium Fintech" Color Palette
 *
 *  Philosophy: Deep Navy Foundation · Emerald Signal · Crimson Alert
 *  Inspired by Bloomberg Terminal + Goldman Sachs digital aesthetic
 *
 *  8dp Grid System Token Reference:
 *    XS  =  4dp   (micro gap)
 *    S   =  8dp   (base unit)
 *    M   = 16dp   (1× block)
 *    L   = 24dp   (3× block)
 *    XL  = 32dp   (4× block)
 *    XXL = 48dp   (6× block)
 * ═══════════════════════════════════════════════════════════════
 */

// ── Primary Brand — Deep Royal Navy ──────────────────────────
// #0D1B3E — The authoritative navy used in top-tier banking apps
val NavyPrimary     = Color(0xFF0D1B3E)   // Primary brand navy
val NavyMedium      = Color(0xFF122250)   // Slightly lighter surface
val NavyLight       = Color(0xFF1A3066)   // Card surfaces / elevated layers
val NavyAccent      = Color(0xFF1E3A8A)   // Interactive states

// ── Primary Action — Electric Indigo (kept for compat) ───────
val ElectricIndigo      = Color(0xFF4F7FFF)   // Updated → vivid royal blue
val ElectricIndigoLight = Color(0xFF7EA9FF)
val ElectricIndigoDark  = Color(0xFF2B5FCC)

// ── Success Signal — Emerald Green (#00C896) ─────────────────
// Crisp, vibrant — "MATCHED" status, approve buttons
val EmeraldMatch     = Color(0xFF00C896)   // ★ Primary match color
val EmeraldMatchDark = Color(0xFF00A07A)
val EmeraldMatchBg   = Color(0xFF00C896).copy(alpha = 0.12f)

// ── Error Signal — Crimson Red (#FF2D5B) ─────────────────────
// Bold, unmistakable — "MISMATCH" status, reject buttons
val CrimsonMismatch     = Color(0xFFFF2D5B)   // ★ Primary mismatch color
val CrimsonMismatchDark = Color(0xFFCC1A43)
val CrimsonMismatchBg   = Color(0xFFFF2D5B).copy(alpha = 0.12f)

// ── Accent — Electra Gold (#FFB800) ─────────────────────────
// Premium accent for pending / in-review states
val GoldPending     = Color(0xFFFFB800)
val GoldPendingDark = Color(0xFFCC9200)
val GoldPendingBg   = Color(0xFFFFB800).copy(alpha = 0.12f)

// ── Secondary Accent — Neon Cyan (Data / Highlights) ─────────
val NeonCyan     = Color(0xFF00D9FF)
val NeonCyanLight = Color(0xFF40E8FF)
val NeonCyanDark  = Color(0xFF00AACF)

// ── Background Stack — True Dark Navy Layers ─────────────────
// Base → Surface → Elevated — perfect 8dp elevation steps
val DeepSlateBlue  = Color(0xFF060E20)   // Screen background (darkest)
val SlateBlue800   = Color(0xFF0D1B3E)   // Surface (cards)
val SlateBlue700   = Color(0xFF122250)   // Surface variant
val SlateBlue600   = Color(0xFF1A3066)   // Outlined borders
val SlateBlue500   = Color(0xFF2D4A8A)   // Disabled / muted icons

// ── Text Hierarchy ───────────────────────────────────────────
val PureWhite    = Color(0xFFFFFFFF)   // Primary labels
val SilverText   = Color(0xFFB8C8E8)   // Secondary labels
val MutedText    = Color(0xFF6B82B0)   // Tertiary / hints
val CoolGray     = Color(0xFF94A3B8)   // Legacy compat

// ── Light Mode Surface Stack ─────────────────────────────────
val SoftGrey50  = Color(0xFFF0F4FF)   // Screen bg (light)
val SoftGrey100 = Color(0xFFE4ECFF)   // Card bg (light)
val SoftGrey200 = Color(0xFFCCD8F5)   // Dividers / borders
val SoftGrey300 = Color(0xFFADBBE0)   // Disabled strokes

// ── Glassmorphism Layers ─────────────────────────────────────
val GlassWhite5          = Color(0x0DFFFFFF)   //  5% — gradient start
val GlassWhite1          = Color(0x03FFFFFF)   //  1% — gradient end
val GlassBorder          = Color(0x26FFFFFF)   // 15% — luminous border
val GlassMorphismOverlay = Color(0x1AFFFFFF)

// ── Premium Gradient Pairs ───────────────────────────────────
// Use: Brush.linearGradient(listOf(GradientNavyStart, GradientNavyEnd))
val GradientNavyStart  = Color(0xFF0D1B3E)
val GradientNavyEnd    = Color(0xFF060E20)
val GradientBlueStart  = Color(0xFF4F7FFF)
val GradientBlueEnd    = Color(0xFF1E3A8A)
val GradientStart      = Color(0xFF4F7FFF)   // compat alias
val GradientEnd        = Color(0xFF1E3A8A)   // compat alias

// ── Status Semantic Aliases (direct usage in components) ─────
val PendingYellow = GoldPending
val ApprovedGreen = EmeraldMatch
val RejectedRed   = CrimsonMismatch
val MatchedGreen  = EmeraldMatch        // ★ Used throughout 3-way match
val MismatchRed   = CrimsonMismatch     // ★ Used throughout 3-way match

// ── NeonMint aliases (backwards-compat) ──────────────────────
val NeonMint      = EmeraldMatch
val NeonMintLight = EmeraldMatch.copy(alpha = 0.8f)
val NeonMintDark  = EmeraldMatchDark

// ── VividRose aliases (backwards-compat) ─────────────────────
val VividRose      = CrimsonMismatch
val VividRoseLight = Color(0xFFFF6B8A)
val VividRoseDark  = CrimsonMismatchDark

// ── AmberWarning aliases (backwards-compat) ──────────────────
val AmberWarning      = GoldPending
val AmberWarningLight = Color(0xFFFFCC33)
val AmberWarningDark  = GoldPendingDark

// ── Shadow ───────────────────────────────────────────────────
val CardShadowColor = Color(0x33000000)   // 20% black

// ── Chart Palette ─────────────────────────────────────────────
val ChartBlue   = Color(0xFF4F7FFF)
val ChartPurple = Color(0xFF8B5CF6)
val ChartPink   = Color(0xFFFF6B9D)
val ChartTeal   = Color(0xFF00D9B8)
val ChartOrange = Color(0xFFFF8C38)
