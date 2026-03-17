package com.example.reconix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reconix.repository.InvoiceRepository
import com.example.reconix.shared.InvoiceDTO
import com.example.reconix.shared.InvoiceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class InvoiceListUiState {
    data object Loading : InvoiceListUiState()
    data class Success(val invoices: List<InvoiceDTO>) : InvoiceListUiState()
    data class Error(val message: String) : InvoiceListUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * ViewModel for InvoiceListScreen.
 * Fetches all invoices from GET /invoice/list and exposes them as StateFlow.
 * Survives tab switches — call [load] from a LaunchedEffect(Unit) so the
 * first fetch is triggered once; call [refresh] for manual pull-to-refresh.
 */
class InvoiceListViewModel : ViewModel() {

    private val repository = InvoiceRepository()

    private val _uiState = MutableStateFlow<InvoiceListUiState>(InvoiceListUiState.Loading)
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()

    /**
     * Load invoices from the backend. Safe to call multiple times — idempotent
     * while a request is already in-flight.
     */
    fun load() {
        // Don't re-fetch if data is already loaded (avoids flicker on tab re-entry)
        if (_uiState.value is InvoiceListUiState.Success) return
        fetchInvoices()
    }

    /** Force a fresh fetch regardless of current state (manual refresh / retry). */
    fun refresh() {
        fetchInvoices()
    }

    private fun fetchInvoices() {
        viewModelScope.launch {
            _uiState.value = InvoiceListUiState.Loading
            repository.getInvoices()
                .onSuccess { invoices ->
                    _uiState.value = InvoiceListUiState.Success(invoices)
                }
                .onFailure { e ->
                    _uiState.value = InvoiceListUiState.Error(
                        e.message ?: "Failed to load invoices"
                    )
                }
        }
    }

    /** Filter helpers exposed as computed properties for the UI. */
    fun filterByStatus(status: InvoiceStatus?): List<InvoiceDTO> {
        val all = (uiState.value as? InvoiceListUiState.Success)?.invoices ?: return emptyList()
        return if (status == null) all else all.filter { it.status == status }
    }
}
