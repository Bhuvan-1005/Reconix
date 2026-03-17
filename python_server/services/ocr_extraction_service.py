"""
services/ocr_extraction_service.py - OCR.space API integration + regex parsing
Uses the OCR.space REST API (key configured in core/config.py) to extract text
from PDF and image (JPG/PNG) files, then parses the text into structured data.
"""
import re
import os
import tempfile
from typing import List, Optional

import requests

from core.config import settings
from schemas import OcrExtractedData, OcrLineItem

# Supported file extensions
ALLOWED_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png"}
# MIME types accepted by OCR.space
_MIME_MAP = {
    ".pdf":  "application/pdf",
    ".jpg":  "image/jpeg",
    ".jpeg": "image/jpeg",
    ".png":  "image/png",
}


def is_supported_file(file_name: str) -> bool:
    """Return True if the file extension is one we can process."""
    ext = os.path.splitext(file_name)[1].lower()
    return ext in ALLOWED_EXTENSIONS


def extract_from_pdf(file_path: str, file_name: str | None = None) -> OcrExtractedData:
    """
    Extract data from a file already saved to disk.
    file_name is used to determine the MIME type; falls back to basename if omitted.
    """
    if not os.path.exists(file_path):
        return _empty_result(f"File not found: {file_path}")

    name = file_name or os.path.basename(file_path)
    ext  = os.path.splitext(name)[1].lower()

    if ext not in ALLOWED_EXTENSIONS:
        return _empty_result(f"Unsupported file type: '{ext}'. Please upload a PDF, JPG, or PNG.")

    text = _call_ocr_api(file_path, name)
    if not text or not text.strip():
        return _empty_result("OCR returned no text – file may be blank or unreadable")

    # ── DEBUG: print full raw extracted text to terminal ──────────────────
    print("\n" + "═" * 60, flush=True)
    print(f"📄  OCR RAW TEXT  ─  {name}", flush=True)
    print("═" * 60, flush=True)
    print(text, flush=True)
    print("═" * 60 + "\n", flush=True)

    result = _parse_text(text)

    # ── DEBUG: print parsed structured fields to terminal ──────────────────────
    print("📊  OCR PARSED FIELDS:", flush=True)
    print(f"   Vendor      : {result.vendorName}", flush=True)
    print(f"   PO Number   : {result.detectedPoNumber}", flush=True)
    print(f"   Total Amount: {result.totalAmount}", flush=True)
    print(f"   Confidence  : {result.confidenceScore}%", flush=True)
    print(f"   Line Items  : {len(result.lineItems)} item(s)", flush=True)
    for i, li in enumerate(result.lineItems, 1):
        print(f"     [{i}] {li.description}  qty={li.quantity}  price={li.unitPrice}", flush=True)
    print(flush=True)

    return result


def extract_from_bytes(file_bytes: bytes, file_name: str) -> OcrExtractedData:
    """
    Extract data from raw bytes (e.g. from a FastAPI UploadFile).
    Returns an empty result immediately for unsupported extensions.
    """
    ext = os.path.splitext(file_name)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        return _empty_result(
            f"Unsupported file type '{ext}'. Please upload a PDF, JPG, or PNG."
        )

    suffix = ext or ".pdf"
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
        tmp.write(file_bytes)
        tmp_path = tmp.name
    try:
        return extract_from_pdf(tmp_path, file_name)
    finally:
        try:
            os.unlink(tmp_path)
        except OSError:
            pass


# ─────────────────────────────────────────────────────────────────────────────
# OCR.space API call
# ─────────────────────────────────────────────────────────────────────────────

def _call_ocr_api(file_path: str, file_name: str) -> str:
    """
    Send file to OCR.space and return the extracted plain text.
    Returns "" on any error so callers treat it as no-text.
    """
    ext      = os.path.splitext(file_name)[1].lower()
    mime     = _MIME_MAP.get(ext, "application/octet-stream")
    file_type = ext.lstrip(".")  # "pdf" | "jpg" | "jpeg" | "png"

    print(f"🔍 Sending '{file_name}' to OCR.space API (engine 2)…")
    try:
        with open(file_path, "rb") as fh:
            response = requests.post(
                settings.ocr_api_url,
                data={
                    "apikey":            settings.ocr_api_key,
                    "language":          "eng",
                    "OCREngine":         "2",        # Engine 2 – more accurate
                    "filetype":          file_type.upper(),
                    "scale":             "true",
                    "isTable":           "true",     # Structured invoice tables
                    "detectOrientation": "true",
                },
                files={"file": (file_name, fh, mime)},
                timeout=60,
            )
        response.raise_for_status()
        payload = response.json()

        if payload.get("IsErroredOnProcessing"):
            errors = payload.get("ErrorMessage") or payload.get("ErrorDetails") or "Unknown OCR error"
            print(f"🔍 ❌ OCR.space error: {errors}")
            return ""

        results = payload.get("ParsedResults") or []
        if not results:
            print("🔍 ⚠️ OCR.space returned no ParsedResults")
            return ""

        text = "\n".join(r.get("ParsedText", "") for r in results).strip()
        print(f"🔍 ✅ OCR.space extracted {len(text)} chars")
        return text

    except requests.exceptions.Timeout:
        print("🔍 ❌ OCR.space API timed out")
        return ""
    except requests.exceptions.RequestException as exc:
        print(f"🔍 ❌ OCR.space request failed: {exc}")
        return ""
    except Exception as exc:
        print(f"🔍 ❌ Unexpected OCR error: {exc}")
        return ""


# ─────────────────────────────────────────────────────────────────────────────
# Text parsing
# ─────────────────────────────────────────────────────────────────────────────

def _parse_text(text: str) -> OcrExtractedData:
    match_count = 0
    total_checks = 4

    # PO number
    po_match = re.search(r'(?i)(?:PO|P\.O\.|Purchase Order)[#:\s-]*(\w+-?\d+)', text)
    detected_po = po_match.group(1) if po_match else None
    if detected_po:
        match_count += 1

    # Vendor name
    vendor_match = re.search(r'(?i)(?:from|vendor|supplier|company)[:\s]*([A-Za-z][A-Za-z\s&.,]+)', text)
    vendor_name = vendor_match.group(1).strip() if vendor_match else None
    if vendor_name:
        match_count += 1

    # Total amount
    total_match = re.search(
        r'(?i)(?:total|amount due|grand total|balance due|invoice total)[:\s]*\$?([\d,]+\.?\d*)', text
    )
    total_amount: Optional[float] = None
    if total_match:
        try:
            total_amount = float(total_match.group(1).replace(",", ""))
            match_count += 1
        except ValueError:
            pass

    # Line items: qty  description  unit_price  total
    line_item_pattern = re.compile(r'(\d+)\s+(.+?)\s+\$?([\d,]+\.?\d{0,2})\s+\$?([\d,]+\.?\d{0,2})')
    line_items: List[OcrLineItem] = []
    for m in line_item_pattern.finditer(text):
        try:
            line_items.append(OcrLineItem(
                description=m.group(2).strip(),
                quantity=int(m.group(1)),
                unitPrice=float(m.group(3).replace(",", "")),
                amount=float(m.group(4).replace(",", "")),
            ))
        except ValueError:
            pass
    if line_items:
        match_count += 1

    confidence = (match_count / total_checks) * 100.0
    return OcrExtractedData(
        detectedPoNumber=detected_po,
        vendorName=vendor_name,
        lineItems=line_items,
        totalAmount=total_amount,
        confidenceScore=confidence,
    )


def _empty_result(error_message: str) -> OcrExtractedData:
    print(f"🔍 ⚠️ {error_message}")
    return OcrExtractedData(
        detectedPoNumber=None,
        vendorName=None,
        lineItems=[],
        totalAmount=None,
        confidenceScore=0.0,
    )
