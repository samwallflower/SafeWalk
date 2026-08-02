package com.samwallflower.safewalk.request.auth;

import lombok.Data;

@Data
public class UserRegisterRequest {
    //when a user registers what info we want from them
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
