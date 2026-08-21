package com.samwallflower.safewalk.request.emergencyauthority;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmergencyAuthorityRequest {

    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be exactly 2 uppercase letters (ISO 3166-1 alpha-2)")
    private String countryCode;

    @Size(min = 2, max = 100, message = "Country name must be between 2 and 100 characters")
    private String countryName;

    @Pattern(regexp = "^\\d{2,15}$", message = "Police number must contain only digits (2 to 15 characters)")
    private String policeNumber;

    @Pattern(regexp = "^\\d{2,15}$", message = "Ambulance number must contain only digits (2 to 15 characters)")
    private String ambulanceNumber;

    @Pattern(regexp = "^\\d{2,15}$", message = "General emergency number must contain only digits (2 to 15 characters)")
    private String generalEmergencyNumber;
}
