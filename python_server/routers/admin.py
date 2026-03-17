"""
routers/admin.py - Admin-only routes (RBAC protected)
POST /admin/users              — Create a new user (Finance Manager / Vendor)
GET  /admin/users              — List all users
PUT  /admin/users/{id}/deactivate — Deactivate a user
GET  /admin/audit              — Full audit log
GET  /admin/tolerance          — Get current tolerance config
PUT  /admin/tolerance          — Update tolerance config
"""
from datetime import datetime, timezone
from typing import List, Optional

from fastapi import APIRouter, Depends, status
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from sqlalchemy.orm import Session

from core.security import admin_only
from database import get_db
from models import InvoiceAction, Invoice, User
from schemas import ApiError, UserDTO
from core.security import hash_password

router = APIRouter(prefix="/admin", tags=["admin"])


# ── Schemas ──────────────────────────────────────────────────────────────────

class CreateUserRequest(BaseModel):
    username: str
    fullName: str
    email: Optional[str] = None
    role: str           # FINANCE_MANAGER | VENDOR
    password: str
    vendorId: Optional[str] = None


class CreateUserResponse(BaseModel):
    success: bool
    message: str
    user: Optional[UserDTO] = None


class AuditLogItem(BaseModel):
    id: int
    invoiceId: str
    actionType: str
    performedBy: str
    notes: Optional[str]
    timestamp: str
    invoiceTotal: float


class ToleranceConfig(BaseModel):
    priceTolerancePct: float    # e.g.  2.0  →  2 %
    quantityTolerance: int      # e.g.  1    →  1 unit
    amountThreshold: float      # e.g.  50.0 →  ₹50 rounding ignore


# ── In-memory tolerance store (replace with DB table if needed) ──────────────
_tolerance = ToleranceConfig(priceTolerancePct=2.0, quantityTolerance=1, amountThreshold=50.0)


# ── Endpoints ────────────────────────────────────────────────────────────────

@router.post("/users", response_model=CreateUserResponse)
def create_user(
    req: CreateUserRequest,
    db: Session = Depends(get_db),
    _: dict = Depends(admin_only),
):
    """Create a Finance Manager or Vendor account.  Admin only."""
    req.role = req.role.upper()
    if req.role not in ("FINANCE_MANAGER", "VENDOR", "ADMIN"):
        return JSONResponse(
            status_code=400,
            content=ApiError(code=400, message="role must be FINANCE_MANAGER, VENDOR, or ADMIN").model_dump(),
        )
    if db.query(User).filter(User.username == req.username).first():
        return JSONResponse(
            status_code=409,
            content=ApiError(code=409, message=f"Username '{req.username}' already exists").model_dump(),
        )
    now = datetime.now(timezone.utc).isoformat()
    new_user = User(
        username=req.username,
        full_name=req.fullName,
        email=req.email,
        role=req.role,
        password_hash=hash_password(req.password),
        vendor_id=req.vendorId,
        is_active=True,
        created_at=now,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return CreateUserResponse(
        success=True,
        message=f"User '{req.username}' created with role {req.role}",
        user=UserDTO(
            id=new_user.id,
            username=new_user.username,
            fullName=new_user.full_name,
            email=new_user.email,
            role=new_user.role,
            vendorId=new_user.vendor_id,
        ),
    )


@router.get("/users")
def list_users(
    db: Session = Depends(get_db),
    _: dict = Depends(admin_only),
):
    """Return all users.  Admin only."""
    users = db.query(User).all()
    return [
        UserDTO(
            id=u.id,
            username=u.username,
            fullName=u.full_name,
            email=u.email,
            role=u.role,
            vendorId=u.vendor_id,
        ).model_dump() | {"isActive": u.is_active}
        for u in users
    ]


@router.put("/users/{user_id}/deactivate")
def deactivate_user(
    user_id: int,
    db: Session = Depends(get_db),
    current: dict = Depends(admin_only),
):
    """Deactivate a user.  Admin only (cannot deactivate self)."""
    if user_id == current.get("user_id"):
        return JSONResponse(
            status_code=400,
            content=ApiError(code=400, message="Cannot deactivate your own account").model_dump(),
        )
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        return JSONResponse(
            status_code=404,
            content=ApiError(code=404, message=f"User {user_id} not found").model_dump(),
        )
    user.is_active = False
    db.commit()
    return {"success": True, "message": f"User '{user.username}' deactivated"}


@router.get("/audit")
def get_audit_log(
    limit: int = 50,
    db: Session = Depends(get_db),
    _: dict = Depends(admin_only),
):
    """Full audit trail of invoice actions.  Admin only."""
    from sqlalchemy import desc
    rows = (
        db.query(InvoiceAction, Invoice, User)
        .join(Invoice, InvoiceAction.invoice_id == Invoice.id)
        .join(User, InvoiceAction.performed_by == User.id)
        .order_by(desc(InvoiceAction.timestamp))
        .limit(limit)
        .all()
    )
    return [
        AuditLogItem(
            id=action.id,
            invoiceId=action.invoice_id,
            actionType=action.action_type,
            performedBy=user.full_name,
            notes=action.notes,
            timestamp=action.timestamp,
            invoiceTotal=invoice.total_amount,
        ).model_dump()
        for action, invoice, user in rows
    ]


@router.get("/tolerance")
def get_tolerance(_: dict = Depends(admin_only)):
    """Get current tolerance configuration.  Admin only."""
    return _tolerance.model_dump()


@router.put("/tolerance")
def update_tolerance(
    config: ToleranceConfig,
    _: dict = Depends(admin_only),
):
    """Update tolerance configuration.  Admin only."""
    global _tolerance
    _tolerance = config
    return {"success": True, "tolerance": _tolerance.model_dump()}
