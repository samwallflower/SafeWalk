package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.IncidentReportDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class IncidentReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IIncidentReportService incidentReportService;

    @Test
    void getAllIncidentReports_returns200() throws Exception {
        IncidentReportDto dto = new IncidentReportDto();
        dto.setId(1L);

        when(incidentReportService.getAllIncidentReports()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/incident-reports/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.message").value("Incident reports retrieved successfully"));
    }

    @Test
    void getIncidentReportById_returns200() throws Exception {
        IncidentReportDto dto = new IncidentReportDto();
        dto.setId(5L);

        when(incidentReportService.getIncidentReportById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incident-reports/5/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void addIncidentReport_returns200() throws Exception {
        AddIncidentReportRequest request = new AddIncidentReportRequest();
        request.setDescription("Test incident");

        IncidentReportDto responseDto = new IncidentReportDto();
        responseDto.setId(10L);
        responseDto.setDescription("Test incident");

        when(incidentReportService.addIncidentReport(any(AddIncidentReportRequest.class), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/incident-reports/1/report/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.description").value("Test incident"));
    }

    @Test
    void updateIncidentReport_returns200() throws Exception {
        UpdateIncidentReportRequest request = new UpdateIncidentReportRequest();
        request.setDescription("Updated description");

        IncidentReportDto responseDto = new IncidentReportDto();
        responseDto.setId(2L);
        responseDto.setDescription("Updated description");

        when(incidentReportService.updateIncidentReport(any(UpdateIncidentReportRequest.class), eq(1L), eq(2L)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/incident-reports/1/report/2/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated description"));
    }

    @Test
    void deleteIncidentReport_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/incident-reports/1/report/2/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Incident report deleted successfully"));

        verify(incidentReportService).deleteIncidentReportById(2L, 1L);
    }
}