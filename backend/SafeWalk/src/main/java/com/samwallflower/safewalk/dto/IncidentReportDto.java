package com.samwallflower.safewalk.dto;

import com.samwallflower.safewalk.enums.ReportStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.samwallflower.safewalk.model.IncidentReport}
 */
@Data
public class IncidentReportDto implements Serializable {
    private Long id;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Boolean isAnonymous;
    private Integer upvotes;
    private Integer downvotes;
    private UserDto user;
    private IncidentCategoryDto category;
    private ReportStatus status;
}