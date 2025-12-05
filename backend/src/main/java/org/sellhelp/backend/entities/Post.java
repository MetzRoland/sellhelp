package org.sellhelp.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "reward", nullable = false)
    private Integer reward;

    @Column(name = "created_at", nullable = false)
    @CurrentTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User postPublisher;

    @ManyToOne
    @JoinColumn(name = "selected_user_id")
    private User selectedUser;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private PostStatus postStatus;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<PostFile> postFiles;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> postComments;

    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Application> applications;
}
