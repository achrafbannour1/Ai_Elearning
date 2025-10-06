package com.example.backend.services;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OllamaService {
    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventService eventService;

    @Value("${ollama.url:http://localhost:11434/api/chat}")
    private String ollamaUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public Map<String, Object> processQuery(String userQuery) {
        logger.info("Processing query: {}", userQuery);

        Map<String, Object> response = new HashMap<>();

        try {
            // Get context about the platform
            String platformContext = buildPlatformContext();

            String ollamaResponse = queryOllamaModel(userQuery, platformContext);
            logger.info("Ollama raw response: {}", ollamaResponse);

            String intent = extractIntent(ollamaResponse);
            logger.info("Extracted intent: {}", intent);

            response.put("intent", intent);

            switch (intent) {
                case "list_events":
                    handleListEvents(response);
                    break;
                case "join_event":
                    handleJoinEvent(response, ollamaResponse);
                    break;
                case "navigate":
                    handleNavigation(response, ollamaResponse);
                    break;
                case "chat":
                case "question":
                    handleGeneralQuery(response, ollamaResponse);
                    break;
                default:
                    handleGeneralQuery(response, ollamaResponse);
            }
        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "An error occurred while processing your request");
            response.put("message", "Sorry, I encountered a problem. Please try again.");
        }

        return response;
    }

    private String buildPlatformContext() {
        try {
            List<Event> events = eventRepository.findAll();
            long availableEvents = events.stream()
                    .filter(e -> !e.isFull() && e.getSeatsLeft() > 0)
                    .count();

            // Get current user info if authenticated
            String userInfo = "";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                userRepository.findByEmail(auth.getName()).ifPresent(user -> {
                    int enrolledCount = user.getEvents().size();
                });
            }

            StringBuilder context = new StringBuilder();
            context.append("Platform Information:\n");
            context.append("- Total events: ").append(events.size()).append("\n");
            context.append("- Available events with seats: ").append(availableEvents).append("\n");
            context.append("- Platform name: E-Learning Platform\n");
            context.append("- Features: Event registration, course enrollment, user authentication\n");

            if (!events.isEmpty()) {
                context.append("\nUpcoming Events:\n");
                events.stream().limit(3).forEach(event -> {
                    context.append("- ").append(event.getTitle())
                            .append(" (ID: ").append(event.getId()).append(")")
                            .append(" - ").append(event.getSeatsLeft()).append(" seats left")
                            .append(" on ").append(event.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                            .append("\n");
                });
            }

            return context.toString();
        } catch (Exception e) {
            logger.error("Error building platform context: {}", e.getMessage());
            return "E-Learning Platform with events and courses.";
        }
    }

    private void handleListEvents(Map<String, Object> response) {
        try {
            List<Event> events = eventRepository.findAll();
            long availableCount = events.stream()
                    .filter(e -> !e.isFull() && e.getSeatsLeft() > 0)
                    .count();

            response.put("events", events);
            response.put("available_count", availableCount);
            response.put("total_count", events.size());
            response.put("success", true);

            String message = String.format("We have %d events available with %d having open seats.",
                    events.size(), availableCount);
            response.put("message", message);

            logger.info("Listed {} events, {} available", events.size(), availableCount);
        } catch (Exception e) {
            logger.error("Error listing events: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Could not retrieve events. Please try again.");
        }
    }
/*
    private void handleJoinEvent(Map<String, Object> response, String ollamaResponse) {
        try {
            Long eventId = extractEventId(ollamaResponse);

            if (eventId == null || eventId == 0) {
                logger.warn("No valid event ID extracted from response");
                response.put("success", false);
                response.put("message", "Please specify which event you'd like to join. You can say 'join event 1' or provide the event title.");
                return;
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                logger.warn("No authenticated user found");
                response.put("success", false);
                response.put("message", "You need to be logged in to join an event.");
                return;
            }

            String email = auth.getName();
            logger.info("User {} attempting to join event {}", email, eventId);

            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                try {
                    eventService.registerUser(user.getId(), eventId);
                    response.put("success", true);
                    response.put("event_id", eventId);
                    response.put("message", "Successfully joined event " + eventId + "!");
                    logger.info("User {} successfully joined event {}", email, eventId);
                } catch (ResponseStatusException e) {
                    logger.warn("Failed to register user {} for event {}: {}", email, eventId, e.getReason());
                    response.put("success", false);
                    response.put("message", e.getReason());
                } catch (Exception e) {
                    logger.error("Unexpected error registering user {} for event {}: {}", email, eventId, e.getMessage(), e);
                    response.put("success", false);
                    response.put("message", "An error occurred while joining the event. Please try again.");
                }
            }, () -> {
                logger.error("User not found with email: {}", email);
                response.put("success", false);
                response.put("message", "User account not found. Please log in again.");
            });

        } catch (Exception e) {
            logger.error("Error handling join event: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Could not process your request to join the event.");
        }
    }
*/
private void handleJoinEvent(Map<String, Object> response, String ollamaResponse) {
    try {
        Long eventId = extractEventId(ollamaResponse);
        logger.info("Extracted event ID: {}", eventId);
        if (eventId == null || eventId == 0) {
            logger.warn("No valid event ID extracted from response: {}", ollamaResponse);
            response.put("success", false);
            response.put("message", "Please specify which event you'd like to join. You can say 'join event 1' or provide the event title.");
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            logger.warn("No authenticated user found. Auth: {}, Name: {}", auth, auth != null ? auth.getName() : "null");
            response.put("success", false);
            response.put("message", "You need to be logged in to join an event.");
            return;
        }

        String email = auth.getName();
        logger.info("User {} attempting to join event {}", email, eventId);

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            logger.info("Found user: {} (ID: {})", user.getEmail(), user.getId());
            try {
                eventService.registerUser(user.getId(), eventId);
                response.put("success", true);
                response.put("event_id", eventId);
                response.put("message", "Successfully joined event " + eventId + "!");
                logger.info("User {} successfully joined event {}", email, eventId);
            } catch (ResponseStatusException e) {
                logger.warn("Failed to register user {} for event {}: {}", email, eventId, e.getReason());
                response.put("success", false);
                response.put("message", e.getReason());
            }
        }, () -> {
            logger.error("User not found with email: {}", email);
            response.put("success", false);
            response.put("message", "User account not found. Please log in again.");
        });
    } catch (Exception e) {
        logger.error("Error handling join event: {}", e.getMessage(), e);
        response.put("success", false);
        response.put("message", "Could not process your request to join the event.");
    }
}
    private void handleNavigation(Map<String, Object> response, String ollamaResponse) {
        try {
            String page = extractPage(ollamaResponse);

            if (page == null || page.isEmpty()) {
                logger.warn("No valid page extracted from response");
                response.put("success", false);
                response.put("message", "I'm not sure which page you want to go to. Try saying 'go to events page' or 'navigate to home'.");
                return;
            }

            response.put("page", page);
            response.put("success", true);
            response.put("message", "Navigating to " + page + " page");
            logger.info("Navigation request to page: {}", page);

        } catch (Exception e) {
            logger.error("Error handling navigation: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Could not navigate. Please try again.");
        }
    }

    private void handleGeneralQuery(Map<String, Object> response, String ollamaResponse) {
        try {
            String aiResponse = extractChatResponse(ollamaResponse);

            if (aiResponse == null || aiResponse.isEmpty()) {
                aiResponse = "I'm here to help! You can ask me about events, join events, or navigate the platform.";
            }

            response.put("message", aiResponse);
            response.put("success", true);
            logger.info("General query handled with response: {}", aiResponse);

        } catch (Exception e) {
            logger.error("Error handling general query: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "I'm here to help with your e-learning needs. Try asking about events!");
        }
    }

    private String queryOllamaModel(String userQuery, String platformContext) {
        JSONObject payload = new JSONObject();
        payload.put("model", "llama3");

        JSONArray messages = new JSONArray();

        // System message with platform context
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "You are a helpful AI assistant for an e-learning platform. " +
                        "You must ALWAYS respond in valid JSON format.\n\n" +
                        platformContext + "\n\n" +
                        "Analyze user queries and respond with this exact JSON structure:\n" +
                        "For listing/asking about events: {\"intent\": \"list_events\", \"details\": {}}\n" +
                        "For joining/registering for an event: {\"intent\": \"join_event\", \"details\": {\"event_id\": <number>}}\n" +
                        "For navigation requests: {\"intent\": \"navigate\", \"details\": {\"page\": \"<page_name>\"}}\n" +
                        "For general questions, greetings, or information requests: {\"intent\": \"question\", \"details\": {\"response\": \"<your helpful answer>\"}}\n" +
                        "For unclear requests: {\"intent\": \"unknown\", \"details\": {\"response\": \"I can help you with events, courses, and platform navigation. What would you like to know?\"}}\n\n" +
                        "When answering questions:\n" +
                        "- Use the platform information provided above\n" +
                        "- Be friendly, helpful, and conversational\n" +
                        "- Answer questions about the platform, events, features, and general topics\n" +
                        "- If you don't know something specific about the platform, say so honestly\n" +
                        "- Keep responses concise but informative\n\n" +
                        "NEVER respond with plain text. ALWAYS use the JSON structure above."
        );
        messages.put(systemMsg);

        // User message
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userQuery);
        messages.put(userMsg);

        payload.put("messages", messages);
        payload.put("stream", false);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        Request request = new Request.Builder()
                .url(ollamaUrl)
                .post(body)
                .build();

        try (Response resp = client.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                logger.error("Ollama request failed with code: {}", resp.code());
                throw new IOException("Ollama request failed with code " + resp.code());
            }

            String responseBody = resp.body().string();
            logger.debug("Ollama full response: {}", responseBody);

            JSONObject json = new JSONObject(responseBody);
            String content = json.getJSONObject("message").getString("content");

            // Clean up the response if it has markdown code blocks
            content = cleanJsonResponse(content);

            return content;

        } catch (IOException e) {
            logger.error("IO error communicating with Ollama: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not communicate with AI service: " + e.getMessage()
            );
        } catch (JSONException e) {
            logger.error("Invalid JSON from Ollama: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI service returned invalid response"
            );
        }
    }

    private String cleanJsonResponse(String content) {
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }

    private String extractIntent(String response) {
        try {
            JSONObject json = new JSONObject(response);
            String intent = json.optString("intent", "question");
            logger.debug("Extracted intent: {}", intent);
            return intent;
        } catch (JSONException e) {
            logger.error("Failed to parse intent from response: {}. Response was: {}", e.getMessage(), response);
            return "question";
        }
    }

    private Long extractEventId(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject details = json.optJSONObject("details");

            if (details == null) {
                logger.warn("No details object in response");
                return null;
            }

            if (!details.has("event_id")) {
                logger.warn("No event_id field in details");
                return null;
            }

            long eventId = details.getLong("event_id");

            if (eventId <= 0) {
                logger.warn("Invalid event_id: {}", eventId);
                return null;
            }

            logger.debug("Extracted event ID: {}", eventId);
            return eventId;

        } catch (JSONException e) {
            logger.error("Failed to parse event ID from response: {}. Response was: {}", e.getMessage(), response);
            return null;
        }
    }

    private String extractPage(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject details = json.optJSONObject("details");

            if (details == null) {
                logger.warn("No details object in response");
                return null;
            }

            String page = details.optString("page", null);
            logger.debug("Extracted page: {}", page);
            return page;

        } catch (JSONException e) {
            logger.error("Failed to parse page from response: {}. Response was: {}", e.getMessage(), response);
            return null;
        }
    }

    private String extractChatResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject details = json.optJSONObject("details");

            if (details == null) {
                logger.warn("No details object in response for chat");
                return null;
            }

            String chatResponse = details.optString("response", null);
            logger.debug("Extracted chat response: {}", chatResponse);
            return chatResponse;

        } catch (JSONException e) {
            logger.error("Failed to parse chat response: {}. Response was: {}", e.getMessage(), response);
            return null;
        }
    }
}