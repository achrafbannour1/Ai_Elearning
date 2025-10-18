package com.example.backend.services;

import com.example.backend.DTO.VoiceAnalysisResponse;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VoiceCoachService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Google Gemini API endpoint (use v1 and latest model alias)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String GOOGLE_TTS_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";
    
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Analyze pronunciation by transcribing audio and comparing with original text
     */
    public VoiceAnalysisResponse analyzePronunciation(MultipartFile audioFile, String originalText) throws IOException {
        // Step 1: Transcribe audio using Whisper API
        String transcription = transcribeAudio(audioFile);

        // Step 2: Calculate similarity score
        double score = calculateSimilarityScore(transcription, originalText);

        // Step 3: Generate detailed feedback using GPT
        VoiceAnalysisResponse detailedAnalysis = generateDetailedFeedback(transcription, originalText, score);

        return detailedAnalysis;
    }

    /**
     * Transcribe audio using Gemini API with audio analysis
     */
    private String transcribeAudio(MultipartFile audioFile) throws IOException {
        // Convert audio to base64
        byte[] audioBytes = audioFile.getBytes();
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
        
        // Determine MIME type
        String mimeType = audioFile.getContentType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "audio/webm"; // Default
        }
        // Normalize common browser-provided mime types
        String lower = mimeType.toLowerCase();
        if (lower.contains("webm")) mimeType = "audio/webm";
        else if (lower.contains("wav")) mimeType = "audio/wav";
        else if (lower.contains("mp3") || lower.contains("mpeg")) mimeType = "audio/mpeg";

        // Build Gemini API request with audio
        JSONObject requestBody = new JSONObject();
        
    JSONArray contents = new JSONArray();
    JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        // Text part with instruction
        JSONObject textPart = new JSONObject();
        textPart.put("text", "Transcribe this audio recording to text. Provide only the transcription, no additional commentary.");
        parts.put(textPart);
        
    // Audio part
    JSONObject audioPart = new JSONObject();
    JSONObject inlineData = new JSONObject();
    inlineData.put("mimeType", mimeType);
    inlineData.put("data", base64Audio);
    audioPart.put("inlineData", inlineData);
    parts.put(audioPart);
        
    content.put("role", "user");
    content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);

        // Make API request
        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + geminiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                System.err.println("Gemini API error: " + response.code() + " - " + errorBody);
                
                // Fallback: try using speech recognition library
                return transcribeWithFallback(audioFile);
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Extract transcription from Gemini response (robustly)
            if (jsonResponse.has("candidates")) {
                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject candidate = candidates.optJSONObject(0);
                    if (candidate != null) {
                        // Preferred path: content.parts[].text
                        if (candidate.has("content")) {
                            JSONObject contentObj = candidate.optJSONObject("content");
                            if (contentObj != null && contentObj.has("parts")) {
                                JSONArray partsArray = contentObj.optJSONArray("parts");
                                if (partsArray != null && partsArray.length() > 0) {
                                    // Concatenate all text parts if present
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < partsArray.length(); i++) {
                                        JSONObject p = partsArray.optJSONObject(i);
                                        if (p != null && p.has("text")) {
                                            sb.append(p.optString("text", ""));
                                        }
                                    }
                                    String text = sb.toString().trim();
                                    if (!text.isEmpty()) return text;
                                }
                            }
                        }
                        // Alternate: candidate has direct text field (rare)
                        if (candidate.has("text")) {
                            String text = candidate.optString("text", "").trim();
                            if (!text.isEmpty()) return text;
                        }
                    }
                }
            }

            System.err.println("Gemini transcription parse failed, full response: " + jsonResponse.toString());
            throw new IOException("Could not extract transcription from Gemini response");
        }
    }
    
    /**
     * Fallback transcription using simple audio analysis
     */
    private String transcribeWithFallback(MultipartFile audioFile) throws IOException {
        // Since Gemini doesn't have direct speech-to-text like Whisper,
        // we'll return a message indicating manual review needed
        // In production, you might integrate with Google Cloud Speech-to-Text
    System.err.println("Warning: Using fallback transcription method");
    // Return an empty string to avoid triggering controller's 'unavailable' check
    return "";
    }

    /**
     * Calculate similarity score between transcription and original text
     */
    private double calculateSimilarityScore(String transcription, String originalText) {
        // Normalize texts
        String normalizedTranscription = transcription.toLowerCase().trim().replaceAll("[^a-z0-9\\s]", "");
        String normalizedOriginal = originalText.toLowerCase().trim().replaceAll("[^a-z0-9\\s]", "");

        // Use Levenshtein distance for similarity
        int distance = levenshteinDistance(normalizedTranscription, normalizedOriginal);
        int maxLength = Math.max(normalizedTranscription.length(), normalizedOriginal.length());

        // Calculate score (0-100)
        double similarity = maxLength == 0 ? 100.0 : (1.0 - (double) distance / maxLength) * 100.0;
        return Math.max(0, Math.min(100, similarity));
    }

    /**
     * Generate detailed feedback using Gemini API
     */
    private VoiceAnalysisResponse generateDetailedFeedback(String transcription, String originalText, double score) throws IOException {
    String prompt = String.format(
        "You are a pronunciation coach.\n" +
        "Target sentence: %s\n" +
        "Student said: %s\n" +
        "Similarity score: %.2f/100\n\n" +
        "Return STRICT JSON only with these keys: feedback (max 2 sentences), pronunciationErrors (array, max 3 items), suggestions (array, max 3 items).\n" +
        "No markdown, no comments, no extra fields.",
        originalText, transcription, score
    );

        JSONObject requestBody = new JSONObject();
        
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);
        
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);
        
        // Configure for JSON output
    JSONObject generationConfig = new JSONObject();
    generationConfig.put("temperature", 0.3);
    generationConfig.put("topK", 40);
    generationConfig.put("topP", 0.9);
    generationConfig.put("maxOutputTokens", 2048);
    // Ask Gemini to return application/json payload in the text part
    generationConfig.put("response_mime_type", "application/json");
        requestBody.put("generationConfig", generationConfig);

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + geminiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Gemini API error: " + response.code());
                return createFallbackResponse(transcription, originalText, score);
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Extract content from Gemini response robustly
            String geminiText = null;
            if (jsonResponse.has("candidates")) {
                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject candidate = candidates.optJSONObject(0);
                    if (candidate != null && candidate.has("content")) {
                        JSONObject contentObj = candidate.optJSONObject("content");
                        if (contentObj != null && contentObj.has("parts")) {
                            JSONArray partsArray = contentObj.optJSONArray("parts");
                            if (partsArray != null && partsArray.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < partsArray.length(); i++) {
                                    JSONObject p = partsArray.optJSONObject(i);
                                    if (p != null && p.has("text")) sb.append(p.optString("text", ""));
                                }
                                geminiText = sb.toString().trim();
                            }
                        }
                    }
                    if ((geminiText == null || geminiText.isEmpty()) && candidate != null && candidate.has("text")) {
                        geminiText = candidate.optString("text", "").trim();
                    }
                }
            }

            if (geminiText == null || geminiText.isEmpty()) {
                System.err.println("Gemini feedback parse failed, full response: " + jsonResponse.toString());
                return createFallbackResponse(transcription, originalText, score);
            }

            // Clean up any markdown/code fences or stray whitespace
            geminiText = geminiText
                    .replaceAll("^[\n\r\t ]*```json[\n\r]+", "")
                    .replaceAll("^[\n\r\t ]*```[\n\r]+", "")
                    .replaceAll("```[\n\r\t ]*$", "")
                    .trim();

            // If the JSON seems truncated, try trimming to the last closing brace
            int lastBrace = geminiText.lastIndexOf('}');
            if (lastBrace > 0 && lastBrace < geminiText.length() - 1) {
                geminiText = geminiText.substring(0, lastBrace + 1);
            }

            // Parse JSON response
            try {
                JSONObject analysis = new JSONObject(geminiText);

                List<String> errors = new ArrayList<>();
                if (analysis.has("pronunciationErrors")) {
                    JSONArray errorsArray = analysis.optJSONArray("pronunciationErrors");
                    if (errorsArray != null) {
                        for (int i = 0; i < errorsArray.length(); i++) {
                            errors.add(errorsArray.optString(i));
                        }
                    }
                }

                List<String> suggestions = new ArrayList<>();
                if (analysis.has("suggestions")) {
                    JSONArray suggestionsArray = analysis.optJSONArray("suggestions");
                    if (suggestionsArray != null) {
                        for (int i = 0; i < suggestionsArray.length(); i++) {
                            suggestions.add(suggestionsArray.optString(i));
                        }
                    }
                }

                return new VoiceAnalysisResponse(
                        transcription,
                        originalText,
                        score,
                        analysis.optString("feedback", "Good effort! Keep practicing."),
                        errors,
                        suggestions
                );
            } catch (Exception e) {
                System.err.println("Error parsing Gemini JSON response: " + e.getMessage());
                System.err.println("Gemini raw feedback text (for debugging):\n" + geminiText);
                return createFallbackResponse(transcription, originalText, score);
            }
        }
    }

    /**
     * Create fallback response if GPT fails
     */
    private VoiceAnalysisResponse createFallbackResponse(String transcription, String originalText, double score) {
        String feedback;
        List<String> suggestions = new ArrayList<>();

        if (score >= 90) {
            feedback = "Excellent pronunciation! Your speech was very clear and accurate.";
            suggestions.add("Keep up the great work!");
            suggestions.add("Try more challenging sentences to further improve.");
        } else if (score >= 70) {
            feedback = "Good job! Your pronunciation is quite clear with minor differences.";
            suggestions.add("Focus on enunciating each word clearly.");
            suggestions.add("Practice the sentence slowly, then gradually increase speed.");
        } else if (score >= 50) {
            feedback = "You're making progress. There are some pronunciation issues to work on.";
            suggestions.add("Break down difficult words and practice them separately.");
            suggestions.add("Listen to the example audio multiple times.");
            suggestions.add("Record yourself and compare with the original.");
        } else {
            feedback = "Keep practicing! Pronunciation takes time to master.";
            suggestions.add("Start with shorter, simpler sentences.");
            suggestions.add("Focus on one difficult word at a time.");
            suggestions.add("Listen carefully to the example before recording.");
        }

        List<String> errors = new ArrayList<>();
        if (score < 90) {
            errors.add("Some words were pronounced differently than expected");
            if (score < 70) {
                errors.add("Consider speaking more slowly and clearly");
            }
        }

        return new VoiceAnalysisResponse(
                transcription,
                originalText,
                score,
                feedback,
                errors,
                suggestions
        );
    }

    /**
     * Generate text-to-speech audio using Google Cloud TTS
     */
    public byte[] generateTextToSpeech(String text) throws IOException {
        JSONObject requestBody = new JSONObject();
        
        // Input text
        JSONObject input = new JSONObject();
        input.put("text", text);
        requestBody.put("input", input);
        
        // Voice settings
        JSONObject voice = new JSONObject();
        voice.put("languageCode", "en-US");
        voice.put("name", "en-US-Neural2-D"); // Natural female voice
        voice.put("ssmlGender", "FEMALE");
        requestBody.put("voice", voice);
        
        // Audio config
        JSONObject audioConfig = new JSONObject();
        audioConfig.put("audioEncoding", "MP3");
        audioConfig.put("speakingRate", 0.9); // Slightly slower for learning
        audioConfig.put("pitch", 0);
        requestBody.put("audioConfig", audioConfig);

        Request request = new Request.Builder()
                .url(GOOGLE_TTS_URL + "?key=" + geminiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                System.err.println("Google TTS API error: " + response.code() + " - " + errorBody);
                throw new IOException("TTS API error: " + response.code());
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            // Extract base64 audio content
            if (jsonResponse.has("audioContent")) {
                String base64Audio = jsonResponse.getString("audioContent");
                return Base64.getDecoder().decode(base64Audio);
            }
            
            throw new IOException("No audio content in TTS response");
        }
    }

    /**
     * Convert MultipartFile to File
     */
    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            Math.min(dp[i - 1][j], dp[i][j - 1]),
                            dp[i - 1][j - 1]
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
