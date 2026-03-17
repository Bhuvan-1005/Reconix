"""
routers/invoices.py - Invoice routes
POST /invoice/submit
GET  /invoice/list
POST /invoice/upload
GET  /invoice/pending
GET  /invoice/{id}
GET  /invoice/{id}/match
POST /invoice/approve
POST /invoice/reject
"""
from fastapi import APIRouter, Depends, File, UploadFile, status
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from core.security import any_authenticated, finance_or_admin
from database import get_db
from models import User
from schemas import ApiError, InvoiceActionRequest, InvoiceDTO, InvoiceStatus
from services import finance_service, invoice_ingestion_service, invoice_service

router = APIRouter(prefix="/invoice", tags=["invoices"])


@router.post("/submit")
def submit_invoice(invoice: InvoiceDTO, db: Session = Depends(get_db), _user: dict = Depends(any_authenticated)):
    """POST /invoice/submit – Validate an invoice via 3-way match."""
    if not invoice.id.strip():
        return JSONResponse(status_code=400, content=ApiError(code=400, message="Invoice ID is required").model_dump())
    if not invoice.poId.strip():
        return JSONResponse(status_code=400, content=ApiError(code=400, message="PO ID is required").model_dump())
    if not invoice.items:
        return JSONResponse(status_code=400, content=ApiError(code=400, message="Invoice must have at least one item").model_dump())

    result = invoice_service.validate_invoice(db, invoice)

    status_map = {
        InvoiceStatus.MATCHED:       status.HTTP_200_OK,
        InvoiceStatus.MISMATCH:      422,
        InvoiceStatus.PENDING:       status.HTTP_202_ACCEPTED,
        InvoiceStatus.MANUAL_REVIEW: status.HTTP_202_ACCEPTED,
    }
    http_status = status_map.get(result.status, status.HTTP_200_OK)
    return JSONResponse(status_code=http_status, content=result.model_dump())


@router.get("/list")
def list_invoices(db: Session = Depends(get_db), user: dict = Depends(any_authenticated)):
    """GET /invoice/list – Finance/Admin see all; Vendor sees only their own."""
    role = (user.get("role") or "").upper()
    if role == "VENDOR":
        user_id = user.get("user_id")
        db_user = db.query(User).filter(User.id == user_id).first()
        vendor_id = db_user.vendor_id if db_user else None
        invoices = invoice_service.get_invoices_by_vendor(db, vendor_id) if vendor_id else []
    else:
        invoices = invoice_service.get_all_invoices(db)
    return [i.model_dump() for i in invoices]


@router.post("/upload")
async def upload_invoice(file: UploadFile = File(None), db: Session = Depends(get_db), _user: dict = Depends(any_authenticated)):
    """POST /invoice/upload – Accept a PDF/image, run OCR, auto-match against a PO."""
    # Guard: no file provided at all
    if file is None or not file.filename:
        return JSONResponse(
            status_code=400,
            content=ApiError(
                code=400,
                message="Nothing is uploaded. Please select a PDF, JPG, or PNG file.",
            ).model_dump(),
        )

    file_bytes = await file.read()

    # Guard: empty file body
    if not file_bytes:
        return JSONResponse(
            status_code=400,
            content=ApiError(
                code=400,
                message="Nothing is uploaded. The selected file is empty.",
            ).model_dump(),
        )

    file_name = file.filename
    response = invoice_ingestion_service.handle_file_upload(
        db=db, file_name=file_name, file_bytes=file_bytes,
        channel="DIRECT_UPLOAD", content_type=file.content_type,
    )
    http_status = status.HTTP_200_OK if response.success else status.HTTP_400_BAD_REQUEST
    return JSONResponse(status_code=http_status, content=response.model_dump())


@router.post("/gemini-extract")
async def gemini_extract_invoice(
    file: UploadFile = File(None),
    db: Session = Depends(get_db),
    _user: dict = Depends(any_authenticated),
):
    """
    POST /invoice/gemini-extract
    AI-powered extraction using Gemini 1.5 Flash.
    Accepts the same multipart/form-data body as /invoice/upload.
    Returns InvoiceUploadResponse with richer extracted fields
    (invoiceNumber, date, taxAmount) that OCR.space cannot provide.
    """
    if file is None or not file.filename:
        return JSONResponse(
            status_code=400,
            content=ApiError(code=400, message="No file uploaded.").model_dump(),
        )

    file_bytes = await file.read()
    if not file_bytes:
        return JSONResponse(
            status_code=400,
            content=ApiError(code=400, message="Uploaded file is empty.").model_dump(),
        )

    response = invoice_ingestion_service.handle_gemini_upload(
        db=db, file_name=file.filename, file_bytes=file_bytes,
        channel="DIRECT_UPLOAD", content_type=file.content_type,
    )
    http_status = status.HTTP_200_OK if response.success else status.HTTP_400_BAD_REQUEST
    return JSONResponse(status_code=http_status, content=response.model_dump())


@router.get("/pending")
def get_pending_invoices(db: Session = Depends(get_db), _user: dict = Depends(finance_or_admin)):
    """GET /invoice/pending – Finance manager: list pending invoices."""
    invoices = finance_service.get_pending_invoices(db)
    return [i.model_dump() for i in invoices]


@router.post("/approve")
def approve_invoice(request: InvoiceActionRequest, db: Session = Depends(get_db), user: dict = Depends(finance_or_admin)):
    """POST /invoice/approve – Finance manager approves an invoice."""
    user_id = user.get("user_id", 1)
    response = finance_service.approve_invoice(db, request.invoiceId, user_id, request.notes)
    return response.model_dump()


@router.post("/reject")
def reject_invoice(request: InvoiceActionRequest, db: Session = Depends(get_db), user: dict = Depends(finance_or_admin)):
    """POST /invoice/reject – Finance manager rejects an invoice."""
    user_id = user.get("user_id", 1)
    response = finance_service.reject_invoice(db, request.invoiceId, user_id, request.notes)
    return response.model_dump()


# ── Parameterised routes declared LAST to avoid shadowing /list, /pending etc ─

@router.get("/{invoice_id}/match")
def get_three_way_match(invoice_id: str, db: Session = Depends(get_db)):
    """GET /invoice/{id}/match – Full 3-way match report for one invoice."""
    match = finance_service.get_three_way_match(db, invoice_id)
    if match is None:
        return JSONResponse(
            status_code=404,
            content=ApiError(code=404, message=f"Invoice not found: {invoice_id}").model_dump(),
        )
    return match.model_dump()


@router.get("/{invoice_id}")
def get_invoice_by_id_route(invoice_id: str, db: Session = Depends(get_db)):
    """GET /invoice/{id} – Fetch a single invoice by its ID."""
    invoice = invoice_service.get_invoice_by_id(db, invoice_id)
    if invoice is None:
        return JSONResponse(
            status_code=404,
            content=ApiError(code=404, message=f"Invoice not found: {invoice_id}").model_dump(),
        )
    return invoice.model_dump()
