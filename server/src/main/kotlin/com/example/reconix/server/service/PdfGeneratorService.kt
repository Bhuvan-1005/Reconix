package com.example.reconix.server.service

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * PDF Generator Service - Creates professional Purchase Order PDFs using OpenPDF
 */
class PdfGeneratorService {

    private val uploadDir = "uploads/po"

    init {
        File(uploadDir).mkdirs()
    }

    /**
     * Generate a Purchase Order PDF document
     * @param poId Purchase Order ID
     * @param vendorName Vendor name
     * @param vendorEmail Vendor email
     * @param items List of line items (itemName, quantity, unitPrice, taxRate)
     * @param totalAmount Total PO amount
     * @return File path of the generated PDF
     */
    fun generatePoPdf(
        poId: String,
        vendorName: String,
        vendorEmail: String,
        items: List<PdfLineItem>,
        totalAmount: Double
    ): String {
        val fileName = "${poId}_${System.currentTimeMillis()}.pdf"
        val filePath = "$uploadDir/$fileName"
        val document = Document(PageSize.A4, 50f, 50f, 50f, 50f)

        try {
            PdfWriter.getInstance(document, FileOutputStream(filePath))
            document.open()

            // ── Company Header ──
            val headerFont = Font(Font.HELVETICA, 24f, Font.BOLD, Color(30, 58, 138))
            val header = Paragraph("RECONIX", headerFont)
            header.alignment = Element.ALIGN_LEFT
            document.add(header)

            val subtitleFont = Font(Font.HELVETICA, 10f, Font.NORMAL, Color(100, 100, 100))
            val subtitle = Paragraph("Automated Invoice Match Validator", subtitleFont)
            subtitle.setSpacingAfter(5f)
            document.add(subtitle)

            // Divider line
            val line = Paragraph("━".repeat(80))
            line.setSpacingAfter(15f)
            document.add(line)

            // ── PO Title ──
            val titleFont = Font(Font.HELVETICA, 18f, Font.BOLD, Color(0, 0, 0))
            val title = Paragraph("PURCHASE ORDER", titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.setSpacingAfter(20f)
            document.add(title)

            // ── PO Details Table ──
            val detailsTable = PdfPTable(2)
            detailsTable.widthPercentage = 100f
            detailsTable.setWidths(floatArrayOf(1f, 2f))

            val labelFont = Font(Font.HELVETICA, 11f, Font.BOLD, Color(55, 65, 81))
            val valueFont = Font(Font.HELVETICA, 11f, Font.NORMAL, Color(17, 24, 39))

            addDetailRow(detailsTable, "PO Number:", poId, labelFont, valueFont)
            addDetailRow(detailsTable, "Date:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), labelFont, valueFont)
            addDetailRow(detailsTable, "Vendor:", vendorName, labelFont, valueFont)
            if (vendorEmail.isNotBlank()) {
                addDetailRow(detailsTable, "Vendor Email:", vendorEmail, labelFont, valueFont)
            }
            addDetailRow(detailsTable, "Status:", "OPEN", labelFont, valueFont)
            detailsTable.setSpacingAfter(25f)
            document.add(detailsTable)

            // ── Line Items Table ──
            val itemsTable = PdfPTable(5)
            itemsTable.widthPercentage = 100f
            itemsTable.setWidths(floatArrayOf(0.5f, 2f, 0.8f, 1f, 1f))

            val headerCellFont = Font(Font.HELVETICA, 10f, Font.BOLD, Color.WHITE)
            val headerBg = Color(30, 58, 138)

            // Table headers
            listOf("#", "Item Description", "Qty", "Unit Price", "Total").forEach { text ->
                val cell = PdfPCell(Phrase(text, headerCellFont))
                cell.backgroundColor = headerBg
                cell.setPadding(8f)
                cell.horizontalAlignment = Element.ALIGN_CENTER
                itemsTable.addCell(cell)
            }

            // Table rows
            val cellFont = Font(Font.HELVETICA, 10f, Font.NORMAL, Color(17, 24, 39))
            items.forEachIndexed { index, item ->
                val rowBg = if (index % 2 == 0) Color(249, 250, 251) else Color.WHITE
                val lineTotal = item.quantity * item.unitPrice

                addItemCell(itemsTable, "${index + 1}", cellFont, rowBg, Element.ALIGN_CENTER)
                addItemCell(itemsTable, item.itemName, cellFont, rowBg, Element.ALIGN_LEFT)
                addItemCell(itemsTable, "${item.quantity}", cellFont, rowBg, Element.ALIGN_CENTER)
                addItemCell(itemsTable, "$${String.format("%.2f", item.unitPrice)}", cellFont, rowBg, Element.ALIGN_RIGHT)
                addItemCell(itemsTable, "$${String.format("%.2f", lineTotal)}", cellFont, rowBg, Element.ALIGN_RIGHT)
            }

            itemsTable.setSpacingAfter(20f)
            document.add(itemsTable)

            // ── Totals Section ──
            val totalsTable = PdfPTable(2)
            totalsTable.widthPercentage = 50f
            totalsTable.horizontalAlignment = Element.ALIGN_RIGHT

            val subtotalAmount = items.sumOf { it.quantity * it.unitPrice }
            val taxAmount = items.sumOf { it.quantity * it.unitPrice * (it.taxRate / 100.0) }

            val totalFont = Font(Font.HELVETICA, 11f, Font.NORMAL)
            val totalBoldFont = Font(Font.HELVETICA, 13f, Font.BOLD, Color(30, 58, 138))

            addTotalRow(totalsTable, "Subtotal:", "$${String.format("%.2f", subtotalAmount)}", totalFont)
            addTotalRow(totalsTable, "Tax:", "$${String.format("%.2f", taxAmount)}", totalFont)

            val grandTotalCell1 = PdfPCell(Phrase("TOTAL:", totalBoldFont))
            grandTotalCell1.border = Rectangle.TOP
            grandTotalCell1.setPadding(8f)
            grandTotalCell1.horizontalAlignment = Element.ALIGN_RIGHT
            totalsTable.addCell(grandTotalCell1)

            val grandTotalCell2 = PdfPCell(Phrase("$${String.format("%.2f", totalAmount)}", totalBoldFont))
            grandTotalCell2.border = Rectangle.TOP
            grandTotalCell2.setPadding(8f)
            grandTotalCell2.horizontalAlignment = Element.ALIGN_RIGHT
            totalsTable.addCell(grandTotalCell2)

            document.add(totalsTable)

            // ── Footer ──
            val footerParagraph = Paragraph()
            footerParagraph.setSpacingBefore(40f)
            val footerFont = Font(Font.HELVETICA, 9f, Font.ITALIC, Color(156, 163, 175))
            footerParagraph.add(Phrase("This is a system-generated document from Reconix Invoice Validator. ", footerFont))
            footerParagraph.add(Phrase("Please reference PO# $poId in all correspondence.", footerFont))
            document.add(footerParagraph)

        } finally {
            document.close()
        }

        println("📄 Generated PO PDF: $filePath")
        return filePath
    }

    private fun addDetailRow(table: PdfPTable, label: String, value: String, labelFont: Font, valueFont: Font) {
        val labelCell = PdfPCell(Phrase(label, labelFont))
        labelCell.border = Rectangle.NO_BORDER
        labelCell.setPadding(4f)
        table.addCell(labelCell)

        val valueCell = PdfPCell(Phrase(value, valueFont))
        valueCell.border = Rectangle.NO_BORDER
        valueCell.setPadding(4f)
        table.addCell(valueCell)
    }

    private fun addItemCell(table: PdfPTable, text: String, font: Font, bgColor: Color, alignment: Int) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = bgColor
        cell.setPadding(6f)
        cell.horizontalAlignment = alignment
        cell.borderColor = Color(229, 231, 235)
        table.addCell(cell)
    }

    private fun addTotalRow(table: PdfPTable, label: String, value: String, font: Font) {
        val labelCell = PdfPCell(Phrase(label, font))
        labelCell.border = Rectangle.NO_BORDER
        labelCell.setPadding(5f)
        labelCell.horizontalAlignment = Element.ALIGN_RIGHT
        table.addCell(labelCell)

        val valueCell = PdfPCell(Phrase(value, font))
        valueCell.border = Rectangle.NO_BORDER
        valueCell.setPadding(5f)
        valueCell.horizontalAlignment = Element.ALIGN_RIGHT
        table.addCell(valueCell)
    }
}

/**
 * Data class for PDF line items
 */
data class PdfLineItem(
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double = 0.0
)
