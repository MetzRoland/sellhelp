package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.MinMaxGroup;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;
import org.sellhelp.backend.dtos.validationGroups.SizeGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDTO {
    @NotBlank(message = "A poszt címe nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 100, message = "A poszt címe nem lehet 100 karakternél hosszabb!", groups = SizeGroup.class)
    private String title;

    @NotBlank(message = "A poszt leírása nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 2000, message = "A poszt leírása nem lehet 2000 karakternél hosszabb!", groups = SizeGroup.class)
    private String description;

    @Min(value = 0, message = "A fizetés nem lehet 0 forint alatt!", groups = MinMaxGroup.class)
    @Max(value = 10000000, message = "A fizetés nem lehet 10 000 000 forint felett!", groups = MinMaxGroup.class)
    private Integer reward;

    @NotBlank(message = "A város neve nem lehet üres!", groups = NotBlankGroup.class)
    private String cityName;
}
