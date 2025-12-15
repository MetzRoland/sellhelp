package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.entities.Review;
import org.sellhelp.backend.entities.UserFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String cityName;
    private boolean isMfa;
    private String totpSecret;
    private Instant createdAt;
    private List<Review> reviews;
    private List<Review> sentReviews;
    private List<UserFile> userFiles;
}
