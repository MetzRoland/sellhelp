package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Optional<Chat> findByHostIdAndGuestId(Integer hostId, Integer guestId);
}
