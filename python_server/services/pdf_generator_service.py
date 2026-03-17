"""
services/pdf_generator_service.py - Generate PO PDFs using ReportLab
Equivalent to server/.../service/PdfGeneratorService.kt
"""
import os
import time
from dataclasses import dataclass, field
from datetime import datetime
from typing import List

UPLOAD_DIR = "uploads/po"
os.makedirs(UPLOAD_DIR, exist_ok=True)


@dataclass
class PdfLineItem:
    item_name: str
    quantity: int
    unit_price: float
    tax_rate: float = 0.0


def generate_po_pdf(
    po_id: str,
    vendor_name: str,
    vendor_email: str,
    items: List[PdfLineItem],
    total_amount: float,
) -> str:
    try:
        from reportlab.lib.pagesizes import A4
        from reportlab.lib import colors
        from reportlab.lib.units import mm
        from reportlab.platypus import (
            SimpleDocTemplate, Table, TableStyle, Paragraph,
            Spacer, HRFlowable,
        )
        from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
        from reportlab.lib.enums import TA_CENTER, TA_RIGHT, TA_LEFT
    except ImportError as e:
        raise RuntimeError(f"reportlab not installed: {e}")

    file_name = f"{po_id}_{int(time.time())}.pdf"
    file_path = os.path.join(UPLOAD_DIR, file_name)

    doc = SimpleDocTemplate(
        file_path,
        pagesize=A4,
        leftMargin=50, rightMargin=50, topMargin=50, bottomMargin=50,
    )

    styles = getSampleStyleSheet()
    dark_blue = colors.HexColor("#1e3a8a")
    light_gray = colors.HexColor("#f9fafb")

    header_style = ParagraphStyle("Header", fontSize=24, textColor=dark_blue, fontName="Helvetica-Bold")
    subtitle_style = ParagraphStyle("Subtitle", fontSize=10, textColor=colors.grey, fontName="Helvetica")
    title_style = ParagraphStyle("Title", fontSize=18, fontName="Helvetica-Bold", alignment=TA_CENTER)
    footer_style = ParagraphStyle("Footer", fontSize=9, fontName="Helvetica-Oblique", textColor=colors.HexColor("#9ca3af"))

    story = []

    # Company header
    story.append(Paragraph("RECONIX", header_style))
    story.append(Paragraph("Automated Invoice Match Validator", subtitle_style))
    story.append(Spacer(1, 8))
    story.append(HRFlowable(width="100%", thickness=1, color=colors.grey))
    story.append(Spacer(1, 12))

    # Document title
    story.append(Paragraph("PURCHASE ORDER", title_style))
    story.append(Spacer(1, 20))

    # Details table
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M")
    detail_rows = [
        ["PO Number:", po_id],
        ["Date:", now_str],
        ["Vendor:", vendor_name],
    ]
    if vendor_email:
        detail_rows.append(["Vendor Email:", vendor_email])
    detail_rows.append(["Status:", "OPEN"])

    detail_table = Table(detail_rows, colWidths=[120, 350])
    detail_table.setStyle(TableStyle([
        ("FONTNAME", (0, 0), (0, -1), "Helvetica-Bold"),
        ("FONTSIZE", (0, 0), (-1, -1), 11),
        ("TEXTCOLOR", (0, 0), (0, -1), colors.HexColor("#374151")),
        ("TEXTCOLOR", (1, 0), (1, -1), colors.HexColor("#111827")),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
    ]))
    story.append(detail_table)
    story.append(Spacer(1, 20))

    # Items table
    col_headers = ["#", "Item Description", "Qty", "Unit Price", "Total"]
    item_rows = [col_headers]
    for idx, item in enumerate(items):
        line_total = item.quantity * item.unit_price
        item_rows.append([
            str(idx + 1),
            item.item_name,
            str(item.quantity),
            f"${item.unit_price:.2f}",
            f"${line_total:.2f}",
        ])

    items_table = Table(item_rows, colWidths=[30, 230, 60, 80, 80])
    # Alternating row colors
    row_styles = [
        ("BACKGROUND", (0, 0), (-1, 0), dark_blue),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTSIZE", (0, 0), (-1, -1), 10),
        ("ALIGN", (0, 0), (-1, -1), "CENTER"),
        ("ALIGN", (1, 1), (1, -1), "LEFT"),
        ("ALIGN", (3, 1), (-1, -1), "RIGHT"),
        ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#e5e7eb")),
        ("ROWBACKGROUND", (0, 1), (-1, -1), [light_gray, colors.white]),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
    ]
    items_table.setStyle(TableStyle(row_styles))
    story.append(items_table)
    story.append(Spacer(1, 20))

    # Totals
    subtotal = sum(it.quantity * it.unit_price for it in items)
    tax_amount = sum(it.quantity * it.unit_price * (it.tax_rate / 100.0) for it in items)
    bold_blue = ParagraphStyle("BoldBlue", fontSize=13, fontName="Helvetica-Bold", textColor=dark_blue, alignment=TA_RIGHT)

    totals_data = [
        ["Subtotal:", f"${subtotal:.2f}"],
        ["Tax:", f"${tax_amount:.2f}"],
        [Paragraph("TOTAL:", bold_blue), Paragraph(f"${total_amount:.2f}", bold_blue)],
    ]
    totals_table = Table(totals_data, colWidths=[350, 130], hAlign="RIGHT")
    totals_table.setStyle(TableStyle([
        ("FONTSIZE", (0, 0), (-1, -1), 11),
        ("ALIGN", (0, 0), (-1, -1), "RIGHT"),
        ("LINEABOVE", (0, 2), (-1, 2), 1, colors.grey),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
    ]))
    story.append(totals_table)
    story.append(Spacer(1, 40))

    # Footer
    story.append(Paragraph(
        f"This is a system-generated document from Reconix Invoice Validator. "
        f"Please reference PO# {po_id} in all correspondence.",
        footer_style,
    ))

    doc.build(story)
    print(f"📄 Generated PO PDF: {file_path}")
    return file_path
