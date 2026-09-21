package com.samwallflower.safewalk.request.incidentreport;

import com.samwallflower.safewalk.model.IncidentCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddIncidentReportRequest {
    // basically what we need from the user
    private String description;

    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;
    private Boolean isAnonymous;
    private IncidentCategory category;
}
