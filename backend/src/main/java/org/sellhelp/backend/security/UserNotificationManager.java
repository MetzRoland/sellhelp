package org.sellhelp.backend.security;

import org.sellhelp.backend.entities.Notification;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.NotificationRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserNotificationManager {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public UserNotificationManager(UserRepository userRepository, NotificationRepository notificationRepository){
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User user, String title, String message){
        if(user == null){
            throw new UserNotFoundException("A felhasználó nem található!");
        }

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .notifiedUser(user)
                .sentAt(Instant.now())
                .build();

        user.getUserNotifications().add(notification);
        userRepository.save(user);
        notificationRepository.save(notification);
    }
}
