package com.example.reconix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reconix.shared.*
import com.example.reconix.ui.components.*
import com.example.reconix.ui.theme.*
import com.example.reconix.utils.formatCurrency
import com.example.reconix.utils.formatInt

/**
 * 3-Way Match Visualization Screen
 * Shows detailed comparison: PO vs GRN vs Invoice
 * Features:
 * - Side-by-side comparison cards
 * - Auto-highlighting matches (GREEN) and mismatches (RED)
 * - Approve/Reject action buttons
 * - Detailed line-item breakdown
 */
@Composable
fun ThreeWayMatchScreen(
    invoiceId: String,
    onApprove: (String, String?) -> Unit,
    onReject: (String, String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Connect to ViewModel
    var isLoading by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectNotes by remember { mutableStateOf("") }

    // Mock data - replace with actual ViewModel
    val matchData = remember {
        ThreeWayMatchDTO(
            invoiceId = invoiceId,
            poId = "PO-123",
            vendorName = "Tech Suppliers Inc",
            invoiceDate = "2026-02-14T10:30:00Z",
            totalAmount = 5230.00,
            status = InvoiceStatus.PENDING,
            matchDetails = listOf(
                ValidationDetailDTO(
                    itemId = "ITEM-001",
                    itemName = "Dell XPS 15 Laptop",
                    poQuantity = 10,
                    grnQuantity = 10,
                    invoiceQuantity = 10,
                    poPrice = 1500.00,
                    invoicePrice = 1500.00,
                    priceDifference = 0.00,
                    quantityMatch = true,
                    priceMatch = true,
                    overallMatch = true
                ),
                ValidationDetailDTO(
                    itemId = "ITEM-002",
                    itemName = "USB-C Docking Station",
                    poQuantity = 5,
                    grnQuantity = 5,
                    invoiceQuantity = 6,
                    poPrice = 250.00,
                    invoicePrice = 250.00,
                    priceDifference = 0.00,
                    quantityMatch = false,
                    priceMatch = true,
                    overallMatch = false
                )
            ),
            overallMatchPercentage = 50.0,
            createdAt = "2026-02-14T10:30:00Z",
            validatedAt = null
        )
    }

    Scaffold(
        topBar = {
            ThreeWayMatchTopBar(
                invoiceId = invoiceId,
                onBack = onBack
            )
        },
        bottomBar = {
            if (!isLoading) {
                ThreeWayMatchActions(
                    matchPercentage = matchData.overallMatchPercentage,
                    onApprove = { onApprove(invoiceId, null) },
                    onReject = { showRejectDialog = true }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Invoice Summary Card
                item {
                    InvoiceSummaryCard(matchData)
                }

                // Overall Match Status
                item {
                    OverallMatchCard(
                        matchPercentage = matchData.overallMatchPercentage,
                        status = matchData.status
                    )
                }

                // Section Header
                item {
                    Text(
                        text = "Line Item Comparison",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Line Item Details
                items(matchData.matchDetails) { detail ->
                    LineItemComparisonCard(detail)
                }
            }
        }

        // Reject Dialog
        if (showRejectDialog) {
            RejectInvoiceDialog(
                notes = rejectNotes,
                onNotesChange = { rejectNotes = it },
                onConfirm = {
                    onReject(invoiceId, rejectNotes)
                    showRejectDialog = false
                },
                onDismiss = {
                    showRejectDialog = false
                    rejectNotes = ""
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreeWayMatchTopBar(
    invoiceId: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "3-Way Match",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = invoiceId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun InvoiceSummaryCard(
    matchData: ThreeWayMatchDTO,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = matchData.vendorName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = matchData.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "PO Number", value = matchData.poId)
                InfoItem(
                    label = "Total Amount",
                    value = "$${matchData.totalAmount.formatCurrency()}",
                    valueColor = ElectricIndigo
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun OverallMatchCard(
    matchPercentage: Double,
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val isMatch = matchPercentage >= 80.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMatch) MatchedGreen.copy(alpha = 0.1f) else MismatchRed.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overall Match Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isMatch) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isMatch) MatchedGreen else MismatchRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isMatch) "All checks passed" else "Discrepancies found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMatch) MatchedGreen else MismatchRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "${matchPercentage.formatInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (isMatch) MatchedGreen else MismatchRed
            )
        }
    }
}

@Composable
private fun LineItemComparisonCard(
    detail: ValidationDetailDTO,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Item ID: ${detail.itemId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = if (detail.overallMatch) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (detail.overallMatch) MatchedGreen else MismatchRed,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComparisonColumn(
                    title = "PO",
                    quantity = detail.poQuantity,
                    price = detail.poPrice,
                    isHighlighted = false,
                    modifier = Modifier.weight(1f)
                )

                ComparisonColumn(
                    title = "GRN",
                    quantity = detail.grnQuantity,
                    price = null,
                    isHighlighted = false,
                    modifier = Modifier.weight(1f)
                )

                ComparisonColumn(
                    title = "Invoice",
                    quantity = detail.invoiceQuantity,
                    price = detail.invoicePrice,
                    isHighlighted = !detail.overallMatch,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            // Match Indicators
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MatchIndicator(
                    isMatch = detail.quantityMatch,
                    label = if (detail.quantityMatch)
                        "Quantity: ${detail.invoiceQuantity} ≤ ${detail.grnQuantity} ✓"
                    else
                        "Quantity: ${detail.invoiceQuantity} > ${detail.grnQuantity} ✗"
                )

                MatchIndicator(
                    isMatch = detail.priceMatch,
                    label = if (detail.priceMatch)
                        "Price: Match within tolerance ✓"
                    else
                        "Price: Difference $${detail.priceDifference.formatCurrency()} ✗"
                )
            }
        }
    }
}

@Composable
private fun ComparisonColumn(
    title: String,
    quantity: Int,
    price: Double?,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isHighlighted) MismatchRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Qty: $quantity",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) MismatchRed else MaterialTheme.colorScheme.onSurface
        )

        if (price != null) {
            Text(
                text = "$${price.formatCurrency()}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = if (isHighlighted) MismatchRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ThreeWayMatchActions(
    matchPercentage: Double,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VividRose
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reject", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (matchPercentage >= 80) ApprovedGreen else AmberWarning
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Approve", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RejectInvoiceDialog(
    notes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Invoice",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Please provide a reason for rejecting this invoice:")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Rejection Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividRose
                )
            ) {
                Text("Confirm Rejection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}








