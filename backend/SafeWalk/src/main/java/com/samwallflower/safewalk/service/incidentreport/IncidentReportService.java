package com.samwallflower.safewalk.service.incidentreport;

import com.samwallflower.safewalk.dto.HeatMapPointDto;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.dto.PageResponse;
import com.samwallflower.safewalk.enums.ReportStatus;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.IncidentCategoryRepository;
import com.samwallflower.safewalk.repository.IncidentReportRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;
import com.samwallflower.safewalk.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;


@Service
@RequiredArgsConstructor
public class IncidentReportService implements IIncidentReportService {
    private final IncidentReportRepository incidentReportRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${app.incident.add.report-time-limit-in-mins}")
    private int report_add_time_limit_in_mins;

    @Value("${DEFAULT_PAGE_SIZE}")
    private int DEFAULT_PAGE_SIZE;

    // Rate Limiter -> one person can only add a report every 5 minutes
    // checkOwnershipOrAdmin checks whether the given user id belongs to the logged in user
    @Override
    public IncidentReportDto addIncidentReport(AddIncidentReportRequest request, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);

        IncidentCategory category = categoryRepository.findByNameIgnoreCase(request.getCategory().getName())
                .orElseThrow(() -> new ResourceNotFoundException("Incident category not found with name: " + request.getCategory().getName()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        incidentReportRepository.findTopByUserIdOrderByTimestampDesc(userId)
                .ifPresent(lastReport -> {
                    long minutesSinceLastReport = MINUTES.between(lastReport.getTimestamp(), LocalDateTime.now());
                    if (minutesSinceLastReport < report_add_time_limit_in_mins) {
                        long minutesToWait = report_add_time_limit_in_mins - minutesSinceLastReport;
                        throw new RateLimitExceededException("Rate limit exceeded. Please wait " + minutesToWait + " more minutes before submitting another report.");
                    }
                });

        IncidentReport newReport = createIncidentReport(request, category);
        newReport.setUser(user);
        return convertToDto(incidentReportRepository.save(newReport));
    }

    private IncidentReport createIncidentReport(AddIncidentReportRequest request, IncidentCategory category) {
        IncidentReport newReport = new IncidentReport();
        newReport.setDescription(request.getDescription());
        newReport.setLatitude(request.getLatitude());
        newReport.setLongitude(request.getLongitude());
        newReport.setIsAnonymous(request.getIsAnonymous());
        newReport.setStatus(ReportStatus.ACTIVE);
        newReport.setCategory(category);
        return newReport;
    }

    /**
     * Suppose user id = 10 sends a request with
     * user id = 20 and report id = 5, then this method will throw an AccessDeniedException because user 10 is not the owner of the report with id 5.
     * even if report 5 belongs to user 20
     * this would be a security breach as the report does not belong to user 10
     * hence checkOwnershipOrAdmin checks the given user id belongs to logged in user or not
     * Dual Security layers
     * @param request
     * @param userId
     * @param id
     * @return
     */
    @Override
    public IncidentReportDto updateIncidentReport(UpdateIncidentReportRequest request, Long userId, Long id) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return incidentReportRepository.findById(id)
                .map(incidentReport -> {
                    if (!incidentReport.getUser().getId().equals(userId)) {
                        throw new AccessDeniedException("You are not authorized to update this incident report");
                    }
                    Optional.ofNullable(request.getDescription()).ifPresent(incidentReport::setDescription);
                    Optional.ofNullable(request.getLatitude()).ifPresent(incidentReport::setLatitude);
                    Optional.ofNullable(request.getLongitude()).ifPresent(incidentReport::setLongitude);
                    Optional.ofNullable(request.getIsAnonymous()).ifPresent(incidentReport::setIsAnonymous);
                    if (request.getCategory() != null) {
                        IncidentCategory category = categoryRepository.findById(request.getCategory().getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Incident category not found with id: " + request.getCategory().getId()));
                        incidentReport.setCategory(category);
                    }
                    incidentReportRepository.save(incidentReport);
                    return convertToDto(incidentReport);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Incident report not found with id: " + id));
    }

    @Override
    public void deleteIncidentReportById(Long id, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        incidentReportRepository.delete(incidentReportRepository.findById(id)
                .map(r -> {
                    if (!r.getUser().getId().equals(userId))
                        throw new AccessDeniedException("You are not authorized to delete this incident report");
                    return r;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Incident report not found with id: " + id))
        );
    }


    @Override
    public void deleteIncidentReportById(Long id) {
        incidentReportRepository.delete(incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident report not found with id: " + id))
        );
    }

    @Override
    public PageResponse<IncidentReportDto> getAllIncidentReports(int page, int size) {
        return toPage(incidentReportRepository.findAll(newestFirst(page, size)));
    }

    @Override
    public IncidentReportDto getIncidentReportById(Long id) {
        return incidentReportRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Incident report not found with id: " + id));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByCategoryName(String categoryName, int page, int size) {
        IncidentCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(()-> new ResourceNotFoundException("Incident category not found with name: " + categoryName));
        return toPage(incidentReportRepository.findByCategoryId(category.getId(), newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByUserId(Long userId, int page, int size) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return toPage(incidentReportRepository.findByUserId(userId, newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByStatus(String status, int page, int size) {
        ReportStatus reportStatus = resolveStatus(status);
        return toPage(incidentReportRepository.findByStatus(reportStatus, newestFirst(page, size)));
    }


    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByTimeRange(String startTime, String endTime, int page, int size) {
        return toPage(incidentReportRepository.findByTimestampBetween(parseDateTime(startTime), parseDateTime(endTime), newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByUpvotes(Integer upvotes, int page, int size) {
        return toPage(incidentReportRepository.findByUpvotes(upvotes, newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByDownvotes(Integer downvotes, int page, int size) {
        return toPage(incidentReportRepository.findByDownvotes(downvotes, newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByAnonymous(Boolean isAnonymous, int page, int size) {
        return toPage(incidentReportRepository.findByIsAnonymous(isAnonymous, newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByCategoryAndStatus(String categoryName, String status, int page, int size) {
        IncidentCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(()->
                        new ResourceNotFoundException("Incident category not found with name: " + categoryName));
        ReportStatus reportStatus = resolveStatus(status);
        return toPage(incidentReportRepository.findByCategoryIdAndStatus(category.getId(), reportStatus, newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByUserIdAndStatus(Long userId, String status, int page, int size) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        ReportStatus reportStatus = resolveStatus(status);
        return toPage(incidentReportRepository.findByUserIdAndStatus(userId, reportStatus, newestFirst(page, size)));
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByLocationAndStatus(Double latitude, Double longitude, Double radiusMeters, String status) {
        ReportStatus reportStatus = resolveStatus(status);
        return incidentReportRepository.findNearByAndStatus(latitude, longitude, radiusMeters, reportStatus.name()).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public PageResponse<IncidentReportDto> getIncidentReportsByTimeRangeAndStatus(String startTime, String endTime, String status, int page, int size) {
        ReportStatus reportStatus = resolveStatus(status);

        return toPage(incidentReportRepository.findByTimestampBetweenAndStatus(parseDateTime(startTime), parseDateTime(endTime), reportStatus, newestFirst(page, size)));
    }

    @Override
    public PageResponse<IncidentReportDto> getActiveIncidentReportsPage(int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page,0),
                Math.min(Math.max(pageSize,1), DEFAULT_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "timestamp"));
        return PageResponse.from(incidentReportRepository.findByStatus(ReportStatus.ACTIVE, pageable).map(this::convertToDto));
    }

    @Override
    public List<IncidentReportDto> getNearByIncidentReports(double latitude, double longitude, double radiusMeters) {
        return incidentReportRepository.findNearBy(latitude, longitude, radiusMeters).stream()
                .map(this::convertToDto)
                .toList();
    }
    // status should be changed by the admin only
    // so no need for user id
    @Override
    public IncidentReportDto updateStatus(Long id, String status) {
        return incidentReportRepository.findById(id)
                .map(incidentReport -> {
                    incidentReport.setStatus(resolveStatus(status));
                    incidentReportRepository.save(incidentReport);
                    return convertToDto(incidentReport);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Incident report not found with id: " + id));
    }

    @Override
    public List<HeatMapPointDto> getHeatMapPoints(double latitude, double longitude, double radiusMeters) {
        return incidentReportRepository.findNearBy(latitude, longitude, radiusMeters).stream()
                .map(incidentReport ->{
                    HeatMapPointDto heatMapPointDto = new HeatMapPointDto();
                    heatMapPointDto.setLatitude(incidentReport.getLatitude());
                    heatMapPointDto.setLongitude(incidentReport.getLongitude());
                    heatMapPointDto.setSeverityWeight(incidentReport.getCategory().getSeverityWeight());
                    return heatMapPointDto;
                })
                .toList();
    }

    @Override
    public IncidentReportDto convertToDto(IncidentReport incidentReport) {
        return modelMapper.map(incidentReport, IncidentReportDto.class);
    }

    @Override
    public long countAllIncidentReports() {
        return incidentReportRepository.count();
    }

    @Override
    public long getSumOfAllUpvotesInIncidentReportsByUserId(Long userId) {
        return incidentReportRepository.sumUpvotesByUserId(userId);
    }

    @Override
    public long getSumOfAllDownvotesInIncidentReportsByUserId(Long userId) {
        return incidentReportRepository.sumDownvotesByUserId(userId);
    }

    @Override
    public long countIncidentReportsByCategoryId(Long categoryId) {
        return incidentReportRepository.countIncidentReportsByCategoryId(categoryId);
    }

    @Override
    public long countIncidentReportsByUserId(Long userId) {
        return incidentReportRepository.countIncidentReportsByUserId(userId);
    }

    @Override
    public long countIncidentReportsByStatus(String status) {
        return incidentReportRepository.countIncidentReportsByStatus(resolveStatus(status));
    }

    @Override
    public long countIncidentReportsByStatusAndUserId(String status, Long userId) {
        return incidentReportRepository.countIncidentReportsByStatusAndUserId(resolveStatus(status), userId);
    }

    // helper methods
    private ReportStatus resolveStatus(String status) {
        return switch (status.toLowerCase().trim()){
            case "active" -> ReportStatus.ACTIVE;
            case "hidden" -> ReportStatus.HIDDEN;
            case "under_review" -> ReportStatus.UNDER_REVIEW;
            default -> throw new IllegalArgumentException("Invalid report status " + status);
        };
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date/time format: " + dateTime);
        }
    }

    private PageResponse<IncidentReportDto> toPage(Page<IncidentReport> page) {
        return PageResponse.from(page.map(this::convertToDto));
    }

    private Pageable newestFirst(int page, int size){
        return PageResponse.pageRequest(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
    }


}
