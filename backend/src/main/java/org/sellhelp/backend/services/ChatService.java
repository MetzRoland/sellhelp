package org.sellhelp.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.ChatMessageResponse;
import org.sellhelp.backend.dtos.responses.ChatResponse;
import org.sellhelp.backend.entities.Chat;
import org.sellhelp.backend.entities.ChatFile;
import org.sellhelp.backend.entities.ChatMessage;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.ChatMessageRepository;
import org.sellhelp.backend.repositories.ChatRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ChatService(ChatRepository chatRepository, ChatMessageRepository messageRepository, S3Service s3Service,
                       UserRepository userRepository, ModelMapper modelMapper){
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.s3Service = s3Service;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public Chat getOrCreateChat(Integer user1Id, Integer user2Id) {

        if (user1Id.equals(user2Id)) {
            throw new RuntimeException("Cannot create chat with yourself");
        }

        int userA = Math.min(user1Id, user2Id);
        int userB = Math.max(user1Id, user2Id);

        Chat chat = chatRepository.findByHostIdAndGuestId(userA, userB)
                .orElseGet(() -> {

                    User host = userRepository.findById(userA)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    User guest = userRepository.findById(userB)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    Chat newChat = Chat.builder()
                            .host(host)
                            .guest(guest)
                            .build();

                    return chatRepository.save(newChat);
                });

        return chat;
    }

    public ChatMessageResponse sendMessage(
            Integer chatId,
            Integer senderId,
            String text,
            List<MultipartFile> files
    ) throws Exception {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        validateUserInChat(chat, senderId);

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .messageSender(sender)
                .message(text == null ? "" : text)
                .build();

        message = messageRepository.save(message);

        if (files != null && !files.isEmpty()) {

            ChatMessage finalMessage = message;
            List<ChatFile> chatFiles = files.stream().map(file -> {
                try {
                    String key = "chat/" + chatId + "/" + finalMessage.getId() + "/" + file.getOriginalFilename();
                    log.info(key);

                    s3Service.uploadFileWithKey(key, file);

                    return ChatFile.builder()
                            .chatMessage(finalMessage)
                            .filePath(key)
                            .build();

                } catch (Exception e) {
                    throw new RuntimeException("File upload failed", e);
                }
            }).toList();

            message.setChatFiles(chatFiles);
            message = messageRepository.save(message);
        }

        return mapToDTO(message);
    }

    public List<ChatMessageResponse> getMessages(Integer chatId, Integer requesterId) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        validateUserInChat(chat, requesterId);

        return messageRepository.findByChatIdOrderBySentAtAsc(chatId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private void validateUserInChat(Chat chat, Integer userId) {
        if (!chat.getHost().getId().equals(userId) &&
                !chat.getGuest().getId().equals(userId)) {
            throw new RuntimeException("User not part of this chat");
        }
    }

    private ChatMessageResponse mapToDTO(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .chatId(msg.getChat().getId())
                .senderId(msg.getMessageSender().getId())
                .message(msg.getMessage())
                .sentAt(msg.getSentAt())
                .files(
                        msg.getChatFiles() == null ? List.of() :
                                msg.getChatFiles().stream()
                                        .map(f -> s3Service.createFileDTO(f.getId(), f.getFilePath()))
                                        .toList()
                )
                .build();
    }

    public List<ChatResponse> getAllChats(Integer currentUserId) {
        return chatRepository.findAll()
                .stream()
                .filter(chat -> Objects.equals(chat.getHost().getId(), currentUserId) || Objects.equals(chat.getGuest().getId(), currentUserId))
                .map(chat -> modelMapper.map(chat, ChatResponse.class))
                .sorted(Comparator.comparing(
                        chatResponse -> chatResponse.getChatMessages().stream()
                                .map(ChatMessageResponse::getSentAt)
                                .max(Comparator.naturalOrder())
                                .orElse(null),
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }
}
