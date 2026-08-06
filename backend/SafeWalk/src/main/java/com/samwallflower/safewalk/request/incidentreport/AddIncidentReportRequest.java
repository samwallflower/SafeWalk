package com.samwallflower.safewalk.request.incidentreport;

import com.samwallflower.safewalk.model.IncidentCategory;
import lombok.Data;

@Data
public class AddIncidentReportRequest {
    // basically what we need from the user
    private String description;
    private Double latitude;
    private Double longitude;
    private Boolean isAnonymous;
    private IncidentCategory category;
}
