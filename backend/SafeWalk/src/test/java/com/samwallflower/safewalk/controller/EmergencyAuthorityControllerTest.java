package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.request.emergencyauthority.AddEmergencyAuthorityRequest;
import com.samwallflower.safewalk.request.emergencyauthority.UpdateEmergencyAuthorityRequest;
import com.samwallflower.safewalk.service.emergencyauthority.IEmergencyAuthorityService;
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

@WebMvcTest(EmergencyAuthorityController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class EmergencyAuthorityControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IEmergencyAuthorityService emergencyAuthorityService;

    private AddEmergencyAuthorityRequest validAddRequest() {
        AddEmergencyAuthorityRequest request = new AddEmergencyAuthorityRequest();
        request.setCountryCode("HU");
        request.setCountryName("Hungary");
        request.setPoliceNumber("107");
        request.setAmbulanceNumber("104");
        request.setGeneralEmergencyNumber("112");
        return request;
    }

    // ---------- addEmergencyAuthority ----------

    @Test
    void addEmergencyAuthority_returns200_onSuccess() throws Exception {
        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setCountryCode("HU");

        when(emergencyAuthorityService.addEmergencyAuthority(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/emergency-authority/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.countryCode").value("HU"));
    }

    @Test
    void addEmergencyAuthority_returns409_whenAlreadyExists() throws Exception {
        when(emergencyAuthorityService.addEmergencyAuthority(any()))
                .thenThrow(new ResourceAlreadyExistsException("Emergency Authority already exists"));

        mockMvc.perform(post("/api/v1/emergency-authority/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void addEmergencyAuthority_returns400_whenCountryCodeBlank() throws Exception {
        AddEmergencyAuthorityRequest request = validAddRequest();
        request.setCountryCode("");

        mockMvc.perform(post("/api/v1/emergency-authority/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---------- updateEmergencyAuthority ----------

    @Test
    void updateEmergencyAuthority_returns200_onSuccess() throws Exception {
        UpdateEmergencyAuthorityRequest request = new UpdateEmergencyAuthorityRequest();
        request.setPoliceNumber("999");

        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setPoliceNumber("999");

        when(emergencyAuthorityService.updateEmergencyAuthority(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/emergency-authority/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.policeNumber").value("999"));
    }

    @Test
    void updateEmergencyAuthority_returns404_whenNotFound() throws Exception {
        UpdateEmergencyAuthorityRequest request = new UpdateEmergencyAuthorityRequest();

        when(emergencyAuthorityService.updateEmergencyAuthority(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Emergency Authority Not Found with id: 999"));

        mockMvc.perform(put("/api/v1/emergency-authority/999/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ---------- delete ----------

    @Test
    void deleteEmergencyAuthority_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/emergency-authority/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteEmergencyAuthority_returns404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Emergency Authority Not Found with id: 999"))
                .when(emergencyAuthorityService).deleteEmergencyAuthority(999L);

        mockMvc.perform(delete("/api/v1/emergency-authority/999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmergencyAuthorityByCountryName_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/emergency-authority/authority/by-country-name/delete")
                        .param("countryName", "Hungary"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEmergencyAuthorityByCountryName_returns400_whenCountryNameBlank() throws Exception {
        mockMvc.perform(delete("/api/v1/emergency-authority/authority/by-country-name/delete")
                        .param("countryName", ""))
                .andExpect(status().isBadRequest());
    }

    // ---------- reads ----------

    @Test
    void getAllEmergencyAuthorities_returns200() throws Exception {
        when(emergencyAuthorityService.getAllEmergencyAuthorities())
                .thenReturn(List.of(new EmergencyAuthorityDto()));

        mockMvc.perform(get("/api/v1/emergency-authority/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getEmergencyAuthorityById_returns200_whenFound() throws Exception {
        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setId(1L);

        when(emergencyAuthorityService.findEmergencyAuthorityById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency-authority/1/authority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getEmergencyAuthorityById_returns404_whenNotFound() throws Exception {
        when(emergencyAuthorityService.findEmergencyAuthorityById(999L))
                .thenThrow(new ResourceNotFoundException("Emergency Authority Not Found with id : 999"));

        mockMvc.perform(get("/api/v1/emergency-authority/999/authority"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmergencyAuthorityByCountryName_returns200() throws Exception {
        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setCountryName("Hungary");

        when(emergencyAuthorityService.findByCountryName("Hungary")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency-authority/authority/by-country-name")
                        .param("countryName", "Hungary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.countryName").value("Hungary"));
    }

    @Test
    void getEmergencyAuthorityByCountryCode_returns200() throws Exception {
        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setCountryCode("HU");

        when(emergencyAuthorityService.findByCountryCode("HU")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency-authority/authority/by-country-code")
                        .param("countryCode", "HU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.countryCode").value("HU"));
    }

    @Test
    void getEmergencyAuthorityByCountryCode_returns400_whenLengthInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/emergency-authority/authority/by-country-code")
                        .param("countryCode", "HUN")) // 3 chars, violates @Size(min=2,max=2)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmergencyAuthorityByLocation_returns200() throws Exception {
        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setCountryCode("HU");

        when(emergencyAuthorityService.findEmergencyAuthorityByLocation(47.5, 21.6)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency-authority/authority/by-location")
                        .param("latitude", "47.5")
                        .param("longitude", "21.6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.countryCode").value("HU"));
    }

    @Test
    void getEmergencyAuthorityByLocation_returns404_whenCountryNotSeeded() throws Exception {
        when(emergencyAuthorityService.findEmergencyAuthorityByLocation(48.8, 2.3))
                .thenThrow(new ResourceNotFoundException("No emergency authority data found for country: FR"));

        mockMvc.perform(get("/api/v1/emergency-authority/authority/by-location")
                        .param("latitude", "48.8")
                        .param("longitude", "2.3"))
                .andExpect(status().isNotFound());
    }
}