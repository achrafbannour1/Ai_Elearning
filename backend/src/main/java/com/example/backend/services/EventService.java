package com.example.backend.services;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event addEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event modifyEvent(Event event) {
        Event existingEvent = eventRepository.findById(event.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + event.getId() + " not found"));
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDate(event.getDate());
        existingEvent.setSeatsLeft(event.getSeatsLeft());
        existingEvent.setFull(event.isFull());
        existingEvent.setImage(event.getImage());
        existingEvent.setDescription(event.getDescription());
        return eventRepository.save(existingEvent);
    }

    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found"));

        // Remove the event from all users' event sets
        for (User user : event.getUsers()) {
            user.getEvents().remove(event);
            userRepository.save(user); // Save each user to update the relationship
        }

        // Clear the users from the event to ensure no references remain
        event.getUsers().clear();
        eventRepository.save(event); // Save the event to update the relationship

        // Now delete the event
        eventRepository.deleteById(eventId);
    }

    public Event retrieveEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found"));
    }

    public void registerUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found"));

        if (user.getEvents().contains(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already registered for this event");
        }

        if (event.isFull() || event.getSeatsLeft() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is full or no seats available");
        }

        event.setSeatsLeft(event.getSeatsLeft() - 1);
        if (event.getSeatsLeft() == 0) {
            event.setFull(true);
        }
        user.getEvents().add(event);
        event.getUsers().add(user);
        userRepository.save(user);
        eventRepository.save(event);
    }
}