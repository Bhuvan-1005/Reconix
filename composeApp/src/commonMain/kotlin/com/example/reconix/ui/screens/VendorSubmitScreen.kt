package com.example.reconix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.ui.components.GlassCard
import com.example.reconix.ui.theme.*

// ── Brand colors ────────────────────────────────────────────
private val Indigo = Color(0xFF6366F1)
private val IndigoFaded = Color(0xFF6366F1).copy(alpha = 0.4f)
private val Slate = Color(0xFF64748B)
private val MidnightBg = Color(0xFF0F172A)

/**
 * ═══════════════════════════════════════════════════════════════
 *  VendorSubmitScreen — Premium Invoice Submission Form
 *
 *  Features:
 *  • Glassmorphic PO details header card
 *  • spacedBy(24.dp) between major sections
 *  • Qty & Unit Price side-by-side with equal weight
 *  • Currency-formatted Amount field (right-aligned, $ prefix)
 *  • Dynamic total = qty * unitPrice + tax
 *  • Sticky "Submit Invoice" button at bottom
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun VendorSubmitScreen(
    poNumber: String = "PO-2026-0451",
    expectedDate: String = "Feb 28, 2026",
    onBack: () -> Unit = {},
    onSubmit: (invoiceNumber: String, amount: Double) -> Unit = { _, _ -> }
) {
    var invoiceNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var taxAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // ── Dynamic total calculation ───────────────────────
    val qty = quantity.toDoubleOrNull() ?: 0.0
    val price = unitPrice.toDoubleOrNull() ?: 0.0
    val tax = taxAmount.toDoubleOrNull() ?: 0.0
    val total = qty * price + tax

    val fieldShape = RoundedCornerShape(16.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Indigo,
        unfocusedBorderColor = Slate,
        focusedLabelColor = Indigo,
        unfocusedLabelColor = Slate,
        cursorColor = Indigo,
        focusedLeadingIconColor = Indigo,
        unfocusedLeadingIconColor = Slate
    )

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(MidnightBg, Color(0xFF070E1B))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = SlateBlue800.copy(alpha = 0.6f),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Text(
                        text = "Submit Invoice",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        },
        bottomBar = {
            // ── Sticky Submit Button ────────────────────
            Surface(
                color = SlateBlue800.copy(alpha = 0.8f),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        isSubmitting = true
                        onSubmit(invoiceNumber, total)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo,
                        disabledContainerColor = IndigoFaded
                    ),
                    enabled = invoiceNumber.isNotBlank() && qty > 0 && price > 0 && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Submitting...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Submit Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ═══════════════════════════════════════════════
            // ── SECTION 1: Linked PO Details (GlassCard) ──
            // ═══════════════════════════════════════════════
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Linked Purchase Order",
                                style = MaterialTheme.typography.labelMedium,
                                color = CoolGray
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = poNumber,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = NeonCyan.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    HorizontalDivider(color = GlassBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Expected Date", style = MaterialTheme.typography.labelSmall, color = CoolGray)
                            Text(expectedDate, fontWeight = FontWeight.SemiBold, color = PureWhite)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Status", style = MaterialTheme.typography.labelSmall, color = CoolGray)
                            Text("OPEN", fontWeight = FontWeight.Bold, color = NeonMint)
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════
            // ── SECTION 2: Invoice Details ────────────────
            // ═══════════════════════════════════════════════
            Text(
                text = "Invoice Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = { invoiceNumber = it },
                label = { Text("Invoice Number") },
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors,
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors,
                minLines = 2,
                maxLines = 3
            )

            // ═══════════════════════════════════════════════
            // ── SECTION 3: Qty + Unit Price (Side-by-side) ─
            // ═══════════════════════════════════════════════
            Text(
                text = "Line Item",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Quantity") },
                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = fieldShape,
                    colors = fieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Unit Price") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = fieldShape,
                    colors = fieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // ═══════════════════════════════════════════════
            // ── SECTION 4: Tax ────────────────────────────
            // ═══════════════════════════════════════════════
            OutlinedTextField(
                value = taxAmount,
                onValueChange = { taxAmount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Tax Amount") },
                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // ═══════════════════════════════════════════════
            // ── SECTION 5: Notes ──────────────────────────
            // ═══════════════════════════════════════════════
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors,
                minLines = 2,
                maxLines = 4
            )

            // ═══════════════════════════════════════════════
            // ── SECTION 6: Dynamic Total ──────────────────
            // ═══════════════════════════════════════════════
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Total Calculation",
                        style = MaterialTheme.typography.labelMedium,
                        color = CoolGray
                    )

                    HorizontalDivider(color = GlassBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal (${qty.toInt()} × $${"%.2f".format(price)})", color = CoolGray, fontSize = 13.sp)
                        Text("$${"%.2f".format(qty * price)}", color = PureWhite, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax", color = CoolGray, fontSize = 13.sp)
                        Text("$${"%.2f".format(tax)}", color = PureWhite, fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(color = GlassBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "$${"%.2f".format(total)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            fontWeight = FontWeight.Bold,
                            color = NeonMint
                        )
                    }
                }
            }

            // Spacer for bottom bar clearance
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
