# Voice Coach Diagnostic Script
# Run this in PowerShell to check your setup

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🎙️ VOICE COACH DIAGNOSTIC TOOL" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Check 1: Backend Port
Write-Host "1. Checking if backend is running on port 8083..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8083/api/voice/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✅ Backend is running!" -ForegroundColor Green
    Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "   ❌ Backend is NOT running or not accessible" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Solution: Run 'cd backend && ./mvnw spring-boot:run'" -ForegroundColor Yellow
}

# Check 2: Configuration
Write-Host "`n2. Checking OpenAI API configuration..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/voice/config-check" -TimeoutSec 5
    if ($response.configured) {
        Write-Host "   ✅ OpenAI API key is configured!" -ForegroundColor Green
        Write-Host "   Key prefix: $($response.keyPrefix)" -ForegroundColor Gray
    } else {
        Write-Host "   ⚠️ OpenAI API key is NOT configured" -ForegroundColor Red
        Write-Host "   Message: $($response.message)" -ForegroundColor Yellow
        Write-Host "   Solution:" -ForegroundColor Yellow
        Write-Host "     1. Get API key from https://platform.openai.com/api-keys" -ForegroundColor Gray
        Write-Host "     2. Add to backend/src/main/resources/application.properties" -ForegroundColor Gray
        Write-Host "     3. Set: openai.api.key=sk-proj-YOUR_KEY_HERE" -ForegroundColor Gray
        Write-Host "     4. Restart backend" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ❌ Cannot check configuration (backend may not be running)" -ForegroundColor Red
}

# Check 3: Frontend Port
Write-Host "`n3. Checking if frontend is running on port 4200..." -ForegroundColor Yellow
try {
    $null = Test-NetConnection -ComputerName localhost -Port 4200 -WarningAction SilentlyContinue -ErrorAction Stop
    Write-Host "   ✅ Frontend is running!" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️ Frontend may not be running" -ForegroundColor Yellow
    Write-Host "   Solution: Run 'cd EDUCATION && ng serve'" -ForegroundColor Gray
}

# Check 4: Java Version
Write-Host "`n4. Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    Write-Host "   ✅ Java installed: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Java not found" -ForegroundColor Red
    Write-Host "   Solution: Install Java 17 or higher" -ForegroundColor Yellow
}

# Check 5: Node.js Version
Write-Host "`n5. Checking Node.js installation..." -ForegroundColor Yellow
try {
    $nodeVersion = & node --version
    Write-Host "   ✅ Node.js installed: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Node.js not found" -ForegroundColor Red
    Write-Host "   Solution: Install Node.js 18 or higher" -ForegroundColor Yellow
}

# Check 6: Angular CLI
Write-Host "`n6. Checking Angular CLI..." -ForegroundColor Yellow
try {
    $ngVersion = & ng version 2>&1 | Select-String "Angular CLI" | Select-Object -First 1
    Write-Host "   ✅ $ngVersion" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️ Angular CLI may not be installed" -ForegroundColor Yellow
    Write-Host "   Solution: Run 'npm install -g @angular/cli'" -ForegroundColor Gray
}

# Check 7: Application Properties
Write-Host "`n7. Checking application.properties file..." -ForegroundColor Yellow
$propsFile = "backend\src\main\resources\application.properties"
if (Test-Path $propsFile) {
    Write-Host "   ✅ application.properties exists" -ForegroundColor Green
    
    $content = Get-Content $propsFile
    $hasKey = $content | Select-String "openai.api.key"
    
    if ($hasKey) {
        $keyLine = $hasKey.Line
        if ($keyLine -match "openai.api.key=sk-") {
            Write-Host "   ✅ OpenAI API key appears to be set" -ForegroundColor Green
        } elseif ($keyLine -match "your-openai-api-key-here") {
            Write-Host "   ⚠️ OpenAI API key is still placeholder" -ForegroundColor Yellow
            Write-Host "   Solution: Replace with actual API key from OpenAI" -ForegroundColor Gray
        } else {
            Write-Host "   ⚠️ OpenAI API key format looks incorrect" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ⚠️ openai.api.key not found in properties" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ❌ application.properties not found" -ForegroundColor Red
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "📊 DIAGNOSTIC SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nIf you see any ❌ or ⚠️ above, please fix those issues." -ForegroundColor Yellow
Write-Host "`nFor detailed help, see:" -ForegroundColor White
Write-Host "  - TROUBLESHOOTING_VOICE_COACH.md" -ForegroundColor Gray
Write-Host "  - QUICK_START_VOICE_COACH.md" -ForegroundColor Gray
Write-Host "`nYou can also test manually at:" -ForegroundColor White
Write-Host "  - Open voice-coach-test.html in browser" -ForegroundColor Gray
Write-Host "`n" -ForegroundColor White
