package com.example.reconix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── private tokens ─────────────────────────────────────────────────────────
private val EmeraldAccent = Color(0xFF00C896)
private val PureWhite     = Color(0xFFFFFFFF)
private val SilverText    = Color(0xFFB8C8E8)
private val MutedText     = Color(0xFF6B82B0)

/**
 * ═══════════════════════════════════════════════════════════════
 *  EmptyStateView — Premium empty-state placeholder
 *
 *  • Emerald-tinted icon circle with radial glow
 *  • Bold white title  |  Silver subtitle
 *  • Centered vertically + horizontally
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconTint: Color = EmeraldAccent
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .padding(32.dp)
        ) {
            // ── Icon with double-ring glow ─────────────────
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.06f))
                )
                // Inner icon circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Title ──────────────────────────────────────
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.2).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtitle ───────────────────────────────────
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MutedText,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}
