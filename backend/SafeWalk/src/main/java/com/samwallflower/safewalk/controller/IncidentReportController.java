package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.HeatMapPointDto;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.dto.PageResponse;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.incidentreport.IIncidentReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/incident-reports")
public class IncidentReportController {
    private final IIncidentReportService incidentReportService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllIncidentReports(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getAllIncidentReports(page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<ApiResponse> getIncidentReportById(@PathVariable Long id) {
        IncidentReportDto incidentReport = incidentReportService.getIncidentReportById(id);
        return ResponseEntity.ok(new ApiResponse("Incident report retrieved successfully", incidentReport));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByUserId(userId, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-category-name/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByCategoryName(@RequestParam String categoryName,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByCategoryName(categoryName, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByStatus(@RequestParam String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByStatus(status, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-time-range/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByTimeRange(@RequestParam String startTime, @RequestParam String endTime, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByTimeRange(startTime, endTime, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-upvotes/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByUpvotes(@RequestParam Integer upvotes, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByUpvotes(upvotes, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-downvotes/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByDownvotes(@RequestParam Integer downvotes, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByDownvotes(downvotes, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-anonymous/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByAnonymous(@RequestParam Boolean isAnonymous, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByAnonymous(isAnonymous, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-category-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByCategoryAndStatus(@RequestParam String categoryName, @RequestParam String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByCategoryAndStatus(categoryName, status, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }
    // basically user can see which of their reports are hidden , under review active etc
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{userId}/by-user-id-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByUserIdAndStatus(@PathVariable Long userId, @RequestParam String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByUserIdAndStatus(userId, status, page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-location-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByLocationAndStatus(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam Double radiusMeters, @RequestParam String status) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByLocationAndStatus(latitude, longitude, radiusMeters, status);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-time-range-and-status/report")
    public ResponseEntity<ApiResponse> getIncidentReportsByTimeRangeAndStatus(@RequestParam String startTime, @RequestParam String endTime, @RequestParam String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByTimeRangeAndStatus(startTime, endTime, status, page, size);
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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status/update")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        IncidentReportDto incidentReport = incidentReportService.updateStatus(id, status);
        return ResponseEntity.ok(new ApiResponse("Incident report status updated successfully", incidentReport));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{userId}/report/{id}/update")
    public ResponseEntity<ApiResponse> updateIncidentReport(@Valid @RequestBody UpdateIncidentReportRequest request, @PathVariable Long id, @PathVariable Long userId) {
        IncidentReportDto incidentReport = incidentReportService.updateIncidentReport(request, userId, id);
        return ResponseEntity.ok(new ApiResponse("Incident report updated successfully", incidentReport));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{userId}/report/{id}/delete")
    public ResponseEntity<ApiResponse> deleteIncidentReport(@PathVariable Long id, @PathVariable Long userId) {
        incidentReportService.deleteIncidentReportById(id, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteIncidentReportById(@PathVariable Long id) {
        incidentReportService.deleteIncidentReportById(id);
        return ResponseEntity.ok(new ApiResponse("Incident report deleted successfully", null));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{userId}/report/add")
    public ResponseEntity<ApiResponse> addIncidentReport(@Valid @RequestBody AddIncidentReportRequest request, @PathVariable Long userId) {
        IncidentReportDto addedIncidentReport = incidentReportService.addIncidentReport(request, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report added successfully", addedIncidentReport));
    }

    // User facing status endpoints -> should only show active reports nothing else

    @GetMapping("/by-status-active/report")
    public ResponseEntity<ApiResponse> getAllActiveIncidentReports(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByStatus("ACTIVE", page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }


    @GetMapping("/by-category-and-status-active/report")
    public ResponseEntity<ApiResponse> getActiveIncidentReportsByCategory(@RequestParam String categoryName, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByCategoryAndStatus(categoryName, "ACTIVE", page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }


    @GetMapping("/by-location-and-status-active/report")
    public ResponseEntity<ApiResponse> getActiveIncidentReportsByLocation(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam Double radiusMeters) {
        List<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByLocationAndStatus(latitude, longitude, radiusMeters, "ACTIVE");
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/by-time-range-and-status-active/report")
    public ResponseEntity<ApiResponse> getActiveIncidentReportsByTimeRange(@RequestParam String startTime, @RequestParam String endTime, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentReportDto> incidentReports = incidentReportService.getIncidentReportsByTimeRangeAndStatus(startTime, endTime, "ACTIVE", page, size);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReports));
    }

    @GetMapping("/page/active/report")
    public ResponseEntity<ApiResponse> getActiveIncidentReportsPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int pageSize) {
        PageResponse<IncidentReportDto> incidentReportsPage = incidentReportService.getActiveIncidentReportsPage(page, pageSize);
        return ResponseEntity.ok(new ApiResponse("Incident reports retrieved successfully", incidentReportsPage));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/report")
    public ResponseEntity<ApiResponse> countAllIncidentReports() {
        long count = incidentReportService.countAllIncidentReports();
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/category/{categoryId}/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByCategoryId(@PathVariable Long categoryId) {
        long count = incidentReportService.countIncidentReportsByCategoryId(categoryId);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/user/{userId}/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByUserId(@PathVariable Long userId) {
        long count = incidentReportService.countIncidentReportsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/status/{status}/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByStatus(@PathVariable String status) {
        long count = incidentReportService.countIncidentReportsByStatus(status);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/by-status-and-user-id/user/{userId}/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByStatusAndUserId(@RequestParam String status, @PathVariable Long userId) {
        long count = incidentReportService.countIncidentReportsByStatusAndUserId(status, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/category/{categoryId}/user/{userId}/report")
    public ResponseEntity<ApiResponse> countIncidentsReportsByCategoryIdAndUserId(@PathVariable Long categoryId, @PathVariable Long userId) {
        long count = incidentReportService.countIncidentsReportsByCategoryIdAndUserId(categoryId, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/by-time-range/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByTimeStampBetween(@RequestParam String start, @RequestParam String end) {
        long count = incidentReportService.countIncidentReportsByTimeStampBetween(start, end);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/by-time-range-and-category-id/category/{categoryId}/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByTimeStampBetweenAndCategoryId(@RequestParam String start, @RequestParam String end, @PathVariable Long categoryId) {
        long count = incidentReportService.countIncidentReportsByTimeStampBetweenAndCategoryId(start, end, categoryId);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/sum-upvotes/user/{userId}/report")
    public ResponseEntity<ApiResponse> getSumOfAllUpvotesInIncidentReportsByUserId(@PathVariable Long userId) {
        long sum = incidentReportService.getSumOfAllUpvotesInIncidentReportsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Sum of all upvotes in incident reports retrieved successfully", sum));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/sum-downvotes/user/{userId}/report")
    public ResponseEntity<ApiResponse> getSumOfAllDownvotesInIncidentReportsByUserId(@PathVariable Long userId) {
        long sum = incidentReportService.getSumOfAllDownvotesInIncidentReportsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Sum of all downvotes in incident reports retrieved successfully", sum));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/by-time-range-and-user-id/user/{userId}/report")
    public ResponseEntity<ApiResponse> countIncidentReportsByTimeStampBetweenAndUserId(@RequestParam String start, @RequestParam String end, @PathVariable Long userId) {
        long count = incidentReportService.countIncidentReportsByTimeStampBetweenAndUserId(start, end, userId);
        return ResponseEntity.ok(new ApiResponse("Incident report count retrieved successfully", count));
    }
}
