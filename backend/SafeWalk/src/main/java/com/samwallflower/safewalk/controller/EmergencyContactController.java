package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.EmergencyContactDto;
import com.samwallflower.safewalk.request.emergencycontact.AddEmergencyContactRequest;
import com.samwallflower.safewalk.request.emergencycontact.UpdateEmergencyContactRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.emergencycontact.IEmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/emergency-contacts")
public class EmergencyContactController {
    private final IEmergencyContactService emergencyContactService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllEmergencyContacts(){
        List<EmergencyContactDto> contacts = emergencyContactService.getAllEmergencyContacts();
        return ResponseEntity.ok(new ApiResponse("All emergency contacts retrieved successfully", contacts));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{userId}/contacts")
    public ResponseEntity<ApiResponse> getEmergencyContactsByUserId(@PathVariable Long userId) {
        List<EmergencyContactDto> contacts = emergencyContactService.getEmergencyContactsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Emergency contacts retrieved successfully", contacts));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{userId}/contacts/{contactId}")
    public ResponseEntity<ApiResponse> getEmergencyContactById(@PathVariable Long userId, @PathVariable Long contactId) {
        EmergencyContactDto contact = emergencyContactService.getEmergencyContactById(userId, contactId);
        return ResponseEntity.ok(new ApiResponse("Emergency contact retrieved successfully", contact));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{userId}/contacts/{contactId}/delete")
    public ResponseEntity<ApiResponse> deleteEmergencyContact(@PathVariable Long userId, @PathVariable Long contactId) {
        emergencyContactService.deleteEmergencyContact(userId, contactId);
        return ResponseEntity.ok(new ApiResponse("Emergency contact deleted successfully", null));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse> addEmergencyContact(@PathVariable Long userId, @Valid @RequestBody AddEmergencyContactRequest request) {
        EmergencyContactDto contact = emergencyContactService.addEmergencyContact(userId, request);
        return ResponseEntity.ok(new ApiResponse("Emergency contact added successfully", contact));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{userId}/contacts/{contactId}/update")
    public ResponseEntity<ApiResponse> updateEmergencyContact(@PathVariable Long userId, @PathVariable Long contactId,@Valid @RequestBody UpdateEmergencyContactRequest request) {
        EmergencyContactDto contact = emergencyContactService.updateEmergencyContact(userId, contactId, request);
        return ResponseEntity.ok(new ApiResponse("Emergency contact updated successfully", contact));
    }

}
