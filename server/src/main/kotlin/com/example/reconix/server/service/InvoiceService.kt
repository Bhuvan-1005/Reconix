package com.example.reconix.server.service

import com.example.reconix.server.database.*
import com.example.reconix.shared.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

/**
 * Invoice Service - Handles 3-Way Match Validation Logic
 *
 * Business Rules:
 * 1. Invoice Quantity must be <= Sum(GRN Quantities) for that item
 * 2. Invoice Unit Price must match PO Unit Price (±$0.05 tolerance)
 */
class InvoiceService {

    companion object {
        private const val PRICE_TOLERANCE = 0.05
    }

    /**
     * Validate an invoice using 3-Way Match logic
     * Compares Invoice against PO and GRNs
     */
    fun validateInvoice(invoice: InvoiceDTO): ValidationResult {
        return transaction {
            val timestamp = Clock.System.now().toString()

            // 1. Fetch the Purchase Order
            val po = PurchaseOrders.selectAll()
                .where { PurchaseOrders.id eq invoice.poId }
                .singleOrNull()
                ?: return@transaction ValidationResult(
                    status = InvoiceStatus.MANUAL_REVIEW,
                    message = "Purchase Order ${invoice.poId} not found - requires manual review",
                    timestamp = timestamp
                )

            // 2. Fetch PO Items
            val poItems = PurchaseOrderItems.selectAll()
                .where { PurchaseOrderItems.poId eq invoice.poId }
                .associate { row ->
                    row[PurchaseOrderItems.itemId] to Pair(
                        row[PurchaseOrderItems.quantity],
                        row[PurchaseOrderItems.unitPrice]
                    )
                }

            // 3. Fetch all GRNs for this PO and sum quantities per item
            val grnIds = Grns.selectAll()
                .where { Grns.poId eq invoice.poId }
                .map { it[Grns.id] }

            val grnQuantities = mutableMapOf<String, Int>()
            for (grnId in grnIds) {
                GrnItems.selectAll()
                    .where { GrnItems.grnId eq grnId }
                    .forEach { row ->
                        val itemId = row[GrnItems.itemId]
                        val qty = row[GrnItems.receivedQuantity]
                        grnQuantities[itemId] = grnQuantities.getOrDefault(itemId, 0) + qty
                    }
            }

            // 4. Validate each invoice item
            val validationErrors = mutableListOf<String>()

            for (invoiceItem in invoice.items) {
                val itemId = invoiceItem.itemId

                // Check if item exists in PO
                val poItemData = poItems[itemId]
                if (poItemData == null) {
                    validationErrors.add("Item $itemId not found in PO ${invoice.poId}")
                    continue
                }

                val (poQuantity, poUnitPrice) = poItemData

                // Rule 1: Invoice Quantity <= Sum(GRN Quantities)
                val totalReceivedQty = grnQuantities[itemId] ?: 0
                if (invoiceItem.quantity > totalReceivedQty) {
                    validationErrors.add(
                        "Item $itemId: Invoice qty (${invoiceItem.quantity}) exceeds received qty ($totalReceivedQty)"
                    )
                }

                // Rule 2: Invoice Unit Price must match PO Unit Price (±$0.05 tolerance)
                val priceDifference = kotlin.math.abs(invoiceItem.unitPrice - poUnitPrice)
                if (priceDifference > PRICE_TOLERANCE) {
                    validationErrors.add(
                        "Item $itemId: Invoice price ($${invoiceItem.unitPrice}) differs from PO price ($${poUnitPrice}) beyond tolerance"
                    )
                }
            }

            // 5. Determine final status
            val finalStatus = if (validationErrors.isEmpty()) {
                InvoiceStatus.MATCHED
            } else {
                InvoiceStatus.MISMATCH
            }

            // 6. Save or update invoice in database
            val existingInvoice = Invoices.selectAll()
                .where { Invoices.id eq invoice.id }
                .singleOrNull()

            if (existingInvoice == null) {
                // Insert new invoice
                Invoices.insert {
                    it[id] = invoice.id
                    it[poId] = invoice.poId
                    it[vendorId] = invoice.vendorId
                    it[totalAmount] = invoice.totalAmount
                    it[status] = finalStatus.name
                    it[createdAt] = timestamp
                    it[validatedAt] = timestamp
                }

                // Insert invoice items
                for (item in invoice.items) {
                    InvoiceItems.insert {
                        it[invoiceId] = invoice.id
                        it[itemId] = item.itemId
                        it[quantity] = item.quantity
                        it[unitPrice] = item.unitPrice
                    }
                }
            } else {
                // Update existing invoice status
                Invoices.update({ Invoices.id eq invoice.id }) {
                    it[status] = finalStatus.name
                    it[validatedAt] = timestamp
                }
            }

            // 7. Return validation result
            val message = if (validationErrors.isEmpty()) {
                "Invoice validated successfully - 3-Way Match PASSED"
            } else {
                "3-Way Match FAILED: ${validationErrors.joinToString("; ")}"
            }

            ValidationResult(
                status = finalStatus,
                message = message,
                timestamp = timestamp
            )
        }
    }

    /**
     * Get all Purchase Orders
     */
    fun getAllPurchaseOrders(): List<PurchaseOrderDTO> {
        return transaction {
            PurchaseOrders.selectAll().map { poRow ->
                val poId = poRow[PurchaseOrders.id]

                val items = PurchaseOrderItems.selectAll()
                    .where { PurchaseOrderItems.poId eq poId }
                    .map { itemRow ->
                        PurchaseOrderItemDTO(
                            itemId = itemRow[PurchaseOrderItems.itemId],
                            itemName = itemRow[PurchaseOrderItems.itemName],
                            quantity = itemRow[PurchaseOrderItems.quantity],
                            unitPrice = itemRow[PurchaseOrderItems.unitPrice]
                        )
                    }

                PurchaseOrderDTO(
                    id = poId,
                    vendorName = poRow[PurchaseOrders.vendorName],
                    totalAmount = poRow[PurchaseOrders.totalAmount],
                    items = items
                )
            }
        }
    }

    /**
     * Get Purchase Order by ID
     */
    fun getPurchaseOrderById(id: String): PurchaseOrderDTO? {
        return transaction {
            val poRow = PurchaseOrders.selectAll()
                .where { PurchaseOrders.id eq id }
                .singleOrNull() ?: return@transaction null

            val items = PurchaseOrderItems.selectAll()
                .where { PurchaseOrderItems.poId eq id }
                .map { itemRow ->
                    PurchaseOrderItemDTO(
                        itemId = itemRow[PurchaseOrderItems.itemId],
                        itemName = itemRow[PurchaseOrderItems.itemName],
                        quantity = itemRow[PurchaseOrderItems.quantity],
                        unitPrice = itemRow[PurchaseOrderItems.unitPrice]
                    )
                }

            PurchaseOrderDTO(
                id = id,
                vendorName = poRow[PurchaseOrders.vendorName],
                totalAmount = poRow[PurchaseOrders.totalAmount],
                items = items
            )
        }
    }

    /**
     * Get all Invoices
     */
    fun getAllInvoices(): List<InvoiceDTO> {
        return transaction {
            Invoices.selectAll().map { invRow ->
                val invoiceId = invRow[Invoices.id]

                val items = InvoiceItems.selectAll()
                    .where { InvoiceItems.invoiceId eq invoiceId }
                    .map { itemRow ->
                        InvoiceItemDTO(
                            itemId = itemRow[InvoiceItems.itemId],
                            quantity = itemRow[InvoiceItems.quantity],
                            unitPrice = itemRow[InvoiceItems.unitPrice]
                        )
                    }

                InvoiceDTO(
                    id = invoiceId,
                    poId = invRow[Invoices.poId],
                    vendorId = invRow[Invoices.vendorId],
                    totalAmount = invRow[Invoices.totalAmount],
                    status = InvoiceStatus.valueOf(invRow[Invoices.status]),
                    items = items
                )
            }
        }
    }

    /**
     * Get GRNs by PO ID
     */
    fun getGrnsByPoId(poId: String): List<GrnDTO> {
        return transaction {
            Grns.selectAll()
                .where { Grns.poId eq poId }
                .map { grnRow ->
                    val grnId = grnRow[Grns.id]

                    val items = GrnItems.selectAll()
                        .where { GrnItems.grnId eq grnId }
                        .map { itemRow ->
                            GrnItemDTO(
                                itemId = itemRow[GrnItems.itemId],
                                receivedQuantity = itemRow[GrnItems.receivedQuantity]
                            )
                        }

                    GrnDTO(
                        id = grnId,
                        poId = poId,
                        items = items,
                        receivedAt = grnRow[Grns.receivedAt]
                    )
                }
        }
    }
}

