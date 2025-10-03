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
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found");
        }
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