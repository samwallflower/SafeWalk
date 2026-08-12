package com.samwallflower.safewalk.request.emergencycontact;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmergencyContactRequest {
    @Size(max = 100, message = "Contact name must be under 100 characters")
    private String contactName;

    @Pattern(
            regexp = "^\\+?[0-9\\s\\-()]{7,20}$",
            message = "Contact phone must be a valid phone number"
    )
    private String contactPhone;
}
