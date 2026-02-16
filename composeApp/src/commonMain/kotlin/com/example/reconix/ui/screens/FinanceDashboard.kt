package com.example.reconix.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                        Text(
                            text = "Pending Invoices",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Pending Invoices List
                    items(pendingInvoices) { invoice ->
                        FinanceInvoiceCard(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }
                }
            }
        } // end Column

        // ── FAB: Upload Invoice ──
        FloatingActionButton(
            onClick = onUploadInvoice,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = ElectricIndigo,
            contentColor = PureWhite,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.CloudUpload,
                contentDescription = "Upload Invoice",
                modifier = Modifier.size(28.dp)
            )
        }

        // ── Bottom Nav: Switch to Vendor ──
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .clickable(onClick = onNavigateToVendor),
            shape = RoundedCornerShape(16.dp),
            color = NeonCyan.copy(alpha = 0.15f),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = NeonCyanLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Vendor",
                    color = NeonCyanLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    } // end Box
}

@Composable
private fun FinanceDashboardHeader(
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Finance Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Invoice Match Validator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.primary
                )
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
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Match Rate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart Placeholder (simplified as progress bars)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MatchRateBar(
                    label = "Matched",
                    count = matchedCount,
                    percentage = matchRate,
                    color = MatchedGreen
                )
                MatchRateBar(
                    label = "Mismatch",
                    count = mismatchedCount,
                    percentage = 100.0 - matchRate,
                    color = MismatchRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Match Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Match Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${matchRate.formatPercentage()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (matchRate >= 80) MatchedGreen else AmberWarning
                )
            }
        }
    }
}

@Composable
private fun MatchRateBar(
    label: String,
    count: Int,
    percentage: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count (${percentage.formatPercentage()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun FinanceInvoiceCard(
    invoice: InvoiceListItemDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = invoice.vendorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                StatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${invoice.totalAmount.formatCurrency()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PO: ${invoice.poId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${invoice.itemCount} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (invoice.matchPercentage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val matchPct = invoice.matchPercentage!! // Safe because we checked for null
                LinearProgressIndicator(
                    progress = { (matchPct / 100).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (matchPct >= 80) MatchedGreen else AmberWarning
                )
            }
        }
    }
}










