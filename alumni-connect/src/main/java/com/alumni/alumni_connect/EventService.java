package com.alumni.alumni_connect;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    private final EventRegistrationRepository registrationRepository;

    private final EmailService emailService;

    public EventService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            EmailService emailService
    ) {

        this.eventRepository = eventRepository;

        this.registrationRepository = registrationRepository;

        this.emailService = emailService;
    }

    // =====================================
    // CREATE EVENT
    // =====================================

    public Event createEvent(Event event) {

        event.setCreatedAt(
                LocalDateTime.now()
        );

        // ADMIN EVENTS AUTO APPROVED

        if (
                event.getRole() != null
                        &&
                event.getRole()
                        .toUpperCase()
                        .contains("ADMIN")
        ) {

            event.setStatus("APPROVED");

        } else {

            event.setStatus("PENDING");
        }

        event.setAttendeeCount(0);

        return eventRepository.save(event);
    }

    // =====================================
    // GET APPROVED EVENTS
    // =====================================

    public List<Event> getApprovedEvents() {

        return eventRepository
                .findByStatusOrderByCreatedAtDesc(
                        "APPROVED"
                );
    }

    // =====================================
    // GET ALL EVENTS
    // =====================================

    public List<Event> getAllEvents() {

        return eventRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // =====================================
    // APPROVE EVENT
    // =====================================

    public Event approveEvent(Long id) {

        Event event =
                eventRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Event not found"
                                )
                        );

        event.setStatus("APPROVED");

        return eventRepository.save(event);
    }

    // =====================================
    // REJECT EVENT
    // =====================================

    public Event rejectEvent(Long id) {

        Event event =
                eventRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Event not found"
                                )
                        );

        event.setStatus("REJECTED");

        return eventRepository.save(event);
    }

    // =====================================
    // REGISTER FOR EVENT
    // =====================================

    @Transactional
    public ResponseEntity<?> registerForEvent(
            Long eventId,
            String studentEmail
    ) {

        // =====================================
        // GET + LOCK EVENT
        // =====================================

        Event event =
                eventRepository.findByIdForUpdate(eventId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Event not found"
                                )
                        );

        // =====================================
        // CHECK DUPLICATE
        // =====================================

        boolean alreadyRegistered =
                registrationRepository
                        .existsByEventIdAndStudentEmail(
                                eventId,
                                studentEmail
                        );

        if (alreadyRegistered) {

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Already registered"
                    )
            );
        }

        // =====================================
        // CREATE REGISTRATION
        // =====================================

        EventRegistration registration =
                new EventRegistration();

        registration.setEventId(eventId);

        registration.setStudentEmail(
                studentEmail
        );

        registration.setRegisteredAt(
                LocalDateTime.now()
        );

        try {

            registrationRepository.save(
                    registration
            );

        } catch (DataIntegrityViolationException e) {

            // DATABASE UNIQUE CONSTRAINT
            // PROTECTS AGAINST RACE CONDITIONS

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Already registered"
                    )
            );
        }

        // =====================================
        // UPDATE RSVP COUNT
        // =====================================

        event.setAttendeeCount(
                event.getAttendeeCount() + 1
        );

        eventRepository.save(event);

        // =====================================
        // SEND EMAIL
        // =====================================

        emailService.sendEventRegistrationEmail(

                studentEmail,

                event.getTitle(),

                event.getEventDate(),

                event.getLocation(),

                event.getMeetingLink()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Registered successfully"
                )
        );
    }

    // =====================================
    // CANCEL REGISTRATION
    // =====================================

    @Transactional
    public ResponseEntity<?> cancelRegistration(
            Long eventId,
            String studentEmail
    ) {

        // =====================================
        // LOCK EVENT
        // =====================================

        Event event =
                eventRepository.findByIdForUpdate(eventId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Event not found"
                                )
                        );

        // =====================================
        // FIND REGISTRATION
        // =====================================

        Optional<EventRegistration> registration =
                registrationRepository
                        .findByEventIdAndStudentEmail(
                                eventId,
                                studentEmail
                        );

        // =====================================
        // NOT REGISTERED
        // =====================================

        if (registration.isEmpty()) {

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Not registered"
                    )
            );
        }

        // =====================================
        // DELETE REGISTRATION
        // =====================================

        registrationRepository.delete(
                registration.get()
        );

        // =====================================
        // DECREASE COUNT
        // =====================================

        if (event.getAttendeeCount() > 0) {

            event.setAttendeeCount(
                    event.getAttendeeCount() - 1
            );

            eventRepository.save(event);
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Registration cancelled"
                )
        );
    }

    // =====================================
    // EVENT ATTENDEES
    // =====================================

    public List<EventRegistration> getAttendees(
            Long eventId
    ) {

        return registrationRepository
                .findByEventId(eventId);
    }
}
