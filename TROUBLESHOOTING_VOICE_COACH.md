# 🔧 Voice Coach Troubleshooting Guide

## Error: "Could not generate example audio. Please try again."

This error occurs when the Text-to-Speech feature fails. Follow these steps to resolve it:

---

## ✅ Step 1: Verify Backend is Running

### Check if backend is running on port 8083:
```powershell
# In PowerShell
Test-NetConnection -ComputerName localhost -Port 8083
```

If nothing responds, start the backend:
```powershell
cd backend
./mvnw spring-boot:run
```

---

## ✅ Step 2: Check OpenAI API Key Configuration

### Open: `backend/src/main/resources/application.properties`

Make sure you have:
```properties
openai.api.key=sk-proj-YOUR_ACTUAL_API_KEY_HERE
```

⚠️ **Common Mistakes:**
- ❌ `openai.api.key=your-openai-api-key-here` (placeholder not replaced)
- ❌ `openai.api.key=` (empty value)
- ❌ Extra spaces around the key
- ❌ Wrong key format (should start with `sk-`)

### How to get your API key:
1. Go to https://platform.openai.com/api-keys
2. Sign in or create an account
3. Click "Create new secret key"
4. Copy the key (starts with `sk-`)
5. Paste it in `application.properties`

---

## ✅ Step 3: Test Backend Endpoints

### Open in browser or use curl:

**Test 1: Health Check**
```
http://localhost:8083/api/voice/health
```
Expected: `Voice Coach API is running!`

**Test 2: Configuration Check**
```
http://localhost:8083/api/voice/config-check
```
Expected: 
```json
{
  "configured": true,
  "message": "✅ OpenAI API key is configured correctly!",
  "keyPrefix": "sk-proj-..."
}
```

If you see `"configured": false`, your API key is not set correctly.

---

## ✅ Step 4: Check Browser Console

### Open Developer Tools (F12):
1. Go to the Console tab
2. Try clicking "Listen to Example"
3. Look for error messages

### Common Console Errors:

**Error: `Failed to fetch`**
- **Cause:** Backend not running or wrong URL
- **Fix:** Start backend on port 8083

**Error: `CORS policy`**
- **Cause:** CORS not enabled
- **Fix:** Already handled with `@CrossOrigin(origins = "*")` in controller

**Error: `401 Unauthorized`**
- **Cause:** Invalid OpenAI API key
- **Fix:** Check your API key

**Error: `429 Too Many Requests`**
- **Cause:** OpenAI rate limit exceeded
- **Fix:** Wait a few minutes or upgrade OpenAI plan

**Error: `500 Internal Server Error`**
- **Cause:** Backend error
- **Fix:** Check backend logs

---

## ✅ Step 5: Check Backend Logs

When you start the backend, look for these messages:

### ✅ Good (Backend started successfully):
```
Started BackendApplication in X.XXX seconds
Tomcat started on port(s): 8083
```

### ❌ Bad (Configuration error):
```
Error creating bean with name 'voiceCoachService'
Could not resolve placeholder 'openai.api.key'
```
**Fix:** Add OpenAI API key to `application.properties`

### ❌ Bad (Port already in use):
```
Port 8083 was already in use
```
**Fix:** 
- Stop other application using port 8083, or
- Change port in `application.properties`: `server.port=8084`

---

## ✅ Step 6: Test with Standalone Test Page

### Open: `voice-coach-test.html` in your browser

1. Click **"Check Backend Configuration"**
   - Should show ✅ if API key is configured
   
2. Click **"Test Health Endpoint"**
   - Should show ✅ if backend is running
   
3. Click **"Generate Speech"** in section 4
   - Should generate audio if everything works

This will help isolate if the issue is frontend or backend.

---

## ✅ Step 7: Verify OpenAI API Key is Valid

### Test your API key directly:

```powershell
# PowerShell command to test OpenAI API
$headers = @{
    "Authorization" = "Bearer YOUR_API_KEY_HERE"
    "Content-Type" = "application/json"
}

$body = @{
    model = "tts-1"
    input = "Hello world"
    voice = "alloy"
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://api.openai.com/v1/audio/speech" -Method POST -Headers $headers -Body $body -OutFile "test.mp3"
```

If this creates a `test.mp3` file, your API key is valid!

---

## 🔍 Step 8: Check Network Tab

### In Browser Developer Tools:
1. Go to **Network** tab
2. Click "Listen to Example"
3. Look for the POST request to `/api/voice/text-to-speech`

### Analyze the request:

**Status 200 ✅**
- Everything works! Issue might be audio playback.

**Status 400 ❌**
- Bad request, check request payload

**Status 401 ❌**
- OpenAI API key invalid or missing

**Status 500 ❌**
- Backend error, check logs

**Failed (red) ❌**
- Backend not reachable

---

## 🛠️ Quick Fixes

### Fix 1: Restart Everything
```powershell
# Stop backend (Ctrl+C in terminal)
# Stop frontend (Ctrl+C in terminal)

# Start backend
cd backend
./mvnw spring-boot:run

# In new terminal, start frontend
cd EDUCATION
ng serve
```

### Fix 2: Clear Browser Cache
- Press Ctrl+Shift+Delete
- Clear cached files
- Refresh page (Ctrl+F5)

### Fix 3: Check Firewall
Make sure Windows Firewall allows:
- Port 8083 (Backend)
- Port 4200 (Frontend)

### Fix 4: Verify Dependencies
```powershell
# Backend - check pom.xml has okhttp3
cd backend
./mvnw dependency:tree | findstr okhttp

# Frontend - check service is imported
cd EDUCATION
ng serve
```

---

## 💡 Alternative: Use Browser's Built-in TTS (Temporary Workaround)

If OpenAI TTS doesn't work, you can use browser's speech synthesis as a fallback.

### Update `voice-coach.component.ts`:

Add this alternative method:
```typescript
playExampleFallback() {
  if (!this.currentSentence) return;

  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(this.currentSentence);
    utterance.lang = 'en-US';
    utterance.rate = 0.8; // Slower for learning
    window.speechSynthesis.speak(utterance);
  } else {
    alert('Text-to-speech not supported in this browser.');
  }
}
```

---

## 📊 Diagnostic Checklist

- [ ] Backend running on port 8083
- [ ] OpenAI API key configured in `application.properties`
- [ ] API key is valid (starts with `sk-`)
- [ ] Health endpoint responds: `http://localhost:8083/api/voice/health`
- [ ] Config check shows configured: `http://localhost:8083/api/voice/config-check`
- [ ] No CORS errors in browser console
- [ ] No 401/500 errors in Network tab
- [ ] Backend logs show no errors
- [ ] Test page works: `voice-coach-test.html`

---

## 🆘 Still Not Working?

### Check these specific files:

1. **Backend Service** (`VoiceCoachService.java`):
   - Line with `@Value("${openai.api.key}")`
   - Check it's reading the property correctly

2. **Frontend Service** (`voice-coach.service.ts`):
   - Line: `private apiUrl = 'http://localhost:8083/api/voice';`
   - Make sure port matches your backend

3. **Application Properties**:
   - No typos in `openai.api.key=`
   - No extra quotes around the key value

---

## 📞 Getting More Info

### Enable Debug Logging:

Add to `application.properties`:
```properties
logging.level.com.example.backend=DEBUG
logging.level.org.springframework.web=DEBUG
```

This will show detailed logs of all requests.

---

## 💰 OpenAI Account Issues

### Check your OpenAI account:
1. Go to https://platform.openai.com/usage
2. Verify you have credits/billing set up
3. Check rate limits
4. Ensure TTS API is enabled

### API Limits:
- Free tier: Very limited
- Pay-as-you-go: Need to add payment method

---

## ✅ Success!

Once fixed, you should:
1. ✅ Click "Listen to Example" → Audio plays
2. ✅ Record your voice → Recording works
3. ✅ See analysis results → Score and feedback appear

---

## 📧 Need More Help?

If you've tried everything:
1. Check backend terminal logs
2. Check browser console (F12)
3. Try the standalone test page
4. Verify OpenAI API key at https://platform.openai.com
