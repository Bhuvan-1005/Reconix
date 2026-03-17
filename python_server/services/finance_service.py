"""
services/finance_service.py - Dashboard metrics, pending invoices, 3-way match, approve/reject
Equivalent to server/.../service/FinanceService.kt
"""
from datetime import datetime, timezone
from typing import List, Optional

from sqlalchemy import desc
from sqlalchemy.orm import Session

from models import (
    Grn, GrnItem, Invoice, InvoiceAction, InvoiceItem,
    PurchaseOrderItem, User, ValidationLog,
)
from schemas import (
    DashboardMetricsDTO, InvoiceActionResponse, InvoiceListItemDTO,
    InvoiceStatus, RecentActivityDTO, ThreeWayMatchDTO, ValidationDetailDTO,
)


def get_dashboard_metrics(db: Session) -> DashboardMetricsDTO:
    all_invoices = db.query(Invoice).all()
    pending = [i for i in all_invoices if i.status == InvoiceStatus.PENDING.value]
    matched = [i for i in all_invoices if i.status == InvoiceStatus.MATCHED.value]
    mismatched = [i for i in all_invoices if i.status == InvoiceStatus.MISMATCH.value]

    total_pending_amount = sum(i.total_amount for i in pending)
    total_payable_amount = sum(i.total_amount for i in matched)
    total_validated = len(matched) + len(mismatched)
    match_rate = (len(matched) / total_validated * 100) if total_validated > 0 else 0.0

    # Recent activity - join invoice_actions -> invoices -> users
    recent_rows = (
        db.query(InvoiceAction, Invoice, User)
        .join(Invoice, InvoiceAction.invoice_id == Invoice.id)
        .join(User, InvoiceAction.performed_by == User.id)
        .order_by(desc(InvoiceAction.timestamp))
        .limit(10)
        .all()
    )
    recent_activity = [
        RecentActivityDTO(
            id=action.id,
            invoiceId=action.invoice_id,
            vendorName=invoice.vendor_id,
            actionType=action.action_type,
            performedBy=user.full_name,
            timestamp=action.timestamp,
            amount=invoice.total_amount,
        )
        for action, invoice, user in recent_rows
    ]

    return DashboardMetricsDTO(
        totalPendingInvoices=len(pending),
        totalPendingAmount=total_pending_amount,
        matchedInvoicesCount=len(matched),
        mismatchedInvoicesCount=len(mismatched),
        matchRate=match_rate,
        totalPayableAmount=total_payable_amount,
        averageProcessingTime="2.5 hours",
        recentActivity=recent_activity,
    )


def get_pending_invoices(db: Session) -> List[InvoiceListItemDTO]:
    invoices = (
        db.query(Invoice)
        .filter(Invoice.status == InvoiceStatus.PENDING.value)
        .order_by(desc(Invoice.created_at))
        .all()
    )
    result = []
    for inv in invoices:
        item_count = db.query(InvoiceItem).filter(InvoiceItem.invoice_id == inv.id).count()
        result.append(InvoiceListItemDTO(
            id=inv.id,
            poId=inv.po_id or "",
            vendorName=inv.vendor_id,
            totalAmount=inv.total_amount,
            status=InvoiceStatus(inv.status),
            createdAt=inv.created_at,
            itemCount=item_count,
            matchPercentage=None,
            rejectionReason=inv.rejection_reason,
        ))
    return result


def get_three_way_match(db: Session, invoice_id: str) -> Optional[ThreeWayMatchDTO]:
    invoice = db.query(Invoice).filter(Invoice.id == invoice_id).first()
    if not invoice:
        return None

    po_id = invoice.po_id

    # PO items: {itemId -> (name, qty, price)}
    po_items = {
        row.item_id: (row.item_name, row.quantity, row.unit_price)
        for row in db.query(PurchaseOrderItem).filter(PurchaseOrderItem.po_id == po_id).all()
    }

    # GRN quantities
    grn_ids = [g.id for g in db.query(Grn).filter(Grn.po_id == po_id).all()]
    grn_quantities: dict[str, int] = {}
    for grn_id in grn_ids:
        for gi in db.query(GrnItem).filter(GrnItem.grn_id == grn_id).all():
            grn_quantities[gi.item_id] = grn_quantities.get(gi.item_id, 0) + gi.received_quantity

    invoice_items = db.query(InvoiceItem).filter(InvoiceItem.invoice_id == invoice_id).all()
    match_details: List[ValidationDetailDTO] = []
    for inv_item in invoice_items:
        item_id = inv_item.item_id
        po_data = po_items.get(item_id, ("Unknown Item", 0, 0.0))
        item_name, po_qty, po_price = po_data
        grn_qty = grn_quantities.get(item_id, 0)
        inv_qty = inv_item.quantity
        inv_price = inv_item.unit_price
        quantity_match = inv_qty <= grn_qty
        price_diff = abs(inv_price - po_price)
        price_match = price_diff <= 0.05
        match_details.append(ValidationDetailDTO(
            itemId=item_id,
            itemName=item_name,
            poQuantity=po_qty,
            grnQuantity=grn_qty,
            invoiceQuantity=inv_qty,
            poPrice=po_price,
            invoicePrice=inv_price,
            priceDifference=price_diff,
            quantityMatch=quantity_match,
            priceMatch=price_match,
            overallMatch=quantity_match and price_match,
        ))

    match_count = sum(1 for d in match_details if d.overallMatch)
    overall_pct = (match_count / len(match_details) * 100) if match_details else 0.0

    return ThreeWayMatchDTO(
        invoiceId=invoice_id,
        poId=po_id or "",
        vendorName=invoice.vendor_id,
        invoiceDate=invoice.created_at,
        totalAmount=invoice.total_amount,
        status=InvoiceStatus(invoice.status),
        matchDetails=match_details,
        overallMatchPercentage=overall_pct,
        createdAt=invoice.created_at,
        validatedAt=invoice.validated_at,
    )


def approve_invoice(db: Session, invoice_id: str, user_id: int, notes: Optional[str]) -> InvoiceActionResponse:
    invoice = db.query(Invoice).filter(Invoice.id == invoice_id).first()
    if not invoice:
        return InvoiceActionResponse(
            success=False,
            message="Invoice not found",
            invoiceId=invoice_id,
            newStatus=InvoiceStatus.PENDING.value,
        )
    now = datetime.now(timezone.utc).isoformat()
    invoice.status = InvoiceStatus.MATCHED.value
    invoice.validated_at = now
    db.add(InvoiceAction(
        invoice_id=invoice_id,
        action_type="APPROVED",
        performed_by=user_id,
        notes=notes,
        timestamp=now,
    ))
    db.commit()
    return InvoiceActionResponse(
        success=True,
        message="Invoice approved successfully",
        invoiceId=invoice_id,
        newStatus=InvoiceStatus.MATCHED.value,
    )


def reject_invoice(db: Session, invoice_id: str, user_id: int, notes: Optional[str]) -> InvoiceActionResponse:
    invoice = db.query(Invoice).filter(Invoice.id == invoice_id).first()
    if not invoice:
        return InvoiceActionResponse(
            success=False,
            message="Invoice not found",
            invoiceId=invoice_id,
            newStatus=InvoiceStatus.PENDING.value,
        )
    now = datetime.now(timezone.utc).isoformat()
    invoice.status = InvoiceStatus.MISMATCH.value
    invoice.validated_at = now
    invoice.rejection_reason = notes or "Rejected by finance manager"
    db.add(InvoiceAction(
        invoice_id=invoice_id,
        action_type="REJECTED",
        performed_by=user_id,
        notes=notes,
        timestamp=now,
    ))
    db.commit()
    return InvoiceActionResponse(
        success=True,
        message="Invoice rejected",
        invoiceId=invoice_id,
        newStatus=InvoiceStatus.MISMATCH.value,
    )


def log_validation_details(db: Session, invoice_id: str, details: List[ValidationDetailDTO]) -> None:
    now = datetime.now(timezone.utc).isoformat()
    for detail in details:
        db.add(ValidationLog(
            invoice_id=invoice_id,
            item_id=detail.itemId,
            po_quantity=detail.poQuantity,
            grn_quantity=detail.grnQuantity,
            invoice_quantity=detail.invoiceQuantity,
            po_price=detail.poPrice,
            invoice_price=detail.invoicePrice,
            price_difference=detail.priceDifference,
            quantity_match=detail.quantityMatch,
            price_match=detail.priceMatch,
            overall_match=detail.overallMatch,
            timestamp=now,
        ))
    db.commit()


def get_recent_activity(db: Session, limit: int = 20) -> List[RecentActivityDTO]:
    """
    Return the most recent invoice actions as an activity feed.
    Equivalent to the /dashboard/activity endpoint.
    """
    rows = (
        db.query(InvoiceAction, Invoice, User)
        .join(Invoice, InvoiceAction.invoice_id == Invoice.id)
        .join(User, InvoiceAction.performed_by == User.id)
        .order_by(desc(InvoiceAction.timestamp))
        .limit(limit)
        .all()
    )
    return [
        RecentActivityDTO(
            id=action.id,
            invoiceId=action.invoice_id,
            vendorName=invoice.vendor_id,
            actionType=action.action_type,
            performedBy=user.full_name,
            timestamp=action.timestamp,
            amount=invoice.total_amount,
        )
        for action, invoice, user in rows
    ]
