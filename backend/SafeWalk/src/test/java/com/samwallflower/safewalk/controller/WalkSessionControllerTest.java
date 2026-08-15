package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.request.walksession.AddWalkSessionRequest;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
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

@WebMvcTest(WalkSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class WalkSessionControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IWalkSessionService walkSessionService;

    private AddWalkSessionRequest validAddRequest() {
        AddWalkSessionRequest request = new AddWalkSessionRequest();
        request.setOriginLatitude(47.4979);
        request.setOriginLongitude(21.6244);
        request.setDestinationLatitude(47.5316);
        request.setDestinationLongitude(21.6273);
        request.setChosenRouteId(10L);
        return request;
    }

    // ---------- startSession ----------

    @Test
    void startSession_returns200_onSuccess() throws Exception {
        WalkSessionDto dto = new WalkSessionDto();
        dto.setId(1L);
        dto.setStatus(SessionStatus.ACTIVE);

        when(walkSessionService.startWalkSessionDto(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/walk-sessions/user/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void startSession_returns400_whenLatitudeMissing() throws Exception {
        AddWalkSessionRequest request = validAddRequest();
        request.setOriginLatitude(null);

        mockMvc.perform(post("/api/v1/walk-sessions/user/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startSession_returns400_whenLatitudeOutOfRange() throws Exception {
        AddWalkSessionRequest request = validAddRequest();
        request.setOriginLatitude(200.0);

        mockMvc.perform(post("/api/v1/walk-sessions/user/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startSession_returns429_whenPriorSessionActive() throws Exception {
        when(walkSessionService.startWalkSessionDto(eq(1L), any()))
                .thenThrow(new RateLimitExceededException("Please complete previous session with id: 5"));

        mockMvc.perform(post("/api/v1/walk-sessions/user/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddRequest())))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void startSession_returns404_whenRouteNotFound() throws Exception {
        when(walkSessionService.startWalkSessionDto(eq(1L), any()))
                .thenThrow(new ResourceNotFoundException("Route not found with id: 10"));

        mockMvc.perform(post("/api/v1/walk-sessions/user/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddRequest())))
                .andExpect(status().isNotFound());
    }

    // ---------- updateWalkSessionLocation ----------

    @Test
    void updateLocation_returns200_onSuccess() throws Exception {
        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(47.51);
        request.setLongitude(21.63);

        WalkSessionDto dto = new WalkSessionDto();
        dto.setLastKnownLatitude(47.51);

        when(walkSessionService.updateLocation(eq(5L), eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/user/1/update/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastKnownLatitude").value(47.51));
    }

    @Test
    void updateLocation_returns400_whenLatitudeMissing() throws Exception {
        UpdateWalkSession request = new UpdateWalkSession();
        request.setLongitude(21.63);
        // latitude left null

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/user/1/update/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLocation_returns500_whenSessionAlreadyCompleted() throws Exception {
        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(47.51);
        request.setLongitude(21.63);

        when(walkSessionService.updateLocation(eq(5L), eq(1L), any()))
                .thenThrow(new ResourceProcessingException("WalkSession already completed with id: 5"));

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/user/1/update/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ---------- delete ----------

    @Test
    void deleteWalkSession_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/walk-sessions/5/session/user/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteWalkSession_returns500_whenNotOwner() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceProcessingException("Walk session with id: 5 does not belong to user with id: 999"))
                .when(walkSessionService).deleteWalkSessionById(5L, 999L);

        mockMvc.perform(delete("/api/v1/walk-sessions/5/session/user/999/delete"))
                .andExpect(status().isInternalServerError());
    }

    // ---------- end session ----------

    @Test
    void endWalkSessionById_returns200_onSuccess() throws Exception {
        WalkSessionDto dto = new WalkSessionDto();
        dto.setStatus(SessionStatus.COMPLETED);

        when(walkSessionService.endSessionById(5L)).thenReturn(dto);

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void endWalkSessionByIdAndUserId_returns500_whenNotOwner() throws Exception {
        when(walkSessionService.endSessionByIdAndUserId(5L, 999L))
                .thenThrow(new ResourceProcessingException("You are not allowed to end this session."));

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/user/999/end"))
                .andExpect(status().isInternalServerError());
    }

    // ---------- update status (admin) ----------

    @Test
    void updateWalkSessionStatus_returns200_onSuccess() throws Exception {
        WalkSessionDto dto = new WalkSessionDto();
        dto.setStatus(SessionStatus.EMERGENCY);

        when(walkSessionService.updateWalkSessionStatus(5L, "emergency")).thenReturn(dto);

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/update/by-status")
                        .param("status", "emergency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EMERGENCY"));
    }

    @Test
    void updateWalkSessionStatus_returns400_whenStatusInvalid() throws Exception {
        when(walkSessionService.updateWalkSessionStatus(5L, "garbage"))
                .thenThrow(new IllegalArgumentException("Invalid session status garbage"));

        mockMvc.perform(put("/api/v1/walk-sessions/5/session/update/by-status")
                        .param("status", "garbage"))
                .andExpect(status().isBadRequest());
    }

    // ---------- reads ----------

    @Test
    void getAllWalkSessions_returns200() throws Exception {
        when(walkSessionService.getAllWalkSession()).thenReturn(List.of(new WalkSessionDto()));

        mockMvc.perform(get("/api/v1/walk-sessions/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getWalkSessionById_returns404_whenNotFound() throws Exception {
        when(walkSessionService.getWalkSessionDtoById(999L))
                .thenThrow(new ResourceNotFoundException("WalkSession not found with id: 999"));

        mockMvc.perform(get("/api/v1/walk-sessions/999/session"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWalkSessionsByUserId_returns200() throws Exception {
        when(walkSessionService.getWalkSessionsByUserId(1L)).thenReturn(List.of(new WalkSessionDto()));

        mockMvc.perform(get("/api/v1/walk-sessions/user/1/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getWalkSessionsByStatus_returns200() throws Exception {
        when(walkSessionService.getWalkSessionByStatus("active")).thenReturn(List.of(new WalkSessionDto()));

        mockMvc.perform(get("/api/v1/walk-sessions/by-status/session")
                        .param("status", "active"))
                .andExpect(status().isOk());
    }

    @Test
    void getWalkSessionsByRouteId_returns200() throws Exception {
        when(walkSessionService.getWalkSessionByRouteId(10L)).thenReturn(List.of(new WalkSessionDto()));

        mockMvc.perform(get("/api/v1/walk-sessions/route/10/session"))
                .andExpect(status().isOk());
    }
}