package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {
}
