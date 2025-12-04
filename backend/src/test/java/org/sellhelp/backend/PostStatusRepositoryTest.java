package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.PostStatus;
import org.sellhelp.backend.repositories.PostStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PostStatusRepositoryTest {

    @Autowired
    private PostStatusRepository postStatusRepository;

    private PostStatus testStatus;

    @BeforeEach
    public void init() {
        testStatus = PostStatus.builder()
                .statusName("OPEN")
                .build();
    }

    @Test
    public void postStatusCanBeAddedToRepositoryAndDB() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);

        assertNotNull(savedStatus.getId());
        assertEquals("OPEN", savedStatus.getStatusName());
    }

    @Test
    public void postStatusCanBeUpdatedInRepositoryAndDB() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);

        savedStatus.setStatusName("CLOSED");
        PostStatus updatedStatus = postStatusRepository.save(savedStatus);

        assertNotNull(updatedStatus.getId());
        assertEquals("CLOSED", updatedStatus.getStatusName());
    }

    @Test
    public void postStatusCanBeDeletedFromRepositoryAndDB() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);
        Integer statusId = savedStatus.getId();

        postStatusRepository.delete(savedStatus);

        assertFalse(postStatusRepository.findById(statusId).isPresent());
    }

    @Test
    public void postStatusGeneralCRUDFunctionalityTest() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);
        Integer statusId = savedStatus.getId();

        assertNotNull(statusId);
        assertEquals("OPEN", postStatusRepository.findById(statusId).get().getStatusName());

        savedStatus.setStatusName("IN_PROGRESS");
        PostStatus updatedStatus = postStatusRepository.save(savedStatus);

        assertEquals("IN_PROGRESS", updatedStatus.getStatusName());

        postStatusRepository.delete(updatedStatus);
        assertFalse(postStatusRepository.findById(updatedStatus.getId()).isPresent());
    }
}
