package com.example.reconix.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.example.reconix.BackHandler

/**
 * ═══════════════════════════════════════════════════════════════
 *  FinancePortalScreen — AppShell wrapper for Finance Manager
 *
 *  Tabs:
 *    HOME     → FinanceDashboard (metrics, pending invoices, match rate)
 *    INVOICES → InvoiceListScreen (role-aware tabbed filter)
 *
 *  FAB → Create Purchase Order
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun FinancePortalScreen(
    onInvoiceClick: (String) -> Unit = {},
    onCreatePO: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(ShellTab.HOME) }

    BackHandler(enabled = currentTab != ShellTab.HOME) { currentTab = ShellTab.HOME }
    BackHandler(enabled = currentTab == ShellTab.HOME) { onBack() }

    AppShell(
        currentTab    = currentTab,
        onTabSelected = { currentTab = it },
        onFabClick    = onCreatePO,
        fabIcon       = Icons.Default.Add
    ) { _ ->
        when (currentTab) {
            ShellTab.HOME -> {
                FinanceDashboard(
                    onInvoiceClick      = onInvoiceClick,
                    onLogout            = onLogout,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
            ShellTab.INVOICES -> {
                InvoiceListScreen(onInvoiceClick = onInvoiceClick)
            }
        }
    }
}
