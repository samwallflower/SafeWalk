package com.samwallflower.safewalk.service.user;

import com.samwallflower.safewalk.dto.UserDto;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.model.Role;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.RoleRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.request.auth.UserRegisterRequest;
import com.samwallflower.safewalk.request.user.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException("User not found with id: " + userId));
        return convertToDto(user);
    }

    @Override
    public UserDto createUser(UserRegisterRequest user) {
        return Optional.of(user)
                .filter(u -> !userRepository.existsByEmail(u.getEmail()))
                .map(req -> {
                    Role role = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new ResourceNotFoundException("Role : ROLE_USER not found"));
                    User newUser = new User();
                    newUser.setFirstName(req.getFirstName());
                    newUser.setLastName(req.getLastName());
                    newUser.setEmail(req.getEmail());
                    newUser.setPassword(req.getPassword()); // In a real application, you should hash the password
                    newUser.setRoles(Set.of(role));
                    return convertToDto(userRepository.save(newUser));
                })
                .orElseThrow(() -> new ResourceAlreadyExistsException("User with email " + user.getEmail() + " already exists."));

    }

    @Override
    public UserDto updateUser(Long userId, UserUpdateRequest user) {

        return userRepository.findById(userId)
                .map(existingUser -> {
                    Optional.ofNullable(user.getFirstName()).ifPresent(existingUser::setFirstName);
                    Optional.ofNullable(user.getLastName()).ifPresent(existingUser::setLastName);
                    Optional.ofNullable(user.getPhoneNumber()).ifPresent(existingUser::setPhoneNumber);
                    User updatedUser = userRepository.save(existingUser);
                    return convertToDto(updatedUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(userRepository::delete, () -> {
                    throw new ResourceNotFoundException("User not found with id: " + userId);
                });
    }

    @Override
    public UserDto setPhoneNumber(Long userId, String phoneNumber) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setPhoneNumber(phoneNumber);
                    User updatedUser = userRepository.save(existingUser);
                    return convertToDto(updatedUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

}
