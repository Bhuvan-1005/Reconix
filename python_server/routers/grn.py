"""
routers/grn.py - Goods Receipt Note routes
GET /grn/list
GET /grn/po/{poId}
"""
from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from core.security import any_authenticated
from database import get_db
from schemas import ApiError
from services import invoice_service

router = APIRouter(prefix="/grn", tags=["grn"])


@router.get("/list")
def get_all_grns(db: Session = Depends(get_db), _user: dict = Depends(any_authenticated)):
    """GET /grn/list – Return every GRN across all POs."""
    grns = invoice_service.get_all_grns(db)
    return [g.model_dump() for g in grns]


@router.get("/po/{po_id}")
def get_grns_by_po(po_id: str, db: Session = Depends(get_db), _user: dict = Depends(any_authenticated)):
    """GET /grn/po/{poId} – Return all GRNs for a specific Purchase Order."""
    grns = invoice_service.get_grns_by_po_id(db, po_id)
    return [g.model_dump() for g in grns]
