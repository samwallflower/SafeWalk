package com.samwallflower.safewalk.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link com.samwallflower.safewalk.model.Route}
 */

@Data
public class RouteDto implements Serializable {
    private Long id;
    private String polyline;
    private Double actualDistanceMeters;
    private Double safetyPenaltyMeters;
    private Double virtualDistanceMeters;
    private Integer rank;
    private String routeRequestId;

}