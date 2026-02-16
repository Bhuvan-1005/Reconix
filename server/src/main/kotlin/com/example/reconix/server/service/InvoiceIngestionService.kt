package com.example.reconix.server.service

import com.example.reconix.server.database.*
import com.example.reconix.shared.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

/**
 * Invoice Ingestion Service - Handles file upload and processing pipeline
 * Supports both direct upload and email-triggered ingestion
 */
class InvoiceIngestionService {

    private val ocrService = OcrExtractionService()
    private val invoiceService = InvoiceService()
    private val uploadDir = "uploads/invoices"

    init {
        File(uploadDir).mkdirs()
    }

    /**
     * Handle an uploaded invoice file
     * @param fileName Original file name
     * @param fileBytes Raw file content
     * @param channel Upload channel (DIRECT_UPLOAD or EMAIL)
     * @return InvoiceUploadResponse with extraction and validation results
     */
    fun handleFileUpload(
        fileName: String,
        fileBytes: ByteArray,
        channel: String = "DIRECT_UPLOAD"
    ): InvoiceUploadResponse {
        val now = Clock.System.now().toString()

        // 1. Save file to disk
        val sanitizedName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val storageName = "${System.currentTimeMillis()}_$sanitizedName"
        val storagePath = "$uploadDir/$storageName"
        File(storagePath).writeBytes(fileBytes)

        val fileType = when {
            fileName.lowercase().endsWith(".pdf") -> "PDF"
            fileName.lowercase().endsWith(".png") || fileName.lowercase().endsWith(".jpg") || fileName.lowercase().endsWith(".jpeg") -> "IMAGE"
            else -> "UNKNOWN"
        }

        println("📥 File saved: $storagePath ($fileType, ${fileBytes.size} bytes, channel=$channel)")

        // 2. Create InvoiceFiles record (without invoiceId initially)
        val fileId = transaction {
            InvoiceFiles.insert {
                it[InvoiceFiles.originalFilename] = fileName
                it[InvoiceFiles.storagePath] = storagePath
                it[InvoiceFiles.fileType] = fileType
                it[InvoiceFiles.uploadChannel] = channel
                it[InvoiceFiles.uploadedAt] = now
                it[InvoiceFiles.ocrProcessed] = false
            } get InvoiceFiles.id
        }

        // 3. Run OCR extraction
        val extractedData = ocrService.extractFromPdf(storagePath)

        // Mark as OCR processed
        transaction {
            InvoiceFiles.update({ InvoiceFiles.id eq fileId }) {
                it[ocrProcessed] = true
            }
        }

        // 4. If PO was detected, create invoice and validate
        if (extractedData.detectedPoNumber != null && extractedData.totalAmount != null) {
            return processExtractedInvoice(extractedData, fileId, now)
        }

        // No PO detected - return for manual review
        return InvoiceUploadResponse(
            success = true,
            invoiceId = null,
            message = "File uploaded and processed. No PO reference detected - manual entry required.",
            extractedData = extractedData,
            validationResult = null
        )
    }

    /**
     * Process extracted invoice data: create invoice record and run validation
     */
    private fun processExtractedInvoice(
        extractedData: OcrExtractedData,
        fileId: Int,
        timestamp: String
    ): InvoiceUploadResponse {
        val poNumber = extractedData.detectedPoNumber!!

        // Check if PO exists
        val poExists = transaction {
            PurchaseOrders.selectAll()
                .where { PurchaseOrders.id eq poNumber }
                .count() > 0
        }

        if (!poExists) {
            return InvoiceUploadResponse(
                success = true,
                invoiceId = null,
                message = "⚠️ Unknown PO: $poNumber not found in database. Manual review required.",
                extractedData = extractedData,
                validationResult = ValidationResult(
                    status = InvoiceStatus.MANUAL_REVIEW,
                    message = "PO $poNumber not found in the system",
                    timestamp = timestamp,
                    details = null
                )
            )
        }

        // Create invoice from extracted data
        val invoiceId = "INV-${System.currentTimeMillis()}"

        // Map OCR line items to invoice items
        val invoiceItems = extractedData.lineItems.mapIndexed { index, item ->
            InvoiceItemDTO(
                itemId = "ITEM-$poNumber-${String.format("%02d", index + 1)}",
                quantity = item.quantity ?: 0,
                unitPrice = item.unitPrice ?: 0.0
            )
        }

        val invoiceDTO = InvoiceDTO(
            id = invoiceId,
            poId = poNumber,
            vendorId = extractedData.vendorName ?: "Unknown",
            totalAmount = extractedData.totalAmount ?: 0.0,
            status = InvoiceStatus.PENDING,
            items = invoiceItems
        )

        // Run 3-way match validation
        val validationResult = invoiceService.validateInvoice(invoiceDTO)

        // Link file to invoice
        transaction {
            InvoiceFiles.update({ InvoiceFiles.id eq fileId }) {
                it[InvoiceFiles.invoiceId] = invoiceId
            }
        }

        return InvoiceUploadResponse(
            success = true,
            invoiceId = invoiceId,
            message = "Invoice $invoiceId created from uploaded file. PO: $poNumber. Status: ${validationResult.status}",
            extractedData = extractedData,
            validationResult = validationResult
        )
    }
}
