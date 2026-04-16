package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.PastGroup;
import org.sellhelp.backend.dtos.validationGroups.SizeGroup;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsUpdateDTO {

    @Size(max = 50, message = "A keresztnév maximum 50 karakter lehet!", groups = SizeGroup.class)
    private String firstName;

    @Size(max = 50, message = "A vezetéknév maximum 50 karakter lehet!", groups = SizeGroup.class)
    private String lastName;

    @Size(min = 1, max = 100, message = "A városnév 1 és 100 karakter közötti!", groups = SizeGroup.class)
    private String cityName;

    @Past(message = "A születési dátum nem lehet a jövőben!", groups = PastGroup.class)
    private LocalDate birthDate;
}