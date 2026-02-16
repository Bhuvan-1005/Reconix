package com.example.reconix.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════
 *  Reconix Shape System — Generous rounding, premium feel
 * ═══════════════════════════════════════════════════════════════
 */

/** Semantic shape tokens for direct usage in components */
object AppShapes {
    /** Cards, dialogs, bottom sheets */
    val CardShape = RoundedCornerShape(24.dp)

    /** Buttons, FABs */
    val ButtonShape = RoundedCornerShape(12.dp)

    /** TextFields, search bars */
    val InputShape = RoundedCornerShape(16.dp)

    /** Pill / badge / chip */
    val PillShape = RoundedCornerShape(100.dp)
}

/** Material3 Shapes integration */
val AppMaterialShapes = Shapes(
    small = AppShapes.ButtonShape,
    medium = AppShapes.InputShape,
    large = AppShapes.CardShape,
    extraLarge = AppShapes.CardShape
)
