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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, roleRepository, new ModelMapper());
    }

    private User buildUser(Long id, String email, String firstName, String lastName) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        return u;
    }

    // ---------- getUserById ----------

    @Test
    void getUserById_returnsDto_whenFound() {
        User user = buildUser(1L, "test@example.com", "Jane", "Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = service.getUserById(1L);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_throws_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- createUser ----------

    @Test
    void createUser_success_whenEmailNotTaken() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = service.createUser(request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_throws_whenEmailAlreadyExists() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("taken@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(roleRepository);
    }

    @Test
    void createUser_throws_whenDefaultRoleMissing() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ROLE_USER");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_assignsDefaultRole() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getRoles()).containsExactly(userRole); // verified before returning
            return u;
        });

        service.createUser(request);

        verify(userRepository).save(any(User.class));
    }

    // ---------- updateUser ----------

    @Test
    void updateUser_updatesOnlyProvidedFields() {
        User existing = buildUser(1L, "test@example.com", "old first", "old last");
        existing.setPhoneNumber("111");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("new first");
        // lastName and phoneNumber intentionally left null

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = service.updateUser(1L, request);

        assertThat(result.getFirstName()).isEqualTo("new first");
        assertThat(existing.getLastName()).isEqualTo("old last"); // untouched
        assertThat(existing.getPhoneNumber()).isEqualTo("111"); // untouched
    }

    @Test
    void updateUser_updatesAllFields_whenAllProvided() {
        User existing = buildUser(1L, "test@example.com", "old first", "old last");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("new first");
        request.setLastName("new last");
        request.setPhoneNumber("999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = service.updateUser(1L, request);

        assertThat(result.getFirstName()).isEqualTo("new first");
        assertThat(result.getLastName()).isEqualTo("new last");
        assertThat(result.getPhoneNumber()).isEqualTo("999");
    }

    @Test
    void updateUser_throws_whenNotFound() {
        UserUpdateRequest request = new UserUpdateRequest();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    // ---------- deleteUser ----------

    @Test
    void deleteUser_success_whenFound() {
        User existing = buildUser(1L, "test@example.com", "Jane", "Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteUser(1L);

        verify(userRepository).delete(existing);
    }

    @Test
    void deleteUser_throws_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    // ---------- setPhoneNumber ----------

    @Test
    void setPhoneNumber_success() {
        User existing = buildUser(1L, "test@example.com", "Jane", "Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = service.setPhoneNumber(1L, "555-1234");

        assertThat(result.getPhoneNumber()).isEqualTo("555-1234");
    }

    @Test
    void setPhoneNumber_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setPhoneNumber(99L, "555-1234"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- getUserByEmail ----------

    @Test
    void getUserByEmail_returnsUser_whenFound() {
        User user = buildUser(1L, "test@example.com", "Jane", "Doe");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = service.getUserByEmail("test@example.com");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserByEmail_throws_whenNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByEmail("ghost@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost@example.com");
    }

    // ---------- getAllUsers ----------

    @Test
    void getAllUsers_returnsMappedList() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, "a@example.com", "A", "One"),
                buildUser(2L, "b@example.com", "B", "Two")
        ));

        List<UserDto> result = service.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("a@example.com", "b@example.com");
    }
}