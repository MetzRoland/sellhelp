package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDTO {
    @NotBlank(message = "A keresztnév nem lehet üres!")
    @Size(max = 50, message = "A keresztnév maximum 50 karakter lehet!")
    private String firstName;

    @NotBlank(message = "A vezetéknév nem lehet üres!")
    @Size(max = 50, message = "A vezetéknév maximum 50 karakter lehet!")
    private String lastName;

    @NotBlank(message = "Az email nem lehet üres!")
    @Size(max = 50, message = "Az email maximum 50 karakter lehet!")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Nem megfelelő email formátum!"
    )
    private String email;

    @NotBlank(message = "A jelszó nem lehet üres!")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "A jelszónak legalább 8 karakterből kell állnia, tartalmaznia kell kis- és nagybetűt és számot!"
    )
    private String password;

    @NotNull(message = "A születési dátum nem lehet üres!")
    @Past(message = "A születési dátum nem lehet a jövőben!")
    private LocalDate birthDate;

    @NotBlank(message = "A városnév nem lehet üres!")
    @Size(min = 1, max = 100, message = "A városnév 1 és 100 karakter közötti!")
    private String cityName;
}
