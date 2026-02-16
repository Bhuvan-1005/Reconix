package com.example.reconix.repository

import com.example.reconix.network.KtorClient
import com.example.reconix.shared.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Invoice Repository - Handles all API calls using shared DTOs
 * Type-safe communication with the backend
 */
class InvoiceRepository {

    private val client = KtorClient.httpClient
    private val baseUrl = KtorClient.getBaseUrl()

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
            val response = client.get("$baseUrl${ApiRoutes.PurchaseOrder.LIST}")

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
            val response = client.get("$baseUrl${ApiRoutes.PurchaseOrder.byId(id)}")

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
            val response = client.get("$baseUrl${ApiRoutes.Invoice.LIST}")

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
            val response = client.get("$baseUrl${ApiRoutes.Grn.byPoId(poId)}")

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
}


