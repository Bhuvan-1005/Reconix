package com.example.reconix.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.ui.theme.*
import kotlin.math.roundToInt

// ─── private tokens ─────────────────────────────────────────────────────────
private val NavyCard     = Color(0xFF0D1B3E)
private val NavySurface  = Color(0xFF112048)
private val PureWhite    = Color(0xFFFFFFFF)
private val SilverText   = Color(0xFFB8C8E8)
private val CardShape    = RoundedCornerShape(20.dp)

/**
 * ═══════════════════════════════════════════════════════════════
 *  StatCard — Premium KPI metric widget
 *
 *  • Dark navy glass surface with accent-tinted shadow
 *  • Accent-colored icon in a pill circle
 *  • Monospace count-up animation (0 → target, 1200 ms)
 *  • Left accent bar (3dp wide) matching the icon color
 *  • Fully 8dp-grid aligned: 16dp inner padding, 8dp spacers
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun StatCard(
    icon: ImageVector,
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    accentColor: Color = Color(0xFF4F7FFF)   // Royal Blue default
) {
    // Count-up animation (0 → value in 1200 ms, ease-out)
    val animatedValue = remember { Animatable(0f) }
    LaunchedEffect(value) {
        animatedValue.snapTo(0f)
        animatedValue.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val glassBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to NavySurface,
            1.00f to NavyCard
        )
    )

    Box(
        modifier = modifier
            .clip(CardShape)
            .background(glassBrush)
    ) {
        // ── Left accent bar ────────────────────────────────
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0.0f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // ── Icon circle ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Animated KPI number ────────────────────────
            Text(
                text = "$prefix${animatedValue.value.roundToInt()}$suffix",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = PureWhite,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Label ──────────────────────────────────────
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SilverText,
                letterSpacing = 0.5.sp
            )
        }
    }
}
