package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;
import org.sellhelp.backend.dtos.validationGroups.PastGroup;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRegisterDTO {
    @NotBlank(message = "A város nem lehet üres!", groups = NotBlankGroup.class)
    private String cityName;

    @NotNull(message = "A születési dátum nem lehet üres!", groups = NotBlankGroup.class)
    @Past(message = "A születési dátum nem lehet a jövőben!", groups = PastGroup.class)
    private LocalDate birthDate;

    private String tempToken;
}
