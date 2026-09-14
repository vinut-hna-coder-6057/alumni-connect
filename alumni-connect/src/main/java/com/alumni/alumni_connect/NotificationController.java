package com.alumni.alumni_connect;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController

@CrossOrigin(
        origins = "http://localhost:4200"
)

public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {

        this.notificationService =
                notificationService;
    }

    // =====================================
    // GET ALL NOTIFICATIONS
    // =====================================

    @GetMapping(
            "/notifications"
    )

    public List<Notification> getNotifications() {

        String email =
                getAuthenticatedEmail();

        return notificationService
                .getNotifications(email);
    }

    // =====================================
    // UNREAD COUNT
    // =====================================

    @GetMapping(
            "/notifications/unread"
    )

    public long getUnreadCount() {

        String email =
                getAuthenticatedEmail();

        return notificationService
                .getUnreadCount(email);
    }

    // =====================================
    // MARK READ
    // =====================================

    @PutMapping(
            "/notifications/read/{id}"
    )

    public Notification markRead(

            @PathVariable Long id

    ) {

        String email =
                getAuthenticatedEmail();

        return notificationService
                .markRead(id, email);
    }

    // =====================================
    // GET LOGGED-IN USER EMAIL
    // =====================================

    private String getAuthenticatedEmail() {

        if (
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
