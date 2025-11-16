package org.sellhelp.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String first_name;

    @Column(name = "last_name", nullable = false)
    private String last_name;

    @Column(name = "birth_date", nullable = false)
    private Date birth_date;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "city_id", nullable = false)
    private int city_id;

    @Column(name = "google_id")
    private String google_id;

    @Column(name = "role_id", nullable = false)
    private byte role_id;

    @Column(name = "is_banned")
    private boolean is_banned;

    @Column(name = "created_at")
    private LocalDateTime created_at;
}

