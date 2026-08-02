package com.samwallflower.safewalk.request.emergencycontact;

import lombok.Data;

@Data
public class AddEmergencyContactRequest {
    private String contactName;
    private String contactPhone;
}
