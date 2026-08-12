package com.samwallflower.safewalk.service.incidentcategory;

import com.samwallflower.safewalk.dto.IncidentCategoryDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.repository.IncidentCategoryRepository;
import com.samwallflower.safewalk.request.incidentcategory.AddIncidentCategoryRequest;
import com.samwallflower.safewalk.request.incidentcategory.UpdateIncidentCategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentCategoryServiceTest {

    @Mock private IncidentCategoryRepository incidentCategoryRepository;

    @InjectMocks
    private IncidentCategoryService service;

    @BeforeEach
    void setUp() {
        service = new IncidentCategoryService(incidentCategoryRepository, new ModelMapper());
    }

    private IncidentCategory buildCategory(Long id, String name, int weight, String description) {
        IncidentCategory c = new IncidentCategory();
        c.setId(id);
        c.setName(name);
        c.setSeverityWeight(weight);
        c.setDescription(description);
        return c;
    }

    // ---------- getAll / getById / getByName ----------

    @Test
    void getAllIncidentCategories_returnsMappedList() {
        when(incidentCategoryRepository.findAll()).thenReturn(List.of(
                buildCategory(1L, "robbery", 20, "desc"),
                buildCategory(2L, "vandalism", 20, "desc")
        ));

        List<IncidentCategoryDto> result = service.getAllIncidentCategories();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(IncidentCategoryDto::getName)
                .containsExactlyInAnyOrder("robbery", "vandalism");
    }

    @Test
    void getIncidentCategoryById_returnsDto_whenFound() {
        when(incidentCategoryRepository.findById(1L))
                .thenReturn(Optional.of(buildCategory(1L, "robbery", 20, "desc")));

        IncidentCategoryDto result = service.getIncidentCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("robbery");
    }

    @Test
    void getIncidentCategoryById_throws_whenNotFound() {
        when(incidentCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getIncidentCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getIncidentCategoryByName_returnsDto_whenFound() {
        when(incidentCategoryRepository.findByNameIgnoreCase("harassment"))
                .thenReturn(Optional.of(buildCategory(1L, "harassment", 19, "desc")));

        IncidentCategoryDto result = service.getIncidentCategoryByName("harassment");

        assertThat(result.getName()).isEqualTo("harassment");
    }

    @Test
    void getIncidentCategoryByName_throws_whenNotFound() {
        when(incidentCategoryRepository.findByNameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getIncidentCategoryByName("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void getIncidentCategoryBySeverity_returnsMatchingList() {
        when(incidentCategoryRepository.findAllBySeverityWeight(20)).thenReturn(List.of(
                buildCategory(1L, "robbery", 20, "desc"),
                buildCategory(2L, "vandalism", 20, "desc")
        ));

        List<IncidentCategoryDto> result = service.getIncidentCategoryBySeverity(20);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> dto.getSeverityWeight().equals(20));
    }

    @Test
    void getIncidentCategoryBySeverity_returnsEmptyList_whenNoMatches() {
        when(incidentCategoryRepository.findAllBySeverityWeight(99)).thenReturn(List.of());

        List<IncidentCategoryDto> result = service.getIncidentCategoryBySeverity(99);

        assertThat(result).isEmpty();
    }

    // ---------- addIncidentCategory ----------

    @Test
    void addIncidentCategory_success_withDescription() {
        AddIncidentCategoryRequest request = new AddIncidentCategoryRequest();
        request.setName("road accident");
        request.setSeverityWeight(18);
        request.setDescription("A vehicle incident on a road.");

        when(incidentCategoryRepository.save(any(IncidentCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IncidentCategoryDto result = service.addIncidentCategory(request);

        assertThat(result.getName()).isEqualTo("road accident");
        assertThat(result.getSeverityWeight()).isEqualTo(18);
        assertThat(result.getDescription()).isEqualTo("A vehicle incident on a road.");
        verify(incidentCategoryRepository).save(any(IncidentCategory.class));
    }

    @Test
    void addIncidentCategory_success_withNullDescription() {
        AddIncidentCategoryRequest request = new AddIncidentCategoryRequest();
        request.setName("suspicious activity");
        request.setSeverityWeight(17);
        request.setDescription(null); // optional field omitted

        when(incidentCategoryRepository.save(any(IncidentCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IncidentCategoryDto result = service.addIncidentCategory(request);

        assertThat(result.getName()).isEqualTo("suspicious activity");
        assertThat(result.getDescription()).isNull();
    }

    // ---------- updateIncidentCategory ----------

    @Test
    void updateIncidentCategory_updatesOnlyProvidedFields() {
        IncidentCategory existing = buildCategory(1L, "old name", 10, "old description");

        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();
        request.setName("new name");
        // severityWeight and description intentionally left null

        when(incidentCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentCategoryRepository.save(any(IncidentCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IncidentCategoryDto result = service.updateIncidentCategory(1L, request);

        assertThat(result.getName()).isEqualTo("new name");
        assertThat(existing.getSeverityWeight()).isEqualTo(10); // untouched
        assertThat(existing.getDescription()).isEqualTo("old description"); // untouched
    }

    @Test
    void updateIncidentCategory_updatesAllFields_whenAllProvided() {
        IncidentCategory existing = buildCategory(1L, "old name", 10, "old description");

        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();
        request.setName("new name");
        request.setSeverityWeight(15);
        request.setDescription("new description");

        when(incidentCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentCategoryRepository.save(any(IncidentCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IncidentCategoryDto result = service.updateIncidentCategory(1L, request);

        assertThat(result.getName()).isEqualTo("new name");
        assertThat(result.getSeverityWeight()).isEqualTo(15);
        assertThat(result.getDescription()).isEqualTo("new description");
    }

    @Test
    void updateIncidentCategory_throws_whenNotFound() {
        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();
        when(incidentCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateIncidentCategory(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(incidentCategoryRepository, never()).save(any());
    }

    // ---------- deleteIncidentCategoryById ----------

    @Test
    void deleteIncidentCategoryById_success() {
        IncidentCategory existing = buildCategory(1L, "vandalism", 20, "desc");
        when(incidentCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteIncidentCategoryById(1L);

        verify(incidentCategoryRepository).delete(existing);
    }

    @Test
    void deleteIncidentCategoryById_throws_whenNotFound() {
        when(incidentCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteIncidentCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(incidentCategoryRepository, never()).delete(any());
    }
}