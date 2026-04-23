package org.sellhelp.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_secrets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecret {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "last_used_pass")
    private String lastUsedPassword;

    @Column(name = "is_mfa", nullable = false)
    private boolean isMfa;

    @Column(name = "totp_secret", length = 60)
    private String totpSecret;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
