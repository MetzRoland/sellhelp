package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuperUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private boolean isMfa;
    private boolean banned;
    private Instant createdAt;
    private String role;
}
