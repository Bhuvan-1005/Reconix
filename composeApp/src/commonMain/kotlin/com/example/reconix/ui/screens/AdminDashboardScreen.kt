package com.example.reconix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reconix.shared.*
import com.example.reconix.ui.components.GlassCard
import com.example.reconix.ui.theme.*
import com.example.reconix.BackHandler
import com.example.reconix.viewmodel.*

/**
 * ═══════════════════════════════════════════════════════════════
 *  AdminDashboardScreen — Live API-driven Admin Console
 *
 *  Sections:
 *  • Tolerance Configuration (live + editable)
 *  • User Management (live list + Create / Deactivate)
 *  • Audit Trail (live log)
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
    adminViewModel: AdminViewModel = viewModel { AdminViewModel() }
) {
    val isDark = LocalIsDarkTheme.current
    BackHandler { onLogout() }

    // ── Load data on first composition ─────────────────────────────────────
    LaunchedEffect(Unit) { adminViewModel.loadAll() }

    // ── Observe states ────────────────────────────────────────────────────
    val usersState     by adminViewModel.usersState.collectAsState()
    val auditState     by adminViewModel.auditState.collectAsState()
    val toleranceState by adminViewModel.toleranceState.collectAsState()
    val actionState    by adminViewModel.actionState.collectAsState()

    // ── Snackbar for action feedback ──────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is AdminActionUiState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                adminViewModel.resetActionState()
            }
            is AdminActionUiState.Error -> {
                snackbarHostState.showSnackbar("Error: ${s.message}")
                adminViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    // ── Dialog visibility ─────────────────────────────────────────────────
    var showAddUserDialog    by remember { mutableStateOf(false) }
    var showToleranceDialog  by remember { mutableStateOf(false) }
    var editingTolerance     by remember {
        mutableStateOf(ToleranceConfigDTO(priceTolerancePct = 2.0, quantityTolerance = 1, amountThreshold = 50.0))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { _ ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black else Color(0xFFF2F4F8))
                .statusBarsPadding()
        ) {
            // ── Header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF111111) else Color(0xFFFFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, GoldPending, ElectricIndigo, Color.Transparent)
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Admin Console",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "System Configuration & Monitoring",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }) {
                            Icon(
                                if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = GoldPending.copy(alpha = 0.15f),
                            onClick = onNavigateToProfile
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = GoldPending,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Scrollable content ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Tolerance Configuration ──────────────────────
                AdminSection(
                    title = "Tolerance Configuration",
                    icon  = Icons.Default.Tune,
                    color = ElectricIndigo,
                    actionLabel = "Edit",
                    onAction = {
                        (toleranceState as? AdminToleranceUiState.Success)?.config?.let {
                            editingTolerance = it
                        }
                        showToleranceDialog = true
                    }
                ) {
                    when (val s = toleranceState) {
                        is AdminToleranceUiState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp),
                            strokeWidth = 2.dp, color = ElectricIndigo
                        )
                        is AdminToleranceUiState.Error ->
                            Text(s.message, color = CrimsonMismatch, fontSize = 12.sp)
                        is AdminToleranceUiState.Success -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val cfg = s.config
                            ToleranceRow("Price Variance", "${cfg.priceVariancePct}%", "Auto-approve within threshold")
                            ToleranceRow("Quantity Variance", "${cfg.qtyVarianceUnits} unit", "Flag if exceeds")
                            ToleranceRow("Amount Threshold", "₹${cfg.amountThreshold}", "Ignore rounding differences")
                        }
                    }
                }

                // ── User Management ──────────────────────────────
                AdminSection(
                    title = "User Management",
                    icon  = Icons.Default.Group,
                    color = EmeraldMatch,
                    actionLabel = "Add User",
                    onAction = { showAddUserDialog = true }
                ) {
                    when (val s = usersState) {
                        is AdminUsersUiState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp),
                            strokeWidth = 2.dp, color = EmeraldMatch
                        )
                        is AdminUsersUiState.Error ->
                            Text(s.message, color = CrimsonMismatch, fontSize = 12.sp)
                        is AdminUsersUiState.Success -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (s.users.isEmpty()) {
                                Text("No users found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            } else {
                                s.users.forEach { user ->
                                    LiveUserRow(user = user, onDeactivate = { adminViewModel.deactivateUser(user.id) })
                                }
                            }
                        }
                    }
                }

                // ── Audit Trail ──────────────────────────────────
                AdminSection(
                    title = "Audit Trail",
                    icon  = Icons.Default.History,
                    color = GoldPending
                ) {
                    when (val s = auditState) {
                        is AdminAuditUiState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp),
                            strokeWidth = 2.dp, color = GoldPending
                        )
                        is AdminAuditUiState.Error ->
                            Text(s.message, color = CrimsonMismatch, fontSize = 12.sp)
                        is AdminAuditUiState.Success -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (s.logs.isEmpty()) {
                                Text("No audit logs found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            } else {
                                s.logs.take(20).forEach { log -> LiveAuditRow(log) }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add User Dialog ───────────────────────────────────────────────────
    if (showAddUserDialog) {
        AddUserDialog(
            isBusy = actionState is AdminActionUiState.InProgress,
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username, email, password, role ->
                showAddUserDialog = false
                adminViewModel.createUser(username, email, password, role)
            }
        )
    }

    // ── Edit Tolerance Dialog ─────────────────────────────────────────────
    if (showToleranceDialog) {
        EditToleranceDialog(
            initial   = editingTolerance,
            isBusy    = actionState is AdminActionUiState.InProgress,
            onDismiss = { showToleranceDialog = false },
            onConfirm = { updated ->
                showToleranceDialog = false
                adminViewModel.updateTolerance(updated)
            }
        )
    }
}

// ── Section wrapper ────────────────────────────────────────────────────────────

@Composable
private fun AdminSection(
    title: String,
    icon: ImageVector,
    color: Color,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction) {
                        Text(actionLabel, color = color, fontSize = 12.sp)
                    }
                }
            }
            content()
        }
    }
}

// ── Tolerance row ──────────────────────────────────────────────────────────────

@Composable
private fun ToleranceRow(label: String, value: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(text = hint, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricIndigo.copy(alpha = 0.12f))
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricIndigo)
        }
    }
}

// ── Live user row ──────────────────────────────────────────────────────────────

@Composable
private fun LiveUserRow(user: UserListItemDTO, onDeactivate: () -> Unit) {
    val roleColor = when (user.role) {
        "FINANCE_MANAGER" -> ElectricIndigo
        "ADMIN"           -> GoldPending
        else              -> EmeraldMatch
    }
    val roleName = when (user.role) {
        "FINANCE_MANAGER" -> "Finance"
        "ADMIN"           -> "Admin"
        else              -> "Vendor"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(roleColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = roleName, fontSize = 10.sp, color = roleColor)
                }
                if (!user.isActive) {
                    Text("Inactive", fontSize = 10.sp, color = CrimsonMismatch)
                }
            }
        }
        if (user.isActive) {
            TextButton(
                onClick = onDeactivate,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Deactivate", color = CrimsonMismatch, fontSize = 11.sp)
            }
        }
    }
}

// ── Live audit row ─────────────────────────────────────────────────────────────

@Composable
private fun LiveAuditRow(log: AuditLogItemDTO) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${log.entity} — ${log.action}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "by ${log.actor}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = log.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Add User Dialog ────────────────────────────────────────────────────────────

@Composable
private fun AddUserDialog(
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (username: String, email: String, password: String, role: String) -> Unit
) {
    var username      by remember { mutableStateOf("") }
    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    val roles         = listOf("VENDOR", "FINANCE_MANAGER", "ADMIN")
    var selectedRole  by remember { mutableStateOf("VENDOR") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create User", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Username") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Role", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roles.forEach { role ->
                        val active = role == selectedRole
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                            color = if (active) ElectricIndigo.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { selectedRole = role }
                        ) {
                            Text(
                                text = when (role) {
                                    "FINANCE_MANAGER" -> "Finance"
                                    "ADMIN"           -> "Admin"
                                    else              -> "Vendor"
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = if (active) ElectricIndigo
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank())
                        onConfirm(username.trim(), email.trim(), password, selectedRole)
                },
                enabled = !isBusy && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isBusy) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Edit Tolerance Dialog ──────────────────────────────────────────────────────

@Composable
private fun EditToleranceDialog(
    initial: ToleranceConfigDTO,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ToleranceConfigDTO) -> Unit
) {
    var priceText  by remember { mutableStateOf(initial.priceVariancePct.toString()) }
    var qtyText    by remember { mutableStateOf(initial.qtyVarianceUnits.toString()) }
    var amountText by remember { mutableStateOf(initial.amountThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tolerance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = priceText, onValueChange = { priceText = it },
                    label = { Text("Price Variance (%)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qtyText, onValueChange = { qtyText = it },
                    label = { Text("Quantity Variance (units)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it },
                    label = { Text("Amount Threshold (₹)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        ToleranceConfigDTO(
                            priceTolerancePct = priceText.toDoubleOrNull()  ?: initial.priceTolerancePct,
                            quantityTolerance = qtyText.toIntOrNull()       ?: initial.quantityTolerance,
                            amountThreshold   = amountText.toDoubleOrNull() ?: initial.amountThreshold
                        )
                    )
                },
                enabled = !isBusy
            ) {
                if (isBusy) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
