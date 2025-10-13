package com.example.backend.services;

import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ImageGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);

    @Value("${huggingface.api.key:your-hf-token-here}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final String modelUrl = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0";

    public String generateImage(String prompt) {
        if (apiKey.equals("your-hf-token-here") || apiKey.isEmpty()) {
            logger.error("Hugging Face API key is not configured");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Hugging Face API key not configured. Check application.properties.");
        }

        JSONObject payload = new JSONObject();
        payload.put("inputs", prompt);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(modelUrl)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                logger.error("Hugging Face API error: {} - {}", response.code(), errorBody);
                if (response.code() == 404) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found. Verify model URL or token permissions at https://huggingface.co/stabilityai/stable-diffusion-xl-base-1.0.");
                } else if (response.code() == 503) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI model is warming up. Try again in 1-2 minutes.");
                } else if (response.code() == 429) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit hit. Wait 1 minute and retry.");
                }
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate image: " + response.code() + " - " + errorBody);
            }

            byte[] imageBytes = response.body().bytes();
            if (imageBytes.length == 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Empty image response from AI service");
            }

            String imageUrl = saveImageToStaticFolder(imageBytes, prompt);
            logger.info("Generated image URL: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            logger.error("IO error calling Hugging Face API: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not communicate with AI image service: " + e.getMessage());
        }
    }

    private String saveImageToStaticFolder(byte[] imageBytes, String prompt) throws IOException {
        String fileName = "generated_" + UUID.randomUUID() + ".png";
        Path staticPath = Paths.get("src/main/resources/static/images");
        Files.createDirectories(staticPath);

        // Optional: Resize to 512x512
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (img != null) {
            BufferedImage resized = net.coobird.thumbnailator.Thumbnails.of(img)
                    .size(512, 512)
                    .asBufferedImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", baos);
            imageBytes = baos.toByteArray();
        }

        Path filePath = staticPath.resolve(fileName);
        Files.write(filePath, imageBytes);

        return "/images/" + fileName; // Served as http://localhost:8083/images/...
    }
}