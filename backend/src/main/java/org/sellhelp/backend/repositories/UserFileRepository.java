package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {
    List<UserFile> findByUser(User user);
    Optional<UserFile> findByUserAndFilePath(User user, String filePath);
}
