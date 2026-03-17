"""
database.py - SQLAlchemy engine, session management, Base
Credentials are loaded via Pydantic Settings (reads .env automatically).
"""
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker
from core.config import settings

# Use settings (reads .env). Allow override via DATABASE_URL env var for cloud.
DATABASE_URL = os.getenv("DATABASE_URL") or settings.database_url
if DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

print(f"✅ Connecting to PostgreSQL at {settings.database_host}:{settings.database_port}/{settings.database_name}")

engine = create_engine(
    DATABASE_URL,
    pool_size=10,
    max_overflow=5,
    pool_pre_ping=True,   # verifies connections before use
    echo=False,
)


class Base(DeclarativeBase):
    pass


SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    """FastAPI dependency that provides a DB session per request."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
