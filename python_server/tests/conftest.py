"""
tests/conftest.py
-----------------
Sets up a dedicated PostgreSQL test database for all tests.
Uses TEST_DATABASE_URL if set, otherwise appends '_test' to the DB name
derived from DATABASE_URL (or the default postgresql://postgres:postgres@localhost:5432/invoice_db_test).
"""
import os
import sys

# Make the repo root importable (tests/ is a sub-directory of python_server/)
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# ── Resolve test database URL ─────────────────────────────────────────────────
def _build_test_db_url() -> str:
    """
    Priority:
    1. TEST_DATABASE_URL env var  (explicitly configured test DB)
    2. DATABASE_URL with the db-name suffixed by '_test'
    3. Default: postgresql://postgres:postgres@localhost:5432/invoice_db_test
    """
    if os.getenv("TEST_DATABASE_URL"):
        url = os.environ["TEST_DATABASE_URL"]
    elif os.getenv("DATABASE_URL"):
        url = os.environ["DATABASE_URL"]
        if url.startswith("postgres://"):
            url = url.replace("postgres://", "postgresql://", 1)
        # Append _test to the database name (last path segment)
        if "/" in url.split("@")[-1]:
            base, db_name = url.rsplit("/", 1)
            db_name = db_name.split("?")[0]  # strip query params
            url = f"{base}/{db_name}_test"
    else:
        host = os.getenv("DATABASE_HOST", "localhost")
        port = os.getenv("DATABASE_PORT", "5432")
        name = os.getenv("DATABASE_NAME", "invoice_db") + "_test"
        user = os.getenv("DATABASE_USER", "postgres")
        pwd  = os.getenv("DATABASE_PASSWORD", "postgres")
        url = f"postgresql://{user}:{pwd}@{host}:{port}/{name}"
    return url


TEST_DATABASE_URL = _build_test_db_url()

# Tell database.py to use the test DB URL
os.environ["DATABASE_URL"] = TEST_DATABASE_URL

# ── These imports must come AFTER setting DATABASE_URL ───────────────────────
from database import Base, get_db  # noqa: E402
from seed_data import seed         # noqa: E402

# ── Test engine / session factory ────────────────────────────────────────────
test_engine = create_engine(
    TEST_DATABASE_URL,
    pool_size=5,
    max_overflow=2,
    pool_pre_ping=True,
)
TestingSession = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)


def override_get_db():
    db = TestingSession()
    try:
        yield db
    finally:
        db.close()


# ── Session-scoped fixtures ───────────────────────────────────────────────────

@pytest.fixture(scope="session", autouse=True)
def setup_test_db():
    """Create schema and seed once for the whole test session."""
    Base.metadata.create_all(bind=test_engine)
    with TestingSession() as db:
        seed(db)
    yield
    # Tear down: dispose connections then drop all tables
    test_engine.dispose()
    Base.metadata.drop_all(bind=test_engine)


@pytest.fixture(scope="session")
def client(setup_test_db):
    """FastAPI TestClient with the DB dependency overridden to use the test PostgreSQL DB."""
    from main import app  # noqa: E402

    app.dependency_overrides[get_db] = override_get_db

    with TestClient(app, raise_server_exceptions=True) as c:
        yield c

    app.dependency_overrides.clear()
