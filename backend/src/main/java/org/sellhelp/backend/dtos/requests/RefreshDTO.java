package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshDTO {
    @NotBlank(message = "A refresh token nem lehet üres!", groups = NotBlankGroup.class)
    private String refreshToken;
}
