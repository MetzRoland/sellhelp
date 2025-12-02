package org.sellhelp.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "post_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "status_name", nullable = false, unique = true)
    private String statusName;

    @JsonIgnore
    @OneToMany(mappedBy = "postStatus", cascade = CascadeType.ALL)
    private List<Post> posts;
}
