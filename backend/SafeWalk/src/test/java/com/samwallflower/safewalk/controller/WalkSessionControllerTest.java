package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.request.walksession.AddWalkSessionRequest;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalkSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
public class WalkSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IWalkSessionService walkSessionService;

    @Value("${api.prefix:/api/v1}")
    private String apiPrefix;

    private WalkSessionDto walkSessionDto;

    @BeforeEach
    void setUp() {
        walkSessionDto = new WalkSessionDto();
        walkSessionDto.setId(100L);
        // Set other necessary fields on Dto if needed for validation
    }

    @Test
    void startSession_ReturnsOk() throws Exception {
        // Arrange
        AddWalkSessionRequest request = new AddWalkSessionRequest();
        request.setChosenRouteId(10L);
        request.setOriginLatitude(40.7128);
        request.setOriginLongitude(-74.0060);
        request.setDestinationLatitude(40.7306);
        request.setDestinationLongitude(-73.9352);

        when(walkSessionService.startWalkSessionDto(eq(1L), any(AddWalkSessionRequest.class)))
                .thenReturn(walkSessionDto);

        // Act & Assert
        mockMvc.perform(post(apiPrefix + "/walk-sessions/user/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("WalkSession started successfully"))
                .andExpect(jsonPath("$.data.id").value(100L));
    }

    @Test
    void updateWalkSessionLocation_ReturnsOk() throws Exception {
        // Arrange
        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(40.7130);
        request.setLongitude(-74.0065);

        when(walkSessionService.updateLocation(eq(100L), eq(1L), any(UpdateWalkSession.class)))
                .thenReturn(walkSessionDto);

        // Act & Assert
        mockMvc.perform(put(apiPrefix + "/walk-sessions/100/session/user/1/update/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("WalkSession location updated successfully"))
                .andExpect(jsonPath("$.data.id").value(100L));
    }

    @Test
    void deleteWalkSession_ReturnsOk() throws Exception {
        // Arrange
        doNothing().when(walkSessionService).deleteWalkSessionById(100L, 1L);

        // Act & Assert
        mockMvc.perform(delete(apiPrefix + "/walk-sessions/100/session/user/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("WalkSession deleted successfully"));

        verify(walkSessionService, times(1)).deleteWalkSessionById(100L, 1L);
    }

    @Test
    void getAllWalkSessions_ReturnsOk() throws Exception {
        // Arrange
        when(walkSessionService.getAllWalkSession()).thenReturn(Collections.singletonList(walkSessionDto));

        // Act & Assert
        mockMvc.perform(get(apiPrefix + "/walk-sessions/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("WalkSessions found successfully"))
                .andExpect(jsonPath("$.data[0].id").value(100L));
    }

    @Test
    void getWalkSessionById_ReturnsOk() throws Exception {
        // Arrange
        when(walkSessionService.getWalkSessionDtoById(100L)).thenReturn(walkSessionDto);

        // Act & Assert
        mockMvc.perform(get(apiPrefix + "/walk-sessions/100/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("WalkSession found successfully"))
                .andExpect(jsonPath("$.data.id").value(100L));
    }
}