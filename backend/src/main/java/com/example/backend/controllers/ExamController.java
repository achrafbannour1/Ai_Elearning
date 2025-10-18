package com.example.backend.controllers;
import com.google.auth.oauth2.GoogleCredentials;
import io.swagger.v3.oas.annotations.Parameter;

import com.example.backend.services.AiExamService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final AiExamService aiExamService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    public ExamController(AiExamService aiExamService) {
        this.aiExamService = aiExamService;
    }
    /**
     * Example endpoint using fixed parameters:
     * 5 MCQs, 3 True/False, 2 Short Answer, medium difficulty
     */
    @PostMapping(value = "/generateExamen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> generateExam(
            @RequestPart String text,
            @RequestPart String numMCQ,
            @RequestPart String numTrueFalse,
            @RequestPart String numShortAnswer,
            @RequestPart String difficulty) {
        try {
            // Clean topic name for filename (remove spaces/special chars)
            String topic = text.trim().replaceAll("[^a-zA-Z0-9]", "_");

            // Generate JSON exam content
            String result = aiExamService.generateExamFromTopic(text, numMCQ, numTrueFalse, numShortAnswer, difficulty);
            System.out.println("✅ Exam generated successfully for topic: " + topic);

            // Folder to save the PDFs
            String outputDir = "C:/exams";
            File dir = new File(outputDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println(created ? "📁 Folder created: " + dir.getAbsolutePath() : "⚠️ Folder already exists.");
            }

            // Generate PDFs (base path includes topic)
            aiExamService.generateExamPDFs(result, outputDir,topic );

            String baseUrl = "http://localhost:8083/exams/";
            String questionPdfName = topic + "_exam_questions.pdf";
            String answerPdfName = topic + "_exam_answers.pdf";

            Map<String, String> response = new HashMap<>();
            response.put("message", "Examen généré avec succès !");
            response.put("questionPdf", baseUrl + questionPdfName);
            response.put("answerPdf", baseUrl + answerPdfName);
            response.put("examJson", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping(value = "/modifyExam", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> modifyExam(@RequestBody Map<String, Object> request) {
        try {
            // Get data from request
            String instruction = (String) request.get("instruction");
            String topic = (String) request.getOrDefault("topic", "modified_exam");
            String currentExamJson = (String) request.get("currentExam");

            if (instruction == null || instruction.trim().isEmpty()) {
                throw new IllegalArgumentException("Instruction cannot be empty.");
            }
            if (currentExamJson == null || currentExamJson.trim().isEmpty()) {
                throw new IllegalArgumentException("Current exam JSON is required.");
            }

            // Generate new exam JSON based on instruction and current exam
            String modifiedExamJson = aiExamService.regenerateExamWithPrompt(currentExamJson, instruction);

            // Folder to save PDFs
            String outputDir = "C:/exams";
            File dir = new File(outputDir);
            if (!dir.exists()) dir.mkdirs();

            // Generate PDFs
            aiExamService.generateExamPDFs(modifiedExamJson, outputDir, topic);

            // Construct URLs
            String questionPdfName = topic + "_exam_questions.pdf";
            String answerPdfName = topic + "_exam_answers.pdf";

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Examen modifié généré avec succès !");
            response.put("questionPdf", "http://localhost:8083/exams/" + questionPdfName);
            response.put("answerPdf", "http://localhost:8083/exams/" + answerPdfName);
            response.put("questions", modifiedExamJson); // send back new exam JSON

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping("/health")
    public String health() {
        return "✅ Voice API is running!";
    }

}


