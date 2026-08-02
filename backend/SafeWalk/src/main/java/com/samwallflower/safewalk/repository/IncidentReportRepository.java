package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.model.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    List<IncidentReport> findByUserId(Long userId);
    List<IncidentReport> findByCategoryId(Long categoryId);


}
