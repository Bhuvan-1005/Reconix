package com.example.reconix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.shared.InvoiceStatus
import com.example.reconix.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════
 *  StatusBadge — Pill-shaped tag with dynamic status colors
 *
 *  Features:
 *  • 10% opacity background of the status color
 *  • 1dp border stroke in the status color
 *  • Bold ALL-CAPS text in the status color
 *  • Fully pill-shaped (RoundedCornerShape(100.dp))
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun StatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val (statusColor, displayText) = when (status) {
        InvoiceStatus.PENDING -> Pair(AmberWarning, "PENDING")
        InvoiceStatus.MATCHED -> Pair(NeonMint, "MATCHED")
        InvoiceStatus.MISMATCH -> Pair(VividRose, "MISMATCH")
        InvoiceStatus.MANUAL_REVIEW -> Pair(AmberWarningLight, "⚠ REVIEW")
    }

    val pillShape = RoundedCornerShape(100.dp)

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(statusColor.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = statusColor,
                shape = pillShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Action Status Badge — For finance manager actions
 */
@Composable
fun ActionStatusBadge(
    actionType: String,
    modifier: Modifier = Modifier
) {
    val (statusColor, displayText) = when (actionType.uppercase()) {
        "APPROVED" -> Pair(ApprovedGreen, "✓ APPROVED")
        "REJECTED" -> Pair(RejectedRed, "✗ REJECTED")
        "PENDING" -> Pair(PendingYellow, "⏳ PENDING")
        "PAID" -> Pair(NeonMint, "💰 PAID")
        else -> Pair(SlateBlue600, actionType)
    }

    val pillShape = RoundedCornerShape(100.dp)

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(statusColor.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = statusColor,
                shape = pillShape
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = statusColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Match Status Indicator — Visual indicator for 3-way match results
 */
@Composable
fun MatchIndicator(
    isMatch: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (isMatch) MatchedGreen else MismatchRed)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMatch) MatchedGreen else MismatchRed,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Priority Badge — For urgent invoices
 */
@Composable
fun PriorityBadge(
    priority: String = "HIGH",
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (priority.uppercase()) {
        "HIGH" -> Pair(VividRose.copy(alpha = 0.15f), VividRose)
        "MEDIUM" -> Pair(AmberWarning.copy(alpha = 0.15f), AmberWarning)
        "LOW" -> Pair(SlateBlue600.copy(alpha = 0.15f), SlateBlue600)
        else -> Pair(SlateBlue600.copy(alpha = 0.15f), SlateBlue600)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⚡ $priority",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
