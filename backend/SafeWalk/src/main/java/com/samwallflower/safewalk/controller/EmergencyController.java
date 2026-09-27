package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.EmergencyDto;
import com.samwallflower.safewalk.request.emergency.UpdateEmergencyRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.emergency.IEmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/emergency")
public class EmergencyController {
    private final IEmergencyService emergencyService;

    @PostMapping("/session/{sessionId}/user/{userId}/trigger")
    public ResponseEntity<ApiResponse> triggerEmergencyByUser(@PathVariable Long sessionId, @PathVariable Long userId) {
        EmergencyDto emergency = emergencyService.triggerEmergencyByUser(sessionId, userId);
        return ResponseEntity.ok(new ApiResponse("Emergency protocol triggered successfully", emergency));
    }

    //TODO: ADMIN ONLY
    @PostMapping("/session/{sessionId}/create")
    public ResponseEntity<ApiResponse> createEmergency(@PathVariable Long sessionId, @RequestParam String source) {
        EmergencyDto emergency = emergencyService.addEmergency(sessionId, source);
        return ResponseEntity.ok(new ApiResponse("Emergency created successfully", emergency));
    }

    //TODO:ADMIN ONLY
    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateEmergency(@PathVariable Long id, @RequestBody UpdateEmergencyRequest request) {
        EmergencyDto emergency = emergencyService.updateEmergencyById(id, request);
        return ResponseEntity.ok(new ApiResponse("Emergency updated successfully", emergency));
    }

    //TODO:ADMIN ONLY
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteEmergency(@PathVariable Long id) {
        emergencyService.deleteEmergencyById(id);
        return ResponseEntity.ok(new ApiResponse("Emergency deleted successfully", null));
    }

    // get all emergencies

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllEmergencies() {
        List<EmergencyDto> emergencies = emergencyService.getAllEmergencies();
        return ResponseEntity.ok(new ApiResponse("All emergencies retrieved successfully", emergencies));
    }

    @GetMapping("/by-trigger-source")
    public ResponseEntity<ApiResponse> getAllEmergenciesByTriggerSource(@RequestParam String source) {
        List<EmergencyDto> emergencies = emergencyService.getAllEmergenciesByTriggerSource(source);
        return ResponseEntity.ok(new ApiResponse("Emergencies retrieved successfully for source: " + source, emergencies));
    }

    @GetMapping("/session/{sessionId}/all")
    public ResponseEntity<ApiResponse> getAllEmergenciesByWalkSessionId(@PathVariable Long sessionId) {
        List<EmergencyDto> emergencies = emergencyService.getAllEmergenciesByWalkSessionId(sessionId);
        return ResponseEntity.ok(new ApiResponse("Emergencies retrieved successfully for session: " + sessionId, emergencies));
    }

    @GetMapping("/{id}/emergency")
    public ResponseEntity<ApiResponse> getEmergencyById(@PathVariable Long id) {
        EmergencyDto emergency = emergencyService.getEmergencyById(id);
        return ResponseEntity.ok(new ApiResponse("Emergency retrieved successfully", emergency));
    }

    @PutMapping("/{id}/emergency/session/{sessionId}/user/{userId}/resolve")
    public ResponseEntity<ApiResponse> resolveEmergency(@PathVariable Long id, @PathVariable Long sessionId, @PathVariable Long userId) {
        EmergencyDto emergency = emergencyService.resolveEmergency(id, sessionId, userId);
        return ResponseEntity.ok(new ApiResponse("Emergency resolved successfully", emergency));
    }

    @GetMapping("/session/{sessionId}/active")
    public ResponseEntity<ApiResponse> getActiveEmergencyByWalkSessionId(@PathVariable Long sessionId) {
        EmergencyDto emergency = emergencyService.getActiveEmergencyByWalkSessionId(sessionId);
        return ResponseEntity.ok(new ApiResponse("Active emergency retrieved successfully", emergency));
    }

    @GetMapping("/count-by-trigger-source")
    public ResponseEntity<ApiResponse> countEmergencyByTriggerSource(@RequestParam String source) {
        long count = emergencyService.countEmergencyByTriggerSource(source);
        return ResponseEntity.ok(new ApiResponse("Count of emergencies retrieved successfully for source: " + source, count));
    }
}
