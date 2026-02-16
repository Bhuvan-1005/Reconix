package com.example.reconix.server.plugins

import com.example.reconix.server.service.AuthService
import com.example.reconix.server.service.InvoiceService
import com.example.reconix.server.service.FinanceService
import com.example.reconix.server.service.PurchaseOrderService
import com.example.reconix.server.service.InvoiceIngestionService
import com.example.reconix.shared.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

/**
 * Configure API Routing
 * Uses shared DTOs for type-safe serialization/deserialization
 */
fun Application.configureRouting() {
    val authService = AuthService()
    val invoiceService = InvoiceService()
    val financeService = FinanceService()
    val poService = PurchaseOrderService()
    val ingestionService = InvoiceIngestionService()

    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }

        // ===== AUTHENTICATION ROUTES =====
        route("/auth") {
            /**
             * POST /auth/login
             * Authenticate user against database
             * Request Body: LoginRequest (username, password)
             * Response: LoginResponse (success, message, user, token)
             */
            post("/login") {
                val loginRequest = call.receive<LoginRequest>()

                // Validate input
                if (loginRequest.username.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        LoginResponse(
                            success = false,
                            message = "Username is required",
                            user = null,
                            token = null
                        )
                    )
                }

                if (loginRequest.password.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        LoginResponse(
                            success = false,
                            message = "Password is required",
                            user = null,
                            token = null
                        )
                    )
                }

                // Authenticate against database
                val loginResponse = authService.login(loginRequest)

                val statusCode = if (loginResponse.success) {
                    HttpStatusCode.OK
                } else {
                    HttpStatusCode.Unauthorized
                }

                call.respond(statusCode, loginResponse)
            }

            /**
             * POST /auth/logout
             * Logout user (placeholder for future session management)
             */
            post("/logout") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("success" to true, "message" to "Logged out successfully")
                )
            }
        }

        // ===== PURCHASE ORDER ROUTES =====
        route("/po") {
            /**
             * GET /po/list
             * Returns all Purchase Orders with their items
             */
            get("/list") {
                val purchaseOrders = invoiceService.getAllPurchaseOrders()
                call.respond(HttpStatusCode.OK, purchaseOrders)
            }

            /**
             * POST /po/create
             * Create a new Purchase Order with PDF generation and email
             * Request Body: CreatePurchaseOrderRequest
             * Response: CreatePurchaseOrderResponse
             */
            post("/create") {
                val request = call.receive<CreatePurchaseOrderRequest>()

                // Validate input
                if (request.vendorName.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "Vendor name is required")
                    )
                }

                if (request.items.isEmpty()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "At least one line item is required")
                    )
                }

                val response = poService.createPurchaseOrder(request)
                call.respond(HttpStatusCode.Created, response)
            }

            /**
             * GET /po/{id}
             * Returns a specific Purchase Order by ID
             */
            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "Missing PO ID")
                    )

                val purchaseOrder = invoiceService.getPurchaseOrderById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiError(404, "Purchase Order not found: $id")
                    )

                call.respond(HttpStatusCode.OK, purchaseOrder)
            }
        }

        // ===== INVOICE ROUTES =====
        route("/invoice") {
            /**
             * POST /invoice/submit
             * Submits an invoice for 3-Way Match validation
             * Request Body: InvoiceDTO (from shared module)
             * Response: ValidationResult (from shared module)
             */
            post("/submit") {
                val invoice = call.receive<InvoiceDTO>()

                // Validate required fields
                if (invoice.id.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "Invoice ID is required")
                    )
                }

                if (invoice.poId.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "PO ID is required")
                    )
                }

                if (invoice.items.isEmpty()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "Invoice must have at least one item")
                    )
                }

                // Run 3-Way Match validation
                val result = invoiceService.validateInvoice(invoice)

                val statusCode = when (result.status) {
                    InvoiceStatus.MATCHED -> HttpStatusCode.OK
                    InvoiceStatus.MISMATCH -> HttpStatusCode.UnprocessableEntity
                    InvoiceStatus.PENDING -> HttpStatusCode.Accepted
                    InvoiceStatus.MANUAL_REVIEW -> HttpStatusCode.Accepted
                }

                call.respond(statusCode, result)
            }

            /**
             * GET /invoice/list
             * Returns all invoices
             */
            get("/list") {
                val invoices = invoiceService.getAllInvoices()
                call.respond(HttpStatusCode.OK, invoices)
            }

            /**
             * POST /invoice/upload
             * Upload an invoice PDF file for OCR extraction and validation
             * Request: Multipart file upload
             * Response: InvoiceUploadResponse
             */
            post("/upload") {
                val multipart = call.receiveMultipart()
                var fileName = "unknown.pdf"
                var fileBytes: ByteArray? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName ?: "unknown.pdf"
                            val channel = part.provider()
                            fileBytes = channel.toByteArray()
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (fileBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "No file uploaded")
                    )
                }

                val response = ingestionService.handleFileUpload(
                    fileName = fileName,
                    fileBytes = fileBytes!!,
                    channel = "DIRECT_UPLOAD"
                )

                call.respond(
                    if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest,
                    response
                )
            }

            /**
             * GET /invoice/pending
             * Returns pending invoices for Finance Manager review
             */
            get("/pending") {
                val pendingInvoices = financeService.getPendingInvoices()
                call.respond(HttpStatusCode.OK, pendingInvoices)
            }

            /**
             * GET /invoice/{id}/match
             * Returns detailed 3-Way Match data for an invoice
             */
            get("/{id}/match") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "Missing Invoice ID")
                    )

                val matchData = financeService.getThreeWayMatch(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ApiError(404, "Invoice not found: $id")
                    )

                call.respond(HttpStatusCode.OK, matchData)
            }

            /**
             * POST /invoice/approve
             * Approve an invoice (Finance Manager action)
             */
            post("/approve") {
                val request = call.receive<InvoiceActionRequest>()
                // TODO: Get userId from auth session
                val userId = 1 // Placeholder

                val response = financeService.approveInvoice(
                    invoiceId = request.invoiceId,
                    userId = userId,
                    notes = request.notes
                )

                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * POST /invoice/reject
             * Reject an invoice (Finance Manager action)
             */
            post("/reject") {
                val request = call.receive<InvoiceActionRequest>()
                // TODO: Get userId from auth session
                val userId = 1 // Placeholder

                val response = financeService.rejectInvoice(
                    invoiceId = request.invoiceId,
                    userId = userId,
                    notes = request.notes
                )

                call.respond(HttpStatusCode.OK, response)
            }
        }

        // ===== DASHBOARD ROUTES (Finance Manager) =====
        route("/dashboard") {
            /**
             * GET /dashboard/metrics
             * Returns dashboard metrics for Finance Manager
             */
            get("/metrics") {
                val metrics = financeService.getDashboardMetrics()
                call.respond(HttpStatusCode.OK, metrics)
            }
        }

        // ===== GRN ROUTES =====
        route("/grn") {
            /**
             * GET /grn/po/{poId}
             * Returns all GRNs for a specific Purchase Order
             */
            get("/po/{poId}") {
                val poId = call.parameters["poId"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError(400, "Missing PO ID")
                    )

                val grns = invoiceService.getGrnsByPoId(poId)
                call.respond(HttpStatusCode.OK, grns)
            }
        }
    }
}


