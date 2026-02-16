package com.example.reconix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Skeleton Loader - Modern shimmer effect for loading states
 * Replaces traditional circular progress indicators
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shimmerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    highlightColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            shimmerColor,
            highlightColor,
            shimmerColor
        ),
        start = Offset(shimmerTranslate - 500f, shimmerTranslate - 500f),
        end = Offset(shimmerTranslate, shimmerTranslate)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

/**
 * Skeleton Card Loader - For invoice cards
 */
@Composable
fun SkeletonCardLoader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLoader(modifier = Modifier.size(width = 120.dp, height = 24.dp))
                SkeletonLoader(modifier = Modifier.size(width = 80.dp, height = 24.dp))
            }

            // Vendor section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonLoader(modifier = Modifier.size(40.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonLoader(modifier = Modifier.size(width = 100.dp, height = 16.dp))
                    SkeletonLoader(modifier = Modifier.size(width = 150.dp, height = 20.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Amount
            SkeletonLoader(modifier = Modifier.size(width = 140.dp, height = 32.dp))

            // Button
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

/**
 * Skeleton List - Multiple loading cards
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

