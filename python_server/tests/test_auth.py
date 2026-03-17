"""
tests/test_auth.py
------------------
Tests for POST /auth/login  and  POST /auth/logout
Uses the shared TestClient + PostgreSQL test DB from conftest.py
"""
import pytest


# ── Helpers ──────────────────────────────────────────────────────────────────

def _login(client, username: str, password: str):
    return client.post("/auth/login", json={"username": username, "password": password})


# ── Login – success cases ─────────────────────────────────────────────────────

class TestLoginSuccess:

    def test_vendor_login_returns_200(self, client):
        resp = _login(client, "vendor", "password")
        assert resp.status_code == 200

    def test_vendor_login_body_success_true(self, client):
        body = _login(client, "vendor", "password").json()
        assert body["success"] is True

    def test_vendor_login_contains_token(self, client):
        body = _login(client, "vendor", "password").json()
        assert body.get("token") is not None
        assert len(body["token"]) > 20  # JWT is longer than a trivial string

    def test_vendor_login_token_is_jwt(self, client):
        """A proper JWT has exactly 3 dot-separated segments."""
        token = _login(client, "vendor", "password").json()["token"]
        assert token.count(".") == 2

    def test_vendor_login_user_dto(self, client):
        user = _login(client, "vendor", "password").json()["user"]
        assert user["username"] == "vendor"
        assert user["role"] == "VENDOR"

    def test_finance_login_role(self, client):
        user = _login(client, "finance", "password").json()["user"]
        assert user["role"] == "FINANCE_MANAGER"

    def test_admin_login_role(self, client):
        user = _login(client, "admin", "password").json()["user"]
        assert user["role"] == "ADMIN"

    def test_demo_login(self, client):
        body = _login(client, "demo", "password").json()
        assert body["success"] is True


# ── Login – failure cases ─────────────────────────────────────────────────────

class TestLoginFailure:

    def test_wrong_password_returns_401(self, client):
        resp = _login(client, "vendor", "wrongpassword")
        assert resp.status_code == 401

    def test_wrong_password_success_false(self, client):
        body = _login(client, "vendor", "wrongpassword").json()
        assert body["success"] is False

    def test_unknown_user_returns_401(self, client):
        resp = _login(client, "nonexistent_user_xyz", "password")
        assert resp.status_code == 401

    def test_empty_username_returns_400(self, client):
        resp = _login(client, "   ", "password")
        assert resp.status_code == 400

    def test_empty_password_returns_400(self, client):
        resp = _login(client, "vendor", "   ")
        assert resp.status_code == 400

    def test_no_token_on_failure(self, client):
        body = _login(client, "vendor", "bad").json()
        assert body.get("token") is None


# ── Logout ────────────────────────────────────────────────────────────────────

class TestLogout:

    def test_logout_returns_ok(self, client):
        resp = client.post("/auth/logout")
        assert resp.status_code == 200

    def test_logout_body(self, client):
        body = client.post("/auth/logout").json()
        assert body.get("success") is True
