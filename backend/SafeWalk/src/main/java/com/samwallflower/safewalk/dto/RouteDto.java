package com.samwallflower.safewalk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

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

    // Populated only when app.eval.enabled=true (null, and omitted from JSON, otherwise)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer googleIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer incidentCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Long> incidentIds;

}