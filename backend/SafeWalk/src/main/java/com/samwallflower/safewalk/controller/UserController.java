package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.UserDto;
import com.samwallflower.safewalk.request.auth.UserRegisterRequest;
import com.samwallflower.safewalk.request.user.UserUpdateRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("{userId}/phoneNumber")
    public ResponseEntity<ApiResponse> setPhoneNumber(@PathVariable Long userId, @RequestParam String phoneNumber) {
        UserDto userDto = userService.setPhoneNumber(userId, phoneNumber);
        return ResponseEntity.ok(new ApiResponse("Phone number updated successfully", userDto));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDto> userDtos = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse("Users retrieved successfully", userDtos));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserRegisterRequest userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.ok(new ApiResponse("User created successfully", createdUser));
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest userUpdateRequest) {
        UserDto updatedUser = userService.updateUser(userId, userUpdateRequest);
        return ResponseEntity.ok(new ApiResponse("User updated successfully", updatedUser));
    }
}
