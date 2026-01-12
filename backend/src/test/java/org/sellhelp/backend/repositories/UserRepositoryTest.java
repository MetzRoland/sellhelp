package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.sellhelp.backend.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;
    private UserSecret testSecret;
    private UserFile testFile;
    private Notification testNotification;

    @BeforeEach
    public void init() {
        testSecret = UserSecret.builder()
                .password("123")
                .build();

        testFile = UserFile.builder()
                .filePath("file1.pdf")
                .build();

        testNotification = Notification.builder()
                .title("Notification Title")
                .message("This is a test notification")
                .build();

        List<UserFile> userFiles = new ArrayList<>();
        userFiles.add(testFile);

        userFileRepository.save(testFile);

        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        notificationRepository.save(testNotification);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.doe@test.com")
                .userSecret(testSecret)
                .userFiles(userFiles)
                .userNotifications(notifications)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    @Test
    public void userCanBeAddedToUserRepositoryAndDB() {
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getUserSecret());
        assertEquals("123", savedUser.getUserSecret().getPassword());
        assertEquals(1, savedUser.getUserFiles().size());
        assertEquals(1, savedUser.getUserNotifications().size());
    }

    @Test
    public void userCascadeUpdateTest() {
        User savedUser = userRepository.save(testUser);

        savedUser.getUserSecret().setPassword("newpass");

        savedUser.getUserFiles().clear();
        savedUser.getUserNotifications().clear();

        User updatedUser = userRepository.save(savedUser);

        assertEquals("newpass", updatedUser.getUserSecret().getPassword());
        assertEquals(0, updatedUser.getUserFiles().size(), "UserFiles should be removed via cascade REMOVE");
        assertEquals(0, updatedUser.getUserNotifications().size(), "Notifications should be removed via cascade REMOVE");
    }

    @Test
    public void userCanBeDeletedFromUserRepositoryAndDB() {
        User savedUser = userRepository.save(testUser);

        Integer savedUserId = savedUser.getId();
        Integer notificationId = savedUser.getUserNotifications().get(0).getId();

        userRepository.delete(savedUser);

        assertFalse(userRepository.findById(savedUserId).isPresent());
        assertTrue(userRepository.findById(savedUserId).isEmpty(), "UserFile should be cascade removed");
        assertFalse(notificationRepository.findById(notificationId).isPresent(), "Notification should be cascade removed");
    }

    @Test
    public void userGeneralCRUDFunctionalityTest() {
        User savedUser = userRepository.save(testUser);

        Integer savedUserId = savedUser.getId();

        assertNotNull(savedUserId);
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), savedUser.getBirthDate());
        assertEquals("john.doe@test.com", savedUser.getEmail());
        assertEquals(1, savedUser.getUserFiles().size());
        assertEquals(1, savedUser.getUserNotifications().size());
        assertNotNull(savedUser.getUserSecret());

        savedUser.setFirstName("Jane");
        savedUser.getUserSecret().setPassword("updatedpass");
        User updatedUser = userRepository.save(savedUser);

        assertEquals("Jane", updatedUser.getFirstName());
        assertEquals("updatedpass", updatedUser.getUserSecret().getPassword());

        userRepository.delete(updatedUser);
        assertFalse(userRepository.findById(savedUserId).isPresent());
    }
}
