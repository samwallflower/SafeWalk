package com.samwallflower.safewalk.websocket;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WalkSessionWebSocketController {
    private final IWalkSessionService walkSessionService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/session.location")
    public void handleLocationUpdate(LocationUpdateMessage locationUpdateMessage) {
        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(locationUpdateMessage.getLatitude());
        request.setLongitude(locationUpdateMessage.getLongitude());
        Long userId = 1L; // TODO: change it to retrieve user id from authenticated user principal

        WalkSessionDto updated = walkSessionService.updateLocation(locationUpdateMessage.getSessionId(),userId,request);

        simpMessagingTemplate.convertAndSend("/topic/session/"+locationUpdateMessage.getSessionId(),updated);
    }
}
