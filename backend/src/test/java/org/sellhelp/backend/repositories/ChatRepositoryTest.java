package org.sellhelp.backend.repositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Chat;
import org.sellhelp.backend.entities.ChatMessage;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    private User testhost;
    private User testGuest;
    private Chat testChat;

    @BeforeEach
    void init() {
        testhost = userRepository.save(User.builder()
                .firstName("Hanz")
                .lastName("Fisher")
                .birthDate(LocalDate.of(1983, 2, 23))
                .email("h.fisher@gmail.com")
                .authProvider(AuthProvider.LOCAL)
                .build());

        testGuest = userRepository.save(User.builder()
                .firstName("Roland")
                .lastName("Metz")
                .birthDate(LocalDate.of(2003, 5, 12))
                .email("a@gmail.com")
                .authProvider(AuthProvider.LOCAL)
                .build());

        testChat = Chat.builder()
                .host(testhost)
                .guest(testGuest)
                .build();
    }

    @Test
    void chatCanBeAddedToRepositoryAndDB()
    {
        Chat savedChat = chatRepository.save(testChat);

        assertNotNull(savedChat.getId());
        assertEquals(testChat.getHost(), savedChat.getHost());
        assertEquals(testChat.getGuest(), savedChat.getGuest());
    }

    @Test
    void chatCanBeUpdatedToRepositoryAndDB()
    {
        Chat savedChat = chatRepository.save(testChat);
        Chat test = savedChat.toBuilder().build();

        ArrayList<ChatMessage> chatMessages =
                new ArrayList<ChatMessage>(List.of(
                ChatMessage.builder()
                        .chat(savedChat)
                        .messageSender(testhost)
                        .message("Halo")
                        .build()
        ));
        savedChat.setChatMessages(chatMessages);
        Chat updatedChat = chatRepository.save(savedChat);

        assertNotEquals(test, updatedChat);

        test.setChatMessages(chatMessages);
        assertEquals(test, updatedChat);
    }

    @Test
    void chatCanBeDeletedFromRepositoryAndDB()
    {
        Chat savedChat = chatRepository.save(testChat);
        Integer chatId = savedChat.getId();

        chatRepository.delete(savedChat);

        assertFalse(chatRepository.findById(chatId).isPresent());
    }
}
