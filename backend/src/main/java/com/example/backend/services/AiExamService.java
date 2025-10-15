package com.example.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;



@Service
public class AiExamService {

    @Value("${gemini.api.key}")
    private  String geminiApiKey;


    public String generateExamFromTopic(String topic,
                                        String numMCQ,
                                        String numTrueFalse,
                                        String numShortAnswer,
                                        String difficulty) throws IOException {

        String prompt = String.format("""
        You are a teacher. Generate exam questions on the following topic:
        Topic: %s
        Generate exactly:
        - %s multiple-choice questions
        - %s true/false questions
        - %s short answer questions
        Difficulty: %s
        Return the result ONLY in JSON format:
        [
          {"type": "MCQ", "question": "...", "options": ["A", "B", "C", "D"], "answer": "B"},
          {"type": "TrueFalse", "question": "...", "answer": "True"},
          {"type": "ShortAnswer", "question": "...", "answer": "..."}
        ]
        """, topic, numMCQ, numTrueFalse, numShortAnswer, difficulty);

        return callGeminiAPI(prompt);
    }

    public String regenerateExamWithPrompt(String currentExamJson, String instruction) throws IOException {
        String prompt = String.format("""
        You are an AI exam generator.
        Modify the following exam JSON according to the user's instruction.

        Current Exam JSON:
        %s

        Instruction:
        %s

        Return a valid JSON array with the modified exam.
        """, currentExamJson, instruction);

        return callGeminiAPI(prompt);
    }


    private String callGeminiAPI(String prompt) throws IOException {
        // ✅ Correct endpoint — the API key is passed in the URL, not as a header
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        // Build JSON request
        JSONObject requestBody = new JSONObject()
                .put("contents", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("parts", new org.json.JSONArray()
                                        .put(new JSONObject().put("text", prompt))
                                )
                        )
                );

        // Send JSON
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream stream = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        String response = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        if (status >= 400) {
            throw new IOException("Gemini API error: " + response);
        }

        // Parse response text
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim();
    }

    public void generateExamPDFs(String jsonResponse, String outputDir, String topic) throws Exception {

        // Strip Markdown code fences
        jsonResponse = jsonResponse.replaceAll("(?s)```[a-zA-Z]*\\n", "")
                .replaceAll("```", "");

        JSONArray questions = new JSONArray(jsonResponse);

        Document questionDoc = new Document();
        Document answerDoc = new Document();

        PdfWriter.getInstance(questionDoc, new FileOutputStream(outputDir  + "/" + topic + "_exam_questions.pdf"));
        PdfWriter.getInstance(answerDoc, new FileOutputStream(outputDir  + "/" + topic + "_exam_answers.pdf"));

        questionDoc.open();
        answerDoc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font questionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font answerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLUE);
        Font blankFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY);

        questionDoc.add(new Paragraph("📘 Exam Questions\n\n", titleFont));
        answerDoc.add(new Paragraph("📗 Exam Questions with Answers\n\n", titleFont));

        int count = 1;
        for (int i = 0; i < questions.length(); i++) {
            JSONObject q = questions.getJSONObject(i);
            String type = q.getString("type");
            String question = q.getString("question");

            // Add question to both PDFs
            questionDoc.add(new Paragraph(count + ". [" + type + "] " + question, questionFont));
            answerDoc.add(new Paragraph(count + ". [" + type + "] " + question, questionFont));

            // Add options if present (for MCQ)
            if (q.has("options")) {
                JSONArray opts = q.getJSONArray("options");
                for (int j = 0; j < opts.length(); j++) {
                    String option = opts.getString(j);
                    questionDoc.add(new Paragraph("   " + option, questionFont));
                    answerDoc.add(new Paragraph("   " + option, questionFont));
                }
            }

            // Handle answers
            if (q.has("answer")) {
                String answer = q.getString("answer").replaceAll("(?s)```[a-zA-Z]*\\n", "").replaceAll("```", "");

                if (type.equalsIgnoreCase("ShortAnswer")) {
                    // For questions PDF: add blank lines for the student to write
                    questionDoc.add(new Paragraph("Answer: ____________________________", blankFont));
                } else {
                    // For MCQ/TrueFalse in question PDF we usually don't show answer
                    questionDoc.add(new Paragraph("", blankFont));
                }

                // For answers PDF: always show the actual answer
                answerDoc.add(new Paragraph("Answer: " + answer, answerFont));
            }

            questionDoc.add(new Paragraph("\n"));
            answerDoc.add(new Paragraph("\n"));
            count++;
        }

        questionDoc.close();
        answerDoc.close();
    }
    
}
