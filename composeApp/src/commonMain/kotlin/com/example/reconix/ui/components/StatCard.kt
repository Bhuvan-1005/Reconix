package com.example.reconix.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reconix.ui.theme.*
import kotlin.math.roundToInt

/**
 * ═══════════════════════════════════════════════════════════════
 *  StatCard — Dashboard metric widget with count-up animation
 *
 *  Features:
 *  • Icon in a tinted circle (top-left)
 *  • Animated number that counts from 0 → target
 *  • Gray label at bottom
 *  • Built on top of GlassCard for glassmorphism
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
    accentColor: Color = NeonCyan
) {
    // Count-up animation
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatedValue.snapTo(0f)
        animatedValue.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 1200)
        )
    }

    GlassCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Icon Circle (top-left) ──────────────────────
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Animated Number (center) ────────────────────
            Text(
                text = "$prefix${animatedValue.value.roundToInt()}$suffix",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Label (bottom) ──────────────────────────────
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = CoolGray
            )
        }
    }
}
