package com.samwallflower.safewalk.websocket;

import lombok.Data;

@Data
public class LocationUpdateMessage {
    private Long sessionId;
    private Double  latitude;
    private Double longitude;
}
