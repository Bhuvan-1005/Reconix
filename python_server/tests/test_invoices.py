"""
tests/test_invoices.py
-----------------------
Tests for the invoice-related endpoints:
  POST /invoice/submit
  GET  /invoice/list
  GET  /invoice/pending
  GET  /invoice/{id}
  GET  /invoice/{id}/match
  POST /invoice/approve
  POST /invoice/reject
  POST /invoice/upload  (basic smoke test)
"""
import io
import time
import pytest


# ── Helpers ──────────────────────────────────────────────────────────────────

def _unique_inv_id():
    return f"INV-TEST-{int(time.time()*1000)}"


def _submit(client, inv_id: str, po_id: str = "PO-001", matched: bool = True):
    """Submit a minimal invoice. matched=True uses quantities/prices from seed data."""
    if matched:
        items = [
            {"itemId": "ITEM-001", "quantity": 10, "unitPrice": 25.00},
            {"itemId": "ITEM-002", "quantity": 5,  "unitPrice": 25.00},
        ]
    else:
        items = [
            {"itemId": "ITEM-001", "quantity": 999, "unitPrice": 999.99},  # big mismatch
        ]
    return client.post(
        "/invoice/submit",
        json={
            "id": inv_id,
            "poId": po_id,
            "vendorId": "vendor",
            "totalAmount": sum(i["quantity"] * i["unitPrice"] for i in items),
            "status": "PENDING",
            "items": items,
        },
    )


# ── GET /invoice/list ─────────────────────────────────────────────────────────

class TestListInvoices:

    def test_returns_200(self, client):
        assert client.get("/invoice/list").status_code == 200

    def test_returns_list(self, client):
        assert isinstance(client.get("/invoice/list").json(), list)


# ── GET /invoice/pending ──────────────────────────────────────────────────────

class TestPendingInvoices:

    def test_returns_200(self, client):
        assert client.get("/invoice/pending").status_code == 200

    def test_returns_list(self, client):
        assert isinstance(client.get("/invoice/pending").json(), list)


# ── POST /invoice/submit ─────────────────────────────────────────────────────

class TestSubmitInvoice:

    def test_matched_invoice_200(self, client):
        resp = _submit(client, _unique_inv_id(), matched=True)
        assert resp.status_code == 200

    def test_matched_invoice_status(self, client):
        body = _submit(client, _unique_inv_id(), matched=True).json()
        assert body["status"] == "MATCHED"

    def test_mismatched_invoice_422(self, client):
        resp = _submit(client, _unique_inv_id(), matched=False)
        assert resp.status_code == 422

    def test_mismatched_invoice_status(self, client):
        body = _submit(client, _unique_inv_id(), matched=False).json()
        assert body["status"] == "MISMATCH"

    def test_unknown_po_manual_review(self, client):
        resp = _submit(client, _unique_inv_id(), po_id="PO-DOES-NOT-EXIST")
        body = resp.json()
        assert body["status"] == "MANUAL_REVIEW"

    def test_empty_id_400(self, client):
        resp = client.post(
            "/invoice/submit",
            json={"id": "  ", "poId": "PO-001", "vendorId": "v",
                  "totalAmount": 100.0, "status": "PENDING",
                  "items": [{"itemId": "X", "quantity": 1, "unitPrice": 100.0}]},
        )
        assert resp.status_code == 400

    def test_empty_po_id_400(self, client):
        resp = client.post(
            "/invoice/submit",
            json={"id": "INV-X", "poId": "  ", "vendorId": "v",
                  "totalAmount": 100.0, "status": "PENDING",
                  "items": [{"itemId": "X", "quantity": 1, "unitPrice": 100.0}]},
        )
        assert resp.status_code == 400

    def test_no_items_400(self, client):
        resp = client.post(
            "/invoice/submit",
            json={"id": "INV-NOIT", "poId": "PO-001", "vendorId": "v",
                  "totalAmount": 0.0, "status": "PENDING", "items": []},
        )
        assert resp.status_code == 400


# ── GET /invoice/{id} ────────────────────────────────────────────────────────

class TestGetInvoiceById:

    def test_nonexistent_returns_404(self, client):
        assert client.get("/invoice/NONEXISTENT-9999").status_code == 404

    def test_submitted_invoice_retrievable(self, client):
        inv_id = _unique_inv_id()
        _submit(client, inv_id, matched=True)
        resp = client.get(f"/invoice/{inv_id}")
        assert resp.status_code == 200
        assert resp.json()["id"] == inv_id


# ── GET /invoice/{id}/match ───────────────────────────────────────────────────

class TestThreeWayMatch:

    def test_nonexistent_returns_404(self, client):
        assert client.get("/invoice/GHOST-99/match").status_code == 404

    def test_valid_invoice_match_report(self, client):
        inv_id = _unique_inv_id()
        _submit(client, inv_id, matched=True)
        resp = client.get(f"/invoice/{inv_id}/match")
        assert resp.status_code == 200
        body = resp.json()
        assert body["invoiceId"] == inv_id
        assert "overallMatchPercentage" in body
        assert "matchDetails" in body

    def test_match_percentage_range(self, client):
        inv_id = _unique_inv_id()
        _submit(client, inv_id, matched=True)
        pct = client.get(f"/invoice/{inv_id}/match").json()["overallMatchPercentage"]
        assert 0.0 <= pct <= 100.0


# ── POST /invoice/approve and /reject ────────────────────────────────────────

class TestApproveReject:

    def _submit_and_get_id(self, client) -> str:
        inv_id = _unique_inv_id()
        _submit(client, inv_id, matched=False)  # mismatched → needs finance review
        return inv_id

    def test_approve_success(self, client):
        inv_id = self._submit_and_get_id(client)
        body = client.post("/invoice/approve",
                           json={"invoiceId": inv_id, "action": "APPROVE",
                                 "notes": "OK"}).json()
        assert body["success"] is True
        assert body["newStatus"] == "MATCHED"

    def test_reject_success(self, client):
        inv_id = self._submit_and_get_id(client)
        body = client.post("/invoice/reject",
                           json={"invoiceId": inv_id, "action": "REJECT",
                                 "notes": "Price wrong"}).json()
        assert body["success"] is True
        assert body["newStatus"] == "MISMATCH"

    def test_approve_unknown_invoice(self, client):
        body = client.post("/invoice/approve",
                           json={"invoiceId": "GHOST", "action": "APPROVE"}).json()
        assert body["success"] is False


# ── POST /invoice/upload (smoke test) ────────────────────────────────────────

class TestUploadInvoice:

    def test_upload_no_file_returns_422(self, client):
        # FastAPI returns 422 when required form field is missing
        resp = client.post("/invoice/upload")
        assert resp.status_code == 422

    def test_upload_pdf_bytes_returns_200(self, client):
        """Upload a minimal 1-byte PDF-ish payload – OCR will find nothing but should not crash."""
        fake_pdf = b"%PDF-1.4 fake"
        files = {"file": ("test.pdf", io.BytesIO(fake_pdf), "application/pdf")}
        resp = client.post("/invoice/upload", files=files)
        assert resp.status_code == 200
        body = resp.json()
        assert "success" in body
