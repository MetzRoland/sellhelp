package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsUpdateDTO {

    @Size(max = 50, message = "A keresztnév maximum 50 karakter lehet!")
    private String firstName;

    @Size(max = 50, message = "A vezetéknév maximum 50 karakter lehet!")
    private String lastName;

    @Size(min = 1, max = 100, message = "A városnév 1 és 100 karakter közötti!")
    private String cityName;

    @Past
    private LocalDate birthDate;
}