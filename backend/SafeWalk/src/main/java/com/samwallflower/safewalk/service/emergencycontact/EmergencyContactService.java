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
import com.samwallflower.safewalk.security.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class EmergencyContactService implements IEmergencyContactService{
    private final EmergencyContactRepository emergencyContactRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final int MAX_EMERGENCY_CONTACTS=5;

    // before we add emergency contacts we must make sure the user has less than 5 contacts
    // otherwise someone might add 100 emergency contacts and spam the system
    // we don't want that
    // since emergency contact also affects user we use transactional
    @Override
    @Transactional
    public EmergencyContactDto addEmergencyContact(Long userId, AddEmergencyContactRequest emergencyContact) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + userId));

        SecurityUtils.checkOwnershipOrAdmin(userId);

        List<EmergencyContact> userContacts = user.getEmergencyContacts();

        if (userContacts.size() >= MAX_EMERGENCY_CONTACTS) {
            throw new ResourceProcessingException("User with id: " + userId + " already has 5 emergency contacts. Cannot add more. Try deleting some first or updating existing ones.");
        }

        EmergencyContact contact = new EmergencyContact();
        contact.setContactName(emergencyContact.getContactName());
        Optional.ofNullable(emergencyContact.getContactPhone()).ifPresent(contact::setContactPhone);
        contact.setContactEmail(emergencyContact.getContactEmail());
        contact.setUser(user);

        return convertToDto(emergencyContactRepository.save(contact));
    }

    @Override
    public List<EmergencyContactDto> getEmergencyContactsByUserId(Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + userId));
        return user.getEmergencyContacts()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // before we delete we must ensure that the contact belongs to the user
    // we must also remove the contact from the list of the users current contacts and then save the user
    // then we delete the contact
    @Override
    @Transactional
    public void deleteEmergencyContact(Long userId, Long contactId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        emergencyContactRepository.delete(validateOwnership(userId, contactId));
    }

    @Override
    public EmergencyContactDto getEmergencyContactById(Long userId, Long contactId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return convertToDto(validateOwnership(userId, contactId));
    }

    /**
     * {@code @checkOwnershipOrAdmin} method checks whether the given user id is the logged in user
     * {@code @validateOwnerShip} method checks whether the contact requested actually belongs to the user
     * Double layer of security check
     * @param userId
     * @param contactId
     * @param updateRequest
     * @return
     */
    @Override
    @Transactional
    public EmergencyContactDto updateEmergencyContact(Long userId, Long contactId, UpdateEmergencyContactRequest updateRequest) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        EmergencyContact contact = validateOwnership(userId, contactId);
        Optional.ofNullable(updateRequest.getContactName()).ifPresent(contact::setContactName);
        Optional.ofNullable(updateRequest.getContactPhone()).ifPresent(contact::setContactPhone);
        Optional.ofNullable(updateRequest.getContactEmail()).ifPresent(contact::setContactEmail);
        return convertToDto(emergencyContactRepository.save(contact));
    }

    @Override
    public List<EmergencyContactDto> getAllEmergencyContacts() {
        return emergencyContactRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    private EmergencyContactDto convertToDto(EmergencyContact contact) {
        return modelMapper.map(contact, EmergencyContactDto.class);
    }

    // it gets the contact using the contact id and the cross checks whether user actually owns this contact or not
    private EmergencyContact validateOwnership(Long userId, Long contactId) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId).orElseThrow(() ->
                new ResourceNotFoundException("Emergency contact not found with id: " + contactId));
        if (!contact.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Emergency contact with id: " + contactId + " does not belong to user with id: " + userId);
        }
        return contact;
    }
}
