package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostFileRepository extends JpaRepository<PostFile, Integer> {
    List<PostFile> findAllByPost(Post post);
    List<PostFile> findAllByPostId(Integer postId);
    Optional<PostFile> findByPostFilePath(String postFilePath);
    Integer countByPost(Post post);
}
