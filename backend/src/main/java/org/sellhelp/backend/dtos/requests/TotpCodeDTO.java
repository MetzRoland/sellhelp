package org.sellhelp.backend.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotpCodeDTO {
    private String tempToken;
    private String totpCode;
}
