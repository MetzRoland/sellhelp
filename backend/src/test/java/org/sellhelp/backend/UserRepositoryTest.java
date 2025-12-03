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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void init(){
        UserSecret userSecret = UserSecret.builder()
                .password("123")
                .lastUsedPassword(null)
                .passUpdateToken(null)
                .build();

        UserFile userFile1 = UserFile.builder()
                .filePath("resume.docx")
                .build();

        UserFile userFile2 = UserFile.builder()
                .filePath("resume2.docx")
                .build();

        List<UserFile> userFiles = new ArrayList<>(List.of(userFile1, userFile2));

        Notification notification1 = Notification.builder()
                .title("Notification 1")
                .message("This is notification 1")
                .build();

        List<Notification> userNotifications = new ArrayList<>(List.of(notification1));

        Review review1 = Review.builder()
                .rating((byte) 5)
                .comment("Good job")
                .build();

        List<Review> reviews = new ArrayList<>(List.of(review1));

        testUser = User.builder()
                .firstName("Roland")
                .lastName("Metz")
                .username("metzroland")
                .birthDate(LocalDate.of(2003, 5, 12))
                .email("a@gmail.com")
                .userSecret(userSecret)
                .userNotifications(userNotifications)
                .reviews(reviews)
                .userFiles(userFiles)
                .build();

    }

    @Test
    public void userCanBeAddedToUserRepositoryAndDB(){
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());

        assertEquals("123", savedUser.getUserSecret().getPassword());
        assertEquals("Roland", savedUser.getFirstName());
        assertEquals("Metz", savedUser.getLastName());
        assertEquals("metzroland", savedUser.getUsername());
        assertEquals("a@gmail.com", savedUser.getEmail());
        assertEquals(LocalDate.of(2003, 5, 12), savedUser.getBirthDate());
        assertEquals("Notification 1", savedUser.getUserNotifications().get(0).getTitle());
        assertEquals("resume.docx", savedUser.getUserFiles().get(0).getFilePath());
        assertEquals(2, savedUser.getUserFiles().size());
        assertEquals("Good job", savedUser.getReviews().get(0).getComment());
        assertEquals((byte) 5, savedUser.getReviews().get(0).getRating());
    }

    @Test
    public void userCanBeUpdatedToUserRepositoryAndDB(){
        userRepository.save(testUser);

        testUser.setFirstName("Karoly");

        User updatedUser = userRepository.save(testUser);

        assertNotNull(updatedUser.getId());

        assertEquals("123", updatedUser.getUserSecret().getPassword());
        assertEquals("Karoly", updatedUser.getFirstName());
        assertEquals("Metz", updatedUser.getLastName());
        assertEquals("metzroland", updatedUser.getUsername());
        assertEquals("a@gmail.com", updatedUser.getEmail());
        assertEquals(LocalDate.of(2003, 5, 12), updatedUser.getBirthDate());
        assertEquals("Notification 1", updatedUser.getUserNotifications().get(0).getTitle());
        assertEquals("resume.docx", updatedUser.getUserFiles().get(0).getFilePath());
        assertEquals(2, updatedUser.getUserFiles().size());
        assertEquals("Good job", updatedUser.getReviews().get(0).getComment());
        assertEquals((byte) 5, updatedUser.getReviews().get(0).getRating());
    }

    @Test
    public void userCanBeDeletedFromUserRepositoryAndDB(){
        User savedUser = userRepository.save(testUser);
        Integer savedUserId = savedUser.getId();

        userRepository.delete(savedUser);

        assertFalse(userRepository.findById(savedUserId).isPresent());
    }

    @Test
    public void userGeneralCRUDFunctionalityTest(){
        User savedUser = userRepository.save(testUser);

        Integer savedUserId = savedUser.getId();

        assertNotNull(savedUserId);

        assertEquals("123", savedUser.getUserSecret().getPassword());
        assertEquals("Roland", savedUser.getFirstName());
        assertEquals("Metz", savedUser.getLastName());
        assertEquals("metzroland", savedUser.getUsername());
        assertEquals("a@gmail.com", savedUser.getEmail());
        assertEquals(LocalDate.of(2003, 5, 12), savedUser.getBirthDate());
        assertEquals("Notification 1", savedUser.getUserNotifications().get(0).getTitle());
        assertEquals("resume.docx", savedUser.getUserFiles().get(0).getFilePath());
        assertEquals(2, savedUser.getUserFiles().size());
        assertEquals("Good job", savedUser.getReviews().get(0).getComment());
        assertEquals((byte) 5, savedUser.getReviews().get(0).getRating());

        savedUser.setFirstName("Márk");
        savedUser.getUserNotifications().get(0).setTitle("Notification 1 updated");

        User updatedUser = userRepository.save(savedUser);

        assertEquals("Márk", updatedUser.getFirstName());
        assertEquals("Notification 1 updated", updatedUser.getUserNotifications().get(0).getTitle());

        userRepository.delete(updatedUser);

        assertFalse(userRepository.findById(updatedUser.getId()).isPresent());
    }
}
