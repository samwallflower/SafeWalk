package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.EmergencyContactDto;
import com.samwallflower.safewalk.request.emergencycontact.AddEmergencyContactRequest;
import com.samwallflower.safewalk.request.emergencycontact.UpdateEmergencyContactRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.emergencycontact.IEmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/emergency-contacts")
public class EmergencyContactController {
    private final IEmergencyContactService emergencyContactService;

    @GetMapping("/{userId}/contacts")
    public ResponseEntity<ApiResponse> getEmergencyContactsByUserId(@PathVariable Long userId) {
        List<EmergencyContactDto> contacts = emergencyContactService.getEmergencyContactsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Emergency contacts retrieved successfully", contacts));
    }

    @GetMapping("/{userId}/contacts/{contactId}")
    public ResponseEntity<ApiResponse> getEmergencyContactById(@PathVariable Long userId, @PathVariable Long contactId) {
        EmergencyContactDto contact = emergencyContactService.getEmergencyContactById(userId, contactId);
        return ResponseEntity.ok(new ApiResponse("Emergency contact retrieved successfully", contact));
    }

    @DeleteMapping("/{userId}/contacts/{contactId}/delete")
    public ResponseEntity<ApiResponse> deleteEmergencyContact(@PathVariable Long userId, @PathVariable Long contactId) {
        emergencyContactService.deleteEmergencyContact(userId, contactId);
        return ResponseEntity.ok(new ApiResponse("Emergency contact deleted successfully", null));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse> addEmergencyContact(@PathVariable Long userId, @Valid @RequestBody AddEmergencyContactRequest request) {
        EmergencyContactDto contact = emergencyContactService.addEmergencyContact(userId, request);
        return ResponseEntity.ok(new ApiResponse("Emergency contact added successfully", contact));
    }

    @PutMapping("/{userId}/contacts/{contactId}/update")
    public ResponseEntity<ApiResponse> updateEmergencyContact(@PathVariable Long userId, @PathVariable Long contactId,@Valid @RequestBody UpdateEmergencyContactRequest request) {
        EmergencyContactDto contact = emergencyContactService.updateEmergencyContact(userId, contactId, request);
        return ResponseEntity.ok(new ApiResponse("Emergency contact updated successfully", contact));
    }

}
