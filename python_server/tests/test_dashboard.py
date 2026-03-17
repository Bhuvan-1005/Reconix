"""
tests/test_dashboard.py
------------------------
Tests for  GET /dashboard/metrics  and  GET /dashboard/activity
"""
import pytest


class TestDashboardMetrics:

    def test_returns_200(self, client):
        assert client.get("/dashboard/metrics").status_code == 200

    def test_metrics_has_required_fields(self, client):
        body = client.get("/dashboard/metrics").json()
        expected = (
            "totalPendingInvoices",
            "totalPendingAmount",
            "matchedInvoicesCount",
            "mismatchedInvoicesCount",
            "matchRate",
            "totalPayableAmount",
            "averageProcessingTime",
            "recentActivity",
        )
        for field in expected:
            assert field in body, f"Missing field: {field}"

    def test_match_rate_in_range(self, client):
        rate = client.get("/dashboard/metrics").json()["matchRate"]
        assert 0.0 <= rate <= 100.0

    def test_recent_activity_is_list(self, client):
        activity = client.get("/dashboard/metrics").json()["recentActivity"]
        assert isinstance(activity, list)

    def test_counts_are_non_negative(self, client):
        body = client.get("/dashboard/metrics").json()
        assert body["totalPendingInvoices"] >= 0
        assert body["matchedInvoicesCount"] >= 0
        assert body["mismatchedInvoicesCount"] >= 0


class TestDashboardActivity:

    def test_returns_200(self, client):
        assert client.get("/dashboard/activity").status_code == 200

    def test_returns_list(self, client):
        body = client.get("/dashboard/activity").json()
        assert isinstance(body, list)

    def test_limit_param_respected(self, client):
        body = client.get("/dashboard/activity?limit=3").json()
        assert len(body) <= 3

    def test_activity_item_shape(self, client):
        body = client.get("/dashboard/activity").json()
        if body:
            item = body[0]
            for field in ("invoiceId", "vendorName", "actionType", "performedBy", "timestamp", "amount"):
                assert field in item, f"Missing field: {field}"


class TestGrnEndpoints:
    """GRN endpoint tests bundled here as a sanity check."""

    def test_grn_list_returns_200(self, client):
        assert client.get("/grn/list").status_code == 200

    def test_grn_list_is_list(self, client):
        assert isinstance(client.get("/grn/list").json(), list)

    def test_grn_by_po_returns_list(self, client):
        body = client.get("/grn/po/PO-001").json()
        assert isinstance(body, list)

    def test_grn_items_shape(self, client):
        body = client.get("/grn/po/PO-001").json()
        if body:
            grn = body[0]
            assert "id" in grn
            assert "poId" in grn
            assert "items" in grn


class TestHealthCheck:

    def test_health_ok(self, client):
        resp = client.get("/health")
        assert resp.status_code == 200
        assert resp.text == "OK"
