package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.emergency.IEmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
