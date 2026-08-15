package com.samwallflower.safewalk.service.incidentreport;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentReportServiceTest {

    @Mock private IncidentReportRepository incidentReportRepository;
    @Mock private IncidentCategoryRepository categoryRepository;
    @Mock private IncidentVoteRepository incidentVoteRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private IncidentReportService service;

    @BeforeEach
    void setUp() {
        service = new IncidentReportService(
                incidentReportRepository, categoryRepository, incidentVoteRepository,
                userRepository, new ModelMapper()
        );
    }

    private IncidentCategory buildCategory(Long id, String name, int weight) {
        IncidentCategory c = new IncidentCategory();
        c.setId(id);
        c.setName(name);
        c.setSeverityWeight(weight);
        return c;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    // ---------- addIncidentReport ----------

    @Test
    void addIncidentReport_success_whenNoPriorReport() {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        request.setDescription("Suspicious activity near the plaza");
        request.setLatitude(47.53);
        request.setLongitude(21.62);
        request.setIsAnonymous(false);
        IncidentCategory categoryRef = new IncidentCategory();
        categoryRef.setName("suspicious activity");
        request.setCategory(categoryRef);

        IncidentCategory category = buildCategory(1L, "suspicious activity", 17);
        User user = buildUser(5L);

        when(categoryRepository.findByNameIgnoreCase("suspicious activity")).thenReturn(Optional.of(category));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findTopByUserIdOrderByTimestampDesc(5L)).thenReturn(Optional.empty());
        when(incidentReportRepository.save(any(IncidentReport.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentReportDto result = service.addIncidentReport(request, 5L);

        assertThat(result.getDescription()).isEqualTo("Suspicious activity near the plaza");
        verify(incidentReportRepository).save(any(IncidentReport.class));
    }

    @Test
    void addIncidentReport_throws_whenCategoryNotFound() {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        IncidentCategory categoryRef = new IncidentCategory();
        categoryRef.setName("nonexistent");
        request.setCategory(categoryRef);

        when(categoryRepository.findByNameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addIncidentReport(request, 5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");

        verify(userRepository, never()).findById(any());
        verify(incidentReportRepository, never()).save(any());
    }

    @Test
    void addIncidentReport_throws_whenUserNotFound() {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        IncidentCategory categoryRef = new IncidentCategory();
        categoryRef.setName("harassment");
        request.setCategory(categoryRef);

        when(categoryRepository.findByNameIgnoreCase("harassment"))
                .thenReturn(Optional.of(buildCategory(1L, "harassment", 19)));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addIncidentReport(request, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addIncidentReport_throws_whenRateLimited_withinFiveMinutes() {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        IncidentCategory categoryRef = new IncidentCategory();
        categoryRef.setName("robbery");
        request.setCategory(categoryRef);

        when(categoryRepository.findByNameIgnoreCase("robbery"))
                .thenReturn(Optional.of(buildCategory(1L, "robbery", 20)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(buildUser(5L)));

        IncidentReport lastReport = new IncidentReport();
        lastReport.setTimestamp(LocalDateTime.now().minusMinutes(2)); // only 2 min ago
        when(incidentReportRepository.findTopByUserIdOrderByTimestampDesc(5L))
                .thenReturn(Optional.of(lastReport));

        assertThatThrownBy(() -> service.addIncidentReport(request, 5L))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("3"); // 5 - 2 = 3 minutes remaining

        verify(incidentReportRepository, never()).save(any());
    }

    @Test
    void addIncidentReport_succeeds_whenLastReportOlderThanFiveMinutes() {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        IncidentCategory categoryRef = new IncidentCategory();
        categoryRef.setName("vandalism");
        request.setCategory(categoryRef);

        when(categoryRepository.findByNameIgnoreCase("vandalism"))
                .thenReturn(Optional.of(buildCategory(1L, "vandalism", 20)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(buildUser(5L)));

        IncidentReport lastReport = new IncidentReport();
        lastReport.setTimestamp(LocalDateTime.now().minusMinutes(10));
        when(incidentReportRepository.findTopByUserIdOrderByTimestampDesc(5L))
                .thenReturn(Optional.of(lastReport));
        when(incidentReportRepository.save(any(IncidentReport.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentReportDto result = service.addIncidentReport(request, 5L);

        assertThat(result).isNotNull();
        verify(incidentReportRepository).save(any());
    }

    // ---------- updateIncidentReport ----------

    @Test
    void updateIncidentReport_success_updatesOnlyProvidedFields() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setDescription("old description");
        existing.setLatitude(47.0);
        existing.setLongitude(21.0);
        existing.setUser(buildUser(5L));
        existing.setCategory(buildCategory(1L, "harassment", 19));

        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();
        request.setDescription("new description");
        // latitude/longitude intentionally left null — should stay unchanged

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentReportRepository.save(any(IncidentReport.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentReportDto result = service.updateIncidentReport(request, 5L, 1L);

        assertThat(result.getDescription()).isEqualTo("new description");
        assertThat(existing.getLatitude()).isEqualTo(47.0); // untouched
    }

    @Test
    void updateIncidentReport_throws_whenNotOwner() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setUser(buildUser(5L)); // owned by user 5

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));

        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();

        assertThatThrownBy(() -> service.updateIncidentReport(request, 999L, 1L)) // different user
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("not authorized");

        verify(incidentReportRepository, never()).save(any());
    }

    @Test
    void updateIncidentReport_throws_whenReportNotFound() {
        when(incidentReportRepository.findById(404L)).thenReturn(Optional.empty());

        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();

        assertThatThrownBy(() -> service.updateIncidentReport(request, 5L, 404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateIncidentReport_throws_whenNewCategoryNotFound() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setUser(buildUser(5L));

        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();
        IncidentCategory categoryRef = new IncidentCategory();
        categoryRef.setId(999L);
        request.setCategory(categoryRef);

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateIncidentReport(request, 5L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ---------- delete ----------

    @Test
    void deleteIncidentReportById_withUserId_throws_whenNotOwner() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setUser(buildUser(5L));

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.deleteIncidentReportById(1L, 999L))
                .isInstanceOf(ResourceProcessingException.class);

        verify(incidentReportRepository, never()).delete(any());
    }

    @Test
    void deleteIncidentReportById_withUserId_success_whenOwner() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setUser(buildUser(5L));

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteIncidentReportById(1L, 5L);

        verify(incidentReportRepository).delete(existing);
    }

    @Test
    void deleteIncidentReportById_adminOnly_deletesRegardlessOfOwner() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setUser(buildUser(5L));

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteIncidentReportById(1L); // no userId — admin path

        verify(incidentReportRepository).delete(existing);
    }

    // ---------- status resolution ----------

    @Test
    void updateStatus_success_validStatus() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        existing.setStatus(ReportStatus.ACTIVE);

        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentReportRepository.save(any(IncidentReport.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentReportDto result = service.updateStatus(1L, "hidden");

        assertThat(existing.getStatus()).isEqualTo(ReportStatus.HIDDEN);
    }

    @Test
    void updateStatus_throws_whenStatusInvalid() {
        IncidentReport existing = new IncidentReport();
        existing.setId(1L);
        when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateStatus(1L, "NOT_A_REAL_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid report status");
    }

    @Test
    void getIncidentReportsByStatus_throws_whenStatusInvalid() {
        assertThatThrownBy(() -> service.getIncidentReportsByStatus("garbage"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(incidentReportRepository);
    }

    // ---------- time range parsing ----------

    @Test
    void getIncidentReportsByTimeRange_throws_whenDateFormatInvalid() {
        assertThatThrownBy(() -> service.getIncidentReportsByTimeRange("not-a-date", "2026-01-01T00:00:00"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date");
    }

    @Test
    void getIncidentReportsByTimeRange_success_validDates() {
        when(incidentReportRepository.findIncidentReportByTimestampBetween(any(), any()))
                .thenReturn(List.of(new IncidentReport()));

        List<IncidentReportDto> result = service.getIncidentReportsByTimeRange(
                "2026-01-01T00:00:00", "2026-01-02T00:00:00"
        );

        assertThat(result).hasSize(1);
    }

    // ---------- read paths ----------

    @Test
    void getIncidentReportById_throws_whenNotFound() {
        when(incidentReportRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getIncidentReportById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getIncidentReportsByCategoryName_throws_whenCategoryMissing() {
        when(categoryRepository.findByNameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getIncidentReportsByCategoryName("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getHeatMapPoints_mapsSeverityWeightCorrectly() {
        IncidentReport report = new IncidentReport();
        report.setLatitude(47.5);
        report.setLongitude(21.6);
        report.setCategory(buildCategory(1L, "robbery", 20));

        when(incidentReportRepository.findNearBy(47.5, 21.6, 100.0)).thenReturn(List.of(report));

        var points = service.getHeatMapPoints(47.5, 21.6, 100.0);

        assertThat(points).hasSize(1);
        assertThat(points.get(0).getSeverityWeight()).isEqualTo(20);
    }
}