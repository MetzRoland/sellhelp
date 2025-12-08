package org.sellhelp.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    @JsonIgnore
    private ChatMessage chatMessage;

    @Column(name = "file_path", nullable = false)
    private String filePath;
}
