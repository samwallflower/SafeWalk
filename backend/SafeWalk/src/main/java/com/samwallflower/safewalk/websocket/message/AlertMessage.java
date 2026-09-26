package com.samwallflower.safewalk.websocket.message;

import com.samwallflower.safewalk.enums.AlertMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    private Long sessionId;
    private Long emergencyId;
    private AlertMessageType type;
    private String message;

    public AlertMessage(Long sessionId, AlertMessageType type, String message) {
        this.sessionId = sessionId;
        this.type = type;
        this.message = message;
    }
}
