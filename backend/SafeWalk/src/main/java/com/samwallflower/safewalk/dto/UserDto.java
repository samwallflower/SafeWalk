package com.samwallflower.safewalk.dto;

import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.samwallflower.safewalk.model.User}
 */
@Data
public class UserDto implements Serializable {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}