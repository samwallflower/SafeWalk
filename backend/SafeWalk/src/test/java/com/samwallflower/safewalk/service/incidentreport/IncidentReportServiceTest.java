package com.samwallflower.safewalk.service.incidentreport;

import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.enums.ReportStatus;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.IncidentCategoryRepository;
import com.samwallflower.safewalk.repository.IncidentReportRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentReportServiceTest {

    @Mock private IncidentReportRepository incidentReportRepository;
    @Mock private IncidentCategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private IncidentReportService incidentReportService;

    @Test
    void addIncidentReport_success() {
        // Arrange
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        IncidentCategory category = new IncidentCategory();
        category.setName("Theft");
        request.setCategory(category);

        User user = new User();
        user.setId(1L);

        when(categoryRepository.findByNameIgnoreCase(any())).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Simulating no recent reports
        when(incidentReportRepository.findTopByUserIdOrderByTimestampDesc(1L)).thenReturn(Optional.empty());

        IncidentReportDto expectedDto = new IncidentReportDto();
        when(modelMapper.map(any(IncidentReport.class), eq(IncidentReportDto.class))).thenReturn(expectedDto);

        // Act
        IncidentReportDto result = incidentReportService.addIncidentReport(request, 1L);

        // Assert
        assertNotNull(result);
        verify(incidentReportRepository).save(any(IncidentReport.class));
    }

    @Test
    void addIncidentReport_throwsRateLimitExceeded() {
        // Arrange
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        IncidentCategory category = new IncidentCategory();
        category.setName("Theft");
        request.setCategory(category);

        User user = new User();
        user.setId(1L);

        IncidentReport recentReport = new IncidentReport();
        recentReport.setTimestamp(LocalDateTime.now().minusMinutes(2)); // Only 2 mins ago

        when(categoryRepository.findByNameIgnoreCase(any())).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findTopByUserIdOrderByTimestampDesc(1L)).thenReturn(Optional.of(recentReport));

        // Act & Assert
        assertThrows(RateLimitExceededException.class, () ->
                incidentReportService.addIncidentReport(request, 1L)
        );
        verify(incidentReportRepository, never()).save(any());
    }

    @Test
    void updateIncidentReport_success() {
        // Arrange
        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();
        request.setDescription("New Description");

        User user = new User();
        user.setId(1L);

        IncidentReport existingReport = new IncidentReport();
        existingReport.setId(10L);
        existingReport.setUser(user);

        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(existingReport));

        IncidentReportDto expectedDto = new IncidentReportDto();
        when(modelMapper.map(any(IncidentReport.class), eq(IncidentReportDto.class))).thenReturn(expectedDto);

        // Act
        IncidentReportDto result = incidentReportService.updateIncidentReport(request, 1L, 10L);

        // Assert
        assertNotNull(result);
        assertEquals("New Description", existingReport.getDescription());
        verify(incidentReportRepository).save(existingReport);
    }

    @Test
    void updateIncidentReport_throwsResourceProcessingException_whenUserMismatch() {
        // Arrange
        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();

        User owner = new User();
        owner.setId(1L); // Report owned by user 1

        IncidentReport existingReport = new IncidentReport();
        existingReport.setUser(owner);

        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(existingReport));

        // Act & Assert
        assertThrows(ResourceProcessingException.class, () ->
                incidentReportService.updateIncidentReport(request, 999L, 10L) // User 999 tries to update
        );
        verify(incidentReportRepository, never()).save(any());
    }
}