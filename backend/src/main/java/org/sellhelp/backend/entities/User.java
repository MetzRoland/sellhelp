package org.sellhelp.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CurrentTimestamp;
import org.sellhelp.backend.enums.AuthProvider;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "auth_provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "is_banned")
    private boolean banned;

    @Column(name = "created_at", nullable = false)
    @CurrentTimestamp
    private Instant createdAt;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST,  CascadeType.MERGE, CascadeType.REMOVE})
    private UserSecret userSecret;

    @OneToMany(mappedBy = "reviewedUser")
    private List<Review> reviews;

    @OneToMany(mappedBy = "senderUser")
    private List<Review> sentReviews;

    @OneToMany(mappedBy = "notifiedUser", cascade = CascadeType.REMOVE)
    private List<Notification> userNotifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserFile> userFiles;
}
