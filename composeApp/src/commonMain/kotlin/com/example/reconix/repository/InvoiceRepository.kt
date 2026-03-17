package com.example.reconix.repository

import com.example.reconix.auth.AuthManager
import com.example.reconix.network.KtorClient
import com.example.reconix.shared.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.*

/**
 * Invoice Repository - Handles all API calls using shared DTOs
 * Type-safe communication with the backend
 */
class InvoiceRepository {

    private val client = KtorClient.httpClient
    private val baseUrl = KtorClient.getBaseUrl()

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Append the JWT access token as an Authorization header when available.
     * Called inside every request builder block that needs authentication.
     */
    private fun HttpRequestBuilder.addAuthHeader() {
        AuthManager.token?.let {
            headers.append(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Authenticate user against backend
     * @param username User's username
     * @param password User's password
     * @return LoginResponse from shared module
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val loginRequest = LoginRequest(username, password)
            val response = client.post("$baseUrl${ApiRoutes.Auth.LOGIN}") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }

            val loginResponse: LoginResponse = response.body()
            Result.success(loginResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all Purchase Orders from the server
     * @return List of PurchaseOrderDTO from shared module
     */
    suspend fun getPurchaseOrders(): Result<List<PurchaseOrderDTO>> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.PurchaseOrder.LIST}") { addAuthHeader() }

            if (response.status.isSuccess()) {
                val purchaseOrders: List<PurchaseOrderDTO> = response.body()
                Result.success(purchaseOrders)
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch a specific Purchase Order by ID
     * @param id Purchase Order ID
     * @return PurchaseOrderDTO from shared module
     */
    suspend fun getPurchaseOrderById(id: String): Result<PurchaseOrderDTO> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.PurchaseOrder.byId(id)}") { addAuthHeader() }

            if (response.status.isSuccess()) {
                val purchaseOrder: PurchaseOrderDTO = response.body()
                Result.success(purchaseOrder)
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit an invoice for 3-Way Match validation
     * @param invoice InvoiceDTO from shared module
     * @return ValidationResult from shared module
     */
    suspend fun submitInvoice(invoice: InvoiceDTO): Result<ValidationResult> {
        return try {
            val response = client.post("$baseUrl${ApiRoutes.Invoice.SUBMIT}") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(invoice)
            }

            val validationResult: ValidationResult = response.body()
            Result.success(validationResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all invoices
     * @return List of InvoiceDTO from shared module
     */
    suspend fun getInvoices(): Result<List<InvoiceDTO>> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.Invoice.LIST}") { addAuthHeader() }

            if (response.status.isSuccess()) {
                val invoices: List<InvoiceDTO> = response.body()
                Result.success(invoices)
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch GRNs for a specific Purchase Order
     * @param poId Purchase Order ID
     * @return List of GrnDTO from shared module
     */
    suspend fun getGrnsByPoId(poId: String): Result<List<GrnDTO>> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.Grn.byPoId(poId)}") { addAuthHeader() }

            if (response.status.isSuccess()) {
                val grns: List<GrnDTO> = response.body()
                Result.success(grns)
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new Purchase Order
     * @param request CreatePurchaseOrderRequest from shared module
     * @return CreatePurchaseOrderResponse from shared module
     */
    suspend fun createPurchaseOrder(request: CreatePurchaseOrderRequest): Result<CreatePurchaseOrderResponse> {
        return try {
            val response = client.post("$baseUrl${ApiRoutes.PurchaseOrder.CREATE}") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val createResponse: CreatePurchaseOrderResponse = response.body()
                Result.success(createResponse)
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch finance dashboard metrics (pending count, match rate, etc.)
     * @return DashboardMetricsDTO from shared module
     */
    suspend fun getDashboardMetrics(): Result<DashboardMetricsDTO> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.Dashboard.METRICS}") { addAuthHeader() }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch recent activity feed for the finance dashboard.
     * @return List of RecentActivityDTO
     */
    suspend fun getRecentActivity(): Result<List<RecentActivityDTO>> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.Dashboard.ACTIVITY}") { addAuthHeader() }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch invoices pending approval (Finance Manager view).
     * @return List of InvoiceListItemDTO
     */
    suspend fun getPendingInvoices(): Result<List<InvoiceListItemDTO>> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.Invoice.PENDING}") { addAuthHeader() }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Approve an invoice.
     * @param invoiceId Target invoice ID
     * @param notes     Optional approval notes
     * @return InvoiceActionResponse
     */
    suspend fun approveInvoice(invoiceId: String, notes: String? = null): Result<InvoiceActionResponse> {
        return try {
            val response = client.post("$baseUrl${ApiRoutes.Invoice.APPROVE}") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(InvoiceActionRequest(invoiceId = invoiceId, action = "APPROVE", notes = notes))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Approve failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reject an invoice.
     * @param invoiceId Target invoice ID
     * @param notes     Optional rejection reason
     * @return InvoiceActionResponse
     */
    suspend fun rejectInvoice(invoiceId: String, notes: String? = null): Result<InvoiceActionResponse> {
        return try {
            val response = client.post("$baseUrl${ApiRoutes.Invoice.REJECT}") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(InvoiceActionRequest(invoiceId = invoiceId, action = "REJECT", notes = notes))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Reject failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch the full 3-way match report for a single invoice.
     * @param invoiceId Invoice ID
     * @return ThreeWayMatchDTO
     */
    suspend fun getThreeWayMatch(invoiceId: String): Result<ThreeWayMatchDTO> {
        return try {
            val response = client.get("$baseUrl${ApiRoutes.Invoice.threeWayMatch(invoiceId)}") { addAuthHeader() }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload an invoice file (PDF or image) for OCR extraction and automatic 3-way match.
     * @param fileBytes Raw bytes of the file.
     * @param fileName  Original file name (used as the multipart filename).
     * @return InvoiceUploadResponse with OCR data and validation result.
     */
    suspend fun uploadInvoice(fileBytes: ByteArray, fileName: String): Result<InvoiceUploadResponse> {
        return try {
            val response = client.post("$baseUrl${ApiRoutes.Invoice.UPLOAD}") {
                addAuthHeader()
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = fileBytes,
                                headers = Headers.build {
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "form-data; name=\"file\"; filename=\"$fileName\""
                                    )
                                    val mimeType = when {
                                        fileName.endsWith(".pdf", ignoreCase = true)  -> "application/pdf"
                                        fileName.endsWith(".png", ignoreCase = true)  -> "image/png"
                                        fileName.endsWith(".jpg", ignoreCase = true) ||
                                        fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                        else -> "application/octet-stream"
                                    }
                                    append(HttpHeaders.ContentType, mimeType)
                                }
                            )
                        }
                    )
                )
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload an invoice PDF and extract structured data using Gemini 1.5 Flash AI.
     * Returns the same InvoiceUploadResponse as uploadInvoice() but with additional
     * fields: invoiceNumber, date, taxAmount extracted by the LLM.
     */
    suspend fun geminiExtractInvoice(fileBytes: ByteArray, fileName: String): Result<InvoiceUploadResponse> {
        return try {
            val response = client.post("$baseUrl${ApiRoutes.Invoice.GEMINI_EXTRACT}") {
                addAuthHeader()
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = fileBytes,
                                headers = Headers.build {
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "form-data; name=\"file\"; filename=\"$fileName\""
                                    )
                                    val mimeType = when {
                                        fileName.endsWith(".pdf",  ignoreCase = true) -> "application/pdf"
                                        fileName.endsWith(".png",  ignoreCase = true) -> "image/png"
                                        fileName.endsWith(".jpg",  ignoreCase = true) ||
                                        fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                        else -> "application/octet-stream"
                                    }
                                    append(HttpHeaders.ContentType, mimeType)
                                }
                            )
                        }
                    )
                )
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val error: ApiError = response.body()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Admin endpoints ────────────────────────────────────────────────────────

    /** List all users. Admin only. */
    suspend fun adminGetUsers(): Result<List<UserListItemDTO>> = runCatching {
        val response = client.get("$baseUrl${ApiRoutes.Admin.USERS}") { addAuthHeader() }
        if (response.status.isSuccess()) response.body()
        else { val e: ApiError = response.body(); throw Exception(e.message) }
    }

    /** Create a new user. Admin only. */
    suspend fun adminCreateUser(req: CreateUserRequest): Result<CreateUserResponse> = runCatching {
        val response = client.post("$baseUrl${ApiRoutes.Admin.USERS}") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (response.status.isSuccess()) response.body()
        else { val e: ApiError = response.body(); throw Exception(e.message) }
    }

    /** Deactivate a user. Admin only. */
    suspend fun adminDeactivateUser(userId: Int): Result<Unit> = runCatching {
        val response = client.put("$baseUrl${ApiRoutes.Admin.deactivateUser(userId)}") { addAuthHeader() }
        if (!response.status.isSuccess()) {
            val e: ApiError = response.body(); throw Exception(e.message)
        }
    }

    /** Fetch full audit log. Admin only. */
    suspend fun adminGetAuditLog(): Result<List<AuditLogItemDTO>> = runCatching {
        val response = client.get("$baseUrl${ApiRoutes.Admin.AUDIT}") { addAuthHeader() }
        if (response.status.isSuccess()) response.body()
        else { val e: ApiError = response.body(); throw Exception(e.message) }
    }

    /** Get current tolerance config. Admin only. */
    suspend fun adminGetTolerance(): Result<ToleranceConfigDTO> = runCatching {
        val response = client.get("$baseUrl${ApiRoutes.Admin.TOLERANCE}") { addAuthHeader() }
        if (response.status.isSuccess()) response.body()
        else { val e: ApiError = response.body(); throw Exception(e.message) }
    }

    /** Update tolerance config. Admin only. */
    suspend fun adminUpdateTolerance(config: ToleranceConfigDTO): Result<ToleranceConfigDTO> = runCatching {
        val response = client.put("$baseUrl${ApiRoutes.Admin.TOLERANCE}") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(config)
        }
        if (response.status.isSuccess()) response.body<ToleranceConfigDTO>()
        else { val e: ApiError = response.body(); throw Exception(e.message) }
    }

    /** Fetch invoices filtered by RBAC role (vendor only sees own, finance sees all). */
    suspend fun getInvoicesByRole(): Result<List<InvoiceListItemDTO>> = runCatching {
        val response = client.get("$baseUrl${ApiRoutes.Invoice.LIST}") { addAuthHeader() }
        if (response.status.isSuccess()) response.body()
        else { val e: ApiError = response.body(); throw Exception(e.message) }
    }
}
