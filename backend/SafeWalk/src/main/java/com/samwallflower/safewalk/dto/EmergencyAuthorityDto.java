package com.samwallflower.safewalk.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * DTO for {@link com.samwallflower.safewalk.model.EmergencyAuthority}
 */
@Data
public class EmergencyAuthorityDto implements Serializable {
    private Long id;
    private String countryCode;
    private String countryName;
    private String policeNumber;
    private String ambulanceNumber;
    private String generalEmergencyNumber;
}