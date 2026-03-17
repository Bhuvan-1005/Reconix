"""
services/gemini_extraction_service.py
──────────────────────────────────────
Gemini 2.0 Flash integration for structured invoice extraction.

Replaces OCR.space regex-parsing with a multimodal LLM that reads the PDF
directly and returns a clean JSON payload in a single API call.

Requires:
    pip install google-generativeai

Environment:
    GEMINI_API_KEY  –  obtained from https://aistudio.google.com/app/apikey
"""

import json
import logging
import os
from typing import Optional

import google.generativeai as genai  # pip install google-generativeai

from core.config import settings
from schemas import OcrExtractedData, OcrLineItem

logger = logging.getLogger("reconix.gemini")

# ── Extraction prompt ──────────────────────────────────────────────────────────
_EXTRACTION_PROMPT = """
Analyze the invoice PDF attached and extract the following fields.
Return ONLY a valid JSON object — no markdown, no code fences, no extra commentary.

Required schema:
{
  "VendorName":     "<string or null>",
  "InvoiceNumber":  "<string or null>",
  "Date":           "<ISO date string or null>",
  "PONumber":       "<string or null>",
  "TaxAmount":      <number or null>,
  "TotalAmount":    <number or null>,
  "LineItems": [
    {
      "Description": "<string>",
      "Quantity":    <integer>,
      "UnitPrice":   <number>
    }
  ]
}

Rules:
- If a field is not present in the document, set it to null.
- TaxAmount and TotalAmount must be numeric (no currency symbols).
- Quantity must be an integer.
- UnitPrice must be a decimal number.
- LineItems must be a JSON array; use [] if no line items are visible.
""".strip()


# ── Public API ─────────────────────────────────────────────────────────────────

def extract_with_gemini(file_path: str, mime_type: str = "application/pdf") -> OcrExtractedData:
    """
    Upload *file_path* to the Gemini File API, run `gemini-2.0-flash` on it,
    and parse the structured JSON response into an OcrExtractedData object.

    Args:
        file_path: Path to the file on disk.
        mime_type: MIME type to pass to the Gemini File API.  Defaults to
                   "application/pdf"; pass "image/jpeg" or "image/png" for images.

    Uses gemini-2.0-flash model.

    Raises:
        ValueError  – if GEMINI_API_KEY is missing or the model returns non-JSON.
        Exception   – re-raised on network / quota errors so callers can handle.
    """
    if not settings.gemini_api_key:
        raise ValueError(
            "GEMINI_API_KEY is not configured. "
            "Set it as an environment variable or in the .env file."
        )

    # ── 1. Configure the old google.generativeai SDK ──────────────────
    import warnings
    warnings.filterwarnings("ignore", category=FutureWarning)
    genai.configure(api_key=settings.gemini_api_key)

    # ── 2. Upload file to Gemini File API ─────────────────────────────
    logger.info(f"[Gemini] Uploading '{os.path.basename(file_path)}' (mime={mime_type}) ...")
    uploaded_file = genai.upload_file(file_path, mime_type=mime_type)
    logger.info(f"[Gemini] File URI: {uploaded_file.uri}")

    # ── 3. Run the model ──────────────────────────────────────────────
    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        generation_config=genai.GenerationConfig(
            response_mime_type="application/json",  # enforce JSON output
            temperature=0.0,                         # deterministic extraction
        ),
    )
    response = model.generate_content([uploaded_file, _EXTRACTION_PROMPT])
    raw_text = (response.text or "").strip()
    logger.info(f"[Gemini] Response preview: {raw_text[:400]}")

    # ── DEBUG: print full Gemini response to terminal ─────────────────────
    print("\n" + "═" * 60, flush=True)
    print(f"🤖  GEMINI RAW RESPONSE  ─  {os.path.basename(file_path)}", flush=True)
    print("═" * 60, flush=True)
    print(raw_text, flush=True)
    print("═" * 60 + "\n", flush=True)

    # ── 3. Parse JSON (strip markdown code fences if the model adds them) ──────
    if raw_text.startswith("```"):
        raw_text = raw_text.split("```", 2)[1]
        if raw_text.startswith("json"):
            raw_text = raw_text[4:]
        raw_text = raw_text.rsplit("```", 1)[0].strip()

    try:
        data: dict = json.loads(raw_text)
    except json.JSONDecodeError as exc:
        raise ValueError(
            f"Gemini returned non-JSON output: {raw_text[:300]}"
        ) from exc

    # ── 4. Map to OcrExtractedData ─────────────────────────────────────────────
    line_items = [
        OcrLineItem(
            description=li.get("Description") or "",
            quantity=_to_int(li.get("Quantity")),
            unitPrice=_to_float(li.get("UnitPrice")),
            amount=_safe_amount(li),
        )
        for li in (data.get("LineItems") or [])
    ]

    result = OcrExtractedData(
        detectedPoNumber=data.get("PONumber"),
        vendorName=data.get("VendorName"),
        lineItems=line_items,
        totalAmount=_to_float(data.get("TotalAmount")),
        confidenceScore=95.0,      # Gemini multimodal — high baseline confidence
        invoiceNumber=data.get("InvoiceNumber"),
        date=data.get("Date"),
        taxAmount=_to_float(data.get("TaxAmount")),
    )

    # ── DEBUG: print parsed Gemini fields to terminal ────────────────────────
    print("🤖  GEMINI PARSED FIELDS:", flush=True)
    print(f"   Vendor        : {result.vendorName}", flush=True)
    print(f"   Invoice Number: {result.invoiceNumber}", flush=True)
    print(f"   Invoice Date  : {result.date}", flush=True)
    print(f"   PO Number     : {result.detectedPoNumber}", flush=True)
    print(f"   Total Amount  : {result.totalAmount}", flush=True)
    print(f"   Tax Amount    : {result.taxAmount}", flush=True)
    print(f"   Line Items    : {len(result.lineItems)} item(s)", flush=True)
    for i, li in enumerate(result.lineItems, 1):
        print(f"     [{i}] {li.description}  qty={li.quantity}  price={li.unitPrice}", flush=True)
    print(flush=True)

    return result


# ── Private helpers ────────────────────────────────────────────────────────────

def _to_float(val) -> Optional[float]:
    """Safely coerce val to float; returns None on failure."""
    try:
        return float(val) if val is not None else None
    except (TypeError, ValueError):
        return None


def _to_int(val) -> Optional[int]:
    """Safely coerce val to int; returns None on failure."""
    try:
        return int(float(val)) if val is not None else None
    except (TypeError, ValueError):
        return None


def _safe_amount(li: dict) -> Optional[float]:
    """Compute line total = quantity × unit_price, or None if either is missing."""
    q = _to_float(li.get("Quantity"))
    p = _to_float(li.get("UnitPrice"))
    return round(q * p, 2) if (q is not None and p is not None) else None
