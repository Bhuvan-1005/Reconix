package com.example.reconix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.shared.InvoiceStatus
import com.example.reconix.ui.components.*
import com.example.reconix.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * ═══════════════════════════════════════════════════════════════
 *  VendorDashboardScreen — Futuristic Corporate Dashboard
 *
 *  Features:
 *  • Deep Blue → Black gradient background
 *  • "Good Morning" header with profile icon
 *  • StatCard grid with count-up animations
 *  • LazyColumn of GlassCards with staggered slide/fade entrance
 *  • Press-scale tactile feedback on card click
 * ═══════════════════════════════════════════════════════════════
 */

// ── Data class for demo PO items ────────────────────────────
private data class ActiveOrder(
    val poNumber: String,
    val itemName: String,
    val totalAmount: String,
    val date: String,
    val status: InvoiceStatus
)

private val sampleOrders = listOf(
    ActiveOrder("PO-2026-0451", "MacBook Pro 16\" M4 Max", "$4,299.00", "Feb 14, 2026", InvoiceStatus.MATCHED),
    ActiveOrder("PO-2026-0452", "Dell UltraSharp 32\" 4K Monitor", "$1,149.50", "Feb 13, 2026", InvoiceStatus.PENDING),
    ActiveOrder("PO-2026-0453", "Herman Miller Aeron Chair", "$1,395.00", "Feb 12, 2026", InvoiceStatus.MISMATCH),
    ActiveOrder("PO-2026-0454", "Cisco Meraki MR46 Access Point", "$689.00", "Feb 11, 2026", InvoiceStatus.MATCHED),
    ActiveOrder("PO-2026-0455", "HP LaserJet Pro MFP M428fdw", "$379.99", "Feb 10, 2026", InvoiceStatus.PENDING),
    ActiveOrder("PO-2026-0456", "Logitech Rally Plus Video System", "$2,999.00", "Feb 09, 2026", InvoiceStatus.MATCHED),
)

@Composable
fun VendorDashboardScreen(
    vendorName: String = "Acme Corp",
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onCreatePO: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {}
) {
    val backgroundGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to DeepSlateBlue,
            0.3f to Color(0xFF0A1530),
            1.0f to Color(0xFF060E20)
        )
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Full-width header (no side padding) ──
                DashboardHeader(
                    vendorName = vendorName,
                    onProfileClick = onProfileClick,
                    onLogout = onLogout
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.ShoppingCart,
                            value = 5,
                            label = "Open POs",
                            accentColor = ElectricIndigo,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.AccountBalanceWallet,
                            value = 12,
                            label = "Pending Payments",
                            prefix = "$",
                            suffix = "k",
                            accentColor = EmeraldMatch,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ═══════════════════════════════════════════
                // ── SECTION TITLE ──────────────────────────
                // ═══════════════════════════════════════════
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                // ═══════════════════════════════════════════
                // ── ACTIVE ORDERS LIST (Staggered entrance) ─
                // ═══════════════════════════════════════════
                itemsIndexed(sampleOrders) { index, order ->
                    AnimatedOrderCard(
                        order = order,
                        index = index,
                        onClick = { onOrderClick(order.poNumber) }
                    )
                }
            } // end LazyColumn
            } // end Column

            // ── FAB: Create PO ──
            FloatingActionButton(
                onClick = onCreatePO,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = EmeraldMatch,
                contentColor = PureWhite,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create PO",
                    modifier = Modifier.size(28.dp)
                )
            }

            // ── Bottom Nav: Switch to Finance ──
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .clickable(onClick = onNavigateToFinance),
                shape = RoundedCornerShape(16.dp),
                color = ElectricIndigo.copy(alpha = 0.15f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Finance",
                        color = ElectricIndigo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ── Header Composable ────────────────────────────────────────
@Composable
private fun DashboardHeader(
    vendorName: String,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0D1B3E),
                        Color(0xFF122250),
                        Color(0xFF0D1B3E)
                    )
                )
            )
    ) {
        // Top accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, ElectricIndigo, EmeraldMatch, Color.Transparent)
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Good Morning,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CoolGray
                )
                Text(
                    text = vendorName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldMatch)
                    )
                    Text(
                        text = "Vendor Portal · Live",
                        fontSize = 12.sp,
                        color = SilverText,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A3066).copy(alpha = 0.6f),
                    onClick = onProfileClick
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile",
                            tint = SilverText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                // Logout
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A3066).copy(alpha = 0.6f),
                    onClick = onLogout
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = SilverText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Animated Order Card ──────────────────────────────────────
@Composable
private fun AnimatedOrderCard(
    order: ActiveOrder,
    index: Int,
    onClick: () -> Unit
) {
    // Staggered entrance animation
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Press-to-scale tactile feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedProgress.value
                translationY = (1f - animatedProgress.value) * 60f
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = EmeraldMatch),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: PO Number + StatusBadge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.poNumber,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricIndigo
                )
                StatusBadge(status = order.status)
            }

            // Row 2: Item Name
            Text(
                text = order.itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            // Row 3: Amount + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.totalAmount,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.Bold,
                    color = EmeraldMatch
                )
                Text(
                    text = order.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolGray
                )
            }
        }
    }
}
