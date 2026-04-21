package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {
    private Integer id;
    private Integer chatId;
    private Integer senderId;
    private String message;
    private Instant sentAt;
    private List<FileDTO> files;
}
