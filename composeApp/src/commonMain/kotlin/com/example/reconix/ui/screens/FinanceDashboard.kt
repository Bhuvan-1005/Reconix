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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reconix.viewmodel.DashboardUiState
import com.example.reconix.viewmodel.FinanceViewModel

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
    onNavigateToProfile: () -> Unit = {},
    viewModel: FinanceViewModel = viewModel { FinanceViewModel() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.dashboardState.collectAsState()

    // Trigger data load each time this screen enters composition
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    val isLoading    = uiState is DashboardUiState.Loading
    val errorMessage = (uiState as? DashboardUiState.Error)?.message

    val emptyMetrics = DashboardMetricsDTO(
        totalPendingInvoices   = 0,
        totalPendingAmount     = 0.0,
        matchedInvoicesCount   = 0,
        mismatchedInvoicesCount = 0,
        matchRate              = 0.0,
        totalPayableAmount     = 0.0,
        averageProcessingTime  = "—",
        recentActivity         = emptyList()
    )
    val metrics = (uiState as? DashboardUiState.Success)?.metrics ?: emptyMetrics
    val pendingInvoices = (uiState as? DashboardUiState.Success)?.pendingInvoices ?: emptyList()

    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) Color.Black else Color(0xFFF2F4F8)

    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(innerPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            FinanceDashboardHeader(
                onLogout = onLogout,
                onNavigateToProfile = onNavigateToProfile
            )

            if (isLoading) {
                FullScreenSkeletonLoader(
                    modifier  = Modifier.fillMaxSize(),
                    itemCount = 4
                )
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage,
                            color = SilverText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.loadDashboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                        ) { Text("Retry") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 100.dp
                    ),
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
                                color = MaterialTheme.colorScheme.onBackground
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

    } // end Box
    } // end Scaffold
}

@Composable
private fun FinanceDashboardHeader(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) Color(0xFF111111) else Color(0xFFFFFFFF)
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Finance Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Theme toggle
                IconButton(onClick = { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }) {
                    Icon(
                        if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Profile
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricIndigo.copy(alpha = 0.15f),
                    onClick = onNavigateToProfile
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
        val isDark = LocalIsDarkTheme.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                        else listOf(Color(0xFFFFFFFF), Color(0xFFF4F4F4))
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        val isDark = LocalIsDarkTheme.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                        else listOf(Color(0xFFFFFFFF), Color(0xFFF4F4F4))
                    ),
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
                        color = MaterialTheme.colorScheme.onBackground
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
                Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
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
        val isDark = LocalIsDarkTheme.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                        else listOf(Color(0xFFFFFFFF), Color(0xFFF4F4F4))
                    ),
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = invoice.vendorName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = invoice.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            text = if (invoice.poId.isBlank()) "No PO" else "PO: ${invoice.poId}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${invoice.itemCount} line items",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .background(if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
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










