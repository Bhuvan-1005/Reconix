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
    data class VendorSubmit(val poId: String) : Screen()
    data object FinanceDashboard : Screen()
    data object FinanceReview : Screen()
    data object InvoiceUpload : Screen()
    data class ThreeWayMatch(val invoiceId: String) : Screen()
    data object AdminDashboard : Screen()
    data object UserProfile : Screen()
}

@Composable
fun App(
    onRequestFilePick: ((fileName: String, bytes: ByteArray) -> Unit) -> Unit = { _ -> }
) {
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
                            "ADMIN" -> Screen.AdminDashboard
                            else -> Screen.VendorDashboard
                        }
                    } else {
                        LoginScreen(
                            onLoginSuccess = { username ->
                                // AuthManager.login() is already called inside performLogin
                                // (with the JWT token and server role) before onLoginSuccess fires.
                                // We only need to navigate here.
                                currentScreen = when (AuthManager.currentRole) {
                                    "FINANCE_MANAGER" -> Screen.FinanceDashboard
                                    "ADMIN" -> Screen.AdminDashboard
                                    else -> Screen.VendorDashboard
                                }
                            }
                        )
                    }
                }

                is Screen.VendorDashboard -> {
                    VendorPortalScreen(
                        vendorName = AuthManager.currentUser ?: "Vendor",
                        onOrderClick = { poId ->
                            currentScreen = Screen.VendorSubmit(poId)
                        },
                        onCreateInvoice = {
                            currentScreen = Screen.VendorSubmit("")
                        },
                        onUploadInvoice = {
                            currentScreen = Screen.InvoiceUpload
                        },
                        onInvoiceClick = { invoiceId ->
                            currentScreen = Screen.ThreeWayMatch(invoiceId)
                        },
                        onLogout = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        },
                        onNavigateToProfile = {
                            currentScreen = Screen.UserProfile
                        },
                        onBack = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        }
                    )
                }

                is Screen.VendorSubmit -> {
                    VendorSubmitScreen(
                        poId = screen.poId,
                        onBack = { currentScreen = Screen.VendorDashboard },
                        onSubmit = { _, _ ->
                            currentScreen = Screen.VendorDashboard
                        }
                    )
                }

                is Screen.CreatePO -> {
                    CreatePOScreen(
                        onBack = { currentScreen = Screen.FinanceDashboard }
                    )
                }

                is Screen.FinanceDashboard -> {
                    FinancePortalScreen(
                        onInvoiceClick = { invoiceId ->
                            currentScreen = Screen.ThreeWayMatch(invoiceId)
                        },
                        onCreatePO = {
                            currentScreen = Screen.CreatePO
                        },
                        onLogout = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        },
                        onNavigateToProfile = {
                            currentScreen = Screen.UserProfile
                        },
                        onBack = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
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
                    val uploadBackScreen = when (AuthManager.currentRole) {
                        "FINANCE_MANAGER" -> Screen.FinanceDashboard
                        else              -> Screen.VendorDashboard
                    }
                    InvoiceUploadScreen(
                        onBack = { currentScreen = uploadBackScreen },
                        onRequestFilePick = onRequestFilePick
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

                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        onLogout = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        },
                        onNavigateToProfile = {
                            currentScreen = Screen.UserProfile
                        }
                    )
                }

                is Screen.UserProfile -> {
                    // Navigate back to the appropriate dashboard based on role
                    val backScreen = when (AuthManager.currentRole) {
                        "FINANCE_MANAGER" -> Screen.FinanceDashboard
                        "ADMIN" -> Screen.AdminDashboard
                        else -> Screen.VendorDashboard
                    }
                    UserProfileScreen(
                        onBack = { currentScreen = backScreen },
                        onLogout = {
                            AuthManager.logout()
                            currentScreen = Screen.Login
                        }
                    )
                }
            }
        }
    }
}