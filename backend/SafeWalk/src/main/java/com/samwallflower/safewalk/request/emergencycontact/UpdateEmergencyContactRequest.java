package com.samwallflower.safewalk.request.emergencycontact;

import lombok.Data;

@Data
public class UpdateEmergencyContactRequest {
    private String contactName;
    private String contactPhone;
}
