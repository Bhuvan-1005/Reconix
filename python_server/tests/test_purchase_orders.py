"""
tests/test_purchase_orders.py
------------------------------
Tests for  GET /po/list  POST /po/create  GET /po/{id}
"""
import pytest


# ── GET /po/list ──────────────────────────────────────────────────────────────

class TestListPurchaseOrders:

    def test_returns_200(self, client):
        resp = client.get("/po/list")
        assert resp.status_code == 200

    def test_returns_list(self, client):
        body = client.get("/po/list").json()
        assert isinstance(body, list)

    def test_seeded_po_present(self, client):
        ids = [po["id"] for po in client.get("/po/list").json()]
        assert "PO-001" in ids

    def test_po_has_required_fields(self, client):
        pos = client.get("/po/list").json()
        po = next(p for p in pos if p["id"] == "PO-001")
        for field in ("id", "vendorName", "totalAmount", "items"):
            assert field in po, f"Missing field: {field}"

    def test_po_items_not_empty(self, client):
        pos = client.get("/po/list").json()
        po = next(p for p in pos if p["id"] == "PO-001")
        assert len(po["items"]) > 0


# ── GET /po/{id} ──────────────────────────────────────────────────────────────

class TestGetPurchaseOrderById:

    def test_existing_po_returns_200(self, client):
        resp = client.get("/po/PO-001")
        assert resp.status_code == 200

    def test_existing_po_id_correct(self, client):
        body = client.get("/po/PO-001").json()
        assert body["id"] == "PO-001"

    def test_unknown_po_returns_404(self, client):
        resp = client.get("/po/NON-EXISTENT-PO")
        assert resp.status_code == 404

    def test_404_error_shape(self, client):
        body = client.get("/po/NON-EXISTENT-PO").json()
        assert "message" in body
        assert body["code"] == 404


# ── POST /po/create ───────────────────────────────────────────────────────────

_valid_create_body = {
    "vendorName": "Test Vendor Ltd.",
    "vendorEmail": "test@vendor.com",
    "items": [
        {"itemName": "Widget A", "quantity": 10, "unitPrice": 9.99, "taxRate": 5.0},
        {"itemName": "Widget B", "quantity": 5,  "unitPrice": 19.99, "taxRate": 0.0},
    ],
}


class TestCreatePurchaseOrder:

    def test_create_returns_201(self, client):
        resp = client.post("/po/create", json=_valid_create_body)
        assert resp.status_code == 201

    def test_create_success_flag(self, client):
        body = client.post("/po/create", json=_valid_create_body).json()
        assert body["success"] is True

    def test_create_po_id_present(self, client):
        body = client.post("/po/create", json=_valid_create_body).json()
        assert body.get("poId") is not None
        assert body["poId"].startswith("PO-")

    def test_create_total_amount_positive(self, client):
        body = client.post("/po/create", json=_valid_create_body).json()
        assert body["totalAmount"] > 0

    def test_create_missing_vendor_name_400(self, client):
        bad = {**_valid_create_body, "vendorName": "  "}
        resp = client.post("/po/create", json=bad)
        assert resp.status_code == 400

    def test_create_empty_items_400(self, client):
        bad = {**_valid_create_body, "items": []}
        resp = client.post("/po/create", json=bad)
        assert resp.status_code == 400

    def test_created_po_retrievable(self, client):
        po_id = client.post("/po/create", json=_valid_create_body).json()["poId"]
        resp = client.get(f"/po/{po_id}")
        assert resp.status_code == 200
        assert resp.json()["id"] == po_id
