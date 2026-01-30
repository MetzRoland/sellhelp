package org.sellhelp.backend.dtos.responses;

import java.time.Instant;
import java.util.List;

public interface PostResponseInterface {
    Integer getId();
    String getTitle();
    String getDescription();
    String getCityName();
    Integer getReward();
    String getStatusName();
    Instant getCreatedAt();
    UserDTO getPublisher();
    List<PostCommentResponseDTO> getComments();
}
