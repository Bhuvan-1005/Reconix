# Quick Start Script - Run Backend and Configure Firewall
# Run this script as Administrator

Write-Host "🚀 Reconix Invoice Validator - Quick Start" -ForegroundColor Cyan
Write-Host "==========================================`n" -ForegroundColor Cyan

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "⚠️  This script should be run as Administrator to configure firewall" -ForegroundColor Yellow
    Write-Host "   Right-click and select 'Run as Administrator'`n" -ForegroundColor Yellow
}

# Get current IP address
Write-Host "📡 Checking your laptop's IP address..." -ForegroundColor Cyan
$ipInfo = ipconfig | Select-String -Pattern "IPv4"
Write-Host $ipInfo -ForegroundColor White

# Check if port 8081 firewall rule exists
Write-Host "`n🔥 Checking Windows Firewall..." -ForegroundColor Cyan
$firewallRule = Get-NetFirewallRule -DisplayName "Ktor Server 8081" -ErrorAction SilentlyContinue

if ($firewallRule) {
    Write-Host "✅ Firewall rule already exists for port 8081" -ForegroundColor Green
} else {
    if ($isAdmin) {
        Write-Host "⚙️  Creating firewall rule for port 8081..." -ForegroundColor Yellow
        try {
            New-NetFirewallRule -DisplayName "Ktor Server 8081" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow -ErrorAction Stop
            Write-Host "✅ Firewall rule created successfully!" -ForegroundColor Green
        } catch {
            Write-Host "❌ Failed to create firewall rule: $_" -ForegroundColor Red
            Write-Host "   Please add the rule manually in Windows Firewall settings" -ForegroundColor Yellow
        }
    } else {
        Write-Host "⚠️  Cannot create firewall rule (not running as Administrator)" -ForegroundColor Yellow
        Write-Host "   Please run this script as Administrator, or manually allow port 8081" -ForegroundColor Yellow
    }
}

# Test if PostgreSQL container is running
Write-Host "`n🐘 Checking PostgreSQL container..." -ForegroundColor Cyan
try {
    $dockerPs = docker ps --filter "name=postgres" --format "{{.Names}}" 2>$null
    if ($dockerPs -like "*postgres*") {
        Write-Host "✅ PostgreSQL container is running" -ForegroundColor Green
    } else {
        Write-Host "⚠️  PostgreSQL container not found. Starting it..." -ForegroundColor Yellow
        docker-compose up -d
        Write-Host "�� Waiting for PostgreSQL to start (5 seconds)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
} catch {
    Write-Host "⚠️  Docker not available. Make sure PostgreSQL is running." -ForegroundColor Yellow
}

# Ask user if they want to start the backend
Write-Host "`n🎯 Ready to start the backend server!" -ForegroundColor Green
Write-Host "`nOptions:" -ForegroundColor Cyan
Write-Host "  1. Start backend server now" -ForegroundColor White
Write-Host "  2. Test backend connectivity (if already running)" -ForegroundColor White
Write-Host "  3. Exit (I'll start it manually)" -ForegroundColor White

$choice = Read-Host "`nEnter your choice (1-3)"

switch ($choice) {
    "1" {
        Write-Host "`n🚀 Starting Ktor backend server..." -ForegroundColor Cyan
        Write-Host "   Press Ctrl+C to stop the server`n" -ForegroundColor Yellow
        & .\gradlew.bat :server:run
    }
    "2" {
        Write-Host "`n📡 Testing backend connectivity..." -ForegroundColor Cyan

        # Test localhost
        Write-Host "`n1. Testing localhost:8081/health..." -ForegroundColor White
        try {
            $healthLocal = Invoke-RestMethod -Uri "http://localhost:8081/health" -Method Get -TimeoutSec 5
            Write-Host "   ✅ Localhost: $healthLocal" -ForegroundColor Green
        } catch {
            Write-Host "   ❌ Failed: $_" -ForegroundColor Red
        }

        # Test laptop IP
        Write-Host "`n2. Testing 10.116.40.38:8081/health..." -ForegroundColor White
        try {
            $healthIp = Invoke-RestMethod -Uri "http://10.116.40.38:8081/health" -Method Get -TimeoutSec 5
            Write-Host "   ✅ Laptop IP: $healthIp" -ForegroundColor Green
            Write-Host "`n🎉 Backend is accessible from your laptop IP!" -ForegroundColor Green
            Write-Host "   Your Android app should be able to connect." -ForegroundColor Green
        } catch {
            Write-Host "   ❌ Failed: $_" -ForegroundColor Red
            Write-Host "`n⚠️  Backend is running but NOT accessible from your IP!" -ForegroundColor Yellow
            Write-Host "   Possible issues:" -ForegroundColor Yellow
            Write-Host "   - Windows Firewall is blocking port 8081" -ForegroundColor White
            Write-Host "   - Server is only listening on 127.0.0.1 (not 0.0.0.0)" -ForegroundColor White
            Write-Host "   - Your IP address has changed" -ForegroundColor White
        }
    }
    "3" {
        Write-Host "`n✅ Setup complete. Start the backend manually with:" -ForegroundColor Green
        Write-Host "   .\gradlew.bat :server:run`n" -ForegroundColor White
    }
    default {
        Write-Host "`n❌ Invalid choice. Exiting..." -ForegroundColor Red
    }
}

Write-Host "`n📱 Android App Installation:" -ForegroundColor Cyan
Write-Host "   APK Location: composeApp\build\outputs\apk\debug\composeApp-debug.apk" -ForegroundColor White
Write-Host "   Transfer this file to your Android device and install it.`n" -ForegroundColor White

Write-Host "📖 For more help, see IP-UPDATED.md" -ForegroundColor Cyan

