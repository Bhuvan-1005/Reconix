package com.example.reconix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reconix.repository.InvoiceRepository
import com.example.reconix.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI States ─────────────────────────────────────────────────────────────────

sealed class AdminUsersUiState {
    data object Loading : AdminUsersUiState()
    data class Success(val users: List<UserListItemDTO>) : AdminUsersUiState()
    data class Error(val message: String) : AdminUsersUiState()
}

sealed class AdminAuditUiState {
    data object Loading : AdminAuditUiState()
    data class Success(val logs: List<AuditLogItemDTO>) : AdminAuditUiState()
    data class Error(val message: String) : AdminAuditUiState()
}

sealed class AdminToleranceUiState {
    data object Loading : AdminToleranceUiState()
    data class Success(val config: ToleranceConfigDTO) : AdminToleranceUiState()
    data class Error(val message: String) : AdminToleranceUiState()
}

sealed class AdminActionUiState {
    data object Idle : AdminActionUiState()
    data object InProgress : AdminActionUiState()
    data class Success(val message: String) : AdminActionUiState()
    data class Error(val message: String) : AdminActionUiState()
}

/**
 * AdminViewModel — Manages state for the Admin Dashboard.
 *
 * Exposes flows for:
 *  - Users list (CRUD)
 *  - Audit log
 *  - Tolerance configuration
 *  - Generic action state (create user, deactivate, update tolerance)
 */
class AdminViewModel : ViewModel() {

    private val repository = InvoiceRepository()

    // ── Users ─────────────────────────────────────────────────────────────────

    private val _usersState = MutableStateFlow<AdminUsersUiState>(AdminUsersUiState.Loading)
    val usersState: StateFlow<AdminUsersUiState> = _usersState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = AdminUsersUiState.Loading
            repository.adminGetUsers()
                .onSuccess { users ->
                    _usersState.value = AdminUsersUiState.Success(users)
                }
                .onFailure { e ->
                    _usersState.value = AdminUsersUiState.Error(
                        e.message ?: "Failed to load users"
                    )
                }
        }
    }

    // ── Audit Log ─────────────────────────────────────────────────────────────

    private val _auditState = MutableStateFlow<AdminAuditUiState>(AdminAuditUiState.Loading)
    val auditState: StateFlow<AdminAuditUiState> = _auditState.asStateFlow()

    fun loadAuditLog() {
        viewModelScope.launch {
            _auditState.value = AdminAuditUiState.Loading
            repository.adminGetAuditLog()
                .onSuccess { logs ->
                    _auditState.value = AdminAuditUiState.Success(logs)
                }
                .onFailure { e ->
                    _auditState.value = AdminAuditUiState.Error(
                        e.message ?: "Failed to load audit log"
                    )
                }
        }
    }

    // ── Tolerance Config ──────────────────────────────────────────────────────

    private val _toleranceState =
        MutableStateFlow<AdminToleranceUiState>(AdminToleranceUiState.Loading)
    val toleranceState: StateFlow<AdminToleranceUiState> = _toleranceState.asStateFlow()

    fun loadTolerance() {
        viewModelScope.launch {
            _toleranceState.value = AdminToleranceUiState.Loading
            repository.adminGetTolerance()
                .onSuccess { config ->
                    _toleranceState.value = AdminToleranceUiState.Success(config)
                }
                .onFailure { e ->
                    _toleranceState.value = AdminToleranceUiState.Error(
                        e.message ?: "Failed to load tolerance config"
                    )
                }
        }
    }

    fun updateTolerance(config: ToleranceConfigDTO) {
        viewModelScope.launch {
            _actionState.value = AdminActionUiState.InProgress
            repository.adminUpdateTolerance(config)
                .onSuccess { updated ->
                    _toleranceState.value = AdminToleranceUiState.Success(updated)
                    _actionState.value = AdminActionUiState.Success("Tolerance updated successfully")
                }
                .onFailure { e ->
                    _actionState.value = AdminActionUiState.Error(
                        e.message ?: "Failed to update tolerance"
                    )
                }
        }
    }

    // ── Action State (create user, deactivate, update tolerance) ─────────────

    private val _actionState = MutableStateFlow<AdminActionUiState>(AdminActionUiState.Idle)
    val actionState: StateFlow<AdminActionUiState> = _actionState.asStateFlow()

    fun createUser(username: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _actionState.value = AdminActionUiState.InProgress
            repository.adminCreateUser(
                CreateUserRequest(
                    username = username,
                    fullName = username,    // use username as display name
                    email    = email.ifBlank { null },
                    password = password,
                    role     = role
                )
            )
                .onSuccess { resp ->
                    _actionState.value = AdminActionUiState.Success(
                        "User '${resp.user?.username ?: username}' created successfully"
                    )
                    loadUsers() // refresh list
                }
                .onFailure { e ->
                    _actionState.value = AdminActionUiState.Error(
                        e.message ?: "Failed to create user"
                    )
                }
        }
    }

    fun deactivateUser(userId: Int) {
        viewModelScope.launch {
            _actionState.value = AdminActionUiState.InProgress
            repository.adminDeactivateUser(userId)
                .onSuccess {
                    _actionState.value = AdminActionUiState.Success("User deactivated")
                    loadUsers() // refresh list
                }
                .onFailure { e ->
                    _actionState.value = AdminActionUiState.Error(
                        e.message ?: "Failed to deactivate user"
                    )
                }
        }
    }

    fun resetActionState() {
        _actionState.value = AdminActionUiState.Idle
    }

    // ── Load all data at once ─────────────────────────────────────────────────

    fun loadAll() {
        loadUsers()
        loadAuditLog()
        loadTolerance()
    }
}
