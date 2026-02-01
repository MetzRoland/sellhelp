package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnedPostResponseDTO implements PostResponseInterface {
    private Integer id;
    private String title;
    private String description;
    private String cityName;
    private Integer reward;
    private String statusName;
    private Instant createdAt;

    private UserDTO publisher;
    public List<JobApplicationResponseDTO> jobApplications;
    private List<PostCommentResponseDTO> comments;
    public UserDTO selectedUser;
}
