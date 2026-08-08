package com.samwallflower.safewalk.service.incidentreport;

import com.samwallflower.safewalk.dto.HeatMapPointDto;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;

import java.util.List;

public interface IIncidentReportService {
    IncidentReportDto addIncidentReport(AddIncidentReportRequest request, Long userId);
    IncidentReportDto updateIncidentReport(UpdateIncidentReportRequest request, Long userId, Long id);
    void deleteIncidentReportById(Long id, Long userId);

    void deleteIncidentReportById(Long id);

    List<IncidentReportDto> getAllIncidentReports();
    IncidentReportDto getIncidentReportById(Long id);
    List<IncidentReportDto> getIncidentReportsByCategoryName(String categoryName);
    List<IncidentReportDto> getIncidentReportsByUserId(Long userId);
    List<IncidentReportDto> getIncidentReportsByStatus(String status);
    List<IncidentReportDto> getIncidentReportsByTimeRange(String startTime, String endTime);
    List<IncidentReportDto> getIncidentReportsByUpvotes(Integer upvotes);
    List<IncidentReportDto> getIncidentReportsByDownvotes(Integer downvotes);
    List<IncidentReportDto> getIncidentReportsByAnonymous(Boolean isAnonymous);
    List<IncidentReportDto> getIncidentReportsByCategoryAndStatus(String categoryName, String status);
    List<IncidentReportDto> getIncidentReportsByUserIdAndStatus(Long userId, String status);
    List<IncidentReportDto> getIncidentReportsByLocationAndStatus(Double latitude, Double longitude, Double radiusMeters, String status);
    List<IncidentReportDto> getIncidentReportsByTimeRangeAndStatus(String startTime, String endTime, String status);

    List<IncidentReportDto> getNearByIncidentReports(double latitude, double longitude, double radiusMeters);
    IncidentReportDto updateStatus(Long id, String status);

    List<HeatMapPointDto> getHeatMapPoints(double latitude, double longitude, double radiusMeters);

    IncidentReportDto convertToDto(IncidentReport incidentReport);
}
