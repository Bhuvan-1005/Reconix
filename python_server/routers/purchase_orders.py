"""
routers/purchase_orders.py - Purchase Order routes
GET  /po/list
POST /po/create
GET  /po/{id}
"""
from fastapi import APIRouter, Depends, status
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from core.security import any_authenticated, finance_or_admin
from database import get_db
from schemas import ApiError, CreatePurchaseOrderRequest
from services import invoice_service, purchase_order_service

router = APIRouter(prefix="/po", tags=["purchase-orders"])


@router.get("/list")
def list_purchase_orders(db: Session = Depends(get_db), _user: dict = Depends(any_authenticated)):
    """GET /po/list – Any authenticated user can list POs."""
    pos = invoice_service.get_all_purchase_orders(db)
    return [p.model_dump() for p in pos]


@router.post("/create", status_code=status.HTTP_201_CREATED)
def create_purchase_order(request: CreatePurchaseOrderRequest, db: Session = Depends(get_db), _user: dict = Depends(finance_or_admin)):
    if not request.vendorName.strip():
        return JSONResponse(
            status_code=400,
            content=ApiError(code=400, message="Vendor name is required").model_dump(),
        )
    if not request.items:
        return JSONResponse(
            status_code=400,
            content=ApiError(code=400, message="At least one line item is required").model_dump(),
        )
    response = purchase_order_service.create_purchase_order(db, request)
    return response.model_dump()


@router.get("/{po_id}")
def get_purchase_order(po_id: str, db: Session = Depends(get_db), _user: dict = Depends(any_authenticated)):
    po = invoice_service.get_purchase_order_by_id(db, po_id)
    if po is None:
        return JSONResponse(
            status_code=404,
            content=ApiError(code=404, message=f"Purchase Order not found: {po_id}").model_dump(),
        )
    return po.model_dump()
