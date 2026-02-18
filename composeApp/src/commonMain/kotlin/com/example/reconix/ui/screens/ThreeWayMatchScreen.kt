package com.example.reconix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = DeepSlateBlue,
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
                CircularProgressIndicator(color = EmeraldMatch)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(DeepSlateBlue, Color(0xFF060E20))
                        )
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { InvoiceSummaryCard(matchData) }
                item {
                    OverallMatchCard(
                        matchPercentage = matchData.overallMatchPercentage,
                        status = matchData.status
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.size(3.dp, 20.dp).clip(RoundedCornerShape(2.dp)).background(ElectricIndigo))
                        Text(
                            text = "Line Item Comparison",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }
                items(matchData.matchDetails) { detail ->
                    LineItemComparisonCard(detail)
                }
                item { Spacer(Modifier.height(16.dp)) }
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
private fun ThreeWayMatchTopBar(invoiceId: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0D1B3E), Color(0xFF060E20))
                )
            )
    ) {
        // Accent gradient line at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, ElectricIndigo.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
        TopAppBar(
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "3-Way Match",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PureWhite
                    )
                    Text(
                        text = invoiceId,
                        fontSize = 12.sp,
                        color = SilverText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SilverText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun InvoiceSummaryCard(matchData: ThreeWayMatchDTO, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF122250), Color(0xFF0D1B3E))
                    ),
                    RoundedCornerShape(24.dp)
                )
        ) {
            // Accent top line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ElectricIndigo, EmeraldMatch)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = matchData.vendorName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = matchData.vendorName.take(2).uppercase() + " · Supplier",
                            fontSize = 11.sp,
                            color = SilverText,
                            letterSpacing = 0.5.sp
                        )
                    }
                    StatusBadge(status = matchData.status)
                }

                HorizontalDivider(color = SlateBlue600.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(label = "PO NUMBER", value = matchData.poId)
                    InfoItem(
                        label = "TOTAL AMOUNT",
                        value = "$${matchData.totalAmount.formatCurrency()}",
                        valueColor = ElectricIndigo
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, valueColor: Color = PureWhite) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MutedText,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

@Composable
private fun OverallMatchCard(matchPercentage: Double, status: InvoiceStatus, modifier: Modifier = Modifier) {
    val isMatch = matchPercentage >= 80.0
    val signalColor   = if (isMatch) EmeraldMatch else CrimsonMismatch
    val signalBg      = if (isMatch) EmeraldMatchBg else CrimsonMismatchBg
    val signalIcon    = if (isMatch) Icons.Default.CheckCircle else Icons.Default.Warning
    val signalLabel   = if (isMatch) "All Checks Passed" else "Discrepancies Found"

    // Animated score number count-up feel
    val animPct by animateFloatAsState(
        targetValue = matchPercentage.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "matchPct"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            signalColor.copy(alpha = 0.06f),
                            Color(0xFF0D1B3E)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
        ) {
            // Colored left accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(signalColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Overall Match Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(signalIcon, null, tint = signalColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = signalLabel,
                            fontSize = 13.sp,
                            color = signalColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Big animated % score
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(signalBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${animPct.toInt()}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = signalColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LineItemComparisonCard(detail: ValidationDetailDTO, modifier: Modifier = Modifier) {
    val matchColor  = if (detail.overallMatch) EmeraldMatch else CrimsonMismatch
    val matchBg     = if (detail.overallMatch) EmeraldMatchBg else CrimsonMismatchBg

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(SlateBlue800, Color(0xFF0B1428))),
                    RoundedCornerShape(20.dp)
                )
        ) {
            // Status left bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(56.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(matchColor)
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.itemName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                        Text(
                            text = detail.itemId,
                            fontSize = 11.sp,
                            color = MutedText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(matchBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (detail.overallMatch) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = matchColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 3-Column comparison grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonColumn("PO",      detail.poQuantity,      detail.poPrice,      false,                   Modifier.weight(1f))
                    ComparisonColumn("GRN",     detail.grnQuantity,     null,                false,                   Modifier.weight(1f))
                    ComparisonColumn("Invoice", detail.invoiceQuantity, detail.invoicePrice, !detail.overallMatch,   Modifier.weight(1f))
                }

                HorizontalDivider(color = SlateBlue600.copy(alpha = 0.25f))

                // Match indicator pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MatchIndicator(
                        isMatch = detail.quantityMatch,
                        label = if (detail.quantityMatch) "Qty ✓" else "Qty ✗",
                        modifier = Modifier.weight(1f)
                    )
                    MatchIndicator(
                        isMatch = detail.priceMatch,
                        label = if (detail.priceMatch) "Price ✓" else "Price ✗",
                        modifier = Modifier.weight(1f)
                    )
                }
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
    val bg    = if (isHighlighted) CrimsonMismatch.copy(alpha = 0.10f) else SlateBlue700.copy(alpha = 0.5f)
    val textC = if (isHighlighted) CrimsonMismatch else SilverText

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) CrimsonMismatch else MutedText,
            letterSpacing = 1.sp
        )
        Text(
            text = "$quantity",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = textC
        )
        if (price != null) {
            Text(
                text = "$${price.formatCurrency()}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isHighlighted) CrimsonMismatch else MutedText
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF060E20), Color(0xFF0D1B3E)))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reject button — Crimson outlined
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CrimsonMismatch
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonMismatch.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reject", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Approve button — gradient Emerald
            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    if (matchPercentage >= 80) EmeraldMatch else GoldPending,
                                    if (matchPercentage >= 80) EmeraldMatchDark else GoldPendingDark
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF060E20), modifier = Modifier.size(18.dp))
                        Text("Approve", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF060E20))
                    }
                }
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
        containerColor = Color(0xFF0D1B3E),
        titleContentColor = PureWhite,
        textContentColor = SilverText,
        title = {
            Text(text = "Reject Invoice", fontWeight = FontWeight.Bold, color = PureWhite)
        },
        text = {
            Column {
                Text("Provide a reason for rejection:", color = SilverText, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Rejection Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonMismatch,
                        unfocusedBorderColor = SlateBlue600,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = SilverText,
                        focusedContainerColor = Color(0xFF122250),
                        unfocusedContainerColor = Color(0xFF122250),
                        cursorColor = CrimsonMismatch
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonMismatch),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Rejection", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SilverText)
            }
        }
    )
}





