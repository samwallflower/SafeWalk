package com.samwallflower.safewalk.request.incidentcategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddIncidentCategoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 1, message = "Severity weight must be at least 1")
    @Max(value = 20, message = "Severity weight must be at most 20")
    private Integer severityWeight;
    private String description;
}
