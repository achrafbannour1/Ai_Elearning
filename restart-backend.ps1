# Voice Coach - Restart with Gemini API
Write-Host "`n🔄 Restarting Voice Coach Backend with Gemini API...`n" -ForegroundColor Cyan

# Check if backend directory exists
if (-not (Test-Path "backend")) {
    Write-Host "❌ Error: backend directory not found!" -ForegroundColor Red
    Write-Host "Please run this script from the project root directory." -ForegroundColor Yellow
    exit 1
}

# Navigate to backend
Set-Location backend

Write-Host "📦 Checking Gemini API configuration..." -ForegroundColor Yellow
$propsFile = "src\main\resources\application.properties"
if (Test-Path $propsFile) {
    $content = Get-Content $propsFile
    $geminiKey = $content | Select-String "gemini.api.key"
    
    if ($geminiKey) {
        Write-Host "✅ Gemini API key found!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Warning: gemini.api.key not found in application.properties" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ application.properties not found!" -ForegroundColor Red
    exit 1
}

Write-Host "`n🏗️ Building and starting backend..." -ForegroundColor Cyan
Write-Host "This may take a few moments...`n" -ForegroundColor Gray

# Start backend
try {
    .\mvnw.cmd spring-boot:run
} catch {
    Write-Host "`n❌ Error starting backend: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nTry manually: cd backend && ./mvnw spring-boot:run" -ForegroundColor Yellow
}

Set-Location ..
