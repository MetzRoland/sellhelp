package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotFoundErrorDTO {
    private int status;
    private LocalDateTime timestamp;
    private String message;
    private String path;
}
