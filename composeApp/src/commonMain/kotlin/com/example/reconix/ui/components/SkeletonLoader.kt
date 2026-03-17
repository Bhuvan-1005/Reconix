package com.example.reconix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.reconix.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  Shared shimmer brush — sweeping left→right highlight (theme-aware)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun shimmerBrush(): Brush {
    val isDark = LocalIsDarkTheme.current
    val base = if (isDark) Color(0xFF1A1A1A) else Color(0xFFDDE5F5)
    val shimmer = if (isDark) Color(0xFF2A2A2A) else Color(0xFFC5D2EC)

    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue  =  600f,
        animationSpec = infiniteRepeatable(
            animation   = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(base, shimmer, base),
        start = Offset(translateX, 0f),
        end   = Offset(translateX + 400f, 200f)
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  SkeletonBox — rectangular shimmer element
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush())
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  SkeletonCircle — circular shimmer element
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush())
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  SkeletonCard — single row card placeholder (icon + two text lines + badge)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Color(0xFF222222), Color(0xFF1A1A1A))
                    else listOf(Color(0xFFE4ECFF), Color(0xFFF0F4FF))
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Avatar circle ──────────────────────────────────
            SkeletonCircle(size = 44.dp)

            // ── Text block ────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(14.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(10.dp))
            }

            // ── Badge placeholder ─────────────────────────────
            SkeletonBox(modifier = Modifier.width(56.dp).height(24.dp), cornerRadius = 12.dp)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SkeletonWideCard — a taller detail card for match / invoice screens
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SkeletonWideCard(modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Color(0xFF222222), Color(0xFF1A1A1A))
                    else listOf(Color(0xFFE4ECFF), Color(0xFFF0F4FF))
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.width(100.dp).height(12.dp))
                SkeletonBox(modifier = Modifier.width(60.dp).height(20.dp), cornerRadius = 10.dp)
            }
            // Title
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            // Body lines
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.9f).height(11.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.65f).height(11.dp))
            Spacer(modifier = Modifier.height(4.dp))
            // Progress bar
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(5.dp), cornerRadius = 3.dp)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SkeletonListLoader  — stacked list of SkeletonCards
//  Used in FinanceDashboard, VendorDashboard, InvoiceList, etc.
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SkeletonListLoader(
    itemCount: Int = 4,
    modifier:  Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) { index ->
            SkeletonCard(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SkeletonDetailLoader  — used in ThreeWayMatch / review screens
//  Shows 1 wide header card + N detail rows
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SkeletonDetailLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Big summary card
        SkeletonWideCard()
        // Three column-spanning bars
        SkeletonWideCard()
        // Regular cards
        SkeletonCard()
        SkeletonCard()
        SkeletonCard()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  FullScreenSkeletonLoader — centred on a dark background (use instead of
//  CircularProgressIndicator in screens that fill the whole viewport)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun FullScreenSkeletonLoader(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Color.Black, Color(0xFF0A0A0A), Color.Black)
                    else listOf(Color(0xFFF0F4FF), Color(0xFFE4ECFF), Color(0xFFF0F4FF))
                )
            )
    ) {
        // Ambient glow (top)
        val glowAlpha by rememberInfiniteTransition(label = "loaderGlow").animateFloat(
            initialValue = 0.05f,
            targetValue  = 0.14f,
            animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
            label = "glow"
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF4F7FFF).copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Skeleton stat bar at the top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shimmerBrush())
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shimmerBrush())
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // List cards
            repeat(itemCount) {
                SkeletonCard()
            }
        }

        // Bottom pulsing dot indicator (matches SplashScreen's style)
        val ringScale by rememberInfiniteTransition(label = "loadRing").animateFloat(
            initialValue = 1f,
            targetValue  = 1.9f,
            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "ring"
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple ring
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(ringScale)
                    .alpha((2f - ringScale) * 0.35f)
                    .clip(CircleShape)
                    .background(Color(0xFF00C896))
            )
            // Core dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C896))
            )
        }
    }
}
