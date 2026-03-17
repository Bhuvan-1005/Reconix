"""
main.py - FastAPI application entry point

Start the server:
    uvicorn main:app --host 0.0.0.0 --port 8081
"""
import logging
import os
import time
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, PlainTextResponse

# ── Logging setup (must be first) ────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger("reconix")

# ── Import models before create_all so SQLAlchemy discovers them ─────────────
import models  # noqa: F401
from database import Base, SessionLocal, engine
from routers import admin, auth, dashboard, grn, invoices, purchase_orders
from seed_data import seed


# ── Startup / shutdown lifecycle ─────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("🚀 Starting Reconix Python Server...")
    Base.metadata.create_all(bind=engine)
    # ── Safe schema migrations (add columns introduced after initial deploy) ──
    _run_migrations()
    with SessionLocal() as db:
        seed(db)
    logger.info("✅ Database ready. Listening for requests.")
    yield
    # Shutdown
    logger.info("🛑 Server shutting down.")


def _run_migrations():
    """Idempotent ALTER TABLE statements for columns added after initial deploy."""
    migrations = [
        "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS rejection_reason TEXT;",
        "ALTER TABLE invoices ALTER COLUMN po_id DROP NOT NULL;",
    ]
    with engine.connect() as conn:
        for sql in migrations:
            try:
                conn.execute(__import__('sqlalchemy').text(sql))
                conn.commit()
            except Exception as exc:  # pragma: no cover
                logger.warning(f"Migration skipped ({sql!r}): {exc}")


# ── FastAPI app ───────────────────────────────────────────────────────────────
app = FastAPI(
    title="Reconix Invoice Validator API",
    description="Python backend for Reconix - converted from Kotlin/Ktor",
    version="2.0.0",
    lifespan=lifespan,
)

# ── CORS ──────────────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "Accept", "*"],
)


# ── Global exception handlers (always return JSON ApiError format) ───────────
@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"code": exc.status_code, "message": str(exc.detail)},
    )


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    msg = "; ".join(f"{'.'.join(str(l) for l in e['loc'])}: {e['msg']}" for e in exc.errors())
    return JSONResponse(
        status_code=422,
        content={"code": 422, "message": msg},
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled error on {request.url.path}: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"code": 500, "message": f"Internal server error: {exc}"},
    )


# ── Request / Response logger middleware ─────────────────────────────────────
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start = time.perf_counter()
    client = request.client.host if request.client else "unknown"
    logger.info(f"→  {request.method:<7} {request.url.path}  [from {client}]")

    try:
        response = await call_next(request)
    except Exception as exc:  # catch anything the exception handlers miss
        logger.error(f"Middleware caught unhandled error: {exc}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"Internal server error: {exc}"},
        )

    ms = (time.perf_counter() - start) * 1000
    s = response.status_code
    color = "\033[32m" if s < 300 else ("\033[33m" if s < 500 else "\033[31m")
    logger.info(f"←  {request.method:<7} {request.url.path}  {color}{s}\033[0m  {ms:.0f}ms")
    return response


# ── Routers ───────────────────────────────────────────────────────────────────
app.include_router(auth.router)
app.include_router(admin.router)
app.include_router(purchase_orders.router)
app.include_router(invoices.router)
app.include_router(dashboard.router)
app.include_router(grn.router)


# ── Health check ──────────────────────────────────────────────────────────────
@app.get("/health", response_class=PlainTextResponse)
def health():
    return "OK"


# ── Entry point ───────────────────────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", "8081"))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=False, access_log=False)

