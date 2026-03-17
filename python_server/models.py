"""
models.py - SQLAlchemy ORM models
Equivalent to server/.../database/Tables.kt
"""
from sqlalchemy import (
    Boolean, Column, Double, ForeignKey, Integer, String, Text
)
from database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String(100), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    full_name = Column(String(255), nullable=False)
    email = Column(String(255), nullable=True)
    role = Column(String(50), nullable=False)
    vendor_id = Column(String(50), nullable=True)
    is_active = Column(Boolean, default=True, nullable=False)
    created_at = Column(String(50), nullable=False)
    last_login_at = Column(String(50), nullable=True)


class PurchaseOrder(Base):
    __tablename__ = "purchase_orders"

    id = Column(String(50), primary_key=True)
    vendor_name = Column(String(255), nullable=False)
    vendor_email = Column(String(255), nullable=True)
    total_amount = Column(Double, nullable=False)
    status = Column(String(20), default="OPEN", nullable=False)
    pdf_path = Column(String(500), nullable=True)
    created_at = Column(String(50), nullable=False)


class PurchaseOrderItem(Base):
    __tablename__ = "purchase_order_items"

    id = Column(Integer, primary_key=True, autoincrement=True)
    po_id = Column(String(50), ForeignKey("purchase_orders.id", ondelete="CASCADE"), nullable=False)
    item_id = Column(String(50), nullable=False)
    item_name = Column(String(255), nullable=False)
    quantity = Column(Integer, nullable=False)
    unit_price = Column(Double, nullable=False)


class Grn(Base):
    __tablename__ = "grns"

    id = Column(String(50), primary_key=True)
    po_id = Column(String(50), ForeignKey("purchase_orders.id", ondelete="CASCADE"), nullable=False)
    received_at = Column(String(50), nullable=False)


class GrnItem(Base):
    __tablename__ = "grn_items"

    id = Column(Integer, primary_key=True, autoincrement=True)
    grn_id = Column(String(50), ForeignKey("grns.id", ondelete="CASCADE"), nullable=False)
    item_id = Column(String(50), nullable=False)
    received_quantity = Column(Integer, nullable=False)


class Invoice(Base):
    __tablename__ = "invoices"

    id = Column(String(50), primary_key=True)
    po_id = Column(String(50), ForeignKey("purchase_orders.id", ondelete="CASCADE"), nullable=True)
    vendor_id = Column(String(50), nullable=False)
    total_amount = Column(Double, nullable=False)
    status = Column(String(20), nullable=False)
    created_at = Column(String(50), nullable=False)
    validated_at = Column(String(50), nullable=True)
    rejection_reason = Column(Text, nullable=True)   # populated when status = MISMATCH/REJECTED


class InvoiceItem(Base):
    __tablename__ = "invoice_items"

    id = Column(Integer, primary_key=True, autoincrement=True)
    invoice_id = Column(String(50), ForeignKey("invoices.id", ondelete="CASCADE"), nullable=False)
    item_id = Column(String(50), nullable=False)
    quantity = Column(Integer, nullable=False)
    unit_price = Column(Double, nullable=False)


class InvoiceAction(Base):
    __tablename__ = "invoice_actions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    invoice_id = Column(String(50), ForeignKey("invoices.id", ondelete="CASCADE"), nullable=False)
    action_type = Column(String(50), nullable=False)
    performed_by = Column(Integer, ForeignKey("users.id"), nullable=False)
    notes = Column(Text, nullable=True)
    timestamp = Column(String(50), nullable=False)


class ValidationLog(Base):
    __tablename__ = "validation_logs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    invoice_id = Column(String(50), ForeignKey("invoices.id", ondelete="CASCADE"), nullable=False)
    item_id = Column(String(50), nullable=False)
    po_quantity = Column(Integer, nullable=False)
    grn_quantity = Column(Integer, nullable=False)
    invoice_quantity = Column(Integer, nullable=False)
    po_price = Column(Double, nullable=False)
    invoice_price = Column(Double, nullable=False)
    price_difference = Column(Double, nullable=False)
    quantity_match = Column(Boolean, nullable=False)
    price_match = Column(Boolean, nullable=False)
    overall_match = Column(Boolean, nullable=False)
    timestamp = Column(String(50), nullable=False)


class InvoiceFile(Base):
    __tablename__ = "invoice_files"

    id = Column(Integer, primary_key=True, autoincrement=True)
    invoice_id = Column(String(50), ForeignKey("invoices.id", ondelete="CASCADE"), nullable=True)
    original_filename = Column(String(500), nullable=False)
    storage_path = Column(String(500), nullable=False)
    file_type = Column(String(20), nullable=False)
    upload_channel = Column(String(20), nullable=False)
    uploaded_at = Column(String(50), nullable=False)
    ocr_processed = Column(Boolean, default=False, nullable=False)


class OcrResult(Base):
    """Stores structured data extracted by OCR.space from an uploaded invoice file."""
    __tablename__ = "ocr_results"

    id = Column(Integer, primary_key=True, autoincrement=True)
    file_id = Column(Integer, ForeignKey("invoice_files.id", ondelete="CASCADE"), nullable=True)
    invoice_id = Column(String(50), ForeignKey("invoices.id", ondelete="SET NULL"), nullable=True)
    detected_po_number = Column(String(50), nullable=True)
    vendor_name = Column(String(255), nullable=True)
    total_amount = Column(Double, nullable=True)
    confidence_score = Column(Double, nullable=False, default=0.0)
    raw_text_preview = Column(Text, nullable=True)   # first 500 chars of OCR text
    extracted_at = Column(String(50), nullable=False)


class OcrLineItemRecord(Base):
    """Stores individual line items detected by OCR.space."""
    __tablename__ = "ocr_line_items"

    id = Column(Integer, primary_key=True, autoincrement=True)
    ocr_result_id = Column(Integer, ForeignKey("ocr_results.id", ondelete="CASCADE"), nullable=False)
    description = Column(Text, nullable=False)
    quantity = Column(Integer, nullable=True)
    unit_price = Column(Double, nullable=True)
    amount = Column(Double, nullable=True)
    matched_po_item_id = Column(String(50), nullable=True)  # PO item this maps to
