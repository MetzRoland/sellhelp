package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;
import org.sellhelp.backend.dtos.validationGroups.PatternGroup;
import org.sellhelp.backend.dtos.validationGroups.SizeGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailUpdateDTO {
    @NotBlank(message = "Az email nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 50, message = "Az email maximum 50 karakter lehet!", groups = SizeGroup.class)
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Nem megfelelő email formátum!",
            groups = PatternGroup.class
    )
    private String email;
}
