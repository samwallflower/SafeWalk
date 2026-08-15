package com.samwallflower.safewalk.dto;

import com.samwallflower.safewalk.enums.SessionStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.samwallflower.safewalk.model.WalkSession}
 */
@Data
public class WalkSessionDto implements Serializable {
    private Long id;
    private Long userId;
    private Long routeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double lastKnownLatitude;
    private Double lastKnownLongitude;
    private LocalDateTime lastLocationUpdate;
    private LocalDateTime lastArrivedAt;
    private SessionStatus status;
}