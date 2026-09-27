package com.samwallflower.safewalk.request.auth;

import lombok.Data;

@Data
public class ResendVerificationRequest {
    private String email;
}
