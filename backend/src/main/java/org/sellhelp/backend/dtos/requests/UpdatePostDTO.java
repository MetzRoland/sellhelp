package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.MinMaxGroup;
import org.sellhelp.backend.dtos.validationGroups.SizeGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostDTO {
    @Size(max = 100, message = "A poszt címe nem lehet 100 karakternél hosszabb!", groups = SizeGroup.class)
    private String title;

    @Size(max = 2000, message = "A poszt leírása nem lehet 2000 karakternél hosszabb!", groups = SizeGroup.class)
    private String description;

    @Min(value = 0, message = "A fizetés nem lehet 0 forint alatt!", groups = MinMaxGroup.class)
    @Max(value = 10000000, message = "A fizetés nem lehet 10 000 000 forint felett!", groups = MinMaxGroup.class)
    private Integer reward;

    private String cityName;
}
