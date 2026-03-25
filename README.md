# Reconix — Automated Invoice Match Validator

A full-stack financial application that validates vendor invoices against Purchase Orders (PO) and Goods Receipt Notes (GRN) using **3-Way Match** logic.

**Stack:** Kotlin Multiplatform (Compose Multiplatform) frontend + Python/FastAPI backend + PostgreSQL

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                      SHARED MODULE (:shared)                     │
│   SharedTypes.kt — DTOs, InvoiceStatus enum, ApiRoutes           │
└─────────────────────┬────────────────────────┬───────────────────┘
                      │                        │
                      ▼                        ▼
┌─────────────────────────────┐   ┌────────────────────────────────┐
│   BACKEND (python_server/)  │   │   FRONTEND (:composeApp)       │
│                             │   │                                │
│   FastAPI + SQLAlchemy      │   │   Compose Multiplatform        │
│   PostgreSQL                │◄──│   Ktor HTTP Client             │
│   Uvicorn  :8081            │   │   Android / iOS / Desktop      │
└─────────────────────────────┘   └────────────────────────────────┘
```

---

## Project Structure

```
Invoice Project/
├── python_server/               # FastAPI backend
│   ├── main.py                  # App entry point, router mounting
│   ├── models.py                # SQLAlchemy ORM models
│   ├── schemas.py               # Pydantic request/response schemas
│   ├── database.py              # Engine, SessionLocal, Base
│   ├── seed_data.py             # DB seeding (users, POs, GRNs)
│   ├── core/
│   │   ├── config.py            # Pydantic Settings (reads .env)
│   │   └── security.py          # JWT, password hashing, RBAC
│   ├── routers/
│   │   ├── auth.py              # POST /auth/login, /auth/logout
│   │   ├── invoices.py          # Invoice CRUD + approve/reject
│   │   ├── purchase_orders.py   # PO list, detail, create
│   │   ├── grn.py               # GRN endpoints
│   │   ├── dashboard.py         # Metrics and activity feed
│   │   └── admin.py             # Admin-only user management
│   ├── services/
│   │   ├── invoice_service.py          # 3-Way Match logic
│   │   ├── invoice_ingestion_service.py# OCR + Gemini pipeline
│   │   ├── gemini_extraction_service.py# Google Gemini PDF extraction
│   │   ├── ocr_extraction_service.py   # OCR.space integration
│   │   ├── auth_service.py             # Login logic
│   │   ├── purchase_order_service.py   # PO queries
│   │   ├── finance_service.py          # Finance DB queries
│   │   ├── pdf_generator_service.py    # ReportLab PDF generation
│   │   └── email_service.py            # SMTP email dispatch
│   ├── tests/
│   │   ├── conftest.py                 # pytest fixtures, test DB
│   │   ├── test_auth.py
│   │   ├── test_invoices.py
│   │   ├── test_purchase_orders.py
│   │   └── test_dashboard.py
│   ├── scripts/
│   │   └── show_db.py                  # Dev utility: print DB tables
│   ├── uploads/                        # Uploaded invoices and PO PDFs
│   ├── .env                            # Local env vars (not committed)
│   ├── requirements.txt
│   └── pyproject.toml
│
├── composeApp/                  # Kotlin Multiplatform frontend
│   └── src/
│       ├── commonMain/kotlin/com/example/reconix/
│       │   ├── App.kt                       # Root composable / nav graph
│       │   ├── auth/                        # AuthManager, BiometricAuth (expect)
│       │   ├── network/KtorClient.kt        # HTTP client (expect)
│       │   ├── repository/InvoiceRepository.kt  # All API calls
│       │   ├── viewmodel/                   # Admin, Finance, Vendor, InvoiceList VMs
│       │   ├── ui/screens/                  # All screens
│       │   ├── ui/components/               # GlassCard, StatCard, SkeletonLoader, etc.
│       │   └── ui/theme/                    # Color, Typography, Shape, Theme
│       ├── androidMain/   # Android engine, MainActivity, BiometricAuth
│       ├── iosMain/       # iOS engine, MainViewController, BiometricAuth
│       ├── jvmMain/       # Desktop entry point
│       ├── jsMain/        # JS stubs
│       └── wasmJsMain/    # WasmJS stubs
│
├── shared/                      # Shared Kotlin types (all platforms)
│   └── src/commonMain/kotlin/com/example/reconix/shared/
│       └── SharedTypes.kt       # InvoiceDTO, PurchaseOrderDTO, InvoiceStatus, ApiRoutes
│
├── gradle/libs.versions.toml    # Gradle version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Business Logic: 3-Way Match

Invoices are validated against both the PO and the GRN:

| Rule | Condition | Fail message |
|---|---|---|
| Quantity | Invoice qty <= sum of GRN received qty per item | "qty exceeds received qty" |
| Price | Invoice unit price matches PO unit price within tolerance | "price mismatch" |

**Statuses:** `PENDING` → `MATCHED` or `MISMATCH`

---

## Setup & Running

### Prerequisites

- Python 3.11+
- PostgreSQL 16 (running locally)
- Android Studio (for mobile builds)

### 1. Configure the database

Create a PostgreSQL database named `invoice_db`:

```sql
CREATE DATABASE invoice_db;
```

Copy `.env.example` to `.env` (or edit `python_server/.env`) with your credentials:

```env
DATABASE_USER=postgres
DATABASE_PASSWORD=yourpassword
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=invoice_db
SECRET_KEY=your-secret-key
OCR_API_KEY=your-ocr-api-key
GEMINI_API_KEY=your-gemini-api-key
```

### 2. Install Python dependencies

```bash
cd python_server
python -m venv .venv
.venv\Scripts\activate        # Windows
# or: source .venv/bin/activate  # macOS/Linux
pip install -r requirements.txt
```

### 3. Start the backend

```bash
python main.py
```

Server runs at `http://0.0.0.0:8081`. On first start it auto-creates all tables and seeds demo data.

**Key endpoints:**

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Health check |
| POST | `/auth/login` | Login (returns JWT) |
| GET | `/po/list` | List purchase orders |
| POST | `/invoice/submit` | Submit invoice for validation |
| GET | `/invoice/list` | List all invoices |
| POST | `/invoice/{id}/approve` | Approve invoice (Finance) |
| POST | `/invoice/{id}/reject` | Reject invoice (Finance) |
| GET | `/invoice/{id}/three-way-match` | Run 3-way match |
| GET | `/dashboard/metrics` | Dashboard stats |

### 4. Run the Android app

Update the backend IP in `composeApp/src/androidMain/.../KtorClient.android.kt` to your machine's LAN IP (so a physical device can reach it over Wi-Fi):

```kotlin
actual fun platformBaseUrl(): String = "http://<your-lan-ip>:8081"
```

Then build and install:

```bash
.\gradlew.bat :composeApp:installDebug
```

### 5. Run the tests

```bash
cd python_server
pytest tests/ -v
```

---

## Demo Credentials

| Role | Username | Password |
|---|---|---|
| Vendor | vendor | vendor123 |
| Finance | finance | finance123 |
| Admin | admin | admin123 |

---

## License

MIT
