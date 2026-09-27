package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.EmergencyDto;
import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.service.emergency.IEmergencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmergencyController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class EmergencyControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IEmergencyService emergencyService;

    // ---------- triggerEmergencyByUser ----------

    @Test
    void triggerEmergencyByUser_returns200_onSuccess() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(1L);
        dto.setTriggerSource(EmergencyTriggerSource.MANUAL_SOS);

        when(emergencyService.triggerEmergencyByUser(10L, 1L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/emergency/session/10/user/1/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.triggerSource").value("MANUAL_SOS"));
    }

    @Test
    void triggerEmergencyByUser_returns500_whenNotOwner() throws Exception {
        when(emergencyService.triggerEmergencyByUser(10L, 999L))
                .thenThrow(new ResourceProcessingException("Walk session with id: 10 does not belong to the user with id: 999"));

        mockMvc.perform(post("/api/v1/emergency/session/10/user/999/trigger"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void triggerEmergencyByUser_returns404_whenSessionNotFound() throws Exception {
        when(emergencyService.triggerEmergencyByUser(999L, 1L))
                .thenThrow(new ResourceNotFoundException("Walk session not found with id: 999"));

        mockMvc.perform(post("/api/v1/emergency/session/999/user/1/trigger"))
                .andExpect(status().isNotFound());
    }

    // ---------- createEmergency (dev) ----------

    @Test
    void createEmergency_returns200_onSuccess() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(1L);

        when(emergencyService.addEmergency(eq(10L), eq("MANUAL_SOS"))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/emergency/session/10/create")
                        .param("source", "MANUAL_SOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createEmergency_returns400_whenSourceInvalid() throws Exception {
        when(emergencyService.addEmergency(eq(10L), eq("GARBAGE")))
                .thenThrow(new IllegalArgumentException("Unknown emergency trigger source: GARBAGE"));

        mockMvc.perform(post("/api/v1/emergency/session/10/create")
                        .param("source", "GARBAGE"))
                .andExpect(status().isBadRequest());
    }

    // ---------- updateEmergencyResolved ----------

    @Test
    void updateEmergencyResolved_returns200_onSuccess() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(1L);
        dto.setResolved(true);

        when(emergencyService.updateEmergencyResolveById(1L, true)).thenReturn(dto);

        mockMvc.perform(put("/api/v1/emergency/1/update")
                        .param("resolved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolved").value(true));
    }

    @Test
    void updateEmergencyResolved_returns404_whenNotFound() throws Exception {
        when(emergencyService.updateEmergencyResolveById(999L, true))
                .thenThrow(new ResourceNotFoundException("Emergency not found with id: 999"));

        mockMvc.perform(put("/api/v1/emergency/999/update")
                        .param("resolved", "true"))
                .andExpect(status().isNotFound());
    }

    // ---------- deleteEmergency ----------

    @Test
    void deleteEmergency_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/emergency/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteEmergency_returns404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Emergency not found with id: 999"))
                .when(emergencyService).deleteEmergencyById(999L);

        mockMvc.perform(delete("/api/v1/emergency/999/delete"))
                .andExpect(status().isNotFound());
    }

    // ---------- getAllEmergencies ----------

    @Test
    void getAllEmergencies_returns200() throws Exception {
        when(emergencyService.getAllEmergencies()).thenReturn(List.of(new EmergencyDto(), new EmergencyDto()));

        mockMvc.perform(get("/api/v1/emergency/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ---------- getAllEmergenciesByTriggerSource ----------

    @Test
    void getAllEmergenciesByTriggerSource_returns200() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setTriggerSource(EmergencyTriggerSource.IDLE_TIMEOUT);

        when(emergencyService.getAllEmergenciesByTriggerSource("IDLE_TIMEOUT")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/emergency/by-trigger-source")
                        .param("source", "IDLE_TIMEOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].triggerSource").value("IDLE_TIMEOUT"));
    }

    @Test
    void getAllEmergenciesByTriggerSource_returns400_whenSourceInvalid() throws Exception {
        when(emergencyService.getAllEmergenciesByTriggerSource("GARBAGE"))
                .thenThrow(new IllegalArgumentException("Unknown emergency trigger source: GARBAGE"));

        mockMvc.perform(get("/api/v1/emergency/by-trigger-source")
                        .param("source", "GARBAGE"))
                .andExpect(status().isBadRequest());
    }

    // ---------- getAllEmergenciesByWalkSessionId ----------

    @Test
    void getAllEmergenciesByWalkSessionId_returns200() throws Exception {
        when(emergencyService.getAllEmergenciesByWalkSessionId(10L)).thenReturn(List.of(new EmergencyDto()));

        mockMvc.perform(get("/api/v1/emergency/session/10/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ---------- getEmergencyById ----------

    @Test
    void getEmergencyById_returns200_whenFound() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(5L);

        when(emergencyService.getEmergencyById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency/5/emergency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void getEmergencyById_returns404_whenNotFound() throws Exception {
        when(emergencyService.getEmergencyById(999L))
                .thenThrow(new ResourceNotFoundException("Emergency not found with id: 999"));

        mockMvc.perform(get("/api/v1/emergency/999/emergency"))
                .andExpect(status().isNotFound());
    }

    // ---------- resolveEmergency ----------

    @Test
    void resolveEmergency_returns200_onSuccess() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(5L);
        dto.setResolved(true);

        when(emergencyService.resolveEmergency(5L, 10L, 1L)).thenReturn(dto);

        mockMvc.perform(put("/api/v1/emergency/5/emergency/session/10/user/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolved").value(true));
    }

    @Test
    void resolveEmergency_returns500_whenAlreadyResolved() throws Exception {
        when(emergencyService.resolveEmergency(5L, 10L, 1L))
                .thenThrow(new ResourceProcessingException("Emergency with id: 5 is already resolved."));

        mockMvc.perform(put("/api/v1/emergency/5/emergency/session/10/user/1/resolve"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void resolveEmergency_returns404_whenEmergencyNotFound() throws Exception {
        when(emergencyService.resolveEmergency(999L, 10L, 1L))
                .thenThrow(new ResourceNotFoundException("Emergency not found with id: 999"));

        mockMvc.perform(put("/api/v1/emergency/999/emergency/session/10/user/1/resolve"))
                .andExpect(status().isNotFound());
    }

    // ---------- getActiveEmergencyByWalkSessionId ----------

    @Test
    void getActiveEmergencyByWalkSessionId_returns200_whenFound() throws Exception {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(5L);
        dto.setResolved(false);

        when(emergencyService.getActiveEmergencyByWalkSessionId(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency/session/10/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void getActiveEmergencyByWalkSessionId_returns404_whenNoneActive() throws Exception {
        when(emergencyService.getActiveEmergencyByWalkSessionId(10L))
                .thenThrow(new ResourceNotFoundException("No active emergency found for walk session with id: 10"));

        mockMvc.perform(get("/api/v1/emergency/session/10/active"))
                .andExpect(status().isNotFound());
    }

    // ---------- countEmergencyByTriggerSource ----------

    @Test
    void countEmergencyByTriggerSource_returns200() throws Exception {
        when(emergencyService.countEmergencyByTriggerSource("idle_timeout")).thenReturn(7L);

        mockMvc.perform(get("/api/v1/emergency/count-by-trigger-source")
                        .param("source", "idle_timeout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(7));
    }

    @Test
    void countEmergencyByTriggerSource_returns400_whenSourceInvalid() throws Exception {
        when(emergencyService.countEmergencyByTriggerSource("garbage"))
                .thenThrow(new IllegalArgumentException("Unknown emergency trigger source: garbage"));

        mockMvc.perform(get("/api/v1/emergency/count-by-trigger-source")
                        .param("source", "garbage"))
                .andExpect(status().isBadRequest());
    }
}