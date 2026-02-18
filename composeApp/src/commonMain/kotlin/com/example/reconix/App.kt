package com.example.reconix

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.reconix.auth.AuthManager
import com.example.reconix.ui.LoginScreen
import com.example.reconix.ui.SplashScreen
import com.example.reconix.ui.screens.*
import com.example.reconix.ui.theme.ReconixTheme

/**
 * Navigation destinations for the app
 */
sealed class Screen {
    data object Splash : Screen()
    data object Login : Screen()
    data object VendorDashboard : Screen()
    data object CreatePO : Screen()
    data object VendorSubmit : Screen()
    data object FinanceDashboard : Screen()
    data object FinanceReview : Screen()
    data object InvoiceUpload : Screen()
    data class ThreeWayMatch(val invoiceId: String) : Screen()
}

@Composable
fun App() {
    ReconixTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

            when (val screen = currentScreen) {
                is Screen.Splash -> {
                    SplashScreen(
                        onSplashComplete = { currentScreen = Screen.Login }
                    )
                }

                is Screen.Login -> {
                    if (AuthManager.isAuthenticated) {
                        // Route based on role
                        currentScreen = when (AuthManager.currentRole) {
                            "FINANCE_MANAGER" -> Screen.FinanceDashboard
                            else -> Screen.VendorDashboard
                        }
                    } else {
                        LoginScreen(
                            onLoginSuccess = { username ->
                                AuthManager.login(username)
                                currentScreen = when (AuthManager.currentRole) {
                                    "FINANCE_MANAGER" -> Screen.FinanceDashboard
                                    else -> Screen.VendorDashboard
                                }
                            }
                        )
                    }
                }

                is Screen.VendorDashboard -> {
                    VendorDashboardScreen(
                        vendorName = AuthManager.currentUser ?: "Vendor",
                        onProfileClick = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        },
                        onOrderClick = { poNumber ->
                            currentScreen = Screen.VendorSubmit
                        },
                        onCreatePO = {
                            currentScreen = Screen.CreatePO
                        },
                        onNavigateToFinance = {
                            currentScreen = Screen.FinanceDashboard
                        }
                    )
                }

                is Screen.VendorSubmit -> {
                    VendorSubmitScreen(
                        onBack = { currentScreen = Screen.VendorDashboard },
                        onSubmit = { invoiceNumber, amount ->
                            println("Submitted: $invoiceNumber, Amount: $amount")
                            currentScreen = Screen.VendorDashboard
                        }
                    )
                }

                is Screen.CreatePO -> {
                    CreatePOScreen(
                        onBack = { currentScreen = Screen.VendorDashboard }
                    )
                }

                is Screen.FinanceDashboard -> {
                    FinanceDashboard(
                        onInvoiceClick = { invoiceId ->
                            currentScreen = Screen.ThreeWayMatch(invoiceId)
                        },
                        onLogout = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        },
                        onUploadInvoice = {
                            currentScreen = Screen.InvoiceUpload
                        },
                        onNavigateToVendor = {
                            currentScreen = Screen.VendorDashboard
                        }
                    )
                }

                is Screen.FinanceReview -> {
                    FinanceReviewScreen(
                        onApprove = {
                            println("Payment approved")
                            currentScreen = Screen.FinanceDashboard
                        },
                        onReject = {
                            println("Invoice rejected")
                            currentScreen = Screen.FinanceDashboard
                        },
                        onBack = { currentScreen = Screen.FinanceDashboard }
                    )
                }

                is Screen.InvoiceUpload -> {
                    InvoiceUploadScreen(
                        onBack = { currentScreen = Screen.FinanceDashboard }
                    )
                }

                is Screen.ThreeWayMatch -> {
                    ThreeWayMatchScreen(
                        invoiceId = screen.invoiceId,
                        onApprove = { id, notes ->
                            println("Approved: $id, Notes: $notes")
                            currentScreen = Screen.FinanceDashboard
                        },
                        onReject = { id, notes ->
                            println("Rejected: $id, Notes: $notes")
                            currentScreen = Screen.FinanceDashboard
                        },
                        onBack = { currentScreen = Screen.FinanceDashboard }
                    )
                }
            }
        }
    }
}