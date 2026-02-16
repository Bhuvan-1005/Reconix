package com.example.reconix.server.service

import com.example.reconix.server.database.*
import com.example.reconix.shared.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Enhanced Finance Service for Dashboard and Analytics
 */
class FinanceService {

    /**
     * Get Dashboard Metrics for Finance Manager
     */
    fun getDashboardMetrics(): DashboardMetricsDTO {
        return transaction {
            // Get all invoices with their status
            val allInvoices = Invoices.selectAll().toList()

            val pendingInvoices = allInvoices.filter {
                it[Invoices.status] == InvoiceStatus.PENDING.name
            }
            val matchedInvoices = allInvoices.filter {
                it[Invoices.status] == InvoiceStatus.MATCHED.name
            }
            val mismatchedInvoices = allInvoices.filter {
                it[Invoices.status] == InvoiceStatus.MISMATCH.name
            }

            val totalPendingAmount = pendingInvoices.sumOf { it[Invoices.totalAmount] }
            val totalPayableAmount = matchedInvoices.sumOf { it[Invoices.totalAmount] }

            val totalValidated = matchedInvoices.size + mismatchedInvoices.size
            val matchRate = if (totalValidated > 0) {
                (matchedInvoices.size.toDouble() / totalValidated) * 100
            } else {
                0.0
            }

            // Get recent activity
            val recentActivity = InvoiceActions
                .innerJoin(Invoices)
                .innerJoin(Users)
                .selectAll()
                .orderBy(InvoiceActions.timestamp, SortOrder.DESC)
                .limit(10)
                .map { row ->
                    RecentActivityDTO(
                        id = row[InvoiceActions.id],
                        invoiceId = row[InvoiceActions.invoiceId],
                        vendorName = row[Invoices.vendorId], // Should join with vendor table
                        actionType = row[InvoiceActions.actionType],
                        performedBy = row[Users.fullName],
                        timestamp = row[InvoiceActions.timestamp],
                        amount = row[Invoices.totalAmount]
                    )
                }

            DashboardMetricsDTO(
                totalPendingInvoices = pendingInvoices.size,
                totalPendingAmount = totalPendingAmount,
                matchedInvoicesCount = matchedInvoices.size,
                mismatchedInvoicesCount = mismatchedInvoices.size,
                matchRate = matchRate,
                totalPayableAmount = totalPayableAmount,
                averageProcessingTime = "2.5 hours", // TODO: Calculate from actual data
                recentActivity = recentActivity
            )
        }
    }

    /**
     * Get Pending Invoices for Finance Manager Review
     */
    fun getPendingInvoices(): List<InvoiceListItemDTO> {
        return transaction {
            Invoices.selectAll()
                .where { Invoices.status eq InvoiceStatus.PENDING.name }
                .orderBy(Invoices.createdAt, SortOrder.DESC)
                .map { row ->
                    val invoiceId = row[Invoices.id]
                    val itemCount = InvoiceItems.selectAll()
                        .where { InvoiceItems.invoiceId eq invoiceId }
                        .count()
                        .toInt()

                    InvoiceListItemDTO(
                        id = invoiceId,
                        poId = row[Invoices.poId],
                        vendorName = row[Invoices.vendorId], // Should fetch from vendor table
                        totalAmount = row[Invoices.totalAmount],
                        status = InvoiceStatus.valueOf(row[Invoices.status]),
                        createdAt = row[Invoices.createdAt],
                        itemCount = itemCount,
                        matchPercentage = null
                    )
                }
        }
    }

    /**
     * Get Detailed 3-Way Match Data for an Invoice
     */
    fun getThreeWayMatch(invoiceId: String): ThreeWayMatchDTO? {
        return transaction {
            val invoiceRow = Invoices.selectAll()
                .where { Invoices.id eq invoiceId }
                .singleOrNull() ?: return@transaction null

            val poId = invoiceRow[Invoices.poId]

            // Get PO items
            val poItems = PurchaseOrderItems.selectAll()
                .where { PurchaseOrderItems.poId eq poId }
                .associate { row ->
                    row[PurchaseOrderItems.itemId] to Triple(
                        row[PurchaseOrderItems.itemName],
                        row[PurchaseOrderItems.quantity],
                        row[PurchaseOrderItems.unitPrice]
                    )
                }

            // Get GRN quantities
            val grnIds = Grns.selectAll()
                .where { Grns.poId eq poId }
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

            // Get invoice items
            val invoiceItems = InvoiceItems.selectAll()
                .where { InvoiceItems.invoiceId eq invoiceId }
                .toList()

            // Build detailed match results
            val matchDetails = invoiceItems.map { invRow ->
                val itemId = invRow[InvoiceItems.itemId]
                val (itemName, poQty, poPrice) = poItems[itemId]
                    ?: Triple("Unknown Item", 0, 0.0)
                val grnQty = grnQuantities[itemId] ?: 0
                val invQty = invRow[InvoiceItems.quantity]
                val invPrice = invRow[InvoiceItems.unitPrice]

                val quantityMatch = invQty <= grnQty
                val priceDiff = kotlin.math.abs(invPrice - poPrice)
                val priceMatch = priceDiff <= 0.05

                ValidationDetailDTO(
                    itemId = itemId,
                    itemName = itemName,
                    poQuantity = poQty,
                    grnQuantity = grnQty,
                    invoiceQuantity = invQty,
                    poPrice = poPrice,
                    invoicePrice = invPrice,
                    priceDifference = priceDiff,
                    quantityMatch = quantityMatch,
                    priceMatch = priceMatch,
                    overallMatch = quantityMatch && priceMatch
                )
            }

            val matchCount = matchDetails.count { it.overallMatch }
            val overallMatchPercentage = if (matchDetails.isNotEmpty()) {
                (matchCount.toDouble() / matchDetails.size) * 100
            } else {
                0.0
            }

            ThreeWayMatchDTO(
                invoiceId = invoiceId,
                poId = poId,
                vendorName = invoiceRow[Invoices.vendorId],
                invoiceDate = invoiceRow[Invoices.createdAt],
                totalAmount = invoiceRow[Invoices.totalAmount],
                status = InvoiceStatus.valueOf(invoiceRow[Invoices.status]),
                matchDetails = matchDetails,
                overallMatchPercentage = overallMatchPercentage,
                createdAt = invoiceRow[Invoices.createdAt],
                validatedAt = invoiceRow[Invoices.validatedAt]
            )
        }
    }

    /**
     * Approve an Invoice
     */
    fun approveInvoice(invoiceId: String, userId: Int, notes: String?): InvoiceActionResponse {
        return transaction {
            // Update invoice status
            val updated = Invoices.update({ Invoices.id eq invoiceId }) {
                it[status] = InvoiceStatus.MATCHED.name
                it[validatedAt] = Clock.System.now().toString()
            }

            if (updated == 0) {
                return@transaction InvoiceActionResponse(
                    success = false,
                    message = "Invoice not found",
                    invoiceId = invoiceId,
                    newStatus = InvoiceStatus.PENDING.name
                )
            }

            // Log the action
            InvoiceActions.insert {
                it[InvoiceActions.invoiceId] = invoiceId
                it[actionType] = "APPROVED"
                it[performedBy] = userId
                it[InvoiceActions.notes] = notes
                it[timestamp] = Clock.System.now().toString()
            }

            InvoiceActionResponse(
                success = true,
                message = "Invoice approved successfully",
                invoiceId = invoiceId,
                newStatus = InvoiceStatus.MATCHED.name
            )
        }
    }

    /**
     * Reject an Invoice
     */
    fun rejectInvoice(invoiceId: String, userId: Int, notes: String?): InvoiceActionResponse {
        return transaction {
            // Update invoice status
            val updated = Invoices.update({ Invoices.id eq invoiceId }) {
                it[status] = InvoiceStatus.MISMATCH.name
                it[validatedAt] = Clock.System.now().toString()
            }

            if (updated == 0) {
                return@transaction InvoiceActionResponse(
                    success = false,
                    message = "Invoice not found",
                    invoiceId = invoiceId,
                    newStatus = InvoiceStatus.PENDING.name
                )
            }

            // Log the action
            InvoiceActions.insert {
                it[InvoiceActions.invoiceId] = invoiceId
                it[actionType] = "REJECTED"
                it[performedBy] = userId
                it[InvoiceActions.notes] = notes
                it[timestamp] = Clock.System.now().toString()
            }

            InvoiceActionResponse(
                success = true,
                message = "Invoice rejected",
                invoiceId = invoiceId,
                newStatus = InvoiceStatus.MISMATCH.name
            )
        }
    }

    /**
     * Store Detailed Validation Logs
     */
    fun logValidationDetails(invoiceId: String, details: List<ValidationDetailDTO>) {
        transaction {
            for (detail in details) {
                ValidationLogs.insert {
                    it[ValidationLogs.invoiceId] = invoiceId
                    it[itemId] = detail.itemId
                    it[poQuantity] = detail.poQuantity
                    it[grnQuantity] = detail.grnQuantity
                    it[invoiceQuantity] = detail.invoiceQuantity
                    it[poPrice] = detail.poPrice
                    it[invoicePrice] = detail.invoicePrice
                    it[priceDifference] = detail.priceDifference
                    it[quantityMatch] = detail.quantityMatch
                    it[priceMatch] = detail.priceMatch
                    it[overallMatch] = detail.overallMatch
                    it[timestamp] = Clock.System.now().toString()
                }
            }
        }
    }
}

