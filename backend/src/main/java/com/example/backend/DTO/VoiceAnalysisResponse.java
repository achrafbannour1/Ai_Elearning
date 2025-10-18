package com.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAnalysisResponse {
    private String transcription;
    private String originalText;
    private double score;
    private String feedback;
    private List<String> pronunciationErrors;
    private List<String> suggestions;
}
