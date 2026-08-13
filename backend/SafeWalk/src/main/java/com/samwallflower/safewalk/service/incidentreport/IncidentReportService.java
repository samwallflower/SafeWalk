package com.samwallflower.safewalk.service.incidentreport;

import com.samwallflower.safewalk.dto.HeatMapPointDto;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.enums.ReportStatus;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.IncidentCategoryRepository;
import com.samwallflower.safewalk.repository.IncidentReportRepository;
import com.samwallflower.safewalk.repository.IncidentVoteRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final IncidentVoteRepository incidentVoteRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public IncidentReportDto addIncidentReport(AddIncidentReportRequest request, Long userId) {
        IncidentCategory category = categoryRepository.findByNameIgnoreCase(request.getCategory().getName())
                .orElseThrow(() -> new ResourceNotFoundException("Incident category not found with name: " + request.getCategory().getName()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        incidentReportRepository.findTopByUserIdOrderByTimestampDesc(userId)
                .ifPresent(lastReport -> {
                    long minutesSinceLastReport = MINUTES.between(lastReport.getTimestamp(), LocalDateTime.now());
                    if (minutesSinceLastReport < 5) {
                        long minutesToWait = 5 - minutesSinceLastReport;
                        throw new RateLimitExceededException("Rate limit exceeded. Please wait " + minutesToWait + " more minutes before submitting another report.");
                    }
                });

        IncidentReport newReport = createIncidentReport(request, category);
        newReport.setUser(user);
        incidentReportRepository.save(newReport);
        return convertToDto(newReport);
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


    @Override
    public IncidentReportDto updateIncidentReport(UpdateIncidentReportRequest request, Long userId, Long id) {
        return incidentReportRepository.findById(id)
                .map(incidentReport -> {
                    if (!incidentReport.getUser().getId().equals(userId)) {
                        throw new ResourceProcessingException("You are not authorized to update this incident report");
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
        incidentReportRepository.delete(incidentReportRepository.findById(id)
                .map(r -> {
                    if (!r.getUser().getId().equals(userId))
                        throw new ResourceProcessingException("You are not authorized to delete this incident report");
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
    public List<IncidentReportDto> getAllIncidentReports() {
        return incidentReportRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public IncidentReportDto getIncidentReportById(Long id) {
        return incidentReportRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Incident report not found with id: " + id));
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByCategoryName(String categoryName) {
        IncidentCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(()-> new ResourceNotFoundException("Incident category not found with name: " + categoryName));
        return incidentReportRepository.findByCategoryId(category.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByUserId(Long userId) {
        return incidentReportRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByStatus(String status) {
        ReportStatus reportStatus = resolveStatus(status);
        return incidentReportRepository.findByStatus(reportStatus).stream()
                .map(this::convertToDto)
                .toList();
    }


    @Override
    public List<IncidentReportDto> getIncidentReportsByTimeRange(String startTime, String endTime) {
        return incidentReportRepository.findIncidentReportByTimestampBetween(parseDateTime(startTime), parseDateTime(endTime)).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByUpvotes(Integer upvotes) {
        return incidentReportRepository.findIncidentReportByUpvotes(upvotes).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByDownvotes(Integer downvotes) {
        return incidentReportRepository.findIncidentReportByDownvotes(downvotes).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByAnonymous(Boolean isAnonymous) {
        return incidentReportRepository.findIncidentReportByIsAnonymous(isAnonymous).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByCategoryAndStatus(String categoryName, String status) {
        IncidentCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(()->
                        new ResourceNotFoundException("Incident category not found with name: " + categoryName));
        ReportStatus reportStatus = resolveStatus(status);
        return incidentReportRepository.findByCategoryIdAndStatus(category.getId(), reportStatus)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByUserIdAndStatus(Long userId, String status) {
        ReportStatus reportStatus = resolveStatus(status);
        return incidentReportRepository.findByUserIdAndStatus(userId, reportStatus).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByLocationAndStatus(Double latitude, Double longitude, Double radiusMeters, String status) {
        ReportStatus reportStatus = resolveStatus(status);
        return incidentReportRepository.findNearByAndStatus(latitude, longitude, radiusMeters, reportStatus.name()).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<IncidentReportDto> getIncidentReportsByTimeRangeAndStatus(String startTime, String endTime, String status) {
        ReportStatus reportStatus = resolveStatus(status);

        return incidentReportRepository
                .findIncidentReportByTimestampBetweenAndStatus(parseDateTime(startTime),
                        parseDateTime(endTime), reportStatus).stream()
                .map(this::convertToDto)
                .toList();
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


}
