package com.example.reconix.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reconix.BackHandler
import com.example.reconix.ui.theme.ElectricIndigo
import com.example.reconix.ui.theme.EmeraldMatch

/**
 * ═══════════════════════════════════════════════════════════════
 *  VendorPortalScreen — AppShell wrapper for Vendor
 *
 *  Tabs:
 *    HOME     → VendorDashboardScreen (PO list, stats, orders)
 *    INVOICES → InvoiceListScreen (all invoices from DB)
 *
 *  FAB → Speed-dial: Create Invoice | Upload Invoice
 * ═══════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPortalScreen(
    vendorName: String = "Vendor",
    onOrderClick: (String) -> Unit = {},
    onCreateInvoice: () -> Unit = {},
    onUploadInvoice: () -> Unit = {},
    onInvoiceClick: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var currentTab      by remember { mutableStateOf(ShellTab.HOME) }
    var showActionSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = currentTab != ShellTab.HOME) { currentTab = ShellTab.HOME }
    BackHandler(enabled = currentTab == ShellTab.HOME) { onBack() }

    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Quick Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Create Invoice
                ListItem(
                    headlineContent = { Text("Create Invoice", fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Manually fill in and submit a new invoice") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = EmeraldMatch
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showActionSheet = false
                            onCreateInvoice()
                        }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                // Upload Invoice
                ListItem(
                    headlineContent = { Text("Upload Invoice", fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Submit a new invoice PDF for OCR processing") },
                    leadingContent = {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = ElectricIndigo
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showActionSheet = false
                            onUploadInvoice()
                        }
                )
            }
        }
    }

    AppShell(
        currentTab    = currentTab,
        onTabSelected = { currentTab = it },
        onFabClick    = { showActionSheet = true },
        fabIcon       = Icons.Default.Add
    ) { _ ->
        when (currentTab) {
            ShellTab.HOME -> {
                VendorDashboardScreen(
                    vendorName     = vendorName,
                    onProfileClick = onNavigateToProfile,
                    onLogout       = onLogout,
                    onOrderClick   = onOrderClick
                )
            }
            ShellTab.INVOICES -> {
                InvoiceListScreen(onInvoiceClick = onInvoiceClick)
            }
        }
    }
}
