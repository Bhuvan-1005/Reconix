package com.example.reconix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.reconix.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════
 *  GlassCard — Frosted-glass container with glassmorphism effect
 *
 *  Features:
 *  • Vertical gradient: White @ 5% → White @ 1%
 *  • 1dp border stroke at 10% white opacity
 *  • 8dp soft shadow tinted with Electric Indigo
 *  • 24dp rounded corners (AppShapes.CardShape)
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            GlassWhite5,   // 5% white — top
            GlassWhite1    // 1% white — bottom
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = AppShapes.CardShape,
                ambientColor = ElectricIndigo.copy(alpha = 0.15f),
                spotColor = ElectricIndigo.copy(alpha = 0.25f)
            )
            .clip(AppShapes.CardShape)
            .background(glassBrush)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = GlassBorder
                ),
                shape = AppShapes.CardShape
            )
            .padding(20.dp),
        content = content
    )
}
