package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.UserFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserFileRepositoryTest {
    @Autowired
    private UserFileRepository userFileRepository;

    private UserFile testUserFile;

    @BeforeEach
    public void init(){
        testUserFile = UserFile.builder()
                .filePath("file1.docx")
                .build();
    }

    @Test
    @DisplayName("Verify that a UserFile can be added to repository and database")
    public void userFileCanBeAddedToUserFileRepositoryAndDB(){
        UserFile savedUserFile = userFileRepository.save(testUserFile);

        assertNotNull(savedUserFile.getId());
        assertEquals("file1.docx", savedUserFile.getFilePath());
    }

    @Test
    @DisplayName("Verify that a UserFile can be updated in repository and database")
    public void userFileCanBeUpdatedToUserFileRepositoryAndDB(){
        userFileRepository.save(testUserFile);

        testUserFile.setFilePath("file-updated.docx");
        UserFile updatedUserFile = userFileRepository.save(testUserFile);

        assertNotNull(updatedUserFile.getId());
        assertEquals("file-updated.docx", updatedUserFile.getFilePath());
    }

    @Test
    @DisplayName("Verify that a UserFile can be deleted from repository and database")
    public void userFileCanBeDeletedFromUserFileRepositoryAndDB(){
        UserFile savedUserFile = userFileRepository.save(testUserFile);
        Integer savedUserId = savedUserFile.getId();

        userFileRepository.delete(savedUserFile);

        assertFalse(userFileRepository.findById(savedUserId).isPresent());
    }

    @Test
    @DisplayName("General CRUD functionality test for UserFile")
    public void userGeneralCRUDFunctionalityTest(){
        UserFile savedUserFile = userFileRepository.save(testUserFile);
        Integer savedUserFileId = savedUserFile.getId();

        assertNotNull(savedUserFileId);
        assertEquals("file1.docx", savedUserFile.getFilePath());

        savedUserFile.setFilePath("other-file.pdf");
        UserFile updatedUserFile = userFileRepository.save(savedUserFile);

        assertEquals("other-file.pdf", updatedUserFile.getFilePath());

        userFileRepository.delete(updatedUserFile);
        assertFalse(userFileRepository.findById(updatedUserFile.getId()).isPresent());
    }
}