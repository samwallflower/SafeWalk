package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.model.IncidentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentCategoryRepository extends JpaRepository<IncidentCategory, Long> {
    Optional<IncidentCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    Optional<IncidentCategory> findBySeverityWeight(Integer severityWeight);
    List<IncidentCategory> findAllBySeverityWeight(Integer severityWeight);
}
