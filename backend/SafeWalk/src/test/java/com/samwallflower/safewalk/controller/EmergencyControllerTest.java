package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.EmergencyDto;
import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.service.emergency.IEmergencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmergencyController.class, properties = "api.prefix=/api/v1")
@AutoConfigureMockMvc(addFilters = false)
class EmergencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IEmergencyService emergencyService;

    private EmergencyDto mockEmergencyDto;
    private static final String BASE_URL = "/api/v1/emergency";

    @BeforeEach
    void setUp() {
        mockEmergencyDto = new EmergencyDto();
        mockEmergencyDto.setId(1L);
        mockEmergencyDto.setTriggerSource(EmergencyTriggerSource.MANUAL_SOS);
        mockEmergencyDto.setResolved(false);
    }

    @Test
    void triggerEmergencyByUser_ReturnsOk() throws Exception {
        when(emergencyService.triggerEmergencyByUser(10L, 1L)).thenReturn(mockEmergencyDto);

        mockMvc.perform(post(BASE_URL + "/session/10/user/1/trigger")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Emergency protocol triggered successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.triggerSource").value("MANUAL_SOS"));

        verify(emergencyService).triggerEmergencyByUser(10L, 1L);
    }

    @Test
    void createEmergency_ReturnsOk() throws Exception {
        when(emergencyService.addEmergency(10L, "MANUAL_SOS")).thenReturn(mockEmergencyDto);

        mockMvc.perform(post(BASE_URL + "/session/10/create")
                        .param("source", "MANUAL_SOS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Emergency created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(emergencyService).addEmergency(10L, "MANUAL_SOS");
    }

    @Test
    void getAllEmergencies_ReturnsOk() throws Exception {
        when(emergencyService.getAllEmergencies()).thenReturn(List.of(mockEmergencyDto));

        mockMvc.perform(get(BASE_URL + "/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All emergencies retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(emergencyService).getAllEmergencies();
    }

    @Test
    void getAllEmergenciesByTriggerSource_ReturnsOk() throws Exception {
        when(emergencyService.getAllEmergenciesByTriggerSource("MANUAL_SOS")).thenReturn(List.of(mockEmergencyDto));

        mockMvc.perform(get(BASE_URL + "/by-trigger-source")
                        .param("source", "MANUAL_SOS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Emergencies retrieved successfully for source: MANUAL_SOS"))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(emergencyService).getAllEmergenciesByTriggerSource("MANUAL_SOS");
    }

    @Test
    void getAllEmergenciesByWalkSessionId_ReturnsOk() throws Exception {
        when(emergencyService.getAllEmergenciesByWalkSessionId(10L)).thenReturn(List.of(mockEmergencyDto));

        mockMvc.perform(get(BASE_URL + "/session/10/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Emergencies retrieved successfully for session: 10"))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(emergencyService).getAllEmergenciesByWalkSessionId(10L);
    }

    @Test
    void getEmergencyById_ReturnsOk() throws Exception {
        when(emergencyService.getEmergencyById(1L)).thenReturn(mockEmergencyDto);

        mockMvc.perform(get(BASE_URL + "/1/emergency")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Emergency retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(emergencyService).getEmergencyById(1L);
    }

    @Test
    void resolveEmergency_ReturnsOk() throws Exception {
        mockEmergencyDto.setResolved(true);
        when(emergencyService.resolveEmergency(1L, 10L, 1L)).thenReturn(mockEmergencyDto);

        mockMvc.perform(put(BASE_URL + "/1/emergency/session/10/user/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Emergency resolved successfully"))
                .andExpect(jsonPath("$.data.resolved").value(true));

        verify(emergencyService).resolveEmergency(1L, 10L, 1L);
    }

    @Test
    void getActiveEmergencyByWalkSessionId_ReturnsOk() throws Exception {
        when(emergencyService.getActiveEmergencyByWalkSessionId(10L)).thenReturn(mockEmergencyDto);

        mockMvc.perform(get(BASE_URL + "/session/10/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Active emergency retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(emergencyService).getActiveEmergencyByWalkSessionId(10L);
    }

    @Test
    void countEmergencyByTriggerSource_ReturnsOk() throws Exception {
        when(emergencyService.countEmergencyByTriggerSource("SYSTEM")).thenReturn(5L);

        mockMvc.perform(get(BASE_URL + "/count-by-trigger-source")
                        .param("source", "SYSTEM")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Count of emergencies retrieved successfully for source: SYSTEM"))
                .andExpect(jsonPath("$.data").value(5));

        verify(emergencyService).countEmergencyByTriggerSource("SYSTEM");
    }
}