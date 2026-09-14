package com.alumni.alumni_connect;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    private final MessageService messageService;

    public MessageController(
            MessageService messageService
    ) {

        this.messageService = messageService;
    }

    // =====================================
    // SEND MESSAGE
    // =====================================

    @MessageMapping("/chat")
    public void sendMessage(
            @Payload Message message
    ) {

        messageService.sendMessage(message);
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

        return messageService.getConversation(
                sender,
                receiver
        );
    }

    // =====================================
    // GET INBOX CONVERSATIONS
    // =====================================

    @GetMapping("/conversations")
    public List<ConversationDTO> getConversations() {

        return messageService.getConversations();
    }
}
