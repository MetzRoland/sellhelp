package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.ArrayList;
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
                        new ArrayList<>(List.of(
                                Notification.builder()
                                        .title("Notification 1")
                                        .message("This is notification 1")
                                        .build()
                        ))
                )
                .reviews(
                        new ArrayList<>(List.of(
                                Review.builder()
                                        .rating((byte) 5)
                                        .comment("Good job")
                                        .build()
                        ))
                )
                .userFiles(
                        new ArrayList<>(List.of(
                                UserFile.builder()
                                        .filePath("resume.docx")
                                        .build(),
                                UserFile.builder()
                                        .filePath("resume2.docx")
                                        .build()
                        ))
                )
                .build();
    }

    @Test
    public void userCanBeAddedToUserRepositoryAndDB(){
        User savedUser = userRepository.save(testUser);

        assertNotNull(null, savedUser.getId());

        assertEquals(null, "123", savedUser.getUserSecret().getPassword());

        assertEquals(null, "Roland", savedUser.getFirstName());

        assertEquals(null, "Metz", savedUser.getLastName());

        assertEquals(null, "metzroland", savedUser.getUsername());

        assertEquals(null, "a@gmail.com", savedUser.getEmail());

        assertEquals(null, LocalDate.of(2003, 5, 12), savedUser.getBirthDate());

        assertEquals(null, "Notification 1", savedUser.getUserNotifications().get(0).getTitle());

        assertEquals(null, "resume.docx", savedUser.getUserFiles().get(0).getFilePath());

        assertEquals(null, 2, savedUser.getUserFiles().size());

        assertEquals(null, "Good job", savedUser.getReviews().get(0).getComment());

        assertEquals(null, (byte) 5, savedUser.getReviews().get(0).getRating());
    }

    @Test
    public void userCanBeUpdatedToUserRepositoryAndDB(){
        userRepository.save(testUser);

        testUser.setFirstName("Karoly");

        User updatedUser = userRepository.save(testUser);

        assertNotNull(null, updatedUser.getId());

        assertEquals(null, "123", updatedUser.getUserSecret().getPassword());

        assertEquals(null, "Karoly", updatedUser.getFirstName());

        assertEquals(null, "Metz", updatedUser.getLastName());

        assertEquals(null, "metzroland", updatedUser.getUsername());

        assertEquals(null, "a@gmail.com", updatedUser.getEmail());

        assertEquals(null, LocalDate.of(2003, 5, 12), updatedUser.getBirthDate());

        assertEquals(null, "Notification 1", updatedUser.getUserNotifications().get(0).getTitle());

        assertEquals(null, "resume.docx", updatedUser.getUserFiles().get(0).getFilePath());

        assertEquals(null, 2, updatedUser.getUserFiles().size());

        assertEquals(null, "Good job", updatedUser.getReviews().get(0).getComment());

        assertEquals(null, (byte) 5, updatedUser.getReviews().get(0).getRating());
    }
}
