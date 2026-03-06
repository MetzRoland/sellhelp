package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Chat;
import org.sellhelp.backend.entities.ChatMessage;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    private User sender;
    private Chat chat;
    private ChatMessage testChatMessage;

    @BeforeEach
    public void init() {
        sender = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .authProvider(AuthProvider.LOCAL)
                .build();

        userRepository.save(sender);

        chat = Chat.builder()
                .host(sender)
                .guest(sender)
                .chatMessages(new ArrayList<>())
                .build();
        chatRepository.save(chat);

        testChatMessage = ChatMessage.builder()
                .chat(chat)
                .messageSender(sender)
                .message("Hello World!")
                .build();
    }

    @Test
    @DisplayName("ChatMessage can be added to repository and DB")
    public void chatMessageCanBeAddedToRepositoryAndDB() {
        ChatMessage saved = chatMessageRepository.save(testChatMessage);

        assertNotNull(saved.getId());
        assertEquals("Hello World!", saved.getMessage());
        assertEquals(sender.getId(), saved.getMessageSender().getId());
        assertEquals(chat.getId(), saved.getChat().getId());
    }

    @Test
    @DisplayName("ChatMessage can be updated in repository and DB")
    public void chatMessageCanBeUpdatedInRepositoryAndDB() {
        ChatMessage saved = chatMessageRepository.save(testChatMessage);

        saved.setMessage("Updated message");
        ChatMessage updated = chatMessageRepository.save(saved);

        assertEquals("Updated message", updated.getMessage());
    }

    @Test
    @DisplayName("ChatMessage can be deleted from repository and DB")
    public void chatMessageCanBeDeletedFromRepositoryAndDB() {
        ChatMessage saved = chatMessageRepository.save(testChatMessage);
        Integer id = saved.getId();

        chatMessageRepository.delete(saved);

        assertFalse(chatMessageRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("ChatMessage full CRUD operations test")
    public void chatMessageCRUDTest() {
        ChatMessage saved = chatMessageRepository.save(testChatMessage);
        Integer id = saved.getId();
        assertNotNull(id);

        ChatMessage read = chatMessageRepository.findById(id).orElse(null);
        assertNotNull(read);
        assertEquals("Hello World!", read.getMessage());

        read.setMessage("New message");
        ChatMessage updated = chatMessageRepository.save(read);
        assertEquals("New message", updated.getMessage());

        chatMessageRepository.delete(updated);
        assertFalse(chatMessageRepository.findById(id).isPresent());
    }
}