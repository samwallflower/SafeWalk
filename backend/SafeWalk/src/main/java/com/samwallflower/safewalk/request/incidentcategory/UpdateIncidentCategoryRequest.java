package com.samwallflower.safewalk.request.incidentcategory;

import lombok.Data;

@Data
public class UpdateIncidentCategoryRequest {
    private String name;
    private Integer severityWeight;
    private String description;
}
