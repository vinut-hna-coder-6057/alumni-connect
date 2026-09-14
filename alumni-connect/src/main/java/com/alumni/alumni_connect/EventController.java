package com.alumni.alumni_connect;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    private final EventService eventService;

    public EventController(
            EventService eventService
    ) {

        this.eventService = eventService;
    }

    // =====================================
    // CREATE EVENT
    // =====================================

    @PostMapping("/events")
    public Event createEvent(
            @RequestBody Event event
    ) {

        return eventService.createEvent(event);
    }

    // =====================================
    // STUDENT EVENTS
    // ONLY APPROVED EVENTS
    // =====================================

    @GetMapping("/events")
    public List<Event> getApprovedEvents() {

        return eventService.getApprovedEvents();
    }

    // =====================================
    // ADMIN EVENT PANEL
    // VIEW ALL EVENTS
    // =====================================

    @GetMapping("/events/all")
    public List<Event> getAllEvents() {

        return eventService.getAllEvents();
    }

    // =====================================
    // APPROVE EVENT
    // =====================================

    @PutMapping("/events/approve/{id}")
    public Event approveEvent(
            @PathVariable Long id
    ) {

        return eventService.approveEvent(id);
    }

    // =====================================
    // REJECT EVENT
    // =====================================

    @PutMapping("/events/reject/{id}")
    public Event rejectEvent(
            @PathVariable Long id
    ) {

        return eventService.rejectEvent(id);
    }

    // =====================================
    // REGISTER FOR EVENT
    // =====================================

    @PostMapping("/events/register")
    public ResponseEntity<?> registerForEvent(

            @RequestParam Long eventId,

            @RequestParam String studentEmail

    ) {

        return eventService.registerForEvent(
                eventId,
                studentEmail
        );
    }

    // =====================================
    // CANCEL REGISTRATION
    // =====================================

    @DeleteMapping("/events/register")
    public ResponseEntity<?> cancelRegistration(

            @RequestParam Long eventId,

            @RequestParam String studentEmail

    ) {

        return eventService.cancelRegistration(
                eventId,
                studentEmail
        );
    }

    // =====================================
    // EVENT ATTENDEES
    // =====================================

    @GetMapping("/events/attendees/{eventId}")
    public List<EventRegistration> getAttendees(

            @PathVariable Long eventId

    ) {

        return eventService.getAttendees(eventId);
    }
}
