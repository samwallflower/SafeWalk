package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.IncidentReportDto;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.request.incidentreport.AddIncidentReportRequest;
import com.samwallflower.safewalk.request.incidentreport.UpdateIncidentReportRequest;
import com.samwallflower.safewalk.service.incidentreport.IIncidentReportService;
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

@WebMvcTest(IncidentReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class IncidentReportControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IIncidentReportService incidentReportService;

    @Test
    void getAllIncidentReports_returns200() throws Exception {
        when(incidentReportService.getAllIncidentReports()).thenReturn(List.of(new IncidentReportDto()));

        mockMvc.perform(get("/api/v1/incident-reports/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getIncidentReportById_returns200_whenFound() throws Exception {
        IncidentReportDto dto = new IncidentReportDto();
        dto.setId(1L);
        when(incidentReportService.getIncidentReportById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incident-reports/1/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getIncidentReportById_returns404_whenNotFound() throws Exception {
        when(incidentReportService.getIncidentReportById(999L))
                .thenThrow(new ResourceNotFoundException("Incident report not found with id: 999"));

        mockMvc.perform(get("/api/v1/incident-reports/999/report"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addIncidentReport_returns200_onSuccess() throws Exception {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        request.setDescription("test");
        request.setLatitude(47.5);
        request.setLongitude(21.6);
        request.setIsAnonymous(false);

        IncidentReportDto dto = new IncidentReportDto();
        dto.setId(10L);

        when(incidentReportService.addIncidentReport(any(), eq(5L))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/incident-reports/5/report/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void addIncidentReport_returns429_whenRateLimited() throws Exception {
        AddIncidentReportRequest request = new AddIncidentReportRequest();

        when(incidentReportService.addIncidentReport(any(), eq(5L)))
                .thenThrow(new RateLimitExceededException("Rate limit exceeded. Please wait 3 more minutes."));

        mockMvc.perform(post("/api/v1/incident-reports/5/report/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests()); // adjust if your handler maps this differently
    }

    @Test
    void updateIncidentReport_returns500_whenNotAuthorized() throws Exception {
        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();

        when(incidentReportService.updateIncidentReport(any(), eq(999L), eq(1L)))
                .thenThrow(new ResourceProcessingException("You are not authorized to update this incident report"));

        mockMvc.perform(put("/api/v1/incident-reports/999/report/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // adjust if your handler maps this differently
    }

    @Test
    void deleteIncidentReport_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/incident-reports/5/report/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getNearByIncidentReports_returns200_withQueryParams() throws Exception {
        when(incidentReportService.getNearByIncidentReports(47.5, 21.6, 50.0))
                .thenReturn(List.of(new IncidentReportDto()));

        mockMvc.perform(get("/api/v1/incident-reports/nearby/report")
                        .param("latitude", "47.5")
                        .param("longitude", "21.6")
                        .param("radiusMeters", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void updateStatus_returns400_whenInvalidStatus() throws Exception {
        when(incidentReportService.updateStatus(eq(1L), eq("garbage")))
                .thenThrow(new IllegalArgumentException("Invalid status: garbage"));

        mockMvc.perform(put("/api/v1/incident-reports/1/status/update")
                        .param("status", "garbage"))
                .andExpect(status().isBadRequest()); // adjust if your handler maps this differently
    }
}