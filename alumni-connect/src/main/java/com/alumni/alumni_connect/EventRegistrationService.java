package com.alumni.alumni_connect;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EventRegistrationService {

    private final EventRepository eventRepository;

    private final EventRegistrationRepository registrationRepository;

    private final EmailService emailService;

    public EventRegistrationService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            EmailService emailService
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.emailService = emailService;
    }

    // =====================================
    // REGISTER FOR EVENT
    // =====================================

    @Transactional
    public String registerForEvent(
            Long eventId,
            String studentEmail
    ) {

        // =====================================
        // LOCK EVENT
        // =====================================

        Event event = eventRepository
                .findByIdForUpdate(eventId)
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
            return "Already registered";
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

        registrationRepository.save(
                registration
        );

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

        return "Registered successfully";
    }

    // =====================================
    // CANCEL REGISTRATION
    // =====================================

    @Transactional
    public String cancelRegistration(
            Long eventId,
            String studentEmail
    ) {

        // LOCK EVENT

        Event event = eventRepository
                .findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Event not found"
                        )
                );

        // CHECK REGISTRATION

        boolean registered =
                registrationRepository
                        .existsByEventIdAndStudentEmail(
                                eventId,
                                studentEmail
                        );

        if (!registered) {
            return "Not registered";
        }

        // DELETE REGISTRATION

        registrationRepository
                .deleteByEventIdAndStudentEmail(
                        eventId,
                        studentEmail
                );

        // DECREASE COUNT

        if (event.getAttendeeCount() > 0) {

            event.setAttendeeCount(
                    event.getAttendeeCount() - 1
            );

            eventRepository.save(event);
        }

        return "Registration cancelled";
    }
}
