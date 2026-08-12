package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.IncidentCategoryDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.request.incidentcategory.AddIncidentCategoryRequest;
import com.samwallflower.safewalk.request.incidentcategory.UpdateIncidentCategoryRequest;
import com.samwallflower.safewalk.service.incidentcategory.IIncidentCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class IncidentCategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IIncidentCategoryService incidentCategoryService;

    @Test
    void getAllIncidentCategories_returns200() throws Exception {
        when(incidentCategoryService.getAllIncidentCategories()).thenReturn(List.of(new IncidentCategoryDto()));

        mockMvc.perform(get("/api/v1/incident-categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getIncidentCategoryById_returns200_whenFound() throws Exception {
        IncidentCategoryDto dto = new IncidentCategoryDto();
        dto.setId(1L);
        dto.setName("robbery");

        when(incidentCategoryService.getIncidentCategoryById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incident-categories/1/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("robbery"));
    }

    @Test
    void getIncidentCategoryById_returns404_whenNotFound() throws Exception {
        when(incidentCategoryService.getIncidentCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Incident Category not found with id: 999"));

        mockMvc.perform(get("/api/v1/incident-categories/999/category"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIncidentCategoryByName_returns200_whenFound() throws Exception {
        IncidentCategoryDto dto = new IncidentCategoryDto();
        dto.setName("harassment");

        when(incidentCategoryService.getIncidentCategoryByName("harassment")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incident-categories/by-name/category")
                        .param("name", "harassment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("harassment"));
    }

    @Test
    void getIncidentCategoryByName_returns404_whenNotFound() throws Exception {
        when(incidentCategoryService.getIncidentCategoryByName("ghost"))
                .thenThrow(new ResourceNotFoundException("Incident Category not found with name: ghost"));

        mockMvc.perform(get("/api/v1/incident-categories/by-name/category")
                        .param("name", "ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addIncidentCategory_returns200_onSuccess() throws Exception {
        AddIncidentCategoryRequest request = new AddIncidentCategoryRequest();
        request.setName("road accident");
        request.setSeverityWeight(18);
        request.setDescription("A vehicle incident on a road.");

        IncidentCategoryDto dto = new IncidentCategoryDto();
        dto.setId(1L);
        dto.setName("road accident");

        when(incidentCategoryService.addIncidentCategory(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/incident-categories/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("road accident"));
    }

    @Test
    void updateIncidentCategory_returns200_onSuccess() throws Exception {
        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();
        request.setName("updated name");

        IncidentCategoryDto dto = new IncidentCategoryDto();
        dto.setId(1L);
        dto.setName("updated name");

        when(incidentCategoryService.updateIncidentCategory(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/incident-categories/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("updated name"));
    }

    @Test
    void updateIncidentCategory_returns404_whenNotFound() throws Exception {
        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();

        when(incidentCategoryService.updateIncidentCategory(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Incident Category not found with id: 999"));

        mockMvc.perform(put("/api/v1/incident-categories/999/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteIncidentCategory_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/incident-categories/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteIncidentCategory_returns404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Incident Category not found with id: 999"))
                .when(incidentCategoryService).deleteIncidentCategoryById(999L);

        mockMvc.perform(delete("/api/v1/incident-categories/999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIncidentCategoryBySeverity_returns200() throws Exception {
        when(incidentCategoryService.getIncidentCategoryBySeverity(20))
                .thenReturn(List.of(new IncidentCategoryDto(), new IncidentCategoryDto()));

        mockMvc.perform(get("/api/v1/incident-categories/by-severity-weight/category")
                        .param("severity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void addIncidentCategory_returns400_whenNameBlank() throws Exception {
        AddIncidentCategoryRequest request = new AddIncidentCategoryRequest();
        request.setName(""); // blank — should fail @NotBlank
        request.setSeverityWeight(15);

        mockMvc.perform(post("/api/v1/incident-categories/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addIncidentCategory_returns400_whenSeverityWeightOutOfRange() throws Exception {
        AddIncidentCategoryRequest request = new AddIncidentCategoryRequest();
        request.setName("robbery");
        request.setSeverityWeight(999); // exceeds max of 20

        mockMvc.perform(post("/api/v1/incident-categories/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateIncidentCategory_returns400_whenSeverityWeightOutOfRange() throws Exception {
        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();
        request.setSeverityWeight(0); // below min of 1

        mockMvc.perform(put("/api/v1/incident-categories/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateIncidentCategory_returns200_whenSeverityWeightOmitted() throws Exception {
        // confirms partial update (null field) is NOT rejected by validation
        UpdateIncidentCategoryRequest request = new UpdateIncidentCategoryRequest();
        request.setName("renamed only");
        // severityWeight left null on purpose

        IncidentCategoryDto dto = new IncidentCategoryDto();
        dto.setName("renamed only");

        when(incidentCategoryService.updateIncidentCategory(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/incident-categories/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}