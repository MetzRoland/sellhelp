package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirstTotpValidationDTO {
    private String totpSecret;
    private String qrCode;

    private String tempToken;

    @NotBlank(message = "A hitelesítő kód nem lehet üres!")
    private String totpCode;
}
