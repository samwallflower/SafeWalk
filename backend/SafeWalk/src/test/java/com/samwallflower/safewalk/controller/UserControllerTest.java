package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.UserDto;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.request.auth.UserRegisterRequest;
import com.samwallflower.safewalk.request.user.UserUpdateRequest;
import com.samwallflower.safewalk.service.user.IUserService;
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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IUserService userService;

    private UserRegisterRequest validRegisterRequest() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        return request;
    }

    // ---------- getUserById ----------

    @Test
    void getUserById_returns200_whenFound() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setEmail("test@example.com");

        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void getUserById_returns404_whenNotFound() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/v1/users/999/user"))
                .andExpect(status().isNotFound());
    }

    // ---------- createUser (register) ----------

    @Test
    void createUser_returns200_onSuccess() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");

        when(userService.createUser(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void createUser_returns409_whenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new ResourceAlreadyExistsException("User with email test@example.com already exists."));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_returns400_whenEmailBlank() throws Exception {
        UserRegisterRequest request = validRegisterRequest();
        request.setEmail("");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns400_whenEmailMalformed() throws Exception {
        UserRegisterRequest request = validRegisterRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns400_whenPasswordTooShort() throws Exception {
        UserRegisterRequest request = validRegisterRequest();
        request.setPassword("short1");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns400_whenPasswordMissingDigit() throws Exception {
        UserRegisterRequest request = validRegisterRequest();
        request.setPassword("onlyletters");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns400_whenFirstNameBlank() throws Exception {
        UserRegisterRequest request = validRegisterRequest();
        request.setFirstName("");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---------- updateUser ----------

    @Test
    void updateUser_returns200_onSuccess() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");

        UserDto dto = new UserDto();
        dto.setFirstName("Updated");

        when(userService.updateUser(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"));
    }

    @Test
    void updateUser_returns200_whenAllFieldsOmitted() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(); // fully empty — valid partial update

        UserDto dto = new UserDto();
        when(userService.updateUser(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_returns400_whenPhoneNumberInvalid() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPhoneNumber("not-a-phone!!!");

        mockMvc.perform(put("/api/v1/users/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_returns404_whenNotFound() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("New Name");

        when(userService.updateUser(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(put("/api/v1/users/999/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ---------- deleteUser ----------

    @Test
    void deleteUser_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteUser_returns404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("User not found with id: 999"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/v1/users/999/delete"))
                .andExpect(status().isNotFound());
    }

    // ---------- setPhoneNumber ----------

    @Test
    void setPhoneNumber_returns200_onSuccess() throws Exception {
        UserDto dto = new UserDto();
        dto.setPhoneNumber("555-1234");

        when(userService.setPhoneNumber(1L, "555-1234")).thenReturn(dto);

        mockMvc.perform(post("/api/v1/users/1/phoneNumber")
                        .param("phoneNumber", "555-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phoneNumber").value("555-1234"));
    }

    @Test
    void setPhoneNumber_returns404_whenUserNotFound() throws Exception {
        when(userService.setPhoneNumber(999L, "555-1234"))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(post("/api/v1/users/999/phoneNumber")
                        .param("phoneNumber", "555-1234"))
                .andExpect(status().isNotFound());
    }

    // ---------- getAllUsers ----------

    @Test
    void getAllUsers_returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(new UserDto(), new UserDto()));

        mockMvc.perform(get("/api/v1/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void setPhoneNumber_returns400_whenPhoneNumberInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/users/1/phoneNumber")
                        .param("phoneNumber", "not-a-phone!!!"))
                .andExpect(status().isBadRequest());
    }
}