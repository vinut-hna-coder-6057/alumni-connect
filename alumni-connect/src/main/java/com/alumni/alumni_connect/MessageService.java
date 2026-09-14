package com.alumni.alumni_connect;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;

    private final MessageRepository repository;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    public MessageService(
            SimpMessagingTemplate messagingTemplate,
            MessageRepository repository,
            NotificationService notificationService,
            UserRepository userRepository
    ) {

        this.messagingTemplate =
                messagingTemplate;

        this.repository =
                repository;

        this.notificationService =
                notificationService;

        this.userRepository =
                userRepository;
    }

    // =====================================
    // SEND MESSAGE
    // =====================================

    public void sendMessage(Message message) {

        // GET REAL LOGGED-IN USER FROM JWT

        String authenticatedEmail =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        // CHECK RECEIVER

        if (message.getReceiverEmail() == null
                || message.getReceiverEmail().isBlank()) {

            System.out.println(
                    "MESSAGE REJECTED: Receiver missing"
            );

            return;
        }

        // CHECK RECEIVER EXISTS

        if (!userRepository
                .findByEmail(
                        message.getReceiverEmail()
                )
                .isPresent()) {

            System.out.println(
                    "MESSAGE REJECTED: Receiver does not exist"
            );

            return;
        }

        // NEVER TRUST SENDER FROM FRONTEND

        message.setSenderEmail(
                authenticatedEmail
        );

        // CHECK CONTENT

        if (message.getContent() == null
                || message.getContent().isBlank()) {

            System.out.println(
                    "MESSAGE REJECTED: Empty content"
            );

            return;
        }

        if (message.getContent().length() > 2000) {

            System.out.println(
                    "MESSAGE REJECTED: Content too long"
            );

            return;
        }

        // SERVER CONTROLS TIMESTAMP

        message.setTimestamp(
                LocalDateTime.now()
        );

        // SAVE MESSAGE

        Message saved =
                repository.save(message);

        // SEND TO RECEIVER

        messagingTemplate.convertAndSend(

                "/topic/messages/"
                        + saved.getReceiverEmail(),

                saved
        );

        // SEND BACK TO SENDER

        messagingTemplate.convertAndSend(

                "/topic/messages/"
                        + saved.getSenderEmail(),

                saved
        );

        // SEND NOTIFICATION

        notificationService.sendNotification(

                saved.getReceiverEmail(),

                "New message from "
                        + saved.getSenderEmail(),

                "MESSAGE",

                "/chat/"
                        + saved.getSenderEmail()
        );

        System.out.println(
                "MESSAGE SENT: "
                        + saved.getContent()
        );
    }

    // =====================================
    // GET CONVERSATION
    // =====================================

    public List<Message> getConversation(

            String sender,

            String receiver

    ) {

        return repository.findConversation(
                sender,
                receiver
        );
    }

    // =====================================
    // GET INBOX CONVERSATIONS
    // =====================================

    public List<ConversationDTO> getConversations() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        List<Message> messages =
                repository.findInboxMessages(email);

        Map<String, ConversationDTO> map =
                new LinkedHashMap<>();

        for (Message msg : messages) {

            String otherUser;

            if (msg.getSenderEmail().equals(email)) {

                otherUser =
                        msg.getReceiverEmail();

            } else {

                otherUser =
                        msg.getSenderEmail();
            }

            if (!map.containsKey(otherUser)) {

                map.put(
                        otherUser,

                        new ConversationDTO(
                                otherUser,
                                msg.getContent(),
                                msg.getTimestamp()
                        )
                );
            }
        }

        return new ArrayList<>(
                map.values()
        );
    }
}
