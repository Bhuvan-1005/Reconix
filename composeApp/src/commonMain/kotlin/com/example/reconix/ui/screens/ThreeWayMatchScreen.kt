package com.example.reconix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reconix.viewmodel.FinanceViewModel
import com.example.reconix.viewmodel.InvoiceActionUiState
import com.example.reconix.viewmodel.ThreeWayMatchUiState
import com.example.reconix.BackHandler

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
    viewModel: FinanceViewModel = viewModel { FinanceViewModel() },
    modifier: Modifier = Modifier
) {
    val threeWayMatchState by viewModel.threeWayMatchState.collectAsState()
    val invoiceActionState by viewModel.invoiceActionState.collectAsState()

    // Load data when the invoice ID is known
    LaunchedEffect(invoiceId) { viewModel.loadThreeWayMatch(invoiceId) }
    BackHandler { onBack() }

    // Navigate back once an approve/reject action completes
    LaunchedEffect(invoiceActionState) {
        when (val s = invoiceActionState) {
            is InvoiceActionUiState.Success -> {
                if (s.newStatus.contains("APPROVED", ignoreCase = true)) {
                    onApprove(invoiceId, null)
                } else {
                    onReject(invoiceId, null)
                }
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    val isLoading = threeWayMatchState is ThreeWayMatchUiState.Loading ||
                    invoiceActionState is InvoiceActionUiState.InProgress
    val matchData  = (threeWayMatchState as? ThreeWayMatchUiState.Success)?.match
    val loadError  = (threeWayMatchState as? ThreeWayMatchUiState.Error)?.message
    val actionError = (invoiceActionState as? InvoiceActionUiState.Error)?.message

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectNotes      by remember { mutableStateOf("") }

    Scaffold(
        containerColor = if (LocalIsDarkTheme.current) Color.Black else Color(0xFFF2F4F8),
        topBar = {
            ThreeWayMatchTopBar(
                invoiceId = invoiceId,
                onBack = onBack
            )
        },
        bottomBar = {
            if (!isLoading && matchData != null) {
                ThreeWayMatchActions(
                    matchPercentage = matchData.overallMatchPercentage,
                    onApprove = { viewModel.approveInvoice(invoiceId, null) },
                    onReject = { showRejectDialog = true }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            // Branded skeleton detail loader — consistent with the app's design
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            if (LocalIsDarkTheme.current) listOf(Color.Black, Color(0xFF0A0A0A))
                            else listOf(Color(0xFFF2F4F8), Color(0xFFE8EAEE))
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { SkeletonDetailLoader() }
                }
            }
        } else if (loadError != null || actionError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                        text = loadError ?: actionError ?: "Unknown error",
                        color = SilverText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { viewModel.loadThreeWayMatch(invoiceId) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                    ) { Text("Retry") }
                }
            }
        } else if (matchData != null) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            if (LocalIsDarkTheme.current) listOf(Color.Black, Color(0xFF0A0A0A))
                            else listOf(Color(0xFFF2F4F8), Color(0xFFE8EAEE))
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
                            color = if (LocalIsDarkTheme.current) Color.White else Color.Black
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
                    viewModel.rejectInvoice(invoiceId, rejectNotes)
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
                    if (LocalIsDarkTheme.current) listOf(Color(0xFF111111), Color(0xFF0A0A0A))
                    else listOf(Color.White, Color(0xFFF5F5F5))
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
                        color = if (LocalIsDarkTheme.current) Color.White else Color.Black
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        if (LocalIsDarkTheme.current) listOf(Color(0xFF1A1A1A), Color(0xFF111111))
                        else listOf(Color.White, Color(0xFFF5F5F5))
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
                            color = if (LocalIsDarkTheme.current) Color.White else Color.Black
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

                HorizontalDivider(color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))

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
private fun InfoItem(label: String, value: String, valueColor: Color = Color.Unspecified) {
    val resolvedColor = if (valueColor == Color.Unspecified) {
        if (LocalIsDarkTheme.current) Color.White else Color.Black
    } else valueColor
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
            color = resolvedColor
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
                            if (LocalIsDarkTheme.current) Color(0xFF111111) else Color(0xFFF5F5F5)
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
                        color = if (LocalIsDarkTheme.current) Color.White else Color.Black
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
                    Brush.linearGradient(
                        if (LocalIsDarkTheme.current) listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                        else listOf(Color(0xFFFFFFFF), Color(0xFFF4F4F4))
                    ),
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
                            color = if (LocalIsDarkTheme.current) Color.White else Color.Black
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

                HorizontalDivider(color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f))

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
    val bg    = if (isHighlighted) CrimsonMismatch.copy(alpha = 0.10f) else if (LocalIsDarkTheme.current) Color(0xFF2A2A2A) else Color(0xFFE8E8E8)
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
                if (LocalIsDarkTheme.current) Color(0xFF111111) else Color(0xFFFFFFFF)
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
        containerColor = if (LocalIsDarkTheme.current) Color(0xFF1A1A1A) else Color.White,
        titleContentColor = if (LocalIsDarkTheme.current) Color.White else Color.Black,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(text = "Reject Invoice", fontWeight = FontWeight.Bold, color = if (LocalIsDarkTheme.current) Color.White else Color.Black)
        },
        text = {
            Column {
                Text("Provide a reason for rejection:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
                        unfocusedBorderColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                        focusedTextColor = if (LocalIsDarkTheme.current) Color.White else Color.Black,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = if (LocalIsDarkTheme.current) Color(0xFF222222) else Color(0xFFF5F5F5),
                        unfocusedContainerColor = if (LocalIsDarkTheme.current) Color(0xFF222222) else Color(0xFFF5F5F5),
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
                Text("Confirm Rejection", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SilverText)
            }
        }
    )
}





