"""
seed_data.py - Seeds initial data into the database
Equivalent to server/.../database/SeedData.kt
"""
import hashlib
from datetime import datetime, timezone

from sqlalchemy.orm import Session

from models import (
    GrnItem, Grn, PurchaseOrder, PurchaseOrderItem, User,
)


def _hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


def seed(db: Session) -> None:
    """Seed database only if PurchaseOrders table is empty."""
    if db.query(PurchaseOrder).count() > 0:
        return

    now = datetime.now(timezone.utc).isoformat()
    hashed = _hash_password("password")

    # ── Users ───────────────────────────────────────────────────────────────
    db.add_all([
        User(username="vendor",  password_hash=hashed, full_name="Vendor User",    email="vendor@example.com",  role="VENDOR",          is_active=True, created_at=now),
        User(username="admin",   password_hash=hashed, full_name="Admin User",     email="admin@example.com",   role="ADMIN",           is_active=True, created_at=now),
        User(username="demo",    password_hash=hashed, full_name="Demo User",      email="demo@example.com",    role="VENDOR",          is_active=True, created_at=now),
        User(username="finance", password_hash=hashed, full_name="Finance Manager",email="finance@example.com", role="FINANCE_MANAGER", is_active=True, created_at=now),
    ])

    # ── PO-001: Office Supplies ─────────────────────────────────────────────
    db.add(PurchaseOrder(id="PO-001", vendor_name="Office Supplies Co.",   vendor_email="supplies@officesupplies.com",  total_amount=1500.00, status="SENT", created_at=now))
    db.add_all([
        PurchaseOrderItem(po_id="PO-001", item_id="ITEM-001", item_name="Printer Paper (Box)", quantity=50, unit_price=25.00),
        PurchaseOrderItem(po_id="PO-001", item_id="ITEM-002", item_name="Ink Cartridges",       quantity=10, unit_price=25.00),
    ])

    # ── PO-002: Electronics ──────────────────────────────────────────────────
    db.add(PurchaseOrder(id="PO-002", vendor_name="Tech Solutions Ltd.",   vendor_email="billing@techsolutions.com",    total_amount=5000.00, status="SENT", created_at=now))
    db.add_all([
        PurchaseOrderItem(po_id="PO-002", item_id="ITEM-003", item_name="Laptop Stand", quantity=20, unit_price=150.00),
        PurchaseOrderItem(po_id="PO-002", item_id="ITEM-004", item_name="USB-C Hub",    quantity=25, unit_price=80.00),
    ])

    # ── PO-003: Furniture ────────────────────────────────────────────────────
    db.add(PurchaseOrder(id="PO-003", vendor_name="Modern Furniture Inc.", vendor_email="orders@modernfurniture.com",   total_amount=8500.00, status="SENT", created_at=now))
    db.add_all([
        PurchaseOrderItem(po_id="PO-003", item_id="ITEM-005", item_name="Office Chair", quantity=10, unit_price=500.00),
        PurchaseOrderItem(po_id="PO-003", item_id="ITEM-006", item_name="Standing Desk", quantity=5, unit_price=700.00),
    ])

    # Need to flush so FK constraints pass for GRNs
    db.flush()

    # ── GRNs ─────────────────────────────────────────────────────────────────
    db.add(Grn(id="GRN-001", po_id="PO-001", received_at=now))
    db.add_all([
        GrnItem(grn_id="GRN-001", item_id="ITEM-001", received_quantity=30),
        GrnItem(grn_id="GRN-001", item_id="ITEM-002", received_quantity=10),
    ])

    db.add(Grn(id="GRN-002", po_id="PO-001", received_at=now))
    db.add(GrnItem(grn_id="GRN-002", item_id="ITEM-001", received_quantity=20))

    db.add(Grn(id="GRN-003", po_id="PO-002", received_at=now))
    db.add_all([
        GrnItem(grn_id="GRN-003", item_id="ITEM-003", received_quantity=20),
        GrnItem(grn_id="GRN-003", item_id="ITEM-004", received_quantity=25),
    ])

    db.add(Grn(id="GRN-004", po_id="PO-003", received_at=now))
    db.add_all([
        GrnItem(grn_id="GRN-004", item_id="ITEM-005", received_quantity=8),
        GrnItem(grn_id="GRN-004", item_id="ITEM-006", received_quantity=5),
    ])

    db.commit()
    print("✅ Seed data inserted successfully")
