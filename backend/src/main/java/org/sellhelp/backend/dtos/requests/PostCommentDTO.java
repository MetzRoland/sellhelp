package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sellhelp.backend.dtos.validationGroups.NotBlankGroup;
import org.sellhelp.backend.dtos.validationGroups.SizeGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDTO {
    @NotBlank(message = "A komment szövege nem lehet üres!", groups = NotBlankGroup.class)
    @Size(max = 2000, message = "A komment nem lehet hosszabb 2000 karakternél!", groups = SizeGroup.class)
    private String message;
}
