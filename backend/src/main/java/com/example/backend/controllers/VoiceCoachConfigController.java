package com.example.backend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "http://localhost:4200")
public class VoiceCoachConfigController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * Check if Gemini API key is configured
     */
    @GetMapping("/config-check")
    public Map<String, Object> checkConfiguration() {
        Map<String, Object> status = new HashMap<>();
        
        boolean isConfigured = geminiApiKey != null 
            && !geminiApiKey.trim().isEmpty() 
            && geminiApiKey.length() > 20; // Gemini keys are longer
        
        status.put("configured", isConfigured);
        status.put("message", isConfigured 
            ? "✅ Gemini API key is configured correctly!" 
            : "⚠️ Please set your Gemini API key in application.properties");
        status.put("apiType", "Google Gemini");
        
        if (isConfigured) {
            status.put("keyPrefix", geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())) + "...");
        }
        
        return status;
    }
}
