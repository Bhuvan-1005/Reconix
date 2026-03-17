package com.example.reconix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reconix.repository.InvoiceRepository
import com.example.reconix.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Dashboard UI State ────────────────────────────────────────────────────────

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val metrics: DashboardMetricsDTO,
        val pendingInvoices: List<InvoiceListItemDTO>,
        val recentActivity: List<RecentActivityDTO>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

// ── Three-Way Match UI State ──────────────────────────────────────────────────

sealed class ThreeWayMatchUiState {
    data object Loading : ThreeWayMatchUiState()
    data class Success(val match: ThreeWayMatchDTO) : ThreeWayMatchUiState()
    data class Error(val message: String) : ThreeWayMatchUiState()
}

// ── Invoice Action UI State (approve / reject) ────────────────────────────────

sealed class InvoiceActionUiState {
    data object Idle : InvoiceActionUiState()
    data object InProgress : InvoiceActionUiState()
    data class Success(val message: String, val newStatus: String) : InvoiceActionUiState()
    data class Error(val message: String) : InvoiceActionUiState()
}

// ── Invoice Upload UI State ───────────────────────────────────────────────────

sealed class UploadUiState {
    data object Idle : UploadUiState()
    /** isGemini = true when the Gemini AI path is active (shows AI loading text). */
    data class Uploading(val isGemini: Boolean = false) : UploadUiState()
    data class Success(val result: InvoiceUploadResponse) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

/**
 * Finance ViewModel — Manages state for the Finance Dashboard,
 * 3-Way Match screen, approve/reject actions, and Invoice Upload.
 */
class FinanceViewModel : ViewModel() {

    private val repository = InvoiceRepository()

    // ── Dashboard ─────────────────────────────────────────────────────────────

    private val _dashboardState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    // ── Three-Way Match ───────────────────────────────────────────────────────

    private val _threeWayMatchState =
        MutableStateFlow<ThreeWayMatchUiState>(ThreeWayMatchUiState.Loading)
    val threeWayMatchState: StateFlow<ThreeWayMatchUiState> = _threeWayMatchState.asStateFlow()

    // ── Invoice Action ────────────────────────────────────────────────────────

    private val _invoiceActionState =
        MutableStateFlow<InvoiceActionUiState>(InvoiceActionUiState.Idle)
    val invoiceActionState: StateFlow<InvoiceActionUiState> = _invoiceActionState.asStateFlow()

    // ── Upload ────────────────────────────────────────────────────────────────

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    // ── Dashboard Loading ─────────────────────────────────────────────────────

    /**
     * Load dashboard data: metrics + pending invoices + recent activity.
     * Call this from a LaunchedEffect whenever the Finance Dashboard enters composition.
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = DashboardUiState.Loading
            try {
                val metricsResult  = repository.getDashboardMetrics()
                val pendingResult  = repository.getPendingInvoices()
                val activityResult = repository.getRecentActivity()

                val metrics = metricsResult.getOrElse { e ->
                    _dashboardState.value = DashboardUiState.Error(
                        e.message ?: "Failed to load metrics"
                    )
                    return@launch
                }
                val pending = pendingResult.getOrElse { e ->
                    _dashboardState.value = DashboardUiState.Error(
                        e.message ?: "Failed to load pending invoices"
                    )
                    return@launch
                }
                val activity = activityResult.getOrElse { emptyList() }

                _dashboardState.value = DashboardUiState.Success(
                    metrics        = metrics,
                    pendingInvoices = pending,
                    recentActivity  = activity
                )
            } catch (e: Exception) {
                _dashboardState.value = DashboardUiState.Error(
                    e.message ?: "Unexpected error loading dashboard"
                )
            }
        }
    }

    /** Alias for pull-to-refresh. */
    fun refreshDashboard() = loadDashboard()

    // ── Three-Way Match ───────────────────────────────────────────────────────

    /**
     * Fetch the 3-way match report for [invoiceId].
     * Call this from a LaunchedEffect(invoiceId) inside ThreeWayMatchScreen.
     */
    fun loadThreeWayMatch(invoiceId: String) {
        viewModelScope.launch {
            _threeWayMatchState.value = ThreeWayMatchUiState.Loading
            repository.getThreeWayMatch(invoiceId)
                .onSuccess { match ->
                    _threeWayMatchState.value = ThreeWayMatchUiState.Success(match)
                }
                .onFailure { e ->
                    _threeWayMatchState.value = ThreeWayMatchUiState.Error(
                        e.message ?: "Failed to load match data"
                    )
                }
        }
    }

    // ── Invoice Actions ───────────────────────────────────────────────────────

    /**
     * Approve an invoice. Transitions [invoiceActionState] to Success/Error.
     * The caller should observe [invoiceActionState] and navigate once Success.
     */
    fun approveInvoice(invoiceId: String, notes: String? = null) {
        viewModelScope.launch {
            _invoiceActionState.value = InvoiceActionUiState.InProgress
            repository.approveInvoice(invoiceId, notes)
                .onSuccess { response ->
                    _invoiceActionState.value = InvoiceActionUiState.Success(
                        message   = response.message,
                        newStatus = response.newStatus
                    )
                }
                .onFailure { e ->
                    _invoiceActionState.value = InvoiceActionUiState.Error(
                        e.message ?: "Approval failed"
                    )
                }
        }
    }

    /**
     * Reject an invoice. Transitions [invoiceActionState] to Success/Error.
     */
    fun rejectInvoice(invoiceId: String, notes: String? = null) {
        viewModelScope.launch {
            _invoiceActionState.value = InvoiceActionUiState.InProgress
            repository.rejectInvoice(invoiceId, notes)
                .onSuccess { response ->
                    _invoiceActionState.value = InvoiceActionUiState.Success(
                        message   = response.message,
                        newStatus = response.newStatus
                    )
                }
                .onFailure { e ->
                    _invoiceActionState.value = InvoiceActionUiState.Error(
                        e.message ?: "Rejection failed"
                    )
                }
        }
    }

    /** Reset action state after navigating away. */
    fun resetActionState() {
        _invoiceActionState.value = InvoiceActionUiState.Idle
    }

    // ── Invoice Upload ────────────────────────────────────────────────────────

    /**
     * Upload an invoice PDF / image for OCR processing and automatic 3-way match.
     * @param useGemini  When true, routes through Gemini 1.5 Flash AI extraction;
     *                   when false, uses the OCR.space pipeline (default).
     */
    fun uploadInvoice(fileBytes: ByteArray, fileName: String, useGemini: Boolean = false) {
        viewModelScope.launch {
            _uploadState.value = UploadUiState.Uploading(isGemini = useGemini)
            val result = if (useGemini) {
                repository.geminiExtractInvoice(fileBytes, fileName)
            } else {
                repository.uploadInvoice(fileBytes, fileName)
            }
            result
                .onSuccess { response ->
                    _uploadState.value = UploadUiState.Success(response)
                }
                .onFailure { e ->
                    _uploadState.value = UploadUiState.Error(
                        e.message ?: if (useGemini) "AI extraction failed" else "Upload failed"
                    )
                }
        }
    }

    /** Reset upload state (e.g., when user wants to upload another file). */
    fun resetUploadState() {
        _uploadState.value = UploadUiState.Idle
    }

    // ── Activity feed helper ──────────────────────────────────────────────────

    /** Returns the activity list from the current dashboard state if available. */
    fun currentActivity(): List<RecentActivityDTO> =
        (_dashboardState.value as? DashboardUiState.Success)?.recentActivity ?: emptyList()
}
