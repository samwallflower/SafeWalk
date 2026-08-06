package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.enums.ReportStatus;
import com.samwallflower.safewalk.model.IncidentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    List<IncidentReport> findByUserId(Long userId);
    List<IncidentReport> findByCategoryId(Long categoryId);
    List<IncidentReport> findByUserIdAndCategoryId(Long userId, Long categoryId);
    List<IncidentReport> findByStatus(ReportStatus status);

    List<IncidentReport> findByUserIdAndStatus(Long userId, ReportStatus status);
    List<IncidentReport> findByCategoryIdAndStatus(Long categoryId, ReportStatus status);

    List<IncidentReport> findIncidentReportByDownvotes(Integer downvotes);
    List<IncidentReport> findIncidentReportByUpvotes(Integer upvotes);
    List<IncidentReport> findIncidentReportByIsAnonymous(Boolean isAnonymous);
    List<IncidentReport> findIncidentReportByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<IncidentReport> findIncidentReportByTimestampBetweenAndStatus(LocalDateTime start, LocalDateTime end, ReportStatus status);


    @Query(value = """
        SELECT * FROM incident_report ir
        WHERE ir.status = 'ACTIVE'
        AND ST_DWithin(
            ST_SetSRID(ST_MakePoint(ir.longitude, ir.latitude),4326)::geography,
            ST_SetSRID(ST_MakePoint(:lng, :lat),4326)::geography,
            :radiusMeters)
        ORDER BY ir.timestamp DESC
    """, nativeQuery = true)
    List<IncidentReport> findNearBy(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

    @Query(value = """
        SELECT * FROM incident_report ir
        WHERE ir.status = :status
        AND ST_DWithin(
            ST_SetSRID(ST_MakePoint(ir.longitude, ir.latitude),4326)::geography,
            ST_SetSRID(ST_MakePoint(:lng, :lat),4326)::geography,
            :radiusMeters)
        ORDER BY ir.timestamp DESC
    """, nativeQuery = true)
    List<IncidentReport> findNearByAndStatus(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters,
            @Param("status") String status
    );

    Page<IncidentReport> findByCategoryId(Long categoryId, Pageable pageable);

    //Finds the most recent report by user
    Optional<IncidentReport> findTopByUserIdOrderByTimestampDesc(Long userId);

}
