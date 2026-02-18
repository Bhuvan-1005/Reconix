package com.example.reconix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.shared.*
import com.example.reconix.ui.components.*
import com.example.reconix.ui.theme.*
import com.example.reconix.utils.formatCurrency
import com.example.reconix.utils.formatPercentage
import com.example.reconix.utils.formatInt

/**
 * Finance Manager Dashboard - The "Control Tower"
 * Features:
 * - High-level metrics cards
 * - Pending invoices list
 * - Match rate visualization
 * - Recent activity feed
 */
@Composable
fun FinanceDashboard(
    onInvoiceClick: (String) -> Unit,
    onLogout: () -> Unit,
    onUploadInvoice: () -> Unit = {},
    onNavigateToVendor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: Connect to ViewModel
    val isLoading by remember { mutableStateOf(false) }

    // Mock data - replace with actual ViewModel
    val metrics = DashboardMetricsDTO(
        totalPendingInvoices = 12,
        totalPendingAmount = 45230.50,
        matchedInvoicesCount = 28,
        mismatchedInvoicesCount = 5,
        matchRate = 84.8,
        totalPayableAmount = 152340.75,
        averageProcessingTime = "2.3 hours",
        recentActivity = emptyList()
    )

    val pendingInvoices = remember {
        listOf(
            InvoiceListItemDTO(
                id = "INV-001",
                poId = "PO-123",
                vendorName = "Tech Suppliers Inc",
                totalAmount = 5230.00,
                status = InvoiceStatus.PENDING,
                createdAt = "2026-02-14T10:30:00Z",
                itemCount = 5,
                matchPercentage = null
            ),
            InvoiceListItemDTO(
                id = "INV-002",
                poId = "PO-124",
                vendorName = "Office Depot",
                totalAmount = 1450.50,
                status = InvoiceStatus.MATCHED,
                createdAt = "2026-02-14T09:15:00Z",
                itemCount = 3,
                matchPercentage = 100.0
            ),
            InvoiceListItemDTO(
                id = "INV-003",
                poId = "PO-999",
                vendorName = "Unknown Vendor LLC",
                totalAmount = 3200.00,
                status = InvoiceStatus.MANUAL_REVIEW,
                createdAt = "2026-02-14T08:00:00Z",
                itemCount = 2,
                matchPercentage = null
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to DeepSlateBlue,
                        0.3f to Color(0xFF0A1530),
                        1.0f to Color(0xFF060E20)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            FinanceDashboardHeader(onLogout = onLogout)

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SkeletonListLoader(itemCount = 3)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Metrics Cards
                    item {
                        MetricsSection(metrics = metrics)
                    }

                    // Match Rate Chart
                    item {
                        MatchRateCard(
                            matchedCount = metrics.matchedInvoicesCount,
                            mismatchedCount = metrics.mismatchedInvoicesCount,
                            matchRate = metrics.matchRate
                        )
                    }

                    // Pending Invoices Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pending Invoices",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            // Count badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GoldPending.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${pendingInvoices.size}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPending
                                )
                            }
                        }
                    }

                    // Pending Invoices List
                    items(pendingInvoices) { invoice ->
                        FinanceInvoiceCard(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }

                    // Bottom padding for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        } // end Column

        // ── FAB: Upload Invoice (pulsing) ─────────────────
        PulsingFab(
            onClick = onUploadInvoice,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )

        // ── Vendor Switch Button ──────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .clickable(onClick = onNavigateToVendor),
            shape = RoundedCornerShape(16.dp),
            color = NeonCyan.copy(alpha = 0.08f),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Vendor",
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    } // end Box
}

@Composable
private fun FinanceDashboardHeader(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0D1B3E),
                        Color(0xFF122250),
                        Color(0xFF0D1B3E)
                    )
                )
            )
    ) {
        // Subtle top accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, ElectricIndigo, EmeraldMatch, Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Finance Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldMatch)
                    )
                    Text(
                        text = "Match Validator · Live",
                        fontSize = 12.sp,
                        color = SilverText,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Logout button — minimal icon style
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A3066).copy(alpha = 0.6f),
                onClick = onLogout
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = SilverText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(
    metrics: DashboardMetricsDTO,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Pending",
                value = metrics.totalPendingInvoices.toString(),
                subtitle = "$${metrics.totalPendingAmount.formatCurrency()}",
                icon = Icons.Default.Warning,
                color = AmberWarning,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Matched",
                value = metrics.matchedInvoicesCount.toString(),
                subtitle = "${metrics.matchRate.formatPercentage()}%",
                icon = Icons.Default.CheckCircle,
                color = NeonMint,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Mismatch",
                value = metrics.mismatchedInvoicesCount.toString(),
                subtitle = "Needs review",
                icon = Icons.Default.Close,
                color = VividRose,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Payable",
                value = "$${(metrics.totalPayableAmount / 1000).formatInt()}k",
                subtitle = "Total amount",
                icon = Icons.Default.AccountBalanceWallet,
                color = ElectricIndigo,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(SlateBlue800, Color(0xFF0A1530))
                    ),
                    RoundedCornerShape(20.dp)
                )
        ) {
            // Accent left border
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 0.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(color)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SilverText,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = color
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
private fun MatchRateCard(
    matchedCount: Int,
    mismatchedCount: Int,
    matchRate: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(SlateBlue800, Color(0xFF0A1530))),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Match Rate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    // Big match % badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (matchRate >= 80) EmeraldMatch.copy(alpha = 0.15f)
                                else GoldPending.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${matchRate.formatPercentage()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (matchRate >= 80) EmeraldMatch else GoldPending
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bars
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MatchRateBar("Matched", matchedCount, matchRate, EmeraldMatch)
                    MatchRateBar("Mismatch", mismatchedCount, 100.0 - matchRate, CrimsonMismatch)
                }
            }
        }
    }
}

@Composable
private fun MatchRateBar(
    label: String,
    count: Int,
    percentage: Double,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(text = label, fontSize = 13.sp, color = SilverText, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "$count  ·  ${percentage.formatPercentage()}%",
                fontSize = 13.sp,
                color = color,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentage / 100).toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f)))
                    )
            )
        }
    }
}

@Composable
private fun FinanceInvoiceCard(
    invoice: InvoiceListItemDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated press scale feel
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(SlateBlue800, Color(0xFF0B1428))),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = invoice.id,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = invoice.vendorName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SilverText
                        )
                    }
                    StatusBadge(status = invoice.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = SlateBlue600.copy(alpha = 0.4f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "AMOUNT",
                            fontSize = 10.sp,
                            color = MutedText,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$${invoice.totalAmount.formatCurrency()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricIndigo
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PO: ${invoice.poId}",
                            fontSize = 12.sp,
                            color = SilverText
                        )
                        Text(
                            text = "${invoice.itemCount} line items",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                if (invoice.matchPercentage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val matchPct = invoice.matchPercentage!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Match",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                        Text(
                            text = "${matchPct.toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (matchPct >= 80) EmeraldMatch else GoldPending
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Gradient progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SlateBlue600.copy(alpha = 0.3f))
                    ) {
                        val pct = (matchPct / 100).toFloat().coerceIn(0f, 1f)
                        val barColor = if (matchPct >= 80) EmeraldMatch else GoldPending
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(barColor, barColor.copy(alpha = 0.55f)))
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pulsing FAB for camera/upload — the bold "take action" button
 */
@Composable
private fun PulsingFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseScale by rememberInfiniteTransition(label = "fabPulse").animateFloat(
        initialValue = 1f,
        targetValue  = 1.22f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fabScale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Pulsing outer ring
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(EmeraldMatch.copy(alpha = 0.18f))
        )
        // Primary FAB
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = EmeraldMatch,
            contentColor = Color(0xFF060E20),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp)
        ) {
            Icon(
                Icons.Default.CloudUpload,
                contentDescription = "Upload Invoice",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}










