package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;
import org.sellhelp.backend.dtos.validationGroups.PatternGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDTO {
    @NotBlank(message = "A jelszó nem lehet üres!", groups = NotBlankGroup.class)
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "A jelszónak legalább 8 karakterből kell állnia, tartalmaznia kell kis- és nagybetűt és számot!",
            groups = PatternGroup.class
    )
    private String password;
    private String token;
}
