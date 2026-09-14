package com.alumni.alumni_connect;

import org.springframework.web.bind.annotation.*;

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
            "/notifications/{email}"
    )

    public List<Notification> getNotifications(

            @PathVariable String email

    ) {

        return notificationService
                .getNotifications(email);
    }

    // =====================================
    // UNREAD COUNT
    // =====================================

    @GetMapping(
            "/notifications/unread/{email}"
    )

    public long getUnreadCount(

            @PathVariable String email

    ) {

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

        return notificationService
                .markRead(id);
    }
}
