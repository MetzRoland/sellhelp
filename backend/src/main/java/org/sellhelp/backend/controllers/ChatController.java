package org.sellhelp.backend.controllers;

import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.ChatMessageResponse;
import org.sellhelp.backend.dtos.responses.ChatResponse;
import org.sellhelp.backend.entities.Chat;
import org.sellhelp.backend.entities.ChatMessage;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUser currentUser;
    private final ModelMapper modelMapper;

    @Autowired
    public ChatController(ChatService chatService, CurrentUser currentUser, ModelMapper modelMapper){
        this.chatService = chatService;
        this.currentUser = currentUser;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/get-or-create")
    public ChatResponse getOrCreateChat(
            @RequestParam Integer otherUserId
    ) {
        Chat chat = chatService.getOrCreateChat(currentUser.getCurrentlyLoggedUserEntity().getId(), otherUserId);

        List<ChatMessageResponse> chatMessages =
                Optional.ofNullable(chat.getChatMessages())
                        .orElse(List.of())
                        .stream()
                        .sorted(Comparator.comparing(ChatMessage::getSentAt))
                        .map(message -> modelMapper.map(message, ChatMessageResponse.class))
                        .toList();

        return ChatResponse.builder()
                .id(chat.getId())
                .hostId(chat.getHost().getId())
                .guestId(chat.getGuest().getId())
                .chatMessages(chatMessages)
                .build();
    }

    @PostMapping("/{chatId}/message-with-files")
    public ChatMessageResponse sendWithFiles(
            @PathVariable Integer chatId,
            @RequestParam Integer senderId,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) List<MultipartFile> files
    ) throws Exception {

        User sender = currentUser.getCurrentlyLoggedUserEntity();
        sender.setId(senderId);

        return chatService.sendMessage(chatId, sender.getId(), message, files);
    }

    @GetMapping("/{chatId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Integer chatId) {
        return chatService.getMessages(chatId, currentUser.getCurrentlyLoggedUserEntity().getId());
    }

    @GetMapping("/chats")
    public List<ChatResponse> getAllChats(){
        return chatService.getAllChats(currentUser.getCurrentlyLoggedUserEntity().getId());
    }
}
