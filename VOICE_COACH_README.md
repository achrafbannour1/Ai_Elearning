# 🎙️ AI Voice Coach - Setup Guide

## Overview
The AI Voice Coach is a comprehensive pronunciation practice system that uses **OpenAI Whisper** for speech-to-text transcription and **OpenAI TTS** for text-to-speech generation. Students can practice pronunciation by reading sentences aloud and receive instant AI-powered feedback with scores and improvement suggestions.

---

## 🎯 Features

### Frontend (Angular)
- ✅ **Exercise Selection**: Choose from 10+ pronunciation exercises with varying difficulty levels
- ✅ **Audio Recording**: Record your pronunciation directly in the browser
- ✅ **Text-to-Speech**: Listen to correct pronunciation examples
- ✅ **Real-time Feedback**: Get instant scores (0-100) and detailed analysis
- ✅ **Pronunciation Errors**: See specific mistakes and corrections
- ✅ **Practice History**: Track your progress with recent attempts
- ✅ **Beautiful UI**: Modern, responsive design with animations

### Backend (Spring Boot)
- ✅ **Whisper Integration**: Transcribe audio to text using OpenAI's Whisper API
- ✅ **GPT Analysis**: Generate intelligent feedback using GPT-3.5
- ✅ **TTS Generation**: Create example audio using OpenAI's TTS API
- ✅ **Similarity Scoring**: Calculate pronunciation accuracy using Levenshtein distance
- ✅ **RESTful API**: Clean endpoints for frontend communication

---

## 📋 Prerequisites

1. **OpenAI API Key**
   - Sign up at https://platform.openai.com/
   - Create an API key with access to:
     - Whisper API (for speech-to-text)
     - GPT-3.5-turbo (for feedback generation)
     - TTS-1 (for text-to-speech)

2. **Angular 16+** (Already configured in your project)

3. **Spring Boot 3.3+** (Already configured in your project)

4. **Java 17+** (Already configured in your project)

---

## 🚀 Installation Steps

### 1. Backend Configuration

#### Step 1.1: Add OpenAI API Key
Edit `backend/src/main/resources/application.properties`:

```properties
# Replace 'your-openai-api-key-here' with your actual OpenAI API key
openai.api.key=sk-proj-xxxxxxxxxxxxxxxxxxxxx
```

⚠️ **IMPORTANT**: Keep your API key secure! Never commit it to version control.

#### Step 1.2: Verify Dependencies
All required dependencies are already in your `pom.xml`:
- ✅ OkHttp (for API calls)
- ✅ JSON parsing libraries
- ✅ Spring Web & Multipart

### 2. Frontend Configuration

#### Step 2.1: Verify Service is Imported
Check that the service is registered in `app.module.ts`. If not, add:

```typescript
import { VoiceCoachService } from './services/voice-coach.service';

@NgModule({
  providers: [
    VoiceCoachService,
    // ... other services
  ]
})
```

#### Step 2.2: Configure API URL (Optional)
If your backend runs on a different port, update the API URL in:
`EDUCATION/src/app/services/voice-coach.service.ts`

```typescript
private apiUrl = 'http://localhost:8083/api/voice';  // Change port if needed
```

---

## 🎬 Running the Application

### 1. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

Or if using Windows:
```bash
cd backend
mvnw.cmd spring-boot:run
```

The backend will start on **http://localhost:8083**

### 2. Start the Frontend

```bash
cd EDUCATION
npm install  # If not already done
ng serve
```

The frontend will start on **http://localhost:4200**

### 3. Navigate to Voice Coach

Open your browser and go to:
- http://localhost:4200/voice-coach

(Or wherever your routing is configured for the voice-coach component)

---

## 🎯 How to Use

### Step 1: Select an Exercise
- Click on any exercise from the list
- Or click "🎲 Random Exercise" for a random sentence
- Filter by difficulty: Easy, Medium, or Hard

### Step 2: Listen to Example
- Click "🔊 Listen to Example" to hear correct pronunciation
- The AI will generate high-quality audio for reference

### Step 3: Record Your Voice
1. Click "🎙️ Start Recording"
2. Read the sentence aloud clearly
3. Click "⏹️ Stop Recording" when finished

### Step 4: Get Feedback
- The system automatically analyzes your recording
- View your score (0-100)
- Read detailed feedback
- See pronunciation errors
- Get improvement suggestions

### Step 5: Practice Again
- Click "🔄 Try This Again" to retry the same sentence
- Or click "➡️ Next Exercise" for a new challenge

---

## 📊 Scoring System

| Score | Label | Description |
|-------|-------|-------------|
| 90-100 | Excellent! | Near-perfect pronunciation |
| 70-89 | Good! | Clear pronunciation with minor issues |
| 50-69 | Fair | Understandable but needs improvement |
| 0-49 | Needs Practice | Significant pronunciation differences |

---

## 🔧 API Endpoints

### Backend REST API

#### 1. Analyze Pronunciation
```
POST /api/voice/analyze
Content-Type: multipart/form-data

Parameters:
- file: Audio file (webm, mp3, wav)
- originalText: The text to compare against

Response:
{
  "transcription": "what the user said",
  "originalText": "what they should have said",
  "score": 85.5,
  "feedback": "Great job! Your pronunciation is clear...",
  "pronunciationErrors": ["word1", "word2"],
  "suggestions": ["tip1", "tip2"]
}
```

#### 2. Generate Text-to-Speech
```
POST /api/voice/text-to-speech
Content-Type: application/json

Request:
{
  "text": "Hello world"
}

Response: Audio file (MP3)
```

#### 3. Health Check
```
GET /api/voice/health

Response: "Voice Coach API is running!"
```

---

## 🎨 Customization

### Add More Exercises
Edit `EDUCATION/src/app/services/voice-coach.service.ts`:

```typescript
exercises: Exercise[] = [
  { 
    id: 11, 
    sentence: 'Your new sentence here', 
    difficulty: 'Easy', 
    category: 'Conversation' 
  },
  // Add more exercises...
];
```

### Change TTS Voice
Edit `backend/src/main/java/com/example/backend/services/VoiceCoachService.java`:

```java
requestBody.put("voice", "nova"); // Options: alloy, echo, fable, onyx, nova, shimmer
```

### Adjust Scoring Algorithm
Modify the `calculateSimilarityScore` method in `VoiceCoachService.java` to use different similarity algorithms.

---

## 🐛 Troubleshooting

### Issue: "Could not access microphone"
**Solution**: 
- Grant microphone permissions in your browser
- Use HTTPS or localhost (required for microphone access)
- Check browser console for specific errors

### Issue: "Whisper API error"
**Solution**:
- Verify your OpenAI API key is correct
- Check if you have credits in your OpenAI account
- Ensure your API key has Whisper API access

### Issue: "Audio file too large"
**Solution**:
- Recording is limited to 25MB by default
- Adjust `spring.servlet.multipart.max-file-size` in application.properties

### Issue: CORS errors
**Solution**:
- Backend has `@CrossOrigin(origins = "*")` enabled
- If still having issues, configure CORS properly in Spring Boot

### Issue: No audio playback
**Solution**:
- Check browser console for codec errors
- Try different browsers (Chrome/Edge recommended)
- Verify audio file MIME types

---

## 💰 OpenAI API Costs

### Approximate Costs (as of 2025):
- **Whisper API**: $0.006 per minute of audio
- **GPT-3.5-turbo**: $0.0015 per 1K tokens (~$0.002 per analysis)
- **TTS-1**: $0.015 per 1K characters (~$0.001 per example)

**Example**: 100 practice sessions ≈ $1-2 total cost

---

## 🔐 Security Notes

1. **Never commit API keys** to version control
2. Consider using environment variables for production:
   ```properties
   openai.api.key=${OPENAI_API_KEY}
   ```
3. Implement rate limiting to prevent abuse
4. Add authentication to protect API endpoints
5. Validate and sanitize all user inputs

---

## 📚 Technology Stack

### Frontend
- Angular 16
- TypeScript
- RxJS
- HTML5 MediaRecorder API

### Backend
- Spring Boot 3.3.4
- Java 17
- OkHttp (HTTP client)
- OpenAI APIs (Whisper, GPT-3.5, TTS)

### AI Services
- **OpenAI Whisper**: Speech-to-text transcription
- **OpenAI GPT-3.5-turbo**: Intelligent feedback generation
- **OpenAI TTS-1**: High-quality text-to-speech

---

## 🎓 Next Steps

### Enhancements You Can Add:
1. **User Authentication**: Track individual user progress
2. **Database Storage**: Save attempt history permanently
3. **Advanced Analytics**: Visualize improvement over time
4. **Custom Exercises**: Let teachers create custom sentences
5. **Multi-language Support**: Add support for other languages
6. **Pronunciation Heatmaps**: Visual word-by-word accuracy
7. **Social Features**: Compare scores with friends
8. **Gamification**: Badges, streaks, and achievements

---

## 📞 Support

If you encounter any issues:
1. Check the troubleshooting section above
2. Review browser and server console logs
3. Verify all configuration files are correct
4. Test API endpoints directly using Postman/curl

---

## 🎉 Success!

You now have a fully functional AI Voice Coach! Students can:
- ✅ Practice pronunciation with AI feedback
- ✅ Get instant scores and detailed analysis
- ✅ Listen to correct pronunciation examples
- ✅ Track their improvement over time

Happy learning! 🚀
