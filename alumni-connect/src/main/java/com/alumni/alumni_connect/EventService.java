package com.alumni.alumni_connect;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

            // ALUMNI EVENTS REQUIRE APPROVAL

            event.setStatus("PENDING");
        }

        // INITIAL RSVP COUNT

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

        Optional<Event> optionalEvent =
                eventRepository.findById(id);

        if (optionalEvent.isEmpty()) {

            throw new RuntimeException(
                    "Event not found"
            );
        }

        Event event =
                optionalEvent.get();

        event.setStatus("APPROVED");

        return eventRepository.save(event);
    }

    // =====================================
    // REJECT EVENT
    // =====================================

    public Event rejectEvent(Long id) {

        Optional<Event> optionalEvent =
                eventRepository.findById(id);

        if (optionalEvent.isEmpty()) {

            throw new RuntimeException(
                    "Event not found"
            );
        }

        Event event =
                optionalEvent.get();

        event.setStatus("REJECTED");

        return eventRepository.save(event);
    }

    // =====================================
    // REGISTER FOR EVENT
    // =====================================

    public ResponseEntity<?> registerForEvent(
            Long eventId,
            String studentEmail
    ) {

        // CHECK DUPLICATE

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

        // CREATE REGISTRATION

        EventRegistration registration =
                new EventRegistration();

        registration.setEventId(eventId);

        registration.setStudentEmail(studentEmail);

        registration.setRegisteredAt(
                LocalDateTime.now()
        );

        registrationRepository.save(
                registration
        );

        // UPDATE RSVP COUNT

        Optional<Event> optionalEvent =
                eventRepository.findById(eventId);

        if (optionalEvent.isPresent()) {

            Event event =
                    optionalEvent.get();

            event.setAttendeeCount(
                    event.getAttendeeCount() + 1
            );

            eventRepository.save(event);

            // SEND EMAIL

            emailService.sendEventRegistrationEmail(

                    studentEmail,

                    event.getTitle(),

                    event.getEventDate()
                            .toString(),

                    event.getLocation(),

                    event.getMeetingLink()
            );
        }

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

    public ResponseEntity<?> cancelRegistration(
            Long eventId,
            String studentEmail
    ) {

        registrationRepository
                .deleteByEventIdAndStudentEmail(
                        eventId,
                        studentEmail
                );

        // UPDATE RSVP COUNT

        Optional<Event> optionalEvent =
                eventRepository.findById(eventId);

        if (optionalEvent.isPresent()) {

            Event event =
                    optionalEvent.get();

            if (event.getAttendeeCount() > 0) {

                event.setAttendeeCount(
                        event.getAttendeeCount() - 1
                );

                eventRepository.save(event);
            }
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
