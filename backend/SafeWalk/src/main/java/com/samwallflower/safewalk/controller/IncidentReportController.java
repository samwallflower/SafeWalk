package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.HeatMapPointDto;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.incidentreport.IIncidentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/incident-reports")
public class IncidentReportController {
    private final IIncidentReportService incidentReportService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllIncidentReports() {
        List<IncidentReportDto> incidentReports = incidentReportService.getAllIncidentReports();
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<ApiResponse> getIncidentReportById(@PathVariable Long id) {
        IncidentReportDto incidentReport = incidentReportService.getIncidentReportById(id);
        return ResponseEntity.ok(new ApiResponse("Incident report retrieved successfully", incidentReport));
    }

    @GetMapping("/user/{userId}/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByUserId(@PathVariable Long userId) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-category-name/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByCategoryName(@RequestParam String categoryName) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByCategoryName(categoryName);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByStatus(@RequestParam String status) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByStatus(status);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-time-range/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByTimeRange(@RequestParam String startTime, @RequestParam String endTime) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByTimeRange(startTime, endTime);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-upvotes/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByUpvotes(@RequestParam Integer upvotes) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByUpvotes(upvotes);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-downvotes/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByDownvotes(@RequestParam Integer downvotes) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByDownvotes(downvotes);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-anonymous/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByAnonymous(@RequestParam Boolean isAnonymous) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByAnonymous(isAnonymous);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-category-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByCategoryAndStatus(@RequestParam String categoryName, @RequestParam String status) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByCategoryAndStatus(categoryName, status);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/{userId}/by-user-id-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByUserIdAndStatus(@PathVariable Long userId, @RequestParam String status) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-location-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByLocationAndStatus(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam Double radiusMeters, @RequestParam String status) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByLocationAndStatus(latitude, longitude, radiusMeters, status);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-time-range-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByTimeRangeAndStatus(@RequestParam String startTime, @RequestParam String endTime, @RequestParam String status) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByTimeRangeAndStatus(startTime, endTime, status);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/nearby/report")
    public ResponseEntity<ApiResponse> getNearByIncidentReports(@RequestParam double latitude, @RequestParam double longitude, @RequestParam double radiusMeters) {
        List<IncidentReportDto> incidentReports = incidentReportService.getNearByIncidentReports(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(new ApiResponse("Nearby incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/heatmap-points/report")
    public ResponseEntity<ApiResponse> getHeatMapPoints(@RequestParam double latitude, @RequestParam double longitude, @RequestParam double radiusMeters) {
        List<HeatMapPointDto> heatMapPoints = incidentReportService.getHeatMapPoints(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(new ApiResponse("Heat map points retrieved successfully", heatMapPoints));
    }

    @PutMapping("/{id}/status/update")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        IncidentReportDto incidentReport = incidentReportService.updateStatus(id, status);
        return ResponseEntity.ok(new ApiResponse("Incident report status updated successfully", incidentReport));
    }

    @PutMapping("/{userId}/report/{id}/update")
    public ResponseEntity<ApiResponse> updateIncidentReport(@RequestBody UpdateIncidentReportRequest request, @PathVariable Long id, @PathVariable Long userId) {
        IncidentReportDto incidentReport = incidentReportService.updateIncidentReport(request, userId, id);
        return ResponseEntity.ok(new ApiResponse("Incident report updated successfully", incidentReport));
    }

    @DeleteMapping("/{userId}/report/{id}/delete")
    public ResponseEntity<ApiResponse> deleteIncidentReport(@PathVariable Long id, @PathVariable Long userId) {
        incidentReportService.deleteIncidentReportById(id, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report deleted successfully", null));
    }

    // for admin only
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteIncidentReportById(@PathVariable Long id) {
        incidentReportService.deleteIncidentReportById(id);
        return ResponseEntity.ok(new ApiResponse("Incident report deleted successfully", null));
    }

    @PostMapping("/{userId}/report/add")
    public ResponseEntity<ApiResponse> addIncidentReport(@RequestBody AddIncidentReportRequest request, @PathVariable Long userId) {
        IncidentReportDto addedIncidentReport = incidentReportService.addIncidentReport(request, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report added successfully", addedIncidentReport));
    }
}
