package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRegisterDTO {
    @NotBlank(message = "A város nem lehet üres!")
    private String cityName;

    @NotNull(message = "A születési dátum nem lehet üres!")
    private LocalDate birthDate;

    private String tempToken;
}
