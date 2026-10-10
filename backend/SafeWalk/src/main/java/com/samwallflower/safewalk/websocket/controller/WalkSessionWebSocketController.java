package com.samwallflower.safewalk.websocket.controller;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import com.samwallflower.safewalk.security.util.SecurityUtils;
import com.samwallflower.safewalk.service.notification.INotificationService;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
import com.samwallflower.safewalk.websocket.connection.WalkSessionConnectionRegistry;
import com.samwallflower.safewalk.websocket.message.LocationUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WalkSessionWebSocketController {
    private final IWalkSessionService walkSessionService;
    private final INotificationService notificationService;
    private final WalkSessionConnectionRegistry connectionRegistry;


    @MessageMapping("/session.location")
    public void handleLocationUpdate(LocationUpdateMessage locationUpdateMessage, SimpMessageHeaderAccessor headerAccessor) {

        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(locationUpdateMessage.getLatitude());
        request.setLongitude(locationUpdateMessage.getLongitude());

        Long userId = SecurityUtils.getCurrentUserId();

        WalkSessionDto updated = walkSessionService.updateLocation(locationUpdateMessage.getSessionId(),userId,request);

        connectionRegistry.registerActivity(headerAccessor.getSessionId(), locationUpdateMessage.getSessionId());

        notificationService.pushLocationUpdate(updated.getId(),updated);

    }
}
