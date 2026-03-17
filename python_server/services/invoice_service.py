"""
services/invoice_service.py - Invoice validation and PO/GRN queries
Equivalent to server/.../service/InvoiceService.kt
"""
from datetime import datetime, timezone
from typing import List, Optional

from sqlalchemy.orm import Session

from models import (
    Grn, GrnItem, Invoice, InvoiceItem,
    PurchaseOrder, PurchaseOrderItem, ValidationLog,
)
from schemas import (
    GrnDTO, GrnItemDTO, InvoiceDTO, InvoiceItemDTO, InvoiceStatus,
    PurchaseOrderDTO, PurchaseOrderItemDTO, ValidationDetailDTO, ValidationResult,
)

PRICE_TOLERANCE = 0.05


def validate_invoice(db: Session, invoice: InvoiceDTO) -> ValidationResult:
    timestamp = datetime.now(timezone.utc).isoformat()

    po = db.query(PurchaseOrder).filter(PurchaseOrder.id == invoice.poId).first()
    if not po:
        return ValidationResult(
            status=InvoiceStatus.MANUAL_REVIEW,
            message=f"Purchase Order {invoice.poId} not found - requires manual review",
            timestamp=timestamp,
        )

    # PO items: {itemId -> (item_name, quantity, unit_price)}
    po_items = {
        row.item_id: (row.item_name, row.quantity, row.unit_price)
        for row in db.query(PurchaseOrderItem).filter(PurchaseOrderItem.po_id == invoice.poId).all()
    }

    # GRN quantities: {itemId -> total_received}
    grn_ids = [g.id for g in db.query(Grn).filter(Grn.po_id == invoice.poId).all()]
    grn_quantities: dict[str, int] = {}
    for grn_id in grn_ids:
        for gi in db.query(GrnItem).filter(GrnItem.grn_id == grn_id).all():
            grn_quantities[gi.item_id] = grn_quantities.get(gi.item_id, 0) + gi.received_quantity

    validation_errors: List[str] = []
    validation_details = []   # collected for ValidationLog
    for item in invoice.items:
        po_data = po_items.get(item.itemId)
        if po_data is None:
            validation_errors.append(f"Item {item.itemId} not found in PO {invoice.poId}")
            continue
        _item_name, _po_qty, po_price = po_data
        received_qty = grn_quantities.get(item.itemId, 0)
        quantity_match = item.quantity <= received_qty
        price_diff = abs(item.unitPrice - po_price)
        price_match = price_diff <= PRICE_TOLERANCE

        if not quantity_match:
            validation_errors.append(
                f"Item {item.itemId}: Invoice qty ({item.quantity}) exceeds received qty ({received_qty})"
            )
        if not price_match:
            validation_errors.append(
                f"Item {item.itemId}: Invoice price (${item.unitPrice}) differs from PO price (${po_price}) beyond tolerance"
            )

        validation_details.append({
            "item_id": item.itemId,
            "po_quantity": _po_qty,
            "grn_quantity": received_qty,
            "invoice_quantity": item.quantity,
            "po_price": po_price,
            "invoice_price": item.unitPrice,
            "price_difference": price_diff,
            "quantity_match": quantity_match,
            "price_match": price_match,
            "overall_match": quantity_match and price_match,
        })

    final_status = InvoiceStatus.MATCHED if not validation_errors else InvoiceStatus.MISMATCH

    # Upsert invoice record
    existing = db.query(Invoice).filter(Invoice.id == invoice.id).first()
    if existing is None:
        db.add(Invoice(
            id=invoice.id,
            po_id=invoice.poId,
            vendor_id=invoice.vendorId,
            total_amount=invoice.totalAmount,
            status=final_status.value,
            created_at=timestamp,
            validated_at=timestamp,
        ))
        for it in invoice.items:
            db.add(InvoiceItem(
                invoice_id=invoice.id,
                item_id=it.itemId,
                quantity=it.quantity,
                unit_price=it.unitPrice,
            ))
    else:
        existing.status = final_status.value
        existing.validated_at = timestamp

    # Persist ValidationLog for each line item
    for det in validation_details:
        db.add(ValidationLog(
            invoice_id=invoice.id,
            item_id=det["item_id"],
            po_quantity=det["po_quantity"],
            grn_quantity=det["grn_quantity"],
            invoice_quantity=det["invoice_quantity"],
            po_price=det["po_price"],
            invoice_price=det["invoice_price"],
            price_difference=det["price_difference"],
            quantity_match=det["quantity_match"],
            price_match=det["price_match"],
            overall_match=det["overall_match"],
            timestamp=timestamp,
        ))

    db.commit()

    message = (
        "Invoice validated successfully - 3-Way Match PASSED"
        if not validation_errors
        else "3-Way Match FAILED: " + "; ".join(validation_errors)
    )

    # Build ValidationDetail list for the response
    details = [
        ValidationDetailDTO(
            itemId=det["item_id"],
            itemName=po_items[det["item_id"]][0] if det["item_id"] in po_items else det["item_id"],
            poQuantity=det["po_quantity"],
            grnQuantity=det["grn_quantity"],
            invoiceQuantity=det["invoice_quantity"],
            poPrice=det["po_price"],
            invoicePrice=det["invoice_price"],
            priceDifference=det["price_difference"],
            quantityMatch=det["quantity_match"],
            priceMatch=det["price_match"],
            overallMatch=det["overall_match"],
        )
        for det in validation_details
    ]
    return ValidationResult(status=final_status, message=message, timestamp=timestamp, details=details)


def get_all_purchase_orders(db: Session) -> List[PurchaseOrderDTO]:
    result = []
    for po in db.query(PurchaseOrder).all():
        items = [
            PurchaseOrderItemDTO(
                itemId=i.item_id,
                itemName=i.item_name,
                quantity=i.quantity,
                unitPrice=i.unit_price,
            )
            for i in db.query(PurchaseOrderItem).filter(PurchaseOrderItem.po_id == po.id).all()
        ]
        result.append(PurchaseOrderDTO(
            id=po.id,
            vendorName=po.vendor_name,
            vendorEmail=po.vendor_email or "",
            totalAmount=po.total_amount,
            items=items,
        ))
    return result


def get_purchase_order_by_id(db: Session, po_id: str) -> Optional[PurchaseOrderDTO]:
    po = db.query(PurchaseOrder).filter(PurchaseOrder.id == po_id).first()
    if not po:
        return None
    items = [
        PurchaseOrderItemDTO(
            itemId=i.item_id,
            itemName=i.item_name,
            quantity=i.quantity,
            unitPrice=i.unit_price,
        )
        for i in db.query(PurchaseOrderItem).filter(PurchaseOrderItem.po_id == po_id).all()
    ]
    return PurchaseOrderDTO(
        id=po.id,
        vendorName=po.vendor_name,
        vendorEmail=po.vendor_email or "",
        totalAmount=po.total_amount,
        items=items,
    )


def get_all_invoices(db: Session) -> List[InvoiceDTO]:
    result = []
    for inv in db.query(Invoice).all():
        items = [
            InvoiceItemDTO(itemId=it.item_id, quantity=it.quantity, unitPrice=it.unit_price)
            for it in db.query(InvoiceItem).filter(InvoiceItem.invoice_id == inv.id).all()
        ]
        result.append(InvoiceDTO(
            id=inv.id,
            poId=inv.po_id or "",
            vendorId=inv.vendor_id,
            totalAmount=inv.total_amount,
            status=InvoiceStatus(inv.status),
            items=items,
            rejectionReason=inv.rejection_reason,
        ))
    return result


def get_invoices_by_vendor(db: Session, vendor_id: str) -> List[InvoiceDTO]:
    """Return only invoices belonging to the given vendor_id."""
    result = []
    for inv in db.query(Invoice).filter(Invoice.vendor_id == vendor_id).all():
        items = [
            InvoiceItemDTO(itemId=it.item_id, quantity=it.quantity, unitPrice=it.unit_price)
            for it in db.query(InvoiceItem).filter(InvoiceItem.invoice_id == inv.id).all()
        ]
        result.append(InvoiceDTO(
            id=inv.id,
            poId=inv.po_id or "",
            vendorId=inv.vendor_id,
            totalAmount=inv.total_amount,
            status=InvoiceStatus(inv.status),
            items=items,
            rejectionReason=inv.rejection_reason,
        ))
    return result


def get_invoice_by_id(db: Session, invoice_id: str) -> Optional[InvoiceDTO]:
    inv = db.query(Invoice).filter(Invoice.id == invoice_id).first()
    if not inv:
        return None
    items = [
        InvoiceItemDTO(itemId=it.item_id, quantity=it.quantity, unitPrice=it.unit_price)
        for it in db.query(InvoiceItem).filter(InvoiceItem.invoice_id == inv.id).all()
    ]
    return InvoiceDTO(
        id=inv.id,
        poId=inv.po_id or "",
        vendorId=inv.vendor_id,
        totalAmount=inv.total_amount,
        status=InvoiceStatus(inv.status),
        items=items,
        rejectionReason=inv.rejection_reason,
    )


def get_grns_by_po_id(db: Session, po_id: str) -> List[GrnDTO]:
    result = []
    for grn in db.query(Grn).filter(Grn.po_id == po_id).all():
        items = [
            GrnItemDTO(itemId=gi.item_id, receivedQuantity=gi.received_quantity)
            for gi in db.query(GrnItem).filter(GrnItem.grn_id == grn.id).all()
        ]
        result.append(GrnDTO(id=grn.id, poId=grn.po_id, items=items, receivedAt=grn.received_at))
    return result


def get_all_grns(db: Session) -> List[GrnDTO]:
    """Return every GRN across all Purchase Orders."""
    result = []
    for grn in db.query(Grn).all():
        items = [
            GrnItemDTO(itemId=gi.item_id, receivedQuantity=gi.received_quantity)
            for gi in db.query(GrnItem).filter(GrnItem.grn_id == grn.id).all()
        ]
        result.append(GrnDTO(id=grn.id, poId=grn.po_id, items=items, receivedAt=grn.received_at))
    return result
