package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTypeRepository extends JpaRepository<ReportType, Integer> {
}
