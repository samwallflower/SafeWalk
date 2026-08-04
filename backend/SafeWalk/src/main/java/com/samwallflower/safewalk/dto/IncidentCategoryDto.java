package com.samwallflower.safewalk.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * DTO for {@link com.samwallflower.safewalk.model.IncidentCategory}
 */
@Data
public class IncidentCategoryDto implements Serializable {
    private Long id;
    private String name;
    private Integer severityWeight;
    private String description;
}