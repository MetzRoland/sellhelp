package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostStatusRepository extends JpaRepository<PostStatus, Integer> {
}
