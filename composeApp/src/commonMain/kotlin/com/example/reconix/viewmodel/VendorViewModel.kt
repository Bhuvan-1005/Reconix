package com.example.reconix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reconix.repository.InvoiceRepository
import com.example.reconix.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * UI State for the Vendor Dashboard
 */
sealed class VendorUiState {
    data object Loading : VendorUiState()
    data class Success(
        val purchaseOrders: List<PurchaseOrderDTO>,
        val selectedPo: PurchaseOrderDTO? = null,
        val validationResult: ValidationResult? = null
    ) : VendorUiState()
    data class Error(val message: String) : VendorUiState()
}

/**
 * Dialog State for Submit Invoice
 */
data class SubmitInvoiceDialogState(
    val isVisible: Boolean = false,
    val purchaseOrder: PurchaseOrderDTO? = null,
    val itemQuantities: Map<String, String> = emptyMap(), // itemId -> quantity input
    val itemPrices: Map<String, String> = emptyMap(), // itemId -> price input
    val isSubmitting: Boolean = false,
    val error: String? = null
)

/**
 * State for PO creation
 */
sealed class CreatePOState {
    data object Idle : CreatePOState()
    data object Creating : CreatePOState()
    data class Success(val response: CreatePurchaseOrderResponse) : CreatePOState()
    data class Error(val message: String) : CreatePOState()
}

/**
 * State for simple invoice submission from VendorSubmitScreen
 */
sealed class InvoiceSubmitState {
    data object Idle : InvoiceSubmitState()
    data object Submitting : InvoiceSubmitState()
    data class Success(val result: ValidationResult) : InvoiceSubmitState()
    data class Error(val message: String) : InvoiceSubmitState()
}

/**
 * Vendor ViewModel - Manages state for the Vendor Dashboard
 * Uses shared DTOs for type-safe communication
 */
class VendorViewModel : ViewModel() {

    private val repository = InvoiceRepository()

    private val _uiState = MutableStateFlow<VendorUiState>(VendorUiState.Loading)
    val uiState: StateFlow<VendorUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(SubmitInvoiceDialogState())
    val dialogState: StateFlow<SubmitInvoiceDialogState> = _dialogState.asStateFlow()

    private val _validationResult = MutableStateFlow<ValidationResult?>(null)
    val validationResult: StateFlow<ValidationResult?> = _validationResult.asStateFlow()

    private val _createPOState = MutableStateFlow<CreatePOState>(CreatePOState.Idle)
    val createPOState: StateFlow<CreatePOState> = _createPOState.asStateFlow()

    // ── Single-PO detail for VendorSubmitScreen ───────────────────────────────

    private val _selectedPurchaseOrder = MutableStateFlow<PurchaseOrderDTO?>(null)
    val selectedPurchaseOrder: StateFlow<PurchaseOrderDTO?> = _selectedPurchaseOrder.asStateFlow()

    // ── Simple invoice submission ─────────────────────────────────────────────

    private val _invoiceSubmitState =
        MutableStateFlow<InvoiceSubmitState>(InvoiceSubmitState.Idle)
    val invoiceSubmitState: StateFlow<InvoiceSubmitState> = _invoiceSubmitState.asStateFlow()

    init {
        loadPurchaseOrders()
    }

    /**
     * Load all Purchase Orders from the server
     */
    fun loadPurchaseOrders() {
        viewModelScope.launch {
            _uiState.value = VendorUiState.Loading

            repository.getPurchaseOrders()
                .onSuccess { purchaseOrders ->
                    _uiState.value = VendorUiState.Success(purchaseOrders = purchaseOrders)
                }
                .onFailure { error ->
                    _uiState.value = VendorUiState.Error(
                        message = error.message ?: "Failed to load Purchase Orders"
                    )
                }
        }
    }

    /**
     * Open the Submit Invoice dialog for a specific PO
     */
    fun openSubmitInvoiceDialog(purchaseOrder: PurchaseOrderDTO) {
        // Pre-fill with PO item quantities and prices
        val quantities = purchaseOrder.items.associate { it.itemId to it.quantity.toString() }
        val prices = purchaseOrder.items.associate { it.itemId to it.unitPrice.toString() }

        _dialogState.value = SubmitInvoiceDialogState(
            isVisible = true,
            purchaseOrder = purchaseOrder,
            itemQuantities = quantities,
            itemPrices = prices
        )
    }

    /**
     * Close the Submit Invoice dialog
     */
    fun closeSubmitInvoiceDialog() {
        _dialogState.value = SubmitInvoiceDialogState()
        _validationResult.value = null
    }

    /**
     * Update quantity for an item in the dialog
     */
    fun updateItemQuantity(itemId: String, quantity: String) {
        val currentState = _dialogState.value
        _dialogState.value = currentState.copy(
            itemQuantities = currentState.itemQuantities + (itemId to quantity),
            error = null
        )
    }

    /**
     * Update price for an item in the dialog
     */
    fun updateItemPrice(itemId: String, price: String) {
        val currentState = _dialogState.value
        _dialogState.value = currentState.copy(
            itemPrices = currentState.itemPrices + (itemId to price),
            error = null
        )
    }

    /**
     * Submit the invoice for validation
     * Uses InvoiceDTO from shared module
     */
    fun submitInvoice() {
        val currentDialogState = _dialogState.value
        val po = currentDialogState.purchaseOrder ?: return

        viewModelScope.launch {
            _dialogState.value = currentDialogState.copy(isSubmitting = true, error = null)

            try {
                // Build InvoiceDTO from dialog state
                val invoiceItems = po.items.mapNotNull { poItem ->
                    val quantityStr = currentDialogState.itemQuantities[poItem.itemId] ?: return@mapNotNull null
                    val priceStr = currentDialogState.itemPrices[poItem.itemId] ?: return@mapNotNull null

                    val quantity = quantityStr.toIntOrNull() ?: return@mapNotNull null
                    val price = priceStr.toDoubleOrNull() ?: return@mapNotNull null

                    InvoiceItemDTO(
                        itemId = poItem.itemId,
                        quantity = quantity,
                        unitPrice = price
                    )
                }

                if (invoiceItems.isEmpty()) {
                    _dialogState.value = currentDialogState.copy(
                        isSubmitting = false,
                        error = "Please enter valid quantities and prices"
                    )
                    return@launch
                }

                val totalAmount = invoiceItems.sumOf { it.quantity * it.unitPrice }

                // Generate random invoice ID
                val randomId = Random.nextInt(10000000, 99999999).toString()

                // Create InvoiceDTO using shared type
                val invoice = InvoiceDTO(
                    id = "INV-$randomId",
                    poId = po.id,
                    vendorId = "VENDOR-001", // In real app, get from auth
                    totalAmount = totalAmount,
                    status = InvoiceStatus.PENDING,
                    items = invoiceItems
                )

                // Submit to server
                repository.submitInvoice(invoice)
                    .onSuccess { result ->
                        _validationResult.value = result
                        _dialogState.value = _dialogState.value.copy(isSubmitting = false)

                        // Update main UI state with validation result
                        val currentUiState = _uiState.value
                        if (currentUiState is VendorUiState.Success) {
                            _uiState.value = currentUiState.copy(
                                selectedPo = po,
                                validationResult = result
                            )
                        }
                    }
                    .onFailure { error ->
                        _dialogState.value = _dialogState.value.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to submit invoice"
                        )
                    }
            } catch (e: Exception) {
                _dialogState.value = _dialogState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    /**
     * Clear the validation result
     */
    fun clearValidationResult() {
        _validationResult.value = null
        val currentState = _uiState.value
        if (currentState is VendorUiState.Success) {
            _uiState.value = currentState.copy(validationResult = null)
        }
    }

    /**
     * Create a new Purchase Order
     * @param vendorName Vendor's name
     * @param vendorEmail Vendor's email address
     * @param items List of line items with name, quantity, price, and tax
     */
    fun createPurchaseOrder(
        vendorName: String,
        vendorEmail: String,
        items: List<CreatePOLineItem>
    ) {
        viewModelScope.launch {
            _createPOState.value = CreatePOState.Creating

            try {
                val request = CreatePurchaseOrderRequest(
                    vendorName = vendorName,
                    vendorEmail = vendorEmail,
                    items = items
                )

                repository.createPurchaseOrder(request)
                    .onSuccess { response ->
                        _createPOState.value = CreatePOState.Success(response)
                        // Reload POs to show the newly created one
                        loadPurchaseOrders()
                    }
                    .onFailure { error ->
                        _createPOState.value = CreatePOState.Error(
                            error.message ?: "Failed to create Purchase Order"
                        )
                    }
            } catch (e: Exception) {
                _createPOState.value = CreatePOState.Error(
                    e.message ?: "An error occurred while creating PO"
                )
            }
        }
    }

    /**
     * Reset PO creation state
     */
    fun resetCreatePOState() {
        _createPOState.value = CreatePOState.Idle
    }

    /**
     * Load a single Purchase Order by its ID (used by VendorSubmitScreen to show PO details).
     */
    fun loadPurchaseOrderById(poId: String) {
        viewModelScope.launch {
            repository.getPurchaseOrderById(poId)
                .onSuccess { po -> _selectedPurchaseOrder.value = po }
                .onFailure { /* silently fail — screen shows empty state */ }
        }
    }

    /**
     * Clears the cached selected purchase order (call on screen exit).
     */
    fun clearSelectedPurchaseOrder() {
        _selectedPurchaseOrder.value = null
    }

    /**
     * Submit a single-line-item invoice against [poId].
     * Used by VendorSubmitScreen where the user fills in qty and unit price for one item.
     */
    fun submitSimpleInvoice(
        invoiceNumber: String,
        poId: String,
        quantity: Int,
        unitPrice: Double
    ) {
        viewModelScope.launch {
            _invoiceSubmitState.value = InvoiceSubmitState.Submitting
            val invoice = InvoiceDTO(
                id          = invoiceNumber,
                poId        = poId,
                vendorId    = "VENDOR-001",
                totalAmount = quantity.toDouble() * unitPrice,
                status      = InvoiceStatus.PENDING,
                items       = listOf(
                    InvoiceItemDTO(
                        itemId    = "ITEM-001",
                        quantity  = quantity,
                        unitPrice = unitPrice
                    )
                )
            )
            repository.submitInvoice(invoice)
                .onSuccess { result ->
                    _invoiceSubmitState.value = InvoiceSubmitState.Success(result)
                }
                .onFailure { e ->
                    _invoiceSubmitState.value = InvoiceSubmitState.Error(
                        e.message ?: "Submission failed"
                    )
                }
        }
    }

    /**
     * Reset invoice submit state (call after handling the result).
     */
    fun resetInvoiceSubmitState() {
        _invoiceSubmitState.value = InvoiceSubmitState.Idle
    }
}

