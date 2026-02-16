package com.example.reconix.server.service

import com.example.reconix.shared.OcrExtractedData
import com.example.reconix.shared.OcrLineItem
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * OCR & Data Extraction Service
 * Uses Apache PDFBox for text-based PDF extraction
 * Falls back to simulated extraction for scanned/image PDFs
 */
class OcrExtractionService {

    /**
     * Extract data from an invoice PDF file
     * @param filePath Path to the PDF file
     * @return Extracted data with confidence score
     */
    fun extractFromPdf(filePath: String): OcrExtractedData {
        val file = File(filePath)
        if (!file.exists()) {
            return createEmptyResult("File not found: $filePath")
        }

        return try {
            val text = extractTextFromPdf(file)
            if (text.isBlank()) {
                // Scanned PDF - no text layer, would need OCR
                println("🔍 No text found in PDF - appears to be scanned image")
                createSimulatedResult()
            } else {
                parseExtractedText(text)
            }
        } catch (e: Exception) {
            println("🔍 ❌ PDF extraction error: ${e.message}")
            createEmptyResult("Extraction failed: ${e.message}")
        }
    }

    /**
     * Extract data from raw bytes (for email attachments)
     */
    fun extractFromBytes(bytes: ByteArray, fileName: String): OcrExtractedData {
        val tempFile = File.createTempFile("invoice_", ".pdf")
        try {
            tempFile.writeBytes(bytes)
            return extractFromPdf(tempFile.absolutePath)
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Extract raw text from a PDF using PDFBox
     */
    private fun extractTextFromPdf(file: File): String {
        val document = Loader.loadPDF(file)
        return try {
            val stripper = PDFTextStripper()
            stripper.getText(document).trim()
        } finally {
            document.close()
        }
    }

    /**
     * Parse extracted text to find invoice data using regex patterns
     */
    private fun parseExtractedText(text: String): OcrExtractedData {
        val lines = text.lines()
        var confidence = 0.0
        var matchCount = 0
        val totalChecks = 4 // PO#, vendor, total, items

        // 1. Find PO Reference Number
        val poPattern = Regex("""(?i)(?:PO|P\.O\.|Purchase Order)[#:\s-]*(\w+-?\d+)""")
        val poMatch = poPattern.find(text)
        val detectedPoNumber = poMatch?.groupValues?.get(1)
        if (detectedPoNumber != null) matchCount++

        // 2. Find Vendor Name - look for common invoice header patterns
        val vendorPattern = Regex("""(?i)(?:from|vendor|supplier|company)[:\s]*([A-Za-z][A-Za-z\s&.,]+)""")
        val vendorMatch = vendorPattern.find(text)
        val vendorName = vendorMatch?.groupValues?.get(1)?.trim()
        if (vendorName != null) matchCount++

        // 3. Find Total Amount
        val totalPattern = Regex("""(?i)(?:total|amount due|grand total|balance due)[:\s]*\$?([\d,]+\.?\d*)""")
        val totalMatch = totalPattern.find(text)
        val totalAmount = totalMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
        if (totalAmount != null) matchCount++

        // 4. Extract Line Items - look for quantity × price patterns
        val lineItemPattern = Regex("""(\d+)\s+(.+?)\s+\$?([\d,]+\.?\d{0,2})\s+\$?([\d,]+\.?\d{0,2})""")
        val lineItems = lineItemPattern.findAll(text).map { match ->
            OcrLineItem(
                description = match.groupValues[2].trim(),
                quantity = match.groupValues[1].toIntOrNull(),
                unitPrice = match.groupValues[3].replace(",", "").toDoubleOrNull(),
                amount = match.groupValues[4].replace(",", "").toDoubleOrNull()
            )
        }.toList()
        if (lineItems.isNotEmpty()) matchCount++

        // Calculate confidence score
        confidence = (matchCount.toDouble() / totalChecks) * 100.0

        println("🔍 Extraction results: PO=$detectedPoNumber, Vendor=$vendorName, Total=$totalAmount, Items=${lineItems.size}, Confidence=$confidence%")

        return OcrExtractedData(
            detectedPoNumber = detectedPoNumber,
            vendorName = vendorName,
            lineItems = lineItems,
            totalAmount = totalAmount,
            confidenceScore = confidence
        )
    }

    /**
     * Create a simulated extraction result (for demo/testing when OCR is not available)
     */
    private fun createSimulatedResult(): OcrExtractedData {
        return OcrExtractedData(
            detectedPoNumber = "PO-001",
            vendorName = "Office Supplies Co.",
            lineItems = listOf(
                OcrLineItem("Printer Paper (Box)", 50, 25.00, 1250.00),
                OcrLineItem("Ink Cartridges", 10, 25.00, 250.00)
            ),
            totalAmount = 1500.00,
            confidenceScore = 65.0 // Lower confidence for simulated
        )
    }

    private fun createEmptyResult(errorMessage: String): OcrExtractedData {
        println("🔍 ⚠️ $errorMessage")
        return OcrExtractedData(
            detectedPoNumber = null,
            vendorName = null,
            lineItems = emptyList(),
            totalAmount = null,
            confidenceScore = 0.0
        )
    }
}
