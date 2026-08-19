package com.samwallflower.safewalk.service.notification;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.websocket.AlertMessage;

public interface INotificationService {
    void pushLocationUpdate(Long sessionId, WalkSessionDto sessionDto);
    void pushEmergencyAlert(Long sessionId, AlertMessage message);
    void pushIdleWarning(Long sessionId);
    void pushRouteDeviationWarning(Long sessionId);
}
