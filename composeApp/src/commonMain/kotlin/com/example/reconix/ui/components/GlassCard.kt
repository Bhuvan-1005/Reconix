package com.example.reconix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.reconix.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════
 *  GlassCard — Theme-Aware Glassmorphism Card
 *
 *  Dark:  Deep navy vertical gradient surface + blue-tinted border
 *  Light: Soft white gradient surface + grey-tinted border
 *  • Configurable padding and corner radius
 *  • Optional accent top-line color
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    innerPadding: Dp = 20.dp,
    accentColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val cardShape = RoundedCornerShape(cornerRadius)

    // ── Surface gradient ────────────────────────────────────
    val glassBrush = if (isDark) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF1E1E1E),
                0.50f to Color(0xFF141414),
                1.00f to Color(0xFF080808)
            )
        )
    } else {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFFFFFFF),
                0.50f to Color(0xFFF8F8F8),
                1.00f to Color(0xFFF0F0F0)
            )
        )
    }

    // ── Border gradient ─────────────────────────────────────
    val defaultAccent = if (isDark) ElectricIndigo else ElectricIndigoDark
    val borderBrush = if (isDark) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to (accentColor ?: defaultAccent).copy(alpha = 0.45f),
                0.35f to Color.White.copy(alpha = 0.08f),
                1.00f to Color.Transparent
            )
        )
    } else {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to (accentColor ?: defaultAccent).copy(alpha = 0.25f),
                0.35f to Color.Black.copy(alpha = 0.06f),
                1.00f to Color.Transparent
            )
        )
    }

    val shadowColor = if (isDark) ElectricIndigo.copy(alpha = 0.12f) else Color(0x1A000000)

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = cardShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(cardShape)
            .background(glassBrush)
            .border(
                border = BorderStroke(width = 1.dp, brush = borderBrush),
                shape = cardShape
            )
            .padding(innerPadding),
        content = content
    )
}

/**
 * EmeraldGlassCard — GlassCard with emerald luminous top edge (for MATCHED / success)
 */
@Composable
fun EmeraldGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    innerPadding: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) = GlassCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    innerPadding = innerPadding,
    accentColor = Color(0xFF00C896),
    content = content
)

/**
 * CrimsonGlassCard — GlassCard with crimson luminous top edge (for MISMATCH / error)
 */
@Composable
fun CrimsonGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    innerPadding: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) = GlassCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    innerPadding = innerPadding,
    accentColor = Color(0xFFFF2D5B),
    content = content
)
