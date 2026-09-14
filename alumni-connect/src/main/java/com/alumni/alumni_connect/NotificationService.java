package com.alumni.alumni_connect;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(

            NotificationRepository repository,

            SimpMessagingTemplate messagingTemplate

    ) {

        this.repository =
                repository;

        this.messagingTemplate =
                messagingTemplate;
    }

    // =====================================
    // CREATE NOTIFICATION
    // =====================================

    public void sendNotification(

            String email,

            String message,

            String type,

            String linkUrl

    ) {

        Notification notification =
                new Notification();

        notification.setEmail(email);

        notification.setMessage(message);

        notification.setType(type);

        notification.setLinkUrl(linkUrl);

        notification.setTimestamp(
                LocalDateTime.now()
        );

        // SAVE DATABASE

        Notification saved =
                repository.save(notification);

        // REALTIME WEBSOCKET

        messagingTemplate.convertAndSend(

                "/topic/notifications/" + email,

                saved
        );
    }

    // =====================================
    // GET NOTIFICATIONS
    // =====================================

    public List<Notification> getNotifications(
            String email
    ) {

        return repository
                .findByEmailOrderByTimestampDesc(
                        email
                );
    }

    // =====================================
    // UNREAD COUNT
    // =====================================

    public long getUnreadCount(
            String email
    ) {

        return repository
                .countByEmailAndIsReadFalse(
                        email
                );
    }

    // =====================================
    // MARK READ
    // =====================================

    public Notification markRead(

            Long id,

            String email

    ) {

        Notification notification =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notification not found"
                                )
                        );

        // =====================================
        // CHECK OWNERSHIP
        // =====================================

        if (
                !notification
                        .getEmail()
                        .equals(email)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify this notification"
            );
        }

        // =====================================
        // MARK AS READ
        // =====================================

        notification.setRead(true);

        return repository.save(
                notification
        );
    }
}
