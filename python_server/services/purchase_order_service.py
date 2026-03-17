"""
services/purchase_order_service.py - Create POs, generate PDF, send email
Equivalent to server/.../service/PurchaseOrderService.kt
"""
from datetime import datetime, timezone
from typing import List

from sqlalchemy.orm import Session

from models import PurchaseOrder, PurchaseOrderItem
from schemas import CreatePOLineItem, CreatePurchaseOrderRequest, CreatePurchaseOrderResponse
from services.pdf_generator_service import PdfLineItem, generate_po_pdf
from services.email_service import send_po_to_vendor


def create_purchase_order(db: Session, request: CreatePurchaseOrderRequest) -> CreatePurchaseOrderResponse:
    now = datetime.now(timezone.utc).isoformat()

    # Determine next PO number
    last_po = db.query(PurchaseOrder).order_by(PurchaseOrder.id.desc()).first()
    if last_po:
        try:
            next_num = int(last_po.id.replace("PO-", "")) + 1
        except ValueError:
            next_num = 1
    else:
        next_num = 1
    po_id = f"PO-{next_num:03d}"

    # Calculate total (including tax)
    total_amount = sum(
        item.quantity * item.unitPrice * (1 + item.taxRate / 100.0)
        for item in request.items
    )

    # Insert PO
    po = PurchaseOrder(
        id=po_id,
        vendor_name=request.vendorName,
        vendor_email=request.vendorEmail or None,
        total_amount=total_amount,
        status="OPEN",
        created_at=now,
    )
    db.add(po)
    db.flush()

    # Insert PO items
    for idx, item in enumerate(request.items):
        item_id = f"ITEM-{po_id}-{idx + 1:02d}"
        db.add(PurchaseOrderItem(
            po_id=po_id,
            item_id=item_id,
            item_name=item.itemName,
            quantity=item.quantity,
            unit_price=item.unitPrice,
        ))
    db.flush()

    # Generate PDF
    pdf_generated = False
    pdf_path: str | None = None
    try:
        pdf_line_items = [
            PdfLineItem(
                item_name=item.itemName,
                quantity=item.quantity,
                unit_price=item.unitPrice,
                tax_rate=item.taxRate,
            )
            for item in request.items
        ]
        pdf_path = generate_po_pdf(
            po_id=po_id,
            vendor_name=request.vendorName,
            vendor_email=request.vendorEmail,
            items=pdf_line_items,
            total_amount=total_amount,
        )
        pdf_generated = True
        po.pdf_path = pdf_path
    except Exception as exc:
        print(f"⚠️ PDF generation failed: {exc}")

    # Send email
    email_sent = False
    if request.vendorEmail and pdf_path:
        email_sent = send_po_to_vendor(
            vendor_email=request.vendorEmail,
            po_id=po_id,
            pdf_path=pdf_path,
        )
        if email_sent:
            po.status = "SENT"

    db.commit()

    # Build response message
    parts = [f"Purchase Order {po_id} created successfully"]
    if pdf_generated:
        parts.append("PDF generated")
    if email_sent:
        parts.append(f"Email sent to {request.vendorEmail}")
    elif request.vendorEmail:
        parts.append("Email not sent (SMTP not configured)")

    return CreatePurchaseOrderResponse(
        success=True,
        poId=po_id,
        status="SENT" if email_sent else "OPEN",
        message=". ".join(parts),
        totalAmount=total_amount,
        pdfGenerated=pdf_generated,
        emailSent=email_sent,
    )
