package com.samwallflower.safewalk.service.emergencycontact;

import com.samwallflower.safewalk.dto.EmergencyContactDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.model.EmergencyContact;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.EmergencyContactRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.request.emergencycontact.AddEmergencyContactRequest;
import com.samwallflower.safewalk.request.emergencycontact.UpdateEmergencyContactRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactServiceTest {

    @Mock private EmergencyContactRepository emergencyContactRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private EmergencyContactService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyContactService(emergencyContactRepository, userRepository, new ModelMapper());
    }

    private User buildUser(Long id, List<EmergencyContact> contacts) {
        User u = new User();
        u.setId(id);
        u.setEmergencyContacts(contacts);
        return u;
    }

    private EmergencyContact buildContact(Long id, User owner, String name, String phone) {
        EmergencyContact c = new EmergencyContact();
        c.setId(id);
        c.setUser(owner);
        c.setContactName(name);
        c.setContactPhone(phone);
        return c;
    }

    // ---------- addEmergencyContact ----------

    @Test
    void addEmergencyContact_throws_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        AddEmergencyContactRequest request = new AddEmergencyContactRequest();

        assertThatThrownBy(() -> service.addEmergencyContact(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void addEmergencyContact_success_whenUnderLimit() {
        User user = buildUser(1L, new ArrayList<>(List.of(
                buildContact(10L, null, "Mom", "111"),
                buildContact(11L, null, "Dad", "222")
        ))); // 2 contacts, under the cap of 5

        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("Best Friend");
        request.setContactPhone("333");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(emergencyContactRepository.save(any(EmergencyContact.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyContactDto result = service.addEmergencyContact(1L, request);

        assertThat(result.getContactName()).isEqualTo("Best Friend");
        verify(emergencyContactRepository).save(any(EmergencyContact.class));
    }

    @Test
    void addEmergencyContact_throws_whenAtLimitOfFive() {
        User user = buildUser(1L, new ArrayList<>(List.of(
                buildContact(1L, null, "A", "1"),
                buildContact(2L, null, "B", "2"),
                buildContact(3L, null, "C", "3"),
                buildContact(4L, null, "D", "4"),
                buildContact(5L, null, "E", "5")
        ))); // exactly 5 — should be rejected (>= 5 check)

        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("F");
        request.setContactPhone("6");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.addEmergencyContact(1L, request))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("already has 5");

        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void addEmergencyContact_success_whenFourContacts_boundaryCheck() {
        // exactly 4 — one below the cap, should succeed (boundary test for >= 5)
        User user = buildUser(1L, new ArrayList<>(List.of(
                buildContact(1L, null, "A", "1"),
                buildContact(2L, null, "B", "2"),
                buildContact(3L, null, "C", "3"),
                buildContact(4L, null, "D", "4")
        )));

        AddEmergencyContactRequest request = new AddEmergencyContactRequest();
        request.setContactName("E");
        request.setContactPhone("5");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(emergencyContactRepository.save(any(EmergencyContact.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyContactDto result = service.addEmergencyContact(1L, request);

        assertThat(result.getContactName()).isEqualTo("E");
    }

    // ---------- getEmergencyContactsByUserId ----------

    @Test
    void getEmergencyContactsByUserId_returnsMappedList() {
        User user = buildUser(1L, List.of(
                buildContact(10L, null, "Mom", "111"),
                buildContact(11L, null, "Dad", "222")
        ));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<EmergencyContactDto> result = service.getEmergencyContactsByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EmergencyContactDto::getContactName)
                .containsExactlyInAnyOrder("Mom", "Dad");
    }

    @Test
    void getEmergencyContactsByUserId_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEmergencyContactsByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getEmergencyContactsByUserId_returnsEmptyList_whenNoContacts() {
        User user = buildUser(1L, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<EmergencyContactDto> result = service.getEmergencyContactsByUserId(1L);

        assertThat(result).isEmpty();
    }

    // ---------- validateOwnership (via getEmergencyContactById / delete / update) ----------

    @Test
    void getEmergencyContactById_success_whenOwnedByUser() {
        User owner = buildUser(1L, null);
        EmergencyContact contact = buildContact(10L, owner, "Mom", "111");

        when(emergencyContactRepository.findById(10L)).thenReturn(Optional.of(contact));

        EmergencyContactDto result = service.getEmergencyContactById(1L, 10L);

        assertThat(result.getContactName()).isEqualTo("Mom");
    }

    @Test
    void getEmergencyContactById_throws_whenContactNotFound() {
        when(emergencyContactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEmergencyContactById(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getEmergencyContactById_throws_whenContactBelongsToDifferentUser() {
        User actualOwner = buildUser(2L, null);
        EmergencyContact contact = buildContact(10L, actualOwner, "Mom", "111");

        when(emergencyContactRepository.findById(10L)).thenReturn(Optional.of(contact));

        // requesting user (1L) does not own this contact (owned by 2L)
        assertThatThrownBy(() -> service.getEmergencyContactById(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("does not belong");
    }

    // ---------- deleteEmergencyContact ----------

    @Test
    void deleteEmergencyContact_success_whenOwnedByUser() {
        User owner = buildUser(1L, null);
        EmergencyContact contact = buildContact(10L, owner, "Mom", "111");

        when(emergencyContactRepository.findById(10L)).thenReturn(Optional.of(contact));

        service.deleteEmergencyContact(1L, 10L);

        verify(emergencyContactRepository).delete(contact);
    }

    @Test
    void deleteEmergencyContact_throws_whenNotOwner() {
        User actualOwner = buildUser(2L, null);
        EmergencyContact contact = buildContact(10L, actualOwner, "Mom", "111");

        when(emergencyContactRepository.findById(10L)).thenReturn(Optional.of(contact));

        assertThatThrownBy(() -> service.deleteEmergencyContact(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emergencyContactRepository, never()).delete(any());
    }

    // ---------- updateEmergencyContact ----------

    @Test
    void updateEmergencyContact_updatesOnlyProvidedFields() {
        User owner = buildUser(1L, null);
        EmergencyContact contact = buildContact(10L, owner, "old name", "old phone");

        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest();
        request.setContactName("new name");
        // contactPhone intentionally left null

        when(emergencyContactRepository.findById(10L)).thenReturn(Optional.of(contact));
        when(emergencyContactRepository.save(any(EmergencyContact.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyContactDto result = service.updateEmergencyContact(1L, 10L, request);

        assertThat(result.getContactName()).isEqualTo("new name");
        assertThat(contact.getContactPhone()).isEqualTo("old phone"); // untouched
    }

    @Test
    void updateEmergencyContact_throws_whenNotOwner() {
        User actualOwner = buildUser(2L, null);
        EmergencyContact contact = buildContact(10L, actualOwner, "name", "phone");

        when(emergencyContactRepository.findById(10L)).thenReturn(Optional.of(contact));

        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest();

        assertThatThrownBy(() -> service.updateEmergencyContact(1L, 10L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emergencyContactRepository, never()).save(any());
    }
}