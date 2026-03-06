package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.PostStatus;
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
    @DisplayName("Verify that a PostStatus can be added to repository and database")
    public void postStatusCanBeAddedToRepositoryAndDB() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);

        assertNotNull(savedStatus.getId());
        assertEquals("OPEN", savedStatus.getStatusName());
    }

    @Test
    @DisplayName("Verify that a PostStatus can be updated in repository and database")
    public void postStatusCanBeUpdatedInRepositoryAndDB() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);

        savedStatus.setStatusName("CLOSED");
        PostStatus updatedStatus = postStatusRepository.save(savedStatus);

        assertNotNull(updatedStatus.getId());
        assertEquals("CLOSED", updatedStatus.getStatusName());
    }

    @Test
    @DisplayName("Verify that a PostStatus can be deleted from repository and database")
    public void postStatusCanBeDeletedFromRepositoryAndDB() {
        PostStatus savedStatus = postStatusRepository.save(testStatus);
        Integer statusId = savedStatus.getId();

        postStatusRepository.delete(savedStatus);

        assertFalse(postStatusRepository.findById(statusId).isPresent());
    }

    @Test
    @DisplayName("General CRUD functionality test for PostStatus")
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