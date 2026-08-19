package com.samwallflower.safewalk.service.notification;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.AlertMessageType;
import com.samwallflower.safewalk.websocket.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService{
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void pushLocationUpdate(Long sessionId, WalkSessionDto sessionDto) {
        String destination = "/topic/session/" + sessionId;
        log.debug("Pushing location update to {}", destination);
        simpMessagingTemplate.convertAndSend(destination, sessionDto);
    }

    @Override
    public void pushEmergencyAlert(Long sessionId, AlertMessage message) {
        String destination = "/topic/emergency/" + sessionId;
        log.info("Pushing emergency alert to {}",  destination);
        simpMessagingTemplate.convertAndSend(destination, message);

    }

    @Override
    public void pushIdleWarning(Long sessionId) {
        String message = "No movement detected. Please confirm you're okay.";
        String destination = "/topic/idle-warning/" + sessionId;
        AlertMessage alert = new AlertMessage(
                sessionId,
                AlertMessageType.IDLE_WARNING,
                message
        );
        simpMessagingTemplate.convertAndSend(destination, alert);

    }

    @Override
    public void pushRouteDeviationWarning(Long sessionId) {
        String destination = "/topic/route-deviation/" + sessionId;
        String message = "You have deviated from your planned route.";
        AlertMessage alert = new AlertMessage(sessionId,
                AlertMessageType.ROUTE_DEVIATION,
                message);
        simpMessagingTemplate.convertAndSend(destination, alert);
    }
}
