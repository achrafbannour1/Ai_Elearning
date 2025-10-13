package com.example.backend.controllers;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.services.EventService;
import com.example.backend.services.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageGenerationService imageGenerationService;

    // Existing endpoints (unchanged)
    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.retrieveEvent(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event created = eventService.addEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        event.setId(id);
        Event updated = eventService.modifyEvent(event);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String roleStr = payload.get("role");
        if (roleStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        try {
            user.setRole(com.example.backend.entity.Role.valueOf(roleStr));
            User updated = userRepository.save(user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleStr);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Event> events = eventRepository.findAll();
        List<User> users = userRepository.findAll();

        long totalEvents = events.size();
        long availableEvents = events.stream().filter(e -> !e.isFull() && e.getSeatsLeft() > 0).count();
        long fullEvents = events.stream().filter(Event::isFull).count();
        long totalUsers = users.size();
        long adminUsers = users.stream().filter(u -> u.getRole().toString().equals("ROLE_ADMIN")).count();
        long studentUsers = users.stream().filter(u -> u.getRole().toString().equals("ROLE_STUDENT")).count();
        int totalRegistrations = users.stream().mapToInt(u -> u.getEvents().size()).sum();

        stats.put("totalEvents", totalEvents);
        stats.put("availableEvents", availableEvents);
        stats.put("fullEvents", fullEvents);
        stats.put("totalUsers", totalUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("studentUsers", studentUsers);
        stats.put("totalRegistrations", totalRegistrations);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/events/{id}/registrations")
    public ResponseEntity<List<User>> getEventRegistrations(@PathVariable Long id) {
        Event event = eventService.retrieveEvent(id);
        return ResponseEntity.ok(List.copyOf(event.getUsers()));
    }

    @DeleteMapping("/events/{eventId}/registrations/{userId}")
    public ResponseEntity<Map<String, String>> removeUserFromEvent(@PathVariable Long eventId, @PathVariable Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getEvents().contains(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not registered for this event");
        }

        user.getEvents().remove(event);
        event.getUsers().remove(user);
        event.setSeatsLeft(event.getSeatsLeft() + 1);
        if (event.getSeatsLeft() > 0) {
            event.setFull(false);
        }

        userRepository.save(user);
        eventRepository.save(event);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User removed from event successfully");
        return ResponseEntity.ok(response);
    }

    // New endpoint for image generation
    @PostMapping("/generate-image")
    public ResponseEntity<Map<String, String>> generateEventImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt is required");
        }

        try {
            String imageUrl = imageGenerationService.generateImage(prompt);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Image generated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate image: " + e.getMessage());
        }
    }
}