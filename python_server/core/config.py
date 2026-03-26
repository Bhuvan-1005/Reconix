"""
core/config.py - Centralised environment / settings management
All environment variables are read here via Pydantic Settings.
Usage:
    from core.config import settings
    print(settings.secret_key)
"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # ── Database individual fields (loaded from .env) ──────
    database_user: str = "postgres"
    database_password: str = "postgres"
    database_host: str = "localhost"
    database_port: int = 5432
    database_name: str = "invoice_db"

    @property
    def database_url(self) -> str:
        return (
            f"postgresql://{self.database_user}:{self.database_password}"
            f"@{self.database_host}:{self.database_port}/{self.database_name}"
        )

    # ── Auth / JWT ─────────────────────────────────────────
    # IMPORTANT: override SECRET_KEY in production via environment variable
    secret_key: str = "reconix-secret-change-this-in-production-2025"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 60 * 24  # 24 hours

    # ── Email / SMTP ───────────────────────────────────────
    smtp_host: str = ""
    smtp_port: int = 587
    smtp_user: str = ""
    smtp_password: str = ""

    # ── Email / IMAP ───────────────────────────────────────
    imap_host: str = ""
    imap_port: int = 993
    imap_user: str = ""
    imap_password: str = ""

    # ── Upload directories ─────────────────────────────────
    upload_dir_invoices: str = "uploads/invoices"
    upload_dir_po: str = "uploads/po"

    # ── OCR.space API ──────────────────────────────────────
    ocr_api_key: str = "K86456252188957"
    ocr_api_url: str = "https://api.ocr.space/parse/image"

    # ── Gemini AI ──────────────────────────────────────────
    gemini_api_key: str = "AIzaSyAVJ37fZ5eNJIbjIIMv_7g-V69CWpiUQr4"


settings = Settings()
