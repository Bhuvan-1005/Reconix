# Automated Invoice Match Validator

A **Full Stack Kotlin Multiplatform** financial application that validates vendor invoices against Purchase Orders (PO) and Goods Receipt Notes (GRN) using **3-Way Match** validation logic.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        SHARED MODULE (:shared)                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  SharedTypes.kt                                          │   │
│  │  - InvoiceDTO, PurchaseOrderDTO, ValidationResult        │   │
│  │  - InvoiceStatus enum (MATCHED, MISMATCH, PENDING)       │   │
│  │  - ApiRoutes object                                      │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────┬───────────────┘
                        │                         │
                        ▼                         ▼
┌───────────────────────────────┐   ┌────────────────────────────┐
│     BACKEND (:server)         │   │   FRONTEND (:composeApp)   │
│  ┌─────────────────────────┐  │   │  ┌──────────────────────┐  │
│  │ Ktor Server (Netty)     │  │   │  │ Jetpack Compose      │  │
│  │ - POST /invoice/submit  │◄─┼───┼──│ - VendorDashboard    │  │
│  │ - GET /po/list          │  │   │  │ - VendorViewModel    │  │
│  └─────────────────────────┘  │   │  └──────────────────────┘  │
│  ┌─────────────────────────┐  │   │  ┌──────────────────────┐  │
│  │ Exposed ORM             │  │   │  │ Ktor Client          │  │
│  │ - PostgreSQL            │  │   │  │ - InvoiceRepository  │  │
│  │ - HikariCP Pool         │  │   │  │ - KtorClient         │  │
│  └─────────────────────────┘  │   │  └──────────────────────┘  │
└───────────────────────────────┘   └────────────────────────────┘
```

## 📋 Business Logic: 3-Way Match

The system validates Vendor Invoices against Purchase Orders (PO) and Goods Receipt Notes (GRN):

### Validation Rules:
1. **Quantity Rule**: Invoice Quantity must be ≤ Sum(GRN Quantities) for each item
2. **Price Rule**: Invoice Unit Price must match PO Unit Price (±$0.05 tolerance)

### Validation Status:
- ✅ **MATCHED**: All rules passed
- ❌ **MISMATCH**: One or more rules failed
- ⏳ **PENDING**: Awaiting validation

## 🛠️ Project Structure

```
Invoice Project/
├── shared/                          # Shared Module
│   └── src/commonMain/kotlin/
│       └── com/example/reconix/shared/
│           └── SharedTypes.kt       # DTOs, Enums, API Routes
│
├── server/                          # Backend Module
│   └── src/main/kotlin/
│       └── com/example/reconix/server/
│           ├── Application.kt       # Ktor entry point
│           ├── database/
│           │   ├── DatabaseFactory.kt
│           │   ├── Tables.kt        # Exposed Tables
│           │   └── SeedData.kt
│           ├── service/
│           │   └── InvoiceService.kt # 3-Way Match Logic
│           └── plugins/
│               ├── Routing.kt
│               ├── Serialization.kt
│               ├── StatusPages.kt
│               └── Cors.kt
│
├── composeApp/                      # Frontend Module
│   └── src/
│       ├── commonMain/kotlin/
│       │   └── com/example/reconix/
│       │       ├── App.kt
│       │       ├── network/KtorClient.kt
│       │       ├── repository/InvoiceRepository.kt
│       │       ├── viewmodel/VendorViewModel.kt
│       │       └── ui/VendorDashboard.kt
│       ├── androidMain/kotlin/      # Android Ktor Client
│       ├── iosMain/kotlin/          # iOS Ktor Client
│       └── jvmMain/kotlin/          # Desktop Ktor Client
│
├── docker-compose.yml               # PostgreSQL setup
└── gradle/libs.versions.toml        # Version catalog
```

## 🚀 Implementation Steps

### Prerequisites
- JDK 11+
- Docker Desktop
- Android Studio / IntelliJ IDEA
- Android SDK (for mobile development)

### Step 1: Start PostgreSQL Database

```powershell
# Start the PostgreSQL container
docker-compose up -d

# Verify it's running
docker ps

# Check logs
docker-compose logs postgres
```

**Database Configuration:**
- Host: `localhost`
- Port: `5432`
- User: `user`
- Password: `password`
- Database: `invoice_db`

### Step 2: Run the Ktor Server

```powershell
# From project root (Windows)
.\gradlew.bat :server:run

# macOS/Linux
./gradlew :server:run
```

**Server endpoints available at `http://localhost:8080`:**
- `GET /health` - Health check
- `GET /po/list` - List all Purchase Orders
- `GET /po/{id}` - Get specific PO
- `POST /invoice/submit` - Submit invoice for validation
- `GET /invoice/list` - List all invoices
- `GET /grn/po/{poId}` - Get GRNs for a PO

### Step 3: Run the Android App

```powershell
# Install on connected device/emulator (Windows)
.\gradlew.bat :composeApp:installDebug

# macOS/Linux
./gradlew :composeApp:installDebug
```

**Note:** The Android app connects to `http://10.0.2.2:8080/` (emulator localhost mapping).

## 🔒 Type Safety Guarantee

The **Critical Requirement** is achieved through the shared module:

```kotlin
// shared/src/commonMain/kotlin/.../SharedTypes.kt
@Serializable
data class InvoiceDTO(
    val id: String,
    val poId: String,
    val vendorId: String,
    val totalAmount: Double,
    val status: InvoiceStatus,  // ← Shared enum
    val items: List<InvoiceItemDTO>  // ← Shared type
)
```

**If you change `InvoiceDTO` in the shared module:**
- ✅ Server (`InvoiceService.kt`) won't compile until fixed
- ✅ Android App (`InvoiceRepository.kt`) won't compile until fixed
- ✅ ViewModel (`VendorViewModel.kt`) won't compile until fixed

## 🧪 Sample API Requests

### Submit Invoice (3-Way Match Test)
```bash
curl -X POST http://localhost:8080/invoice/submit \
  -H "Content-Type: application/json" \
  -d '{
    "id": "INV-001",
    "poId": "PO-001",
    "vendorId": "VENDOR-001",
    "totalAmount": 1500.00,
    "status": "PENDING",
    "items": [
      {"itemId": "ITEM-001", "quantity": 50, "unitPrice": 25.00},
      {"itemId": "ITEM-002", "quantity": 10, "unitPrice": 25.00}
    ]
  }'
```

### Expected Response (MATCHED)
```json
{
  "status": "MATCHED",
  "message": "Invoice validated successfully - 3-Way Match PASSED",
  "timestamp": "2026-02-14T10:30:00Z"
}
```

### Expected Response (MISMATCH)
```json
{
  "status": "MISMATCH",
  "message": "3-Way Match FAILED: Item ITEM-001: Invoice qty (100) exceeds received qty (50)",
  "timestamp": "2026-02-14T10:30:00Z"
}
```

## 📝 License

MIT License
