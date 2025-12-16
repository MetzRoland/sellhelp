package org.sellhelp.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailUpdateDTO {
    @NotBlank(message = "A jelszó nem lehet üres!")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "A jelszónak legalább 8 karakterből kell állnia, tartalmaznia kell kis- és nagybetűt és számot!"
    )
    private String password;
}
