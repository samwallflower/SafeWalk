package com.samwallflower.safewalk.websocket;

import com.samwallflower.safewalk.enums.AlertMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertMessage {
    private Long sessionId;
    private AlertMessageType type;
    private String message;
}
