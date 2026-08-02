package com.samwallflower.safewalk.service.emergencycontact;

import com.samwallflower.safewalk.dto.EmergencyContactDto;
import com.samwallflower.safewalk.request.emergencycontact.AddEmergencyContactRequest;
import com.samwallflower.safewalk.request.emergencycontact.UpdateEmergencyContactRequest;

import java.util.List;

public interface IEmergencyContactService {
    EmergencyContactDto addEmergencyContact(Long userId, AddEmergencyContactRequest emergencyContact);
    List<EmergencyContactDto> getEmergencyContactsByUserId(Long userId);
    void deleteEmergencyContact(Long userId, Long contactId);
    EmergencyContactDto getEmergencyContactById(Long userId, Long contactId);
    EmergencyContactDto updateEmergencyContact(Long userId, Long contactId, UpdateEmergencyContactRequest updateRequest);
}
