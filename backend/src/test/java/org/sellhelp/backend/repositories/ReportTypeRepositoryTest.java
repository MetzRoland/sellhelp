package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.ReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReportTypeRepositoryTest {

    @Autowired
    private ReportTypeRepository reportTypeRepository;

    private ReportType testReportType;

    @BeforeEach
    public void init() {
        testReportType = ReportType.builder()
                .name("Spam")
                .build();
    }

    @Test
    @DisplayName("Verify that a ReportType can be added to repository and database")
    public void reportTypeCanBeAddedToRepositoryAndDB() {
        ReportType savedType = reportTypeRepository.save(testReportType);

        assertNotNull(savedType.getId());
        assertEquals("Spam", savedType.getName());
    }

    @Test
    @DisplayName("Verify that a ReportType can be updated in repository and database")
    public void reportTypeCanBeUpdatedInRepositoryAndDB() {
        ReportType savedType = reportTypeRepository.save(testReportType);

        savedType.setName("Harassment");
        ReportType updatedType = reportTypeRepository.save(savedType);

        assertNotNull(updatedType.getId());
        assertEquals("Harassment", updatedType.getName());
    }

    @Test
    @DisplayName("Verify that a ReportType can be deleted from repository and database")
    public void reportTypeCanBeDeletedFromRepositoryAndDB() {
        ReportType savedType = reportTypeRepository.save(testReportType);
        Integer typeId = savedType.getId();

        reportTypeRepository.delete(savedType);

        assertFalse(reportTypeRepository.findById(typeId).isPresent());
    }

    @Test
    @DisplayName("General CRUD functionality test for ReportType")
    public void reportTypeGeneralCRUDFunctionalityTest() {
        ReportType savedType = reportTypeRepository.save(testReportType);
        Integer typeId = savedType.getId();

        assertNotNull(typeId);
        assertEquals("Spam", reportTypeRepository.findById(typeId).get().getName());

        savedType.setName("Fraud");
        ReportType updatedType = reportTypeRepository.save(savedType);

        assertEquals("Fraud", updatedType.getName());

        reportTypeRepository.delete(updatedType);
        assertFalse(reportTypeRepository.findById(updatedType.getId()).isPresent());
    }
}