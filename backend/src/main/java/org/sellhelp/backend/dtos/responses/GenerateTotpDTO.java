package org.sellhelp.backend.dtos.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTotpDTO {
    private String totpSecret;
    private String qrCode;

    private String tempToken;
}
