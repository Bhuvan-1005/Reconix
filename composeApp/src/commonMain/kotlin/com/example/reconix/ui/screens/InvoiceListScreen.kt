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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.auth.AuthManager
import com.example.reconix.shared.InvoiceDTO
import com.example.reconix.shared.InvoiceStatus
import com.example.reconix.ui.components.GlassCard
import com.example.reconix.ui.components.StatusBadge
import com.example.reconix.ui.components.SkeletonListLoader
import com.example.reconix.ui.theme.*
import com.example.reconix.viewmodel.InvoiceListUiState
import com.example.reconix.viewmodel.InvoiceListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color

/**
 * ═══════════════════════════════════════════════════════════════
 *  InvoiceListScreen — RBAC-aware invoice list
 *
 *  • Finance Manager / Admin: Tabbed filter (All | Pending | Matched | Mismatched)
 *  • Vendor: Flat list (no filter tabs)
 * ═══════════════════════════════════════════════════════════════
 */

private enum class InvoiceFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    MATCHED("Matched"),
    MISMATCHED("Mismatched")
}

@Composable
fun InvoiceListScreen(
    onInvoiceClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: InvoiceListViewModel = viewModel { InvoiceListViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load invoices when the screen first enters composition
    LaunchedEffect(Unit) { viewModel.load() }

    val role = AuthManager.currentRole.ifBlank { "VENDOR" }
    val isFinanceOrAdmin = role == "FINANCE_MANAGER" || role == "ADMIN"

    var activeFilter by remember { mutableStateOf(InvoiceFilter.ALL) }

    val invoices = (uiState as? InvoiceListUiState.Success)?.invoices ?: emptyList()
    val isLoading = uiState is InvoiceListUiState.Loading
    val errorMessage = (uiState as? InvoiceListUiState.Error)?.message

    // Apply filter
    val displayed = when (activeFilter) {
        InvoiceFilter.ALL        -> invoices
        InvoiceFilter.PENDING    -> invoices.filter { it.status == InvoiceStatus.PENDING }
        InvoiceFilter.MATCHED    -> invoices.filter { it.status == InvoiceStatus.MATCHED }
        InvoiceFilter.MISMATCHED -> invoices.filter { it.status == InvoiceStatus.MISMATCH }
    }

    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF2F4F8))
            .statusBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "All Invoices",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isLoading) "Loading..." else "${displayed.size} records",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo.copy(alpha = 0.12f))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh invoices",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (invoices.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ElectricIndigo.copy(alpha = 0.15f))
                            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "${invoices.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo
                        )
                    }
                }
            }
        }

        // ── Filter Tabs (Finance/Admin only) ─────────────────
        if (isFinanceOrAdmin) {
            PrimaryScrollableTabRow(
                selectedTabIndex = activeFilter.ordinal,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = ElectricIndigo,
                edgePadding = 16.dp
            ) {
                InvoiceFilter.entries.forEach { filter ->
                    val selected = filter == activeFilter
                    Tab(
                        selected = selected,
                        onClick  = { activeFilter = filter },
                        text = {
                            Text(
                                text  = filter.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) ElectricIndigo
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        when {
            isLoading -> {
                SkeletonListLoader(
                    itemCount = 6,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            errorMessage != null -> {
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
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigo
                            )
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            displayed.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No invoices found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayed) { invoice ->
                        InvoiceListCard(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceListCard(
    invoice: InvoiceDTO,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: ID + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.id,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricIndigo
                )
                StatusBadge(status = invoice.status)
            }

            // Row 2: Vendor
            Text(
                text = invoice.vendorId,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Row 3: PO + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (invoice.poId.isBlank()) "No PO" else "PO: ${invoice.poId}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$${String.format("%.2f", invoice.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.Bold,
                    color = EmeraldMatch
                )
            }

            // Row 4: Line items count
            Text(
                text = "${invoice.items.size} line item${if (invoice.items.size != 1) "s" else ""}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row 5: Rejection reason banner (vendor-facing)
            val rejectionReason = invoice.rejectionReason
            if (invoice.status == InvoiceStatus.MISMATCH && !rejectionReason.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonMismatch.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = CrimsonMismatch,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = rejectionReason,
                        fontSize = 11.sp,
                        color = CrimsonMismatch
                    )
                }
            }
        }
    }
}
