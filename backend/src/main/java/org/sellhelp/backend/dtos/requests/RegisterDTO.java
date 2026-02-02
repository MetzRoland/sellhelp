package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;
import org.sellhelp.backend.dtos.validationGroups.PastGroup;
import org.sellhelp.backend.dtos.validationGroups.PatternGroup;
import org.sellhelp.backend.dtos.validationGroups.SizeGroup;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDTO {
    @NotBlank(message = "A keresztnév nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 50, message = "A keresztnév maximum 50 karakter lehet!", groups = SizeGroup.class)
    private String firstName;

    @NotBlank(message = "A vezetéknév nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 50, message = "A vezetéknév maximum 50 karakter lehet!", groups = SizeGroup.class)
    private String lastName;

    @NotBlank(message = "Az email nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 50, message = "Az email maximum 50 karakter lehet!", groups = SizeGroup.class)
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Nem megfelelő email formátum!",
            groups = PatternGroup.class
    )
    private String email;

    @NotBlank(message = "A jelszó nem lehet üres!", groups = NotBlankGroup.class)
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "A jelszónak legalább 8 karakterből kell állnia, tartalmaznia kell kis- és nagybetűt és számot!",
            groups = PatternGroup.class
    )
    private String password;

    @NotNull(message = "A születési dátum nem lehet üres!", groups = NotBlankGroup.class)
    @Past(message = "A születési dátum nem lehet a jövőben!", groups = PastGroup.class)
    private LocalDate birthDate;

    @NotBlank(message = "A városnév nem lehet üres!", groups = NotBlankGroup.class)
    @Size(min = 1, max = 100, message = "A városnév 1 és 100 karakter közötti!", groups = SizeGroup.class)
    private String cityName;
}
