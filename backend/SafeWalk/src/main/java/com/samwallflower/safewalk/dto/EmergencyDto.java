package com.samwallflower.safewalk.dto;

import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link com.samwallflower.safewalk.model.Emergency}
 */
@Data
public class EmergencyDto implements Serializable {
    private Long id;
    private WalkSessionDto walkSession;
    private EmergencyTriggerSource triggerSource;
    private Double triggerLatitude;
    private Double triggerLongitude;
    private List<EmergencyContactDto> notifiedEmergencyContacts;
    private LocalDateTime triggerTimestamp;
}