package com.example.reconix.shared

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Invoice Status Enum - Used across Frontend and Backend
 * Changing this will break both sides until fixed (Type Safety!)
 */
@Serializable
enum class InvoiceStatus {
    PENDING,
    MATCHED,
    MISMATCH,
    MANUAL_REVIEW
}

/**
 * Invoice Item DTO - Line item for invoices
 * @property itemId Unique identifier for the item
 * @property quantity Number of units
 * @property unitPrice Price per unit
 */
@Serializable
data class InvoiceItemDTO(
    val itemId: String,
    val quantity: Int,
    val unitPrice: Double
)

/**
 * Purchase Order Item DTO - Line item for POs
 */
@Serializable
data class PurchaseOrderItemDTO(
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double
)

/**
 * Purchase Order DTO - Represents a Purchase Order
 * @property id Unique PO identifier
 * @property vendorName Name of the vendor
 * @property vendorEmail Vendor's email address for PO notifications
 * @property totalAmount Total PO amount
 * @property items List of line items
 */
@Serializable
data class PurchaseOrderDTO(
    val id: String,
    val vendorName: String,
    val vendorEmail: String = "",
    val totalAmount: Double,
    val items: List<PurchaseOrderItemDTO>
)

/**
 * Invoice DTO - Used to submit and receive invoice data
 * @property id Unique invoice identifier
 * @property poId Associated Purchase Order ID
 * @property vendorId Vendor identifier
 * @property totalAmount Total invoice amount
 * @property status Current validation status
 * @property items List of invoice line items
 */
@Serializable
data class InvoiceDTO(
    val id: String,
    val poId: String,
    val vendorId: String,
    val totalAmount: Double,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val items: List<InvoiceItemDTO>
)

/**
 * Goods Receipt Note Item DTO
 */
@Serializable
data class GrnItemDTO(
    val itemId: String,
    val receivedQuantity: Int
)

/**
 * Goods Receipt Note DTO - Records goods received against a PO
 */
@Serializable
data class GrnDTO(
    val id: String,
    val poId: String,
    val items: List<GrnItemDTO>,
    val receivedAt: String // ISO 8601 timestamp
)

/**
 * Validation Result DTO - Result of invoice validation
 * @property status The validation outcome
 * @property message Detailed message about the validation
 * @property timestamp When the validation occurred (ISO 8601)
 */
@Serializable
data class ValidationResult(
    val status: InvoiceStatus,
    val message: String,
    val timestamp: String,
    val details: List<ValidationDetailDTO>? = null // Detailed line-item validation
)

/**
 * Validation Detail DTO - Line item match results
 */
@Serializable
data class ValidationDetailDTO(
    val itemId: String,
    val itemName: String,
    val poQuantity: Int,
    val grnQuantity: Int,
    val invoiceQuantity: Int,
    val poPrice: Double,
    val invoicePrice: Double,
    val priceDifference: Double,
    val quantityMatch: Boolean,
    val priceMatch: Boolean,
    val overallMatch: Boolean
)

/**
 * 3-Way Match Result DTO - Complete match visualization data
 */
@Serializable
data class ThreeWayMatchDTO(
    val invoiceId: String,
    val poId: String,
    val vendorName: String,
    val invoiceDate: String,
    val totalAmount: Double,
    val status: InvoiceStatus,
    val matchDetails: List<ValidationDetailDTO>,
    val overallMatchPercentage: Double, // 0.0 to 100.0
    val createdAt: String,
    val validatedAt: String?
)

/**
 * Finance Dashboard Metrics DTO
 */
@Serializable
data class DashboardMetricsDTO(
    val totalPendingInvoices: Int,
    val totalPendingAmount: Double,
    val matchedInvoicesCount: Int,
    val mismatchedInvoicesCount: Int,
    val matchRate: Double, // Percentage
    val totalPayableAmount: Double,
    val averageProcessingTime: String, // e.g., "2.5 hours"
    val recentActivity: List<RecentActivityDTO>
)

/**
 * Recent Activity DTO - For activity feed
 */
@Serializable
data class RecentActivityDTO(
    val id: Int,
    val invoiceId: String,
    val vendorName: String,
    val actionType: String, // SUBMITTED, APPROVED, REJECTED, VALIDATED
    val performedBy: String,
    val timestamp: String,
    val amount: Double
)

/**
 * Invoice Action Request DTO - For approve/reject actions
 */
@Serializable
data class InvoiceActionRequest(
    val invoiceId: String,
    val action: String, // APPROVE, REJECT
    val notes: String? = null
)

/**
 * Invoice Action Response DTO
 */
@Serializable
data class InvoiceActionResponse(
    val success: Boolean,
    val message: String,
    val invoiceId: String,
    val newStatus: String
)

/**
 * Invoice List Item DTO - For list views
 */
@Serializable
data class InvoiceListItemDTO(
    val id: String,
    val poId: String,
    val vendorName: String,
    val totalAmount: Double,
    val status: InvoiceStatus,
    val createdAt: String,
    val itemCount: Int,
    val matchPercentage: Double? = null
)

/**
 * API Error Response
 */
// ===== PO Creation DTOs =====

/**
 * Request to create a new Purchase Order
 */
@Serializable
data class CreatePOLineItem(
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double = 0.0
)

@Serializable
data class CreatePurchaseOrderRequest(
    val vendorName: String,
    val vendorEmail: String = "",
    val items: List<CreatePOLineItem>
)

@Serializable
data class CreatePurchaseOrderResponse(
    val success: Boolean,
    val poId: String,
    val status: String,
    val message: String,
    val totalAmount: Double,
    val pdfGenerated: Boolean = false,
    val emailSent: Boolean = false
)

// ===== Invoice Upload / OCR DTOs =====

/**
 * Data extracted from an invoice by OCR
 */
@Serializable
data class OcrExtractedData(
    val detectedPoNumber: String?,
    val vendorName: String?,
    val lineItems: List<OcrLineItem>,
    val totalAmount: Double?,
    val confidenceScore: Double // 0.0 to 100.0
)

@Serializable
data class OcrLineItem(
    val description: String,
    val quantity: Int?,
    val unitPrice: Double?,
    val amount: Double?
)

@Serializable
data class InvoiceUploadResponse(
    val success: Boolean,
    val invoiceId: String?,
    val message: String,
    val extractedData: OcrExtractedData? = null,
    val validationResult: ValidationResult? = null
)

@Serializable
data class ApiError(
    val code: Int,
    val message: String
)

/**
 * Login Request DTO
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Login Response DTO
 */
@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserDTO? = null,
    val token: String? = null
)

/**
 * User DTO
 */
@Serializable
data class UserDTO(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String?,
    val role: String,
    val vendorId: String? = null // For VENDOR role
)

/**
 * API Routes - Shared route definitions for type-safe API calls
 * Both Frontend and Backend reference these constants
 */
object ApiRoutes {
    const val BASE_URL = "http://10.116.40.38:8081" // Laptop IP address
    const val BASE_URL_LOCALHOST = "http://localhost:8081"

    object Auth {
        const val LOGIN = "/auth/login"
        const val LOGOUT = "/auth/logout"
    }

    object Invoice {
        const val SUBMIT = "/invoice/submit"
        const val UPLOAD = "/invoice/upload"
        const val LIST = "/invoice/list"
        const val PENDING = "/invoice/pending" // Finance Manager - Pending invoices
        const val APPROVE = "/invoice/approve"
        const val REJECT = "/invoice/reject"
        fun byId(id: String) = "/invoice/$id"
        fun threeWayMatch(id: String) = "/invoice/$id/match"
        fun uploadStatus(id: String) = "/invoice/upload-status/$id"
    }

    object PurchaseOrder {
        const val LIST = "/po/list"
        const val CREATE = "/po/create"
        fun byId(id: String) = "/po/$id"
    }

    object Grn {
        const val LIST = "/grn/list"
        fun byPoId(poId: String) = "/grn/po/$poId"
    }

    object Dashboard {
        const val METRICS = "/dashboard/metrics" // Finance Manager metrics
        const val ACTIVITY = "/dashboard/activity" // Recent activity feed
    }

    object Analytics {
        const val MATCH_RATE = "/analytics/match-rate"
        const val VENDOR_PERFORMANCE = "/analytics/vendor-performance"
    }
}



