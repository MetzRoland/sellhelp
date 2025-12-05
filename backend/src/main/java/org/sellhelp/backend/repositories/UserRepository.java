package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository - Remove to test GH actions
public interface UserRepository extends JpaRepository<User, Integer> {
}
