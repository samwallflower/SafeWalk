package com.samwallflower.safewalk.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(max = 100, message = "First name must be under 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be under 100 characters")
    private String lastName;

    @Pattern(
            regexp = "^\\+?[0-9\\s\\-()]{7,20}$",
            message = "Contact phone must be a valid phone number"
    )
    private String phoneNumber;
}