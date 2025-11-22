package org.sellhelp.backend.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CurrentTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne()
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne()
    @JoinColumn(name = "user")
    private User user;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "sent_at")
    @CurrentTimestamp
    private LocalDateTime sentAt;
}
