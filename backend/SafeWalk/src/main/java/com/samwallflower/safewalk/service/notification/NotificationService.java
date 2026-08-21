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
    private static final String SESSION_TOPIC_PREFIX = "/topic/session/";
    private static final String ALERT_TOPIC_PREFIX = "/topic/alert/";

    private void push(String topic, Object payload){
        log.debug("Pushing to {}", topic);
        simpMessagingTemplate.convertAndSend(topic, payload);
    }

    private void pushAlert(Long sessionId, AlertMessage alert){
        push(ALERT_TOPIC_PREFIX + sessionId, alert);

    }

    @Override
    public void pushLocationUpdate(Long sessionId, WalkSessionDto sessionDto) {
        push(SESSION_TOPIC_PREFIX + sessionId, sessionDto);
    }

    @Override
    public void pushEmergencyAlert(Long sessionId, AlertMessage message) {
        pushAlert(sessionId, message);

    }

    @Override
    public void pushIdleWarning(Long sessionId) {
        String message = "No movement detected. Please confirm you're okay.";
        AlertMessage alert = new AlertMessage(
                sessionId,
                AlertMessageType.IDLE_WARNING,
                message
        );
        pushAlert(sessionId, alert);
    }

    @Override
    public void pushRouteDeviationWarning(Long sessionId) {
        String message = "You have deviated from your planned route.";
        AlertMessage alert = new AlertMessage(sessionId,
                AlertMessageType.ROUTE_DEVIATION,
                message);
        pushAlert(sessionId, alert);
    }
}
