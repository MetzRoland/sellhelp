package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponseDTO {
    private Integer id;
    private UserDTO applicant;
    private Integer postId;
    private Instant appliedAt;
}
