package org.sellhelp.backend.entities;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "sender_user_id")
    @JsonIgnore
    private User senderUser;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reviewed_user_id")
    @JsonIgnore
    private User reviewedUser;

    @Column(name = "rating", nullable = false)
    private byte rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at", nullable = false)
    @CurrentTimestamp
    private LocalDateTime createdAt;
}
