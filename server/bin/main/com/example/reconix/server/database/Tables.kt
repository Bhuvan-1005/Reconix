package com.example.reconix.server.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * User Roles Enum
 */
enum class UserRole {
    VENDOR,
    FINANCE_MANAGER,
    ADMIN
}

/**
 * Users Table - For authentication and authorization
 */
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255).nullable()
    val role = varchar("role", 50) // VENDOR, FINANCE_MANAGER, ADMIN
    val vendorId = varchar("vendor_id", 50).nullable() // Linked vendor for VENDOR role
    val isActive = bool("is_active").default(true)
    val createdAt = varchar("created_at", 50)
    val lastLoginAt = varchar("last_login_at", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}

/**
 * Purchase Orders Table
 */
object PurchaseOrders : Table("purchase_orders") {
    val id = varchar("id", 50)
    val vendorName = varchar("vendor_name", 255)
    val vendorEmail = varchar("vendor_email", 255).nullable()
    val totalAmount = double("total_amount")
    val status = varchar("status", 20).default("OPEN") // OPEN, SENT, CLOSED
    val pdfPath = varchar("pdf_path", 500).nullable()
    val createdAt = varchar("created_at", 50)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Purchase Order Items Table
 */
object PurchaseOrderItems : Table("purchase_order_items") {
    val id = integer("id").autoIncrement()
    val poId = varchar("po_id", 50).references(PurchaseOrders.id, onDelete = ReferenceOption.CASCADE)
    val itemId = varchar("item_id", 50)
    val itemName = varchar("item_name", 255)
    val quantity = integer("quantity")
    val unitPrice = double("unit_price")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Goods Receipt Notes Table
 */
object Grns : Table("grns") {
    val id = varchar("id", 50)
    val poId = varchar("po_id", 50).references(PurchaseOrders.id, onDelete = ReferenceOption.CASCADE)
    val receivedAt = varchar("received_at", 50)

    override val primaryKey = PrimaryKey(id)
}

/**
 * GRN Items Table
 */
object GrnItems : Table("grn_items") {
    val id = integer("id").autoIncrement()
    val grnId = varchar("grn_id", 50).references(Grns.id, onDelete = ReferenceOption.CASCADE)
    val itemId = varchar("item_id", 50)
    val receivedQuantity = integer("received_quantity")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Invoices Table
 */
object Invoices : Table("invoices") {
    val id = varchar("id", 50)
    val poId = varchar("po_id", 50).references(PurchaseOrders.id, onDelete = ReferenceOption.CASCADE)
    val vendorId = varchar("vendor_id", 50)
    val totalAmount = double("total_amount")
    val status = varchar("status", 20) // PENDING, MATCHED, MISMATCH
    val createdAt = varchar("created_at", 50)
    val validatedAt = varchar("validated_at", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}

/**
 * Invoice Items Table
 */
object InvoiceItems : Table("invoice_items") {
    val id = integer("id").autoIncrement()
    val invoiceId = varchar("invoice_id", 50).references(Invoices.id, onDelete = ReferenceOption.CASCADE)
    val itemId = varchar("item_id", 50)
    val quantity = integer("quantity")
    val unitPrice = double("unit_price")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Invoice Actions Table - Audit trail for invoice lifecycle
 */
object InvoiceActions : Table("invoice_actions") {
    val id = integer("id").autoIncrement()
    val invoiceId = varchar("invoice_id", 50).references(Invoices.id, onDelete = ReferenceOption.CASCADE)
    val actionType = varchar("action_type", 50) // SUBMITTED, VALIDATED, APPROVED, REJECTED, PAID
    val performedBy = integer("performed_by").references(Users.id)
    val notes = text("notes").nullable()
    val timestamp = varchar("timestamp", 50)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Validation Logs Table - Detailed 3-way match results
 */
object ValidationLogs : Table("validation_logs") {
    val id = integer("id").autoIncrement()
    val invoiceId = varchar("invoice_id", 50).references(Invoices.id, onDelete = ReferenceOption.CASCADE)
    val itemId = varchar("item_id", 50)
    val poQuantity = integer("po_quantity")
    val grnQuantity = integer("grn_quantity")
    val invoiceQuantity = integer("invoice_quantity")
    val poPrice = double("po_price")
    val invoicePrice = double("invoice_price")
    val priceDifference = double("price_difference")
    val quantityMatch = bool("quantity_match")
    val priceMatch = bool("price_match")
    val overallMatch = bool("overall_match")
    val timestamp = varchar("timestamp", 50)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Invoice Files Table - Stores uploaded invoice file metadata
 */
object InvoiceFiles : Table("invoice_files") {
    val id = integer("id").autoIncrement()
    val invoiceId = varchar("invoice_id", 50).references(Invoices.id, onDelete = ReferenceOption.CASCADE).nullable()
    val originalFilename = varchar("original_filename", 500)
    val storagePath = varchar("storage_path", 500)
    val fileType = varchar("file_type", 20) // PDF, IMAGE
    val uploadChannel = varchar("upload_channel", 20) // DIRECT_UPLOAD, EMAIL
    val uploadedAt = varchar("uploaded_at", 50)
    val ocrProcessed = bool("ocr_processed").default(false)

    override val primaryKey = PrimaryKey(id)
}
