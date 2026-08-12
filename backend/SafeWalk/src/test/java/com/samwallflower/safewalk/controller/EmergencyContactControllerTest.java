package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.EmergencyContactDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.request.emergencycontact.AddEmergencyContactRequest;
import com.samwallflower.safewalk.request.emergencycontact.UpdateEmergencyContactRequest;
import com.samwallflower.safewalk.service.emergencycontact.IEmergencyContactService;
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

@WebMvcTest(EmergencyContactController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class EmergencyContactControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IEmergencyContactService emergencyContactService;

    @Test
    void getEmergencyContactsByUserId_returns200() throws Exception {
        when(emergencyContactService.getEmergencyContactsByUserId(1L))
                .thenReturn(List.of(new EmergencyContactDto()));

        mockMvc.perform(get("/api/v1/emergency-contacts/1/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getEmergencyContactsByUserId_returns404_whenUserNotFound() throws Exception {
        when(emergencyContactService.getEmergencyContactsByUserId(999L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/v1/emergency-contacts/999/contacts"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmergencyContactById_returns200_whenFound() throws Exception {
        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setId(10L);
        dto.setContactName("Mom");

        when(emergencyContactService.getEmergencyContactById(1L, 10L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/emergency-contacts/1/contacts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactName").value("Mom"));
    }

    @Test
    void getEmergencyContactById_returns404_whenNotOwned() throws Exception {
        when(emergencyContactService.getEmergencyContactById(1L, 10L))
                .thenThrow(new ResourceNotFoundException("Emergency contact with id: 10 does not belong to user with id: 1"));

        mockMvc.perform(get("/api/v1/emergency-contacts/1/contacts/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEmergencyContact_returns200_onSuccess() throws Exception {
        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("Best Friend");
        request.setContactPhone("555-1234");

        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setContactName("Best Friend");

        when(emergencyContactService.addEmergencyContact(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/emergency-contacts/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactName").value("Best Friend"));
    }

    @Test
    void addEmergencyContact_returns500_whenAtContactLimit() throws Exception {
        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("Best Friend");   // valid, non-blank
        request.setContactPhone("555-1234");     // valid, matches pattern

        when(emergencyContactService.addEmergencyContact(eq(1L), any()))
                .thenThrow(new ResourceProcessingException("User with id: 1 already has 5 emergency contacts."));

        mockMvc.perform(post("/api/v1/emergency-contacts/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateEmergencyContact_returns200_onSuccess() throws Exception {
        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest();
        request.setContactName("Updated Name");

        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setContactName("Updated Name");

        when(emergencyContactService.updateEmergencyContact(eq(1L), eq(10L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/emergency-contacts/1/contacts/10/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactName").value("Updated Name"));
    }

    @Test
    void deleteEmergencyContact_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/emergency-contacts/1/contacts/10/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteEmergencyContact_returns404_whenNotOwned() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Emergency contact with id: 10 does not belong to user with id: 1"))
                .when(emergencyContactService).deleteEmergencyContact(1L, 10L);

        mockMvc.perform(delete("/api/v1/emergency-contacts/1/contacts/10/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEmergencyContact_returns400_whenNameBlank() throws Exception {
        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("");
        request.setContactPhone("555-1234");

        mockMvc.perform(post("/api/v1/emergency-contacts/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEmergencyContact_returns400_whenPhoneInvalid() throws Exception {
        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("Mom");
        request.setContactPhone("not-a-phone-number!!!");

        mockMvc.perform(post("/api/v1/emergency-contacts/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmergencyContact_returns200_whenFieldsOmitted() throws Exception {
        // confirms partial update (both fields null) is NOT rejected
        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest();

        EmergencyContactDto dto = new EmergencyContactDto();
        when(emergencyContactService.updateEmergencyContact(eq(1L), eq(10L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/emergency-contacts/1/contacts/10/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateEmergencyContact_returns400_whenPhoneProvidedButInvalid() throws Exception {
        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest();
        request.setContactPhone("!!!");

        mockMvc.perform(put("/api/v1/emergency-contacts/1/contacts/10/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}