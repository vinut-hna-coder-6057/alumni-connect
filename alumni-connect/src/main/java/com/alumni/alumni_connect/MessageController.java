package com.alumni.alumni_connect;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin(
        origins = "http://localhost:4200"
)

public class MessageController {

    // =====================================
    // DEPENDENCIES
    // =====================================

    private final SimpMessagingTemplate
            messagingTemplate;

    private final MessageRepository
            repository;

    private final NotificationService
            notificationService;

        private final UserRepository
         userRepository;
    // =====================================
    // CONSTRUCTOR
    // =====================================

    public MessageController(
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
    @MessageMapping("/chat")
public void sendMessage(@Payload Message message) {

    // Get the real logged-in user from JWT
    String authenticatedEmail =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
if (message.getReceiverEmail() == null
        || message.getReceiverEmail().isBlank()) {

    System.out.println("MESSAGE REJECTED: Receiver missing");
    return;
}

if (!userRepository.findByEmail(message.getReceiverEmail()).isPresent()) {

    System.out.println("MESSAGE REJECTED: Receiver does not exist");
    return;
}

    // NEVER trust senderEmail from frontend
    message.setSenderEmail(authenticatedEmail);
if (message.getContent() == null
        || message.getContent().isBlank()) {

    System.out.println("MESSAGE REJECTED: Empty content");
    return;
}

if (message.getContent().length() > 2000) {

    System.out.println("MESSAGE REJECTED: Content too long");
    return;
}
    // Server controls timestamp
    message.setTimestamp(LocalDateTime.now());

    // Save
    Message saved = repository.save(message);

    // Send to receiver
    messagingTemplate.convertAndSend(
            "/topic/messages/" + saved.getReceiverEmail(),
            saved
    );

    // Send back to sender
    messagingTemplate.convertAndSend(
            "/topic/messages/" + saved.getSenderEmail(),
            saved
    );

    // Notification
    notificationService.sendNotification(
            saved.getReceiverEmail(),
            "New message from " + saved.getSenderEmail(),
            "MESSAGE",
            "/chat/" + saved.getSenderEmail()
    );

    System.out.println(
            "MESSAGE SENT: " + saved.getContent()
    );
}

    // =====================================
    // GET CONVERSATION
    // =====================================
    @GetMapping("/messages/conversation")
public List<Message> getConversation(

        @RequestParam String sender,

        @RequestParam String receiver

) {

    String authenticatedEmail =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

    if (!authenticatedEmail.equals(sender)
            && !authenticatedEmail.equals(receiver)) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not part of this conversation"
        );
    }

    return repository.findConversation(
            sender,
            receiver
    );
}
    // =====================================
    // GET INBOX CONVERSATIONS
    // =====================================

    @GetMapping("/conversations")
public List<ConversationDTO> getConversations() {

    String email = SecurityContextHolder
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

            otherUser = msg.getReceiverEmail();

        } else {

            otherUser = msg.getSenderEmail();
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

    return new ArrayList<>(map.values());
}
}