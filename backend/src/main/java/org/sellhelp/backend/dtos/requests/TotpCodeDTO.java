package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotpCodeDTO {
    private String tempToken;

    @NotBlank(message = "A hitelesítő kód nem lehet üres!", groups = NotBlankGroup.class)
    private String totpCode;
}
