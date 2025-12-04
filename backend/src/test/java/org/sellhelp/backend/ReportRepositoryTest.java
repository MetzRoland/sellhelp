package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Report;
import org.sellhelp.backend.entities.ReportType;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.ReportRepository;
import org.sellhelp.backend.repositories.ReportTypeRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReportRepositoryTest {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportTypeRepository reportTypeRepository;

    private User testSenderUser;
    private User testReportedUser;
    private Report testReport;
    private ReportType testReportType;


    @BeforeEach
    public void init()
    {
        testSenderUser = userRepository.save(User.builder()
                .username("NagyB")
                .firstName("Kis")
                .lastName("Béla")
                .birthDate(LocalDate.of(2001, 11, 3))
                .email("kB@newMail.mail")
                .build());

        testReportedUser = userRepository.save(User.builder()
                .username("fisherman")
                .firstName("Hanz")
                .lastName("Fisher")
                .birthDate(LocalDate.of(1983, 2, 23))
                .email("h.fisher@gmail.com")
                .build());

        testReportType = reportTypeRepository.save(ReportType.builder()
                .name("Illegal activity")
                .build());

        testReport = reportRepository.save(Report.builder()
                        .senderUser(testSenderUser)
                        .reportedUser(testReportedUser)
                        .reportType(testReportType)
                .build());
    }


    @Test
    public void reportCanBeSavedToReportRepositoryAndDB()
    {
        Report savedReport = reportRepository.save(testReport);

        assertNotNull(savedReport.getId());
        assertEquals(testSenderUser.getId(), savedReport.getSenderUser().getId());
        assertEquals(testReportedUser.getId(), savedReport.getReportedUser().getId());
        assertEquals(testReport.getReportType().getName(), savedReport.getReportType().getName());
        assertNotNull(savedReport.getCreatedAt());
    }

    // report should not be updated by anyone
    @Test
    public void reportCanBeUpdatedToReportRepositoryAndDB()
    {
        Report savedReport = reportRepository.save(testReport);
        savedReport.setReportType(ReportType.builder().name("").build());

        Report updatedReport = reportRepository.save(savedReport);
        Report test = savedReport.toBuilder().build();

        assertEquals(test.getId(), updatedReport.getId());
        assertEquals(test, updatedReport);

        test.setCreatedAt(null);
        assertNotEquals(test, updatedReport);
        assertEquals(test.getSenderUser().getId(), updatedReport.getSenderUser().getId());

        test.getReportedUser().setBanned(true);
        assertNotEquals(test, updatedReport);
    }

    @Test
    public void reportCanBeDeletedFromReportRepositoryAndDB()
    {
        Report savedReport = reportRepository.save(testReport);
        Integer reportId = savedReport.getId();

        reportRepository.delete(savedReport);

        assertFalse(reportRepository.findById(reportId).isPresent());
    }
}
