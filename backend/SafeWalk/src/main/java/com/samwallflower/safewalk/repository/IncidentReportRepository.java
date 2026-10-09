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

    //Paginated queries
    Page<IncidentReport> findByCategoryId(Long categoryId, Pageable pageable);
    Page<IncidentReport> findByStatus(ReportStatus status, Pageable pageable);

    Page<IncidentReport> findByUserId(Long userId, Pageable pageable);
    Page<IncidentReport> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);
    Page<IncidentReport> findByUserIdAndStatus(Long userId, ReportStatus status, Pageable pageable);
    Page<IncidentReport> findByCategoryIdAndStatus(Long categoryId, ReportStatus status, Pageable pageable);

    Page<IncidentReport> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<IncidentReport> findByTimestampBetweenAndStatus(LocalDateTime start, LocalDateTime end, ReportStatus status, Pageable pageable);

    Page<IncidentReport> findByIsAnonymous(Boolean isAnonymous, Pageable pageable);
    Page<IncidentReport> findByUpvotes(Integer upvotes, Pageable pageable);
    Page<IncidentReport> findByDownvotes(Integer downvotes, Pageable pageable);

    //Finds the most recent report by user
    Optional<IncidentReport> findTopByUserIdOrderByTimestampDesc(Long userId);

    long countIncidentReportsByCategoryId(Long categoryId);
    long countIncidentReportsByCategoryIdAndUserId(Long categoryId, Long userId);

    long countIncidentReportsByStatus(ReportStatus status);
    long countIncidentReportsByUserId(Long userId);
    long countIncidentReportsByStatusAndUserId(ReportStatus status, Long userId);

    long countIncidentReportsByTimestampBetween(LocalDateTime start, LocalDateTime end);
    long countIncidentReportsByTimestampBetweenAndCategoryId(LocalDateTime start, LocalDateTime end, Long categoryId);
    long countIncidentReportsByTimestampBetweenAndUserId(LocalDateTime start, LocalDateTime end, Long userId);

    @Query("SELECT SUM(ir.upvotes) FROM IncidentReport ir WHERE ir.user.id = :userId")
    Long sumUpvotesByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(ir.downvotes) FROM IncidentReport ir WHERE ir.user.id = :userId")
    Long sumDownvotesByUserId(@Param("userId") Long userId);

}
