package com.samwallflower.safewalk.request.incidentcategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateIncidentCategoryRequest {
    private String name;
    @Min(value = 1, message = "Severity weight must be at least 1")
    @Max(value = 20, message = "Severity weight must be at most 20")
    private Integer severityWeight;
    private String description;
}
