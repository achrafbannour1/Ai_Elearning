package com.example.backend.controllers;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/event")
public class EventController {
    private final EventService eventService;

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
    public void registerUser(@PathVariable("event-id") Long eventId, @AuthenticationPrincipal User user) {
        System.out.println("Registering user ID: " + user.getId() + " for event ID: " + eventId);
        eventService.registerUser(user.getId(), eventId);
    }
}