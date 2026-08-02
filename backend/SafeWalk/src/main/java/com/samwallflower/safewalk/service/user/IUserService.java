package com.samwallflower.safewalk.service.user;

import com.samwallflower.safewalk.dto.UserDto;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.request.auth.UserRegisterRequest;
import com.samwallflower.safewalk.request.user.UserUpdateRequest;

import java.util.List;

public interface IUserService {
    UserDto getUserById(Long userId);
    UserDto createUser(UserRegisterRequest user);
    UserDto updateUser(Long userId, UserUpdateRequest user);
    void deleteUser(Long userId);
    UserDto setPhoneNumber(Long userId, String phoneNumber);
    User getUserByEmail(String email);

    List<UserDto> getAllUsers();
}
