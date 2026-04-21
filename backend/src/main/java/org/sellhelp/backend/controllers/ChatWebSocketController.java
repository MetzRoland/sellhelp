package org.sellhelp.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.sellhelp.backend.dtos.requests.SendMessageRequest;
import org.sellhelp.backend.dtos.responses.ChatMessageResponse;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CurrentUser currentUser;

    @Autowired
    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate, CurrentUser currentUser){
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.currentUser = currentUser;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request) throws Exception {
        User sender = currentUser.getCurrentlyLoggedUserEntity();

        log.info("message::::::{}", request.getMessage());

        ChatMessageResponse response = chatService.sendMessage(
                request.getChatId(),
                request.getSenderId(),
                request.getMessage(),
                null
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getChatId(),
                response
        );
    }
}
