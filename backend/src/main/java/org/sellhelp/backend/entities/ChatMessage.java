package org.sellhelp.backend.entities;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CurrentTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonIgnore
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User messageSender;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "sent_at")
    @CurrentTimestamp
    private Instant sentAt;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    private List<ChatFile> chatFiles;
}
