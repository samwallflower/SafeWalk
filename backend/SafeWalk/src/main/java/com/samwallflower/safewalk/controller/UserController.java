package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.UserDto;
import com.samwallflower.safewalk.request.user.UserRegisterRequest;
import com.samwallflower.safewalk.request.user.UserUpdateRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.user.IUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@EnableMethodSecurity
@EnableWebSecurity
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/{userId}/user")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(new ApiResponse("User retrieved successfully", userDto));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    @PostMapping("{userId}/phoneNumber")
    public ResponseEntity<ApiResponse> setPhoneNumber(@PathVariable Long userId,
                                                      @RequestParam
                                                      @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid phone number")
                                                      String phoneNumber) {
        UserDto userDto = userService.setPhoneNumber(userId, phoneNumber);
        return ResponseEntity.ok(new ApiResponse("Phone number updated successfully", userDto));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDto> userDtos = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse("Users retrieved successfully", userDtos));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserRegisterRequest userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.ok(new ApiResponse("User created successfully", createdUser));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        UserDto updatedUser = userService.updateUser(userId, userUpdateRequest);
        return ResponseEntity.ok(new ApiResponse("User updated successfully", updatedUser));
    }
}
