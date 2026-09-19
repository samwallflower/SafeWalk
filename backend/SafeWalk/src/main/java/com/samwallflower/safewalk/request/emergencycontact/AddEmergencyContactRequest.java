package com.samwallflower.safewalk.request.emergencycontact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddEmergencyContactRequest {

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must be under 100 characters")
    private String contactName;

    @Pattern(
            regexp = "^\\+[1-9]\\d{6,14}$",
            message = "Contact phone must be in E.164 format, e.g. +36301234567"
    )
    private String contactPhone; // optional — SMS is now a bring-your-own-Twilio feature

    @NotBlank(message = "Contact email is required")
    @Size(max = 255, message = "Contact email must be under 255 characters")
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;
}