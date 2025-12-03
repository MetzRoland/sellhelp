package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void init(){
        testUser = User.builder()
                .firstName("Roland")
                .lastName("Metz")
                .username("metzroland")
                .birthDate(LocalDate.of(2003, 5, 12))
                .email("a@gmail.com")
                .userSecret(
                        UserSecret.builder()
                                .password("123")
                                .lastUsedPassword(null)
                                .passUpdateToken(null)
                                .build()
                )
                .userNotifications(
                        List.of(
                                Notification.builder()
                                        .title("Notification 1")
                                        .message("This is notification 1")
                                        .build()
                        )
                )
                .reviews(
                        List.of(
                                Review.builder()
                                        .rating((byte) 5)
                                        .comment("Good job")
                                        .build()
                        )
                )
                .userFiles(
                        List.of(
                                UserFile.builder()
                                        .filePath("resume.docx")
                                        .build(),
                                UserFile.builder()
                                        .filePath("resume2.docx")
                                        .build()
                        )
                )
                .build();


        testUser.getReviews().getFirst().setSenderUser(testUser);
        testUser.getReviews().getFirst().setReviewedUser(testUser);

        //testUser.getUserSecret().setUser(testUser);
    }

    @Test
    public void userCanBeAddedToUserRepositoryAndDB(){
        userRepository.save(testUser);

        Integer testUserId = userRepository.findAll().getFirst().getId();

        assertNotNull(null, testUserId);

        assertEquals(null,1, userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getId());

        assertEquals(null, "123", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getUserSecret().getPassword());

        assertEquals(null, "Roland", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getFirstName());

        assertEquals(null, "Metz", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getLastName());

        assertEquals(null, "metzroland", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getUsername());

        assertEquals(null, "a@gmail.com", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getEmail());

        assertEquals(null, LocalDate.of(2003, 5, 12), userRepository.findById(1).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getBirthDate());

        assertEquals(null, "Notification 1", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getUserNotifications().getFirst().getTitle());

        assertEquals(null, "resume.docx", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getUserFiles().getFirst().getFilePath());

        assertEquals(null, 2, userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getUserFiles().size());

        assertEquals(null, "Good job", userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getReviews().getFirst().getComment());

        assertEquals(null, (byte) 5, userRepository.findById(testUserId).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getReviews().getFirst().getRating());
    }
}
