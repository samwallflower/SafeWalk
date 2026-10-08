package com.samwallflower.safewalk.service.incidentreport;

import com.samwallflower.safewalk.dto.HeatMapPointDto;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.dto.PageResponse;
import com.samwallflower.safewalk.enums.ReportStatus;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;

import java.util.List;

public interface IIncidentReportService {
    IncidentReportDto addIncidentReport(AddIncidentReportRequest request, Long userId);
    IncidentReportDto updateIncidentReport(UpdateIncidentReportRequest request, Long userId, Long id);
    void deleteIncidentReportById(Long id, Long userId);

    void deleteIncidentReportById(Long id);

    PageResponse<IncidentReportDto> getAllIncidentReports(int page, int size);
    IncidentReportDto getIncidentReportById(Long id);
    PageResponse<IncidentReportDto> getIncidentReportsByCategoryName(String categoryName, int page,  int size);
    PageResponse<IncidentReportDto> getIncidentReportsByUserId(Long userId, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByStatus(String status, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByTimeRange(String startTime, String endTime, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByUpvotes(Integer upvotes, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByDownvotes(Integer downvotes, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByAnonymous(Boolean isAnonymous, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByCategoryAndStatus(String categoryName, String status, int page, int size);
    PageResponse<IncidentReportDto> getIncidentReportsByUserIdAndStatus(Long userId, String status, int page, int size);
    List<IncidentReportDto> getIncidentReportsByLocationAndStatus(Double latitude, Double longitude, Double radiusMeters, String status);
    PageResponse<IncidentReportDto> getIncidentReportsByTimeRangeAndStatus(String startTime, String endTime, String status, int page, int size);

    PageResponse<IncidentReportDto> getActiveIncidentReportsPage(int page, int pageSize);

    List<IncidentReportDto> getNearByIncidentReports(double latitude, double longitude, double radiusMeters);
    IncidentReportDto updateStatus(Long id, String status);

    List<HeatMapPointDto> getHeatMapPoints(double latitude, double longitude, double radiusMeters);

    IncidentReportDto convertToDto(IncidentReport incidentReport);

    long countAllIncidentReports();

    long getSumOfAllUpvotesInIncidentReportsByUserId(Long userId);
    long getSumOfAllDownvotesInIncidentReportsByUserId(Long userId);

    long countIncidentReportsByCategoryId(Long categoryId);
    long countIncidentReportsByUserId(Long userId);
    long countIncidentReportsByStatus(String status);
    long countIncidentReportsByStatusAndUserId(String status, Long userId);
}
