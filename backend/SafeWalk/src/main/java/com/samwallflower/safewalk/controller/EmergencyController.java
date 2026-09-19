package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.EmergencyDto;
import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
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
        emergencyService.triggerEmergencyByUser(sessionId, userId);
        return ResponseEntity.ok(new ApiResponse("Emergency protocol triggered successfully", null));
    }

    //TODO: ADMIN ONLY
    @PostMapping("/session/{sessionId}/create")
    public ResponseEntity<ApiResponse> createEmergency(@PathVariable Long sessionId, @RequestParam String source) {
        EmergencyDto emergency = emergencyService.addEmergency(sessionId, source);
        return ResponseEntity.ok(new ApiResponse("Emergency created successfully", emergency));
    }

    // get all emergencies

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllEmergencies() {
        List<EmergencyDto> emergencies = emergencyService.getAllEmergencies();
        return ResponseEntity.ok(new ApiResponse("All emergencies retrieved successfully", emergencies));
    }

    @GetMapping("/by-trigger-source")
    public ResponseEntity<ApiResponse> getAllEmergenciesByTriggerSource(@RequestParam EmergencyTriggerSource source) {
        List<EmergencyDto> emergencies = emergencyService.getAllEmergenciesByTriggerSource(source);
        return ResponseEntity.ok(new ApiResponse("Emergencies retrieved successfully for source: " + source, emergencies));
    }

    @GetMapping("/session/{sessionId}/all")
    public ResponseEntity<ApiResponse> getAllEmergenciesByWalkSessionId(@PathVariable Long sessionId) {
        List<EmergencyDto> emergencies = emergencyService.getAllEmergenciesByWalkSessionId(sessionId);
        return ResponseEntity.ok(new ApiResponse("Emergencies retrieved successfully for session: " + sessionId, emergencies));
    }

}
