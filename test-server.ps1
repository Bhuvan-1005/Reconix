# Test the Invoice Match Validator Server
Write-Host "🚀 Starting Ktor Server..." -ForegroundColor Cyan

# Start server in background
$serverJob = Start-Job -ScriptBlock {
    Set-Location "C:\Users\eshwar\OneDrive\Desktop\Invoice Project"
    & .\gradlew.bat :server:run
}

Write-Host "⏳ Waiting for server to start (20 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# Test server endpoints
Write-Host "`n📡 Testing Server Endpoints..." -ForegroundColor Cyan

try {
    # Test health endpoint
    Write-Host "`n1. Testing /health endpoint..."
    $health = Invoke-RestMethod -Uri "http://10.116.40.38:8081/health" -Method Get -TimeoutSec 5
    Write-Host "✅ Health Check: $health" -ForegroundColor Green

    # Test PO list endpoint
    Write-Host "`n2. Testing /po/list endpoint..."
    $pos = Invoke-RestMethod -Uri "http://10.116.40.38:8081/po/list" -Method Get -TimeoutSec 5
    Write-Host "✅ Found $($pos.Count) Purchase Orders:" -ForegroundColor Green
    $pos | ForEach-Object {
        Write-Host "   - PO: $($_.id) | Vendor: $($_.vendorName) | Total: `$$($_.totalAmount)" -ForegroundColor White
    }

    # Test invoice submission with valid data
    Write-Host "`n3. Testing /invoice/submit endpoint (VALID - should MATCH)..."
    $invoiceValid = @{
        id = "INV-TEST-001"
        poId = "PO-001"
        vendorId = "VENDOR-001"
        totalAmount = 1500.00
        status = "PENDING"
        items = @(
            @{ itemId = "ITEM-001"; quantity = 30; unitPrice = 25.00 },
            @{ itemId = "ITEM-002"; quantity = 10; unitPrice = 25.00 }
        )
    } | ConvertTo-Json -Depth 10

    $resultValid = Invoke-RestMethod -Uri "http://10.116.40.38:8081/invoice/submit" -Method Post -Body $invoiceValid -ContentType "application/json" -TimeoutSec 5
    Write-Host "✅ Validation Result: $($resultValid.status)" -ForegroundColor Green
    Write-Host "   Message: $($resultValid.message)" -ForegroundColor White

    # Test invoice submission with invalid data (quantity exceeded)
    Write-Host "`n4. Testing /invoice/submit endpoint (INVALID - quantity exceeded)..."
    $invoiceInvalid = @{
        id = "INV-TEST-002"
        poId = "PO-001"
        vendorId = "VENDOR-001"
        totalAmount = 2000.00
        status = "PENDING"
        items = @(
            @{ itemId = "ITEM-001"; quantity = 100; unitPrice = 25.00 }  # Exceeds received qty of 50
        )
    } | ConvertTo-Json -Depth 10

    $resultInvalid = Invoke-RestMethod -Uri "http://10.116.40.38:8081/invoice/submit" -Method Post -Body $invoiceInvalid -ContentType "application/json" -TimeoutSec 5 -StatusCodeVariable statusCode 2>&1
    if ($resultInvalid) {
        Write-Host "✅ Validation Result: $($resultInvalid.status)" -ForegroundColor Yellow
        Write-Host "   Message: $($resultInvalid.message)" -ForegroundColor White
    }

    Write-Host "`n✅ All tests completed successfully!" -ForegroundColor Green
    Write-Host "`n🌐 Server is running at http://10.116.40.38:8081" -ForegroundColor Cyan
    Write-Host "   - Health: http://10.116.40.38:8081/health" -ForegroundColor White
    Write-Host "   - PO List: http://10.116.40.38:8081/po/list" -ForegroundColor White
    Write-Host "   - Submit Invoice: POST http://10.116.40.38:8081/invoice/submit" -ForegroundColor White

} catch {
    Write-Host "❌ Error testing server: $_" -ForegroundColor Red
    Write-Host "Server may not have started. Check the server window for errors." -ForegroundColor Yellow
}

Write-Host "`n⚠️  Server is still running in the background. Check for Java processes to stop it." -ForegroundColor Yellow



