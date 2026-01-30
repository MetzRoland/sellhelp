package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentResponseDTO {
    private Integer id;
    private String message;

    private UserDTO publisher;
    private Instant createdAt;
}
