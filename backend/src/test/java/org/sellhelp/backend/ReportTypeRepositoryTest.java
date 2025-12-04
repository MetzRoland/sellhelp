package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.ReportType;
import org.sellhelp.backend.repositories.ReportTypeRepository;
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
    public void reportTypeCanBeAddedToRepositoryAndDB() {
        ReportType savedType = reportTypeRepository.save(testReportType);

        assertNotNull(savedType.getId());
        assertEquals("Spam", savedType.getName());
    }

    @Test
    public void reportTypeCanBeUpdatedInRepositoryAndDB() {
        ReportType savedType = reportTypeRepository.save(testReportType);

        savedType.setName("Harassment");
        ReportType updatedType = reportTypeRepository.save(savedType);

        assertNotNull(updatedType.getId());
        assertEquals("Harassment", updatedType.getName());
    }

    @Test
    public void reportTypeCanBeDeletedFromRepositoryAndDB() {
        ReportType savedType = reportTypeRepository.save(testReportType);
        Integer typeId = savedType.getId();

        reportTypeRepository.delete(savedType);

        assertFalse(reportTypeRepository.findById(typeId).isPresent());
    }

    @Test
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
