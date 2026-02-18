package com.example.reconix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.reconix.ui.theme.*

// ─── private tokens ─────────────────────────────────────────────────────────
private val NavyCard     = Color(0xFF0D1B3E)
private val NavySurface  = Color(0xFF112048)
private val NavyBorder   = Color(0xFF1A3066)
private val RoyalBlue    = Color(0xFF4F7FFF)

/**
 * ═══════════════════════════════════════════════════════════════
 *  GlassCard — Premium dark-glassmorphism card
 *
 *  • Deep navy vertical gradient surface
 *  • Hairline Royal-Blue-tinted top border (luminous edge effect)
 *  • Soft indigo shadow glow
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
    val cardShape = RoundedCornerShape(cornerRadius)

    val glassBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to NavySurface,
            0.50f to NavyCard,
            1.00f to Color(0xFF0A1530)
        )
    )

    // Hairline top-border gradient: luminous → transparent
    val borderBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to (accentColor ?: RoyalBlue).copy(alpha = 0.45f),
            0.35f to NavyBorder.copy(alpha = 0.35f),
            1.00f to Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = cardShape,
                ambientColor = RoyalBlue.copy(alpha = 0.12f),
                spotColor   = RoyalBlue.copy(alpha = 0.18f)
            )
            .clip(cardShape)
            .background(glassBrush)
            .border(
                border = BorderStroke(width = 1.dp, brush = borderBrush),
                shape  = cardShape
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
