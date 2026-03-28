package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class NotificationRepositoryTest {
    @Autowired
    private NotificationRepository notificationRepository;

    private Notification testNotification;

    @BeforeEach
    public void init(){
        testNotification = Notification.builder()
                .title("Notification 1")
                .message("Notification 1 message")
                .build();
    }

    @Test
    @DisplayName("Notification can be added to repository and DB")
    public void notificationFileCanBeAddedToNotificationFileRepositoryAndDB(){
        Notification savedNotification = notificationRepository.save(testNotification);

        assertNotNull(savedNotification.getId());
        assertEquals("Notification 1", savedNotification.getTitle());
        assertEquals("Notification 1 message", savedNotification.getMessage());
    }

    @Test
    @DisplayName("Notification can be updated in repository and DB")
    public void notificationFileCanBeUpdatedTonotificationFileRepositoryAndDB(){
        notificationRepository.save(testNotification);

        testNotification.setTitle("Title updated");
        testNotification.setMessage("Message updated");

        Notification updatedNotification = notificationRepository.save(testNotification);

        assertNotNull(updatedNotification.getId());
        assertEquals("Title updated", updatedNotification.getTitle());
        assertEquals("Message updated", updatedNotification.getMessage());
    }

    @Test
    @DisplayName("Notification can be deleted from repository and DB")
    public void notificationFileCanBeDeletedFromNotificationFileRepositoryAndDB(){
        Notification savedNotification = notificationRepository.save(testNotification);
        Integer savedNotificationId = savedNotification.getId();

        notificationRepository.delete(savedNotification);

        assertFalse(notificationRepository.findById(savedNotificationId).isPresent());
    }

    @Test
    @DisplayName("General CRUD functionality test for Notification repository")
    public void userGeneralCRUDFunctionalityTest(){
        Notification savedNotification = notificationRepository.save(testNotification);
        Integer savedNotificationId = savedNotification.getId();

        assertNotNull(savedNotificationId);
        assertEquals("Notification 1", savedNotification.getTitle());
        assertEquals("Notification 1 message", savedNotification.getMessage());

        savedNotification.setTitle("Title updated");
        savedNotification.setMessage("Message updated");

        Notification updateNotification = notificationRepository.save(savedNotification);

        assertEquals("Title updated", updateNotification.getTitle());
        assertEquals("Message updated", updateNotification.getMessage());

        notificationRepository.delete(updateNotification);

        assertFalse(notificationRepository.findById(updateNotification.getId()).isPresent());
    }
}