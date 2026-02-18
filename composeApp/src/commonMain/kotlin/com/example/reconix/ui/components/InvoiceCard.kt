package com.example.reconix.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.shared.InvoiceStatus
import com.example.reconix.shared.PurchaseOrderDTO
import com.example.reconix.utils.formatCurrency
import com.example.reconix.ui.theme.*

// ─── private design tokens ──────────────────────────────────────────────────
private val NavyCard     = Color(0xFF0D1B3E)
private val NavySurface  = Color(0xFF112048)
private val NavyBorder   = Color(0xFF1A3066)
private val RoyalBlue    = Color(0xFF4F7FFF)
private val Emerald      = Color(0xFF00C896)
private val Crimson      = Color(0xFFFF2D5B)
private val Gold         = Color(0xFFFFB800)
private val PureWhite    = Color(0xFFFFFFFF)
private val SilverText   = Color(0xFFB8C8E8)
private val MutedText    = Color(0xFF6B82B0)
private val CardShape    = RoundedCornerShape(20.dp)

private fun accentFor(status: InvoiceStatus): Color = when (status) {
    InvoiceStatus.MATCHED       -> Emerald
    InvoiceStatus.MISMATCH      -> Crimson
    InvoiceStatus.PENDING,
    InvoiceStatus.MANUAL_REVIEW -> Gold
}

/**
 * ═══════════════════════════════════════════════════════════════
 *  InvoiceCard — Premium dark-glass card (full width)
 *
 *  • Deep navy glass surface
 *  • Accent top-line matching the invoice status color
 *  • Status-colored vendor avatar initial
 *  • Monospace amount in Royal Blue
 *  • Action button: gradient fill Royal Blue → Navy
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun InvoiceCard(
    purchaseOrder: PurchaseOrderDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    status: InvoiceStatus = InvoiceStatus.PENDING
) {
    val accent = accentFor(status)
    val glassBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to NavySurface,
            1.00f to NavyCard
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(glassBrush)
            .clickable(onClick = onClick)
            .animateContentSize(tween(300, easing = FastOutSlowInEasing))
    ) {
        // ── Accent top line ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(accent, accent.copy(alpha = 0f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp)
        ) {
            // ── Header: PO ID + Status Badge ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PURCHASE ORDER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = purchaseOrder.id,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PureWhite
                    )
                }
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Vendor row ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar initial circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = purchaseOrder.vendorName.firstOrNull()?.uppercase() ?: "V",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "VENDOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 1.0.sp
                    )
                    Text(
                        text = purchaseOrder.vendorName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SilverText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Divider ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NavyBorder)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Amount row ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL AMOUNT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${purchaseOrder.totalAmount.formatCurrency()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = RoyalBlue
                    )
                }

                // Items pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoyalBlue.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "${purchaseOrder.items.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                        Text(
                            text = "items",
                            fontSize = 11.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Action button ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(RoyalBlue, Color(0xFF0D1B3E).copy(alpha = 0.8f).let { RoyalBlue.copy(alpha = 0.7f) })
                        )
                    )
                    .clickable(onClick = onClick)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create Invoice",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

/**
 * CompactInvoiceCard — Slim row card for list views
 */
@Composable
fun CompactInvoiceCard(
    poId: String,
    vendorName: String,
    amount: Double,
    status: InvoiceStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = accentFor(status)
    val glassBrush = Brush.verticalGradient(
        listOf(NavySurface, NavyCard)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassBrush)
            .clickable(onClick = onClick)
    ) {
        // Accent left bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accent)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = vendorName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SilverText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${amount.formatCurrency()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = RoyalBlue
                )
            }

            StatusBadge(status = status)
        }
    }
}