package org.sellhelp.backend.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int user_id;

    @Column(name = "rating", nullable = false)
    private byte rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime created_at;
}
