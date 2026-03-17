package com.example.reconix.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 *  AppShell — 2-Tab Navigation Shell
 *
 *  • HOME tab (left) — Dashboard
 *  • FAB (center) — Primary action
 *  • INVOICES tab (right) — Invoice list from DB
 *  • Theme-aware frosted glass bottom bar
 * ═══════════════════════════════════════════════════════════════
 */

enum class ShellTab(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    INVOICES("Invoices", Icons.Filled.Description, Icons.Outlined.Description)
}

@Composable
fun AppShell(
    currentTab: ShellTab = ShellTab.HOME,
    onTabSelected: (ShellTab) -> Unit = {},
    onFabClick: () -> Unit = {},
    fabIcon: ImageVector = Icons.Default.DocumentScanner,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ── Main Content ────────────────────────────────────
        content(PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 88.dp))

        // ── Bottom Navigation Bar ───────────────────────────
        FrostedBottomBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onFabClick = onFabClick,
            fabIcon = fabIcon,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FrostedBottomBar(
    currentTab: ShellTab,
    onTabSelected: (ShellTab) -> Unit,
    onFabClick: () -> Unit,
    fabIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // ── Glass Nav Container ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isDark) {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF141414).copy(alpha = 0.98f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                LightCard.copy(alpha = 0.98f),
                                LightCardElevated.copy(alpha = 0.95f)
                            )
                        )
                    }
                )
                // Luminous top edge
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isDark) Color.White.copy(alpha = 0.08f) else LightBorder.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        endY = 2f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left tab: HOME
                NavItem(
                    tab = ShellTab.HOME,
                    isActive = currentTab == ShellTab.HOME,
                    onClick = { onTabSelected(ShellTab.HOME) },
                    modifier = Modifier.weight(1f)
                )

                // Center spacer for FAB
                Spacer(modifier = Modifier.weight(1.5f))

                // Right tab: INVOICES
                NavItem(
                    tab = ShellTab.INVOICES,
                    isActive = currentTab == ShellTab.INVOICES,
                    onClick = { onTabSelected(ShellTab.INVOICES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Centered FAB (overlaps bar) ─────────────────────
        CenteredFab(
            onClick = onFabClick,
            icon = fabIcon,
            modifier = Modifier.align(Alignment.TopCenter)
                .offset(y = (-22).dp)
        )
    }
}

@Composable
private fun NavItem(
    tab: ShellTab,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val activeColor = if (isDark) ElectricIndigo else ElectricIndigoDark
    val inactiveColor = if (isDark) MutedText else SoftGrey300

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navScale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween<Color>(300),
        label = "navColor"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isActive) tab.activeIcon else tab.inactiveIcon,
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )

        // Label only visible when active
        AnimatedContent(
            targetState = isActive,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically { it / 2 })
                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { it / 2 })
            },
            label = "navLabel"
        ) { active ->
            if (active) {
                Text(
                    text = tab.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Active indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color.Transparent)
        )
    }
}

@Composable
private fun CenteredFab(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val pulseScale by rememberInfiniteTransition(label = "fabPulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "fabGlow"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow ring
        Box(
            modifier = Modifier
                .size(66.dp)
                .scale(pulseScale)
                .graphicsLayer { alpha = (2f - pulseScale) * 0.35f }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(ElectricIndigo.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // Primary FAB
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(GradientBlueStart, GradientBlueEnd)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Primary Action",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
