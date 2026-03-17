"""
services/invoice_ingestion_service.py - Handle file uploads, run OCR, auto-create Invoice
Equivalent to server/.../service/InvoiceIngestionService.kt
"""
import os
import re
import time
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy.orm import Session

from models import Invoice, InvoiceFile, InvoiceItem, OcrLineItemRecord, OcrResult, PurchaseOrder, PurchaseOrderItem
from schemas import (
    InvoiceDTO, InvoiceItemDTO, InvoiceStatus,
    InvoiceUploadResponse, ValidationResult,
)
from services import ocr_extraction_service
from services import invoice_service as inv_svc

UPLOAD_DIR = "uploads/invoices"
os.makedirs(UPLOAD_DIR, exist_ok=True)

# Map from MIME type → file extension used for saving + OCR
_MIME_TO_EXT: dict[str, str] = {
    "application/pdf":  ".pdf",
    "image/jpeg":       ".jpg",
    "image/jpg":        ".jpg",
    "image/png":        ".png",
}


def _resolve_filename(file_name: str, content_type: Optional[str]) -> str:
    """
    Return a filename that includes a recognised extension.

    If *file_name* already ends in .pdf/.jpg/.jpeg/.png, return it unchanged.
    Otherwise try to derive the correct extension from *content_type* (MIME type).
    Falls back to ".pdf" when nothing else works.
    """
    if ocr_extraction_service.is_supported_file(file_name):
        return file_name  # already has a good extension

    # Strip any URL/path cruft from the name before appending the extension
    safe_base = re.sub(r"[^a-zA-Z0-9._-]", "_", file_name) or "invoice"

    ext = ".pdf"   # safe default
    if content_type:
        mime = content_type.split(";")[0].strip().lower()  # ignore charset etc.
        ext = _MIME_TO_EXT.get(mime, ".pdf")

    resolved = safe_base + ext
    print(f"[InvoiceIngestion] filename resolved: '{file_name}' + mime='{content_type}' → '{resolved}'", flush=True)
    return resolved


def handle_file_upload(
    db: Session,
    file_name: str,
    file_bytes: bytes,
    channel: str = "DIRECT_UPLOAD",
    content_type: Optional[str] = None,
) -> InvoiceUploadResponse:
    now = datetime.now(timezone.utc).isoformat()

    # ── Validate file name presence ────────────────────────
    if not file_name or file_name.strip() in ("", "unknown"):
        return InvoiceUploadResponse(
            success=False,
            invoiceId=None,
            message="Nothing is uploaded. Please select a PDF, JPG, or PNG file.",
            extractedData=None,
            validationResult=None,
        )

    # Resolve a proper filename even when Android sends a content URI (no extension)
    file_name = _resolve_filename(file_name, content_type)

    if not ocr_extraction_service.is_supported_file(file_name):
        return InvoiceUploadResponse(
            success=False,
            invoiceId=None,
            message=(
                f"Unsupported file format '{os.path.splitext(file_name)[1] or '(none)'}'."
                " Please upload a PDF, JPG, or PNG file."
            ),
            extractedData=None,
            validationResult=None,
        )

    if not file_bytes:
        return InvoiceUploadResponse(
            success=False,
            invoiceId=None,
            message="Nothing is uploaded. The file appears to be empty.",
            extractedData=None,
            validationResult=None,
        )

    # Sanitise filename and persist to disk
    sanitized = re.sub(r"[^a-zA-Z0-9._-]", "_", file_name)
    storage_name = f"{int(time.time() * 1000)}_{sanitized}"
    storage_path = os.path.join(UPLOAD_DIR, storage_name)
    with open(storage_path, "wb") as f:
        f.write(file_bytes)

    lower_name = file_name.lower()
    if lower_name.endswith(".pdf"):
        file_type = "PDF"
    elif any(lower_name.endswith(ext) for ext in (".png", ".jpg", ".jpeg")):
        file_type = "IMAGE"
    else:
        file_type = "UNKNOWN"

    # Save file record
    invoice_file = InvoiceFile(
        original_filename=file_name,
        storage_path=storage_path,
        file_type=file_type,
        upload_channel=channel,
        uploaded_at=now,
        ocr_processed=False,
    )
    db.add(invoice_file)
    db.flush()
    file_id = invoice_file.id

    # Run OCR via OCR.space API
    extracted = ocr_extraction_service.extract_from_pdf(storage_path, file_name)
    invoice_file.ocr_processed = True

    # ── Persist OCR results to database ───────────────────
    ocr_record = OcrResult(
        file_id=file_id,
        invoice_id=None,                         # filled in later if invoice is created
        detected_po_number=extracted.detectedPoNumber,
        vendor_name=extracted.vendorName,
        total_amount=extracted.totalAmount,
        confidence_score=extracted.confidenceScore,
        raw_text_preview=None,                   # OCR.space API mode – no raw text returned
        extracted_at=now,
    )
    db.add(ocr_record)
    db.flush()   # get ocr_record.id

    for li in extracted.lineItems:
        db.add(OcrLineItemRecord(
            ocr_result_id=ocr_record.id,
            description=li.description,
            quantity=li.quantity,
            unit_price=li.unitPrice,
            amount=li.amount,
            matched_po_item_id=None,             # filled in by _process_extracted_invoice
        ))

    db.commit()

    if extracted.detectedPoNumber and extracted.totalAmount is not None:
        return _process_extracted_invoice(db, extracted, file_id, ocr_record.id, now)

    # No PO detected – still create a PENDING invoice so it appears in the list
    invoice_id = f"INV-{int(time.time() * 1000)}"
    db.add(Invoice(
        id=invoice_id,
        po_id=None,
        vendor_id=extracted.vendorName or "Unknown Vendor",
        total_amount=extracted.totalAmount or 0.0,
        status=InvoiceStatus.PENDING.value,
        created_at=now,
        validated_at=None,
    ))
    db.flush()  # ensure Invoice row exists before FK-referencing updates
    db.query(InvoiceFile).filter(InvoiceFile.id == file_id).update({"invoice_id": invoice_id})
    db.query(OcrResult).filter(OcrResult.id == ocr_record.id).update({"invoice_id": invoice_id})
    db.commit()

    return InvoiceUploadResponse(
        success=True,
        invoiceId=invoice_id,
        message=f"Invoice {invoice_id} saved as PENDING. No PO reference detected – manual review required.",
        extractedData=extracted,
        validationResult=None,
    )


def handle_gemini_upload(
    db: Session,
    file_name: str,
    file_bytes: bytes,
    channel: str = "DIRECT_UPLOAD",
    content_type: Optional[str] = None,
) -> InvoiceUploadResponse:
    """
    AI-powered upload path: save file to disk, extract structured data via
    Gemini 1.5 Flash, then run the same PO-matching / 3-way-match pipeline.
    """
    from services import gemini_extraction_service  # local import avoids circular dep

    now = datetime.now(timezone.utc).isoformat()

    # ── Validate file name presence ─────────────────────────────────────────
    if not file_name or file_name.strip() in ("", "unknown"):
        return InvoiceUploadResponse(
            success=False, invoiceId=None,
            message="No file name provided.",
            extractedData=None, validationResult=None,
        )

    # Resolve a proper filename even when Android sends a content URI (no extension)
    file_name = _resolve_filename(file_name, content_type)

    if not ocr_extraction_service.is_supported_file(file_name):
        return InvoiceUploadResponse(
            success=False, invoiceId=None,
            message=(
                f"Unsupported file format '{os.path.splitext(file_name)[1] or '(none)'}'."
                " Please upload a PDF, JPG, or PNG."
            ),
            extractedData=None, validationResult=None,
        )

    if not file_bytes:
        return InvoiceUploadResponse(
            success=False, invoiceId=None,
            message="Uploaded file is empty.",
            extractedData=None, validationResult=None,
        )

    # ── Save to disk ────────────────────────────────────────────────────────
    sanitized = re.sub(r"[^a-zA-Z0-9._-]", "_", file_name)
    storage_name = f"{int(time.time() * 1000)}_{sanitized}"
    storage_path = os.path.join(UPLOAD_DIR, storage_name)
    with open(storage_path, "wb") as f:
        f.write(file_bytes)

    lower_name = file_name.lower()
    if lower_name.endswith(".pdf"):
        file_type = "PDF"
    elif any(lower_name.endswith(ext) for ext in (".png", ".jpg", ".jpeg")):
        file_type = "IMAGE"
    else:
        file_type = "UNKNOWN"

    # ── Persist file record ─────────────────────────────────────────────────
    invoice_file = InvoiceFile(
        original_filename=file_name,
        storage_path=storage_path,
        file_type=file_type,
        upload_channel=channel,
        uploaded_at=now,
        ocr_processed=False,
    )
    db.add(invoice_file)
    db.flush()
    file_id = invoice_file.id

    # ── Gemini extraction ───────────────────────────────────────────────────
    # Determine the actual MIME type from the resolved filename
    _lower = file_name.lower()
    _gemini_mime = (
        "application/pdf" if _lower.endswith(".pdf")
        else "image/png"  if _lower.endswith(".png")
        else "image/jpeg"
    )
    print(f"[Gemini] Starting extraction on {storage_path} (mime={_gemini_mime})", flush=True)
    try:
        extracted = gemini_extraction_service.extract_with_gemini(storage_path, mime_type=_gemini_mime)
        print(f"[Gemini] Extraction OK: vendor={extracted.vendorName} po={extracted.detectedPoNumber} total={extracted.totalAmount}", flush=True)
    except Exception as exc:
        import traceback, logging
        err_trace = traceback.format_exc()
        print("\n" + "!" * 60, flush=True)
        print(f"❌  GEMINI EXTRACTION ERROR: {exc}", flush=True)
        print(err_trace, flush=True)
        print("!" * 60 + "\n", flush=True)
        logging.getLogger("reconix").error(f"Gemini extraction failed: {exc}", exc_info=True)
        db.rollback()
        return InvoiceUploadResponse(
            success=False, invoiceId=None,
            message=f"AI extraction failed: {exc}",
            extractedData=None, validationResult=None,
        )

    invoice_file.ocr_processed = True

    # ── Persist OCR record ──────────────────────────────────────────────────
    ocr_record = OcrResult(
        file_id=file_id,
        invoice_id=None,
        detected_po_number=extracted.detectedPoNumber,
        vendor_name=extracted.vendorName,
        total_amount=extracted.totalAmount,
        confidence_score=extracted.confidenceScore,
        raw_text_preview=None,
        extracted_at=now,
    )
    db.add(ocr_record)
    db.flush()

    for li in extracted.lineItems:
        db.add(OcrLineItemRecord(
            ocr_result_id=ocr_record.id,
            description=li.description,
            quantity=li.quantity,
            unit_price=li.unitPrice,
            amount=li.amount,
            matched_po_item_id=None,
        ))

    db.commit()

    if extracted.detectedPoNumber and extracted.totalAmount is not None:
        return _process_extracted_invoice(db, extracted, file_id, ocr_record.id, now)

    # No PO detected – still create a PENDING invoice so it appears in the list
    invoice_id = f"INV-{int(time.time() * 1000)}"
    db.add(Invoice(
        id=invoice_id,
        po_id=None,
        vendor_id=extracted.vendorName or "Unknown Vendor",
        total_amount=extracted.totalAmount or 0.0,
        status=InvoiceStatus.PENDING.value,
        created_at=now,
        validated_at=None,
    ))
    db.flush()  # ensure Invoice row exists before FK-referencing updates
    db.query(InvoiceFile).filter(InvoiceFile.id == file_id).update({"invoice_id": invoice_id})
    db.query(OcrResult).filter(OcrResult.id == ocr_record.id).update({"invoice_id": invoice_id})
    db.commit()

    return InvoiceUploadResponse(
        success=True,
        invoiceId=invoice_id,
        message=(
            f"Invoice {invoice_id} saved as PENDING. "
            "Gemini AI could not detect a PO reference – manual review required."
        ),
        extractedData=extracted,
        validationResult=None,
    )


def _process_extracted_invoice(
    db,
    extracted_data,
    file_id: int,
    ocr_result_id: int,
    timestamp: str,
) -> InvoiceUploadResponse:
    po_number = extracted_data.detectedPoNumber

    po = db.query(PurchaseOrder).filter(PurchaseOrder.id == po_number).first()
    if po is None:
        return InvoiceUploadResponse(
            success=True,
            invoiceId=None,
            message=f"⚠️ Unknown PO: {po_number} not found in database. Manual review required.",
            extractedData=extracted_data,
            validationResult=ValidationResult(
                status=InvoiceStatus.MANUAL_REVIEW,
                message=f"PO {po_number} not found in the system",
                timestamp=timestamp,
            ),
        )

    # Load PO items for name-based matching
    po_items = db.query(PurchaseOrderItem).filter(PurchaseOrderItem.po_id == po_number).all()
    # index: normalised_name → PurchaseOrderItem
    po_by_name = {row.item_name.lower(): row for row in po_items}

    invoice_id = f"INV-{int(time.time() * 1000)}"
    invoice_items: list[InvoiceItemDTO] = []
    ocr_item_index = 0   # cursor into DB ocr_line_items for this ocr_result

    for idx, li in enumerate(extracted_data.lineItems):
        # ── Match OCR description to a PO item ────────────
        matched_po_item: Optional[PurchaseOrderItem] = _match_po_item(li.description, po_by_name)

        if matched_po_item:
            item_id = matched_po_item.item_id
            unit_price = li.unitPrice if li.unitPrice is not None else matched_po_item.unit_price
        elif po_items:
            # Positional fallback: map by index order
            fallback = po_items[idx % len(po_items)]
            item_id = fallback.item_id
            unit_price = li.unitPrice if li.unitPrice is not None else fallback.unit_price
        else:
            item_id = f"ITEM-{po_number}-{idx + 1:02d}"
            unit_price = li.unitPrice or 0.0

        # Update the persisted ocr_line_item with the matched PO item ID
        db.query(OcrLineItemRecord).filter(
            OcrLineItemRecord.ocr_result_id == ocr_result_id,
            OcrLineItemRecord.description == li.description,
        ).update({"matched_po_item_id": item_id})

        invoice_items.append(InvoiceItemDTO(
            itemId=item_id,
            quantity=li.quantity or 1,
            unitPrice=unit_price,
        ))

    # If the PDF had no line items but we know the total, create a single summary item
    if not invoice_items and po_items:
        first = po_items[0]
        invoice_items.append(InvoiceItemDTO(
            itemId=first.item_id,
            quantity=1,
            unitPrice=extracted_data.totalAmount or 0.0,
        ))

    invoice_dto = InvoiceDTO(
        id=invoice_id,
        poId=po_number,
        vendorId=extracted_data.vendorName or po.vendor_name,
        totalAmount=extracted_data.totalAmount or 0.0,
        status=InvoiceStatus.PENDING,
        items=invoice_items,
    )
    validation_result = inv_svc.validate_invoice(db, invoice_dto)

    # Link file and OCR result to the new invoice
    db.query(InvoiceFile).filter(InvoiceFile.id == file_id).update({"invoice_id": invoice_id})
    db.query(OcrResult).filter(OcrResult.id == ocr_result_id).update({"invoice_id": invoice_id})
    db.commit()

    return InvoiceUploadResponse(
        success=True,
        invoiceId=invoice_id,
        message=(
            f"Invoice {invoice_id} created and stored. "
            f"PO: {po_number}. Validation status: {validation_result.status.value}"
        ),
        extractedData=extracted_data,
        validationResult=validation_result,
    )


def _match_po_item(
    description: str,
    po_by_name: dict,
) -> Optional["PurchaseOrderItem"]:
    """
    Try to match an OCR line-item description against PO item names.
    Strategy (in order):
      1. Exact match
      2. Substring: PO name contained in description
      3. Word-overlap: most words of PO name present in description
    """
    desc_lower = description.lower()

    # 1 – exact
    if desc_lower in po_by_name:
        return po_by_name[desc_lower]

    # 2 – PO name is a substring of OCR description
    for name, item in po_by_name.items():
        if name in desc_lower:
            return item

    # 3 – word overlap: find the PO item whose words best overlap
    desc_words = set(re.split(r'\W+', desc_lower))
    best_item = None
    best_score = 0
    for name, item in po_by_name.items():
        name_words = set(re.split(r'\W+', name))
        overlap = len(desc_words & name_words)
        if overlap > best_score:
            best_score = overlap
            best_item = item

    # Require at least 1 meaningful word overlap
    return best_item if best_score > 0 else None
