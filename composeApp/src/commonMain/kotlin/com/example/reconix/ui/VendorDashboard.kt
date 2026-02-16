package com.example.reconix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reconix.auth.AuthManager
import com.example.reconix.shared.*
import com.example.reconix.viewmodel.*
import com.example.reconix.ui.components.*
import com.example.reconix.utils.formatCurrency

/**
 * Vendor Dashboard Screen
 * Displays Open POs and allows invoice submission
 */
@Composable
fun VendorDashboard(
    viewModel: VendorViewModel = viewModel { VendorViewModel() },
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val validationResult by viewModel.validationResult.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is VendorUiState.Loading -> {
                LoadingScreen()
            }
            is VendorUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadPurchaseOrders() }
                )
            }
            is VendorUiState.Success -> {
                PurchaseOrderList(
                    purchaseOrders = state.purchaseOrders,
                    onPoClick = { po -> viewModel.openSubmitInvoiceDialog(po) },
                    onLogout = onLogout
                )
            }
        }

        // Submit Invoice Dialog
        if (dialogState.isVisible) {
            SubmitInvoiceDialog(
                dialogState = dialogState,
                validationResult = validationResult,
                onQuantityChange = { itemId, qty -> viewModel.updateItemQuantity(itemId, qty) },
                onPriceChange = { itemId, price -> viewModel.updateItemPrice(itemId, price) },
                onSubmit = { viewModel.submitInvoice() },
                onDismiss = { viewModel.closeSubmitInvoiceDialog() }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Invoice Match Validator",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Loading Purchase Orders...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Skeleton loaders
        SkeletonListLoader(
            itemCount = 3,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "❌",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun PurchaseOrderList(
    purchaseOrders: List<PurchaseOrderDTO>,
    onPoClick: (PurchaseOrderDTO) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with user info and logout
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invoice Match Validator",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Open Purchase Orders",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    // User info and logout
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AuthManager.currentUser ?: "Guest",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        if (purchaseOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No Purchase Orders found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(purchaseOrders) { po ->
                    InvoiceCard(
                        purchaseOrder = po,
                        onClick = { onPoClick(po) },
                        status = InvoiceStatus.PENDING
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmitInvoiceDialog(
    dialogState: SubmitInvoiceDialogState,
    validationResult: ValidationResult?,
    onQuantityChange: (String, String) -> Unit,
    onPriceChange: (String, String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val po = dialogState.purchaseOrder ?: return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Submit Invoice",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "PO: ${po.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Validation Result Display
                validationResult?.let { result ->
                    ValidationResultCard(result)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Error Display
                dialogState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Item Inputs
                if (validationResult == null) {
                    Text(
                        text = "Enter invoice quantities and prices:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(po.items) { item ->
                            InvoiceItemInput(
                                item = item,
                                quantity = dialogState.itemQuantities[item.itemId] ?: "",
                                price = dialogState.itemPrices[item.itemId] ?: "",
                                onQuantityChange = { qty -> onQuantityChange(item.itemId, qty) },
                                onPriceChange = { price -> onPriceChange(item.itemId, price) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (validationResult != null) "Close" else "Cancel")
                    }

                    if (validationResult == null) {
                        Button(
                            onClick = onSubmit,
                            modifier = Modifier.weight(1f),
                            enabled = !dialogState.isSubmitting
                        ) {
                            if (dialogState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceItemInput(
    item: PurchaseOrderItemDTO,
    quantity: String,
    price: String,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "PO Qty: ${item.quantity} | PO Price: $${item.unitPrice.formatCurrency()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = { Text("$") }
                )
            }
        }
    }
}

@Composable
private fun ValidationResultCard(result: ValidationResult) {
    val backgroundColor = when (result.status) {
        InvoiceStatus.MATCHED -> Color(0xFF4CAF50)
        InvoiceStatus.MISMATCH -> Color(0xFFF44336)
        InvoiceStatus.PENDING -> Color(0xFFFFC107)
        InvoiceStatus.MANUAL_REVIEW -> Color(0xFFF59E0B)
    }

    val icon = when (result.status) {
        InvoiceStatus.MATCHED -> "✅"
        InvoiceStatus.MISMATCH -> "❌"
        InvoiceStatus.PENDING -> "⏳"
        InvoiceStatus.MANUAL_REVIEW -> "⚠️"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = result.status.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Validated at: ${result.timestamp}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}




