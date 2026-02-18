package com.example.reconix.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════
 *  MainScaffold — Floating Bottom Navigation Shell
 *
 *  Features:
 *  • Floating pill-shaped bottom bar with frosted glass effect
 *  • Outlined icons for inactive, filled + indigo glow for active
 *  • 3 tabs: Dashboard, Invoices, Profile
 *  • Dark gradient background
 * ═══════════════════════════════════════════════════════════════
 */

enum class NavTab(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    INVOICES("Invoices", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScaffold(
    onLogout: () -> Unit = {},
    dashboardContent: @Composable () -> Unit,
    invoicesContent: @Composable () -> Unit,
    profileContent: @Composable () -> Unit
) {
    var activeTab by remember { mutableStateOf(NavTab.DASHBOARD) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            DeepSlateBlue,
            Color(0xFF060E20),
            Color(0xFF020810)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // ── Tab Content ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Leave space for floating bar
        ) {
            when (activeTab) {
                NavTab.DASHBOARD -> dashboardContent()
                NavTab.INVOICES -> invoicesContent()
                NavTab.PROFILE -> profileContent()
            }
        }

        // ── Floating Bottom Bar ─────────────────────────
        FloatingBottomBar(
            activeTab = activeTab,
            onTabSelected = { activeTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun FloatingBottomBar(
    activeTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(28.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 24.dp,
                shape = barShape,
                ambientColor = ElectricIndigo.copy(alpha = 0.25f),
                spotColor = ElectricIndigo.copy(alpha = 0.35f)
            ),
        shape = barShape,
        color = Color(0xFF0D1B3E),
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0D1B3E), Color(0xFF122250), Color(0xFF0D1B3E))
                    ),
                    barShape
                )
        ) {
            // Top luminous line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .clip(barShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, ElectricIndigo.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.entries.forEach { tab ->
                    FloatingNavItem(
                        tab = tab,
                        isActive = activeTab == tab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: NavTab,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isActive) {
                // Pill glow under active icon
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    ElectricIndigo.copy(alpha = 0.25f),
                                    EmeraldMatch.copy(alpha = 0.10f)
                                )
                            )
                        )
                )
            }
            Icon(
                imageVector = if (isActive) tab.filledIcon else tab.outlinedIcon,
                contentDescription = tab.label,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
                tint = if (isActive) ElectricIndigo else SlateBlue500
            )
        }
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) ElectricIndigo else SlateBlue500,
            letterSpacing = if (isActive) 0.sp else 0.sp
        )
        // Active underline dot
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(EmeraldMatch)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
