"""
schemas.py - Pydantic schemas (equivalent to SharedTypes.kt)
All request/response models used by the API.
"""
from __future__ import annotations
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel


# ─────────────────────────────────────────────
# Enums
# ─────────────────────────────────────────────

class InvoiceStatus(str, Enum):
    PENDING = "PENDING"
    MATCHED = "MATCHED"
    MISMATCH = "MISMATCH"
    MANUAL_REVIEW = "MANUAL_REVIEW"


# ─────────────────────────────────────────────
# User / Auth
# ─────────────────────────────────────────────

class LoginRequest(BaseModel):
    username: str
    password: str


class UserDTO(BaseModel):
    id: int
    username: str
    fullName: str
    email: Optional[str] = None
    role: str
    vendorId: Optional[str] = None


class LoginResponse(BaseModel):
    success: bool
    message: str
    user: Optional[UserDTO] = None
    token: Optional[str] = None


# ─────────────────────────────────────────────
# Purchase Orders
# ─────────────────────────────────────────────

class PurchaseOrderItemDTO(BaseModel):
    itemId: str
    itemName: str
    quantity: int
    unitPrice: float


class PurchaseOrderDTO(BaseModel):
    id: str
    vendorName: str
    vendorEmail: str = ""
    totalAmount: float
    items: List[PurchaseOrderItemDTO]


class CreatePOLineItem(BaseModel):
    itemName: str
    quantity: int
    unitPrice: float
    taxRate: float = 0.0


class CreatePurchaseOrderRequest(BaseModel):
    vendorName: str
    vendorEmail: str = ""
    items: List[CreatePOLineItem]


class CreatePurchaseOrderResponse(BaseModel):
    success: bool
    poId: str
    status: str
    message: str
    totalAmount: float
    pdfGenerated: bool = False
    emailSent: bool = False


# ─────────────────────────────────────────────
# GRN
# ─────────────────────────────────────────────

class GrnItemDTO(BaseModel):
    itemId: str
    receivedQuantity: int


class GrnDTO(BaseModel):
    id: str
    poId: str
    items: List[GrnItemDTO]
    receivedAt: str


# ─────────────────────────────────────────────
# Invoice
# ─────────────────────────────────────────────

class InvoiceItemDTO(BaseModel):
    itemId: str
    quantity: int
    unitPrice: float


class InvoiceDTO(BaseModel):
    id: str
    poId: str = ""
    vendorId: str
    totalAmount: float
    status: InvoiceStatus = InvoiceStatus.PENDING
    items: List[InvoiceItemDTO]
    rejectionReason: Optional[str] = None


class InvoiceListItemDTO(BaseModel):
    id: str
    poId: str = ""
    vendorName: str
    totalAmount: float
    status: InvoiceStatus
    createdAt: str
    itemCount: int
    matchPercentage: Optional[float] = None
    rejectionReason: Optional[str] = None   # surfaced to vendor when rejected


# ─────────────────────────────────────────────
# Validation / 3-Way Match
# ─────────────────────────────────────────────

class ValidationDetailDTO(BaseModel):
    itemId: str
    itemName: str
    poQuantity: int
    grnQuantity: int
    invoiceQuantity: int
    poPrice: float
    invoicePrice: float
    priceDifference: float
    quantityMatch: bool
    priceMatch: bool
    overallMatch: bool


class ValidationResult(BaseModel):
    status: InvoiceStatus
    message: str
    timestamp: str
    details: Optional[List[ValidationDetailDTO]] = None


class ThreeWayMatchDTO(BaseModel):
    invoiceId: str
    poId: str = ""
    vendorName: str
    invoiceDate: str
    totalAmount: float
    status: InvoiceStatus
    matchDetails: List[ValidationDetailDTO]
    overallMatchPercentage: float
    createdAt: str
    validatedAt: Optional[str] = None


# ─────────────────────────────────────────────
# Invoice Actions
# ─────────────────────────────────────────────

class InvoiceActionRequest(BaseModel):
    invoiceId: str
    action: str  # APPROVE / REJECT
    notes: Optional[str] = None


class InvoiceActionResponse(BaseModel):
    success: bool
    message: str
    invoiceId: str
    newStatus: str


# ─────────────────────────────────────────────
# Dashboard
# ─────────────────────────────────────────────

class RecentActivityDTO(BaseModel):
    id: int
    invoiceId: str
    vendorName: str
    actionType: str
    performedBy: str
    timestamp: str
    amount: float


class DashboardMetricsDTO(BaseModel):
    totalPendingInvoices: int
    totalPendingAmount: float
    matchedInvoicesCount: int
    mismatchedInvoicesCount: int
    matchRate: float
    totalPayableAmount: float
    averageProcessingTime: str
    recentActivity: List[RecentActivityDTO]


# ─────────────────────────────────────────────
# OCR / Upload
# ─────────────────────────────────────────────

class OcrLineItem(BaseModel):
    description: str
    quantity: Optional[int] = None
    unitPrice: Optional[float] = None
    amount: Optional[float] = None


class OcrExtractedData(BaseModel):
    detectedPoNumber: Optional[str] = None
    vendorName: Optional[str] = None
    lineItems: List[OcrLineItem] = []
    totalAmount: Optional[float] = None
    confidenceScore: float = 0.0
    # ── Gemini-enriched fields (null when extracted via OCR.space) ──
    invoiceNumber: Optional[str] = None
    date: Optional[str] = None
    taxAmount: Optional[float] = None


class InvoiceUploadResponse(BaseModel):
    success: bool
    invoiceId: Optional[str] = None
    message: str
    extractedData: Optional[OcrExtractedData] = None
    validationResult: Optional[ValidationResult] = None


# ─────────────────────────────────────────────
# Error
# ─────────────────────────────────────────────

class ApiError(BaseModel):
    code: int
    message: str
