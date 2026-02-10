package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostFileRepository extends JpaRepository<PostFile, Integer> {
    List<PostFile> findAllByPost(Post post);
    List<PostFile> findAllByPostId(Integer postId);
    Integer countByPost(Post post);
}
