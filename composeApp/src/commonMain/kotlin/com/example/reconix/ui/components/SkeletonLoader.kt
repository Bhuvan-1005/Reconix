package com.example.reconix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ─── private tokens ─────────────────────────────────────────────────────────
private val NavyBase      = Color(0xFF0D1B3E)
private val NavySurface   = Color(0xFF112048)
private val ShimmerLight  = Color(0xFF1A3066)
private val ShimmerPeak   = Color(0xFF253D73)

/**
 * ═══════════════════════════════════════════════════════════════
 *  SkeletonLoader — Navy shimmer block (any size/shape)
 *
 *  • Diagonal shimmer sweep using LinearGradient
 *  • Navy palette matching the fintech dark theme
 *  • No dependency on MaterialTheme colors
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            NavySurface,
            ShimmerLight,
            ShimmerPeak,
            ShimmerLight,
            NavySurface
        ),
        start = Offset(shimmerTranslate - 600f, shimmerTranslate - 600f),
        end   = Offset(shimmerTranslate,        shimmerTranslate)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * SkeletonCardLoader — Full skeleton for a single invoice card (220dp height)
 */
@Composable
fun SkeletonCardLoader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NavyBase)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header line row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLoader(modifier = Modifier.size(width = 120.dp, height = 20.dp))
                SkeletonLoader(modifier = Modifier.size(width = 72.dp, height = 20.dp), cornerRadius = 100.dp)
            }

            // Vendor avatar + lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonLoader(modifier = Modifier.size(36.dp), cornerRadius = 100.dp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonLoader(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                    SkeletonLoader(modifier = Modifier.size(width = 140.dp, height = 16.dp))
                }
            }

            // Divider
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth().height(1.dp),
                cornerRadius = 0.dp
            )

            // Amount
            SkeletonLoader(modifier = Modifier.size(width = 150.dp, height = 28.dp))

            Spacer(modifier = Modifier.weight(1f))

            // Button
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                cornerRadius = 12.dp
            )
        }
    }
}

/**
 * SkeletonListLoader — Stacked list of skeleton cards
 */
@Composable
fun SkeletonListLoader(
    itemCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            SkeletonCardLoader()
        }
    }
}
