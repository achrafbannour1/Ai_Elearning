package com.example.backend.controllers;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository; // Add this import
import com.example.backend.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired; // Add this for UserRepository
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication; // Add this import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/event")
public class EventController {
    private final EventService eventService;
    @Autowired
    private UserRepository userRepository; // Add this field

    @GetMapping("/retrieve-all-events")
    public List<Event> getEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/retrieve-event/{event-id}")
    public Event retrieveEvent(@PathVariable("event-id") Long eventId) {
        return eventService.retrieveEvent(eventId);
    }

    @PostMapping("/add-event")
    public Event addEvent(@RequestBody Event event) {
        return eventService.addEvent(event);
    }

    @DeleteMapping("/remove-event/{event-id}")
    public void removeEvent(@PathVariable("event-id") Long eventId) {
        eventService.deleteEvent(eventId);
    }

    @PutMapping("/modify-event")
    public Event modifyEvent(@RequestBody Event event) {
        return eventService.modifyEvent(event);
    }

    @PostMapping("/register/{event-id}")
    public void registerUser(@PathVariable("event-id") Long eventId, Authentication authentication) { // Change to Authentication
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to be logged in to join an event.");
        }

        String email = authentication.getName(); // Get email from principal
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account not found. Please log in again."));

        System.out.println("Registering user ID: " + user.getId() + " for event ID: " + eventId);
        eventService.registerUser(user.getId(), eventId);
    }
}