package org.sellhelp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {
    private Integer id;
    private Integer hostId;
    private Integer guestId;
    private List<ChatMessageResponse> chatMessages;
}
