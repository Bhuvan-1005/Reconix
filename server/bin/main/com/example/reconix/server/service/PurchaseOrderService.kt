package com.example.reconix.server.service

import com.example.reconix.server.database.*
import com.example.reconix.shared.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Purchase Order Service - Handles PO creation, PDF generation, and email sending
 */
class PurchaseOrderService {

    private val pdfService = PdfGeneratorService()
    private val emailService = EmailService()

    /**
     * Create a new Purchase Order
     * 1. Insert PO + items into database
     * 2. Generate PO PDF
     * 3. Email PDF to vendor (if configured)
     * 4. Return response
     */
    fun createPurchaseOrder(request: CreatePurchaseOrderRequest): CreatePurchaseOrderResponse {
        return transaction {
            val now = Clock.System.now().toString()

            // Generate PO ID: PO-XXX (sequential)
            val lastPo = PurchaseOrders.selectAll()
                .orderBy(PurchaseOrders.id, SortOrder.DESC)
                .limit(1)
                .map { it[PurchaseOrders.id] }
                .firstOrNull()

            val nextNum = if (lastPo != null) {
                val num = lastPo.removePrefix("PO-").toIntOrNull() ?: 0
                num + 1
            } else {
                1
            }
            val poId = "PO-${String.format("%03d", nextNum)}"

            // Calculate total amount
            val totalAmount = request.items.sumOf { item ->
                val subtotal = item.quantity * item.unitPrice
                subtotal + (subtotal * item.taxRate / 100.0)
            }

            // Insert Purchase Order
            PurchaseOrders.insert {
                it[id] = poId
                it[vendorName] = request.vendorName
                it[vendorEmail] = request.vendorEmail.ifBlank { null }
                it[PurchaseOrders.totalAmount] = totalAmount
                it[status] = "OPEN"
                it[createdAt] = now
            }

            // Insert line items
            request.items.forEachIndexed { index, item ->
                val itemId = "ITEM-${poId}-${String.format("%02d", index + 1)}"
                PurchaseOrderItems.insert {
                    it[PurchaseOrderItems.poId] = poId
                    it[PurchaseOrderItems.itemId] = itemId
                    it[itemName] = item.itemName
                    it[quantity] = item.quantity
                    it[unitPrice] = item.unitPrice
                }
            }

            // Generate PDF
            var pdfGenerated = false
            var pdfPath: String? = null
            try {
                pdfPath = pdfService.generatePoPdf(
                    poId = poId,
                    vendorName = request.vendorName,
                    vendorEmail = request.vendorEmail,
                    items = request.items.map { PdfLineItem(it.itemName, it.quantity, it.unitPrice, it.taxRate) },
                    totalAmount = totalAmount
                )
                pdfGenerated = true

                // Update PO with PDF path
                PurchaseOrders.update({ PurchaseOrders.id eq poId }) {
                    it[PurchaseOrders.pdfPath] = pdfPath
                }
            } catch (e: Exception) {
                println("⚠️ PDF generation failed: ${e.message}")
            }

            // Send email to vendor
            var emailSent = false
            if (request.vendorEmail.isNotBlank() && pdfPath != null) {
                emailSent = emailService.sendPoToVendor(request.vendorEmail, poId, pdfPath)
                if (emailSent) {
                    PurchaseOrders.update({ PurchaseOrders.id eq poId }) {
                        it[status] = "SENT"
                    }
                }
            }

            CreatePurchaseOrderResponse(
                success = true,
                poId = poId,
                status = if (emailSent) "SENT" else "OPEN",
                message = buildString {
                    append("Purchase Order $poId created successfully")
                    if (pdfGenerated) append(". PDF generated")
                    if (emailSent) append(". Email sent to ${request.vendorEmail}")
                    else if (request.vendorEmail.isNotBlank()) append(". Email not sent (SMTP not configured)")
                },
                totalAmount = totalAmount,
                pdfGenerated = pdfGenerated,
                emailSent = emailSent
            )
        }
    }
}
