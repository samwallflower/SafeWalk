package com.samwallflower.safewalk.request.emergencyauthority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddEmergencyAuthorityRequest {
    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be exactly two uppercase letters.")
    private String countryCode;// ISO 3166-1 alpha-2, e.g. "HU"
    @NotBlank(message = "Country name is required")
    @Size(min = 2, max = 100, message = "Country name must be between 2 and 100")
    private String countryName;
    @NotBlank
    @Pattern(regexp = "^\\d{2,15}$", message = "Police number must contain only digits (2 to 15 characters)")
    private String policeNumber;        // e.g. "107" (Hungary)
    @NotBlank
    @Pattern(regexp = "^\\d{2,15}$", message = "Ambulance number must contain only digits (2 to 15 characters)")
    private String ambulanceNumber;     // e.g. "104" (Hungary)
    @Pattern(regexp = "^\\d{2,15}$", message = "General emergency number must contain only digits (2 to 15 characters)")
    private String generalEmergencyNumber;
}
