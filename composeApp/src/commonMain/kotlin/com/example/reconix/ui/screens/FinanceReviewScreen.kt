package com.example.reconix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.ui.components.GlassCard
import com.example.reconix.ui.theme.*

// ── Brand colors (aligned with fintech design system) ────────
private val EmeraldGreen = Color(0xFF00C896)   // EmeraldMatch
private val VividRose    = Color(0xFFFF2D5B)   // CrimsonMismatch
private val MidnightBg   = Color(0xFF060E20)   // DeepSlateBlue
private val Indigo       = Color(0xFF4F7FFF)   // ElectricIndigo

/**
 * ═══════════════════════════════════════════════════════════════
 *  FinanceReviewScreen — 3-Way Match Visualizer
 *
 *  Features:
 *  • Side-by-side comparison: PO | GRN | Invoice
 *  • Auto-highlighting: green border + ✓ for match, rose + ⚠ for mismatch
 *  • Bottom action bar: Reject (outlined red) + Approve (filled green)
 *  • Glassmorphic cards on dark gradient
 * ═══════════════════════════════════════════════════════════════
 */

// Sample data classes for the 3-way comparison
private data class ComparisonLineItem(
    val itemName: String,
    val poQty: Int,
    val poPrice: Double,
    val grnQty: Int,
    val invoiceQty: Int,
    val invoicePrice: Double
) {
    val isMatch: Boolean get() = poQty == invoiceQty && poPrice == invoicePrice && poQty == grnQty
}

private val sampleLineItems = listOf(
    ComparisonLineItem("MacBook Pro 16\" M4 Max", 10, 4299.00, 10, 10, 4299.00),
    ComparisonLineItem("Dell UltraSharp 32\" Monitor", 5, 1149.50, 5, 7, 1149.50),
    ComparisonLineItem("Herman Miller Aeron Chair", 8, 1395.00, 8, 8, 1395.00),
    ComparisonLineItem("Cisco Meraki AP", 15, 689.00, 12, 15, 689.00),
    ComparisonLineItem("HP LaserJet Pro MFP", 3, 379.99, 3, 3, 379.99)
)

@Composable
fun FinanceReviewScreen(
    invoiceId: String = "INV-2026-0451",
    vendorName: String = "Tech Suppliers Inc",
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val matchedCount = sampleLineItems.count { it.isMatch }
    val totalCount = sampleLineItems.size
    val matchPercentage = (matchedCount.toDouble() / totalCount * 100)

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(MidnightBg, Color(0xFF070E1B))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = Color(0xFF0D1B3E),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Column {
                        Text(
                            text = "3-Way Match Review",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "$invoiceId • $vendorName",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }
            }
        },
        bottomBar = {
            // ── Action Buttons ──────────────────────────
            Surface(
                color = Color(0xFF0D1B3E),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject Button (Outlined Red)
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, VividRose),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VividRose
                        )
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Reject", fontWeight = FontWeight.Bold)
                    }

                    // Approve Button (Filled Green)
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Approve Payment", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Match Summary Card ──────────────────────
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Match Rate", style = MaterialTheme.typography.labelMedium, color = CoolGray)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${"%.1f".format(matchPercentage)}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (matchPercentage >= 100) EmeraldGreen else AmberWarning
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$matchedCount / $totalCount matched", color = CoolGray, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (matchedCount == totalCount) "✓ All Items Match" else "⚠ Discrepancies Found",
                                fontWeight = FontWeight.SemiBold,
                                color = if (matchedCount == totalCount) EmeraldGreen else VividRose,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Column Headers ──────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeaderLabel("Purchase Order", Modifier.weight(1f))
                    HeaderLabel("Goods Receipt", Modifier.weight(1f))
                    HeaderLabel("Invoice", Modifier.weight(1f))
                }
            }

            // ── Comparison Rows ─────────────────────────
            items(sampleLineItems) { item ->
                ComparisonRow(item = item)
            }

            // Spacer for bottom bar
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun HeaderLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Indigo,
        textAlign = TextAlign.Center,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun ComparisonRow(item: ComparisonLineItem) {
    val borderColor = if (item.isMatch) EmeraldGreen else VividRose
    val bgColor = if (item.isMatch) EmeraldGreen.copy(alpha = 0.05f) else VividRose.copy(alpha = 0.05f)
    val statusIcon = if (item.isMatch) Icons.Default.CheckCircle else Icons.Default.Warning
    val statusTint = if (item.isMatch) EmeraldGreen else VividRose

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(bgColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Item name + match icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = statusIcon,
                contentDescription = if (item.isMatch) "Matched" else "Mismatch",
                tint = statusTint,
                modifier = Modifier.size(20.dp)
            )
        }

        // 3-column comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // PO Block
            ComparisonBlock(
                qty = item.poQty,
                price = item.poPrice,
                modifier = Modifier.weight(1f),
                textColor = CoolGray
            )

            // GRN Block
            GrnBlock(
                qty = item.grnQty,
                expectedQty = item.poQty,
                modifier = Modifier.weight(1f)
            )

            // Invoice Block
            ComparisonBlock(
                qty = item.invoiceQty,
                price = item.invoicePrice,
                modifier = Modifier.weight(1f),
                textColor = PureWhite,
                isHighlighted = !item.isMatch
            )
        }
    }
}

@Composable
private fun ComparisonBlock(
    qty: Int,
    price: Double,
    modifier: Modifier = Modifier,
    textColor: Color = CoolGray,
    isHighlighted: Boolean = false
) {
    val displayColor = if (isHighlighted) VividRose else textColor

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SlateBlue800.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Qty: $qty",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = displayColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = "$${"%.2f".format(price)}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = displayColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GrnBlock(
    qty: Int,
    expectedQty: Int,
    modifier: Modifier = Modifier
) {
    val isMatch = qty == expectedQty
    val displayColor = if (isMatch) CoolGray else VividRose

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SlateBlue800.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rcvd: $qty",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = displayColor,
            textAlign = TextAlign.Center
        )
        if (!isMatch) {
            Text(
                text = "Expected: $expectedQty",
                fontSize = 11.sp,
                color = VividRose.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "✓ Match",
                fontSize = 11.sp,
                color = EmeraldGreen.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
