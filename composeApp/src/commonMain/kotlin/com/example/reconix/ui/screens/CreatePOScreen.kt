package com.example.reconix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.ui.components.GlassCard
import com.example.reconix.ui.theme.*
import com.example.reconix.viewmodel.VendorViewModel
import com.example.reconix.viewmodel.CreatePOState
import com.example.reconix.shared.CreatePOLineItem
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ═══════════════════════════════════════════════════════════════
 *  Create Purchase Order Screen — Procurement Portal
 *  Futuristic glassmorphic form for creating POs
 * ═══════════════════════════════════════════════════════════════
 */

data class LineItemForm(
    val itemName: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val taxRate: String = "0"
)

@Composable
fun CreatePOScreen(
    onBack: () -> Unit = {},
    viewModel: VendorViewModel = viewModel { VendorViewModel() },
    modifier: Modifier = Modifier
) {
    var vendorName by remember { mutableStateOf("") }
    var vendorEmail by remember { mutableStateOf("") }
    var lineItems by remember { mutableStateOf(listOf(LineItemForm())) }
    var showSuccess by remember { mutableStateOf(false) }
    var successPoId by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val createPOState by viewModel.createPOState.collectAsState()

    // Handle PO creation state changes
    LaunchedEffect(createPOState) {
        when (val state = createPOState) {
            is CreatePOState.Success -> {
                successPoId = state.response.poId
                successMessage = state.response.message
                showSuccess = true
                // Reset form
                vendorName = ""
                vendorEmail = ""
                lineItems = listOf(LineItemForm())
            }
            is CreatePOState.Error -> {
                // Error is shown in the button area
            }
            else -> {}
        }
    }

    // Calculate totals
    val subtotal = lineItems.sumOf { item ->
        val qty = item.quantity.toDoubleOrNull() ?: 0.0
        val price = item.unitPrice.toDoubleOrNull() ?: 0.0
        qty * price
    }
    val taxTotal = lineItems.sumOf { item ->
        val qty = item.quantity.toDoubleOrNull() ?: 0.0
        val price = item.unitPrice.toDoubleOrNull() ?: 0.0
        val tax = item.taxRate.toDoubleOrNull() ?: 0.0
        qty * price * (tax / 100.0)
    }
    val grandTotal = subtotal + taxTotal

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSlateBlue, SlateBlue800, DeepSlateBlue)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──
            item {
                CreatePOHeader(onBack = onBack)
            }

            // ── Vendor Details Card ──
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Vendor Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        FuturisticTextField(
                            value = vendorName,
                            onValueChange = { vendorName = it },
                            label = "Vendor Name *",
                            icon = Icons.Default.Person
                        )

                        FuturisticTextField(
                            value = vendorEmail,
                            onValueChange = { vendorEmail = it },
                            label = "Vendor Email",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )
                    }
                }
            }

            // ── Line Items Header ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Line Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = ElectricIndigo.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${lineItems.size}",
                                color = ElectricIndigoLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { lineItems = lineItems + LineItemForm() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NeonCyan.copy(alpha = 0.15f),
                            contentColor = NeonCyanLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Item", fontSize = 13.sp)
                    }
                }
            }

            // ── Line Item Cards ──
            itemsIndexed(lineItems) { index, item ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Item ${index + 1}",
                                color = NeonCyanLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (lineItems.size > 1) {
                                IconButton(
                                    onClick = {
                                        lineItems = lineItems.toMutableList().apply { removeAt(index) }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = VividRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        FuturisticTextField(
                            value = item.itemName,
                            onValueChange = { newVal ->
                                lineItems = lineItems.toMutableList().apply {
                                    set(index, item.copy(itemName = newVal))
                                }
                            },
                            label = "Item Description *",
                            icon = Icons.Default.Description
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FuturisticTextField(
                                value = item.quantity,
                                onValueChange = { newVal ->
                                    lineItems = lineItems.toMutableList().apply {
                                        set(index, item.copy(quantity = newVal))
                                    }
                                },
                                label = "Qty",
                                icon = Icons.Default.Numbers,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )

                            FuturisticTextField(
                                value = item.unitPrice,
                                onValueChange = { newVal ->
                                    lineItems = lineItems.toMutableList().apply {
                                        set(index, item.copy(unitPrice = newVal))
                                    }
                                },
                                label = "Unit Price",
                                icon = Icons.Default.AttachMoney,
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )

                            FuturisticTextField(
                                value = item.taxRate,
                                onValueChange = { newVal ->
                                    lineItems = lineItems.toMutableList().apply {
                                        set(index, item.copy(taxRate = newVal))
                                    }
                                },
                                label = "Tax %",
                                icon = Icons.Default.Percent,
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Line total
                        val lineTotal = (item.quantity.toDoubleOrNull() ?: 0.0) *
                                (item.unitPrice.toDoubleOrNull() ?: 0.0)
                        if (lineTotal > 0) {
                            Text(
                                "Line Total: $${String.format("%.2f", lineTotal)}",
                                color = NeonMint,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            // ── Totals Card ──
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Order Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        TotalRow("Subtotal", subtotal)
                        TotalRow("Tax", taxTotal)

                        HorizontalDivider(
                            color = GlassBorder,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Grand Total",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "$${String.format("%.2f", grandTotal)}",
                                color = NeonCyanLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            // ── Submit Button ──
            item {
                val isSubmitting = createPOState is CreatePOState.Creating
                val errorMessage = (createPOState as? CreatePOState.Error)?.message

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (vendorName.isNotBlank() && lineItems.any { it.itemName.isNotBlank() }) {
                                // Convert LineItemForm to CreatePOLineItem
                                val poItems = lineItems.mapNotNull { item ->
                                    if (item.itemName.isBlank()) return@mapNotNull null
                                    val qty = item.quantity.toIntOrNull() ?: 0
                                    val price = item.unitPrice.toDoubleOrNull() ?: 0.0
                                    val tax = item.taxRate.toDoubleOrNull() ?: 0.0

                                    if (qty <= 0 || price <= 0.0) return@mapNotNull null

                                    CreatePOLineItem(
                                        itemName = item.itemName,
                                        quantity = qty,
                                        unitPrice = price,
                                        taxRate = tax
                                    )
                                }

                                if (poItems.isNotEmpty()) {
                                    viewModel.createPurchaseOrder(vendorName, vendorEmail, poItems)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = vendorName.isNotBlank() && lineItems.any { it.itemName.isNotBlank() } && !isSubmitting,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            disabledContainerColor = SlateBlue600
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PureWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Creating PO...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Create Purchase Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // ── Bottom spacer ──
            item { Spacer(Modifier.height(32.dp)) }
        }

        // ── Success Overlay ──
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + slideInVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepSlateBlue.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                GlassCard {
                    Column(
                        modifier = Modifier.padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonCyanLight,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Purchase Order Created!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            successPoId,
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricIndigo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            successMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoolGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                showSuccess = false
                                viewModel.resetCreatePOState()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigo
                            )
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

// ── Header Composable ────────────────────────────────────────
@Composable
private fun CreatePOHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GlassWhite5)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PureWhite
            )
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                "Create Purchase Order",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Text(
                "Fill in vendor & item details",
                style = MaterialTheme.typography.bodySmall,
                color = CoolGray
            )
        }
    }
}

// ── Futuristic Text Field ────────────────────────────────────
@Composable
fun FuturisticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = CoolGray, fontSize = 12.sp) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricIndigo,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor = PureWhite,
            unfocusedTextColor = CoolGray,
            cursorColor = NeonCyan,
            focusedContainerColor = GlassWhite5,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

// ── Total Row ──────────────────────────────────────────────
@Composable
private fun TotalRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = CoolGray, fontSize = 14.sp)
        Text(
            "$${String.format("%.2f", amount)}",
            color = PureWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}
