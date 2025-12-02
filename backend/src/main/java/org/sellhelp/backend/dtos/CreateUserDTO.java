package org.sellhelp.backend.dtos;

import lombok.Data;
import org.sellhelp.backend.entities.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class CreateUserDTO {
    private String username;
    private String first_name;
    private String last_name;
    private LocalDate birth_date;
    private String email;

    private City city;
    private Role role;

    private List<Notification> notificationList;
    private List<UserFile> userFiles;
    private List<Review> reviews;

    private UserSecret userSecret;

    private String password;
}