package com.samwallflower.safewalk.dto;

import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.samwallflower.safewalk.model.EmergencyContact}
 */
@Data
public class EmergencyContactDto implements Serializable {
    private Long id;
    private String contactName;
    private String contactPhone;
}