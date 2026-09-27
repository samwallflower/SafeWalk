package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.request.auth.ResendVerificationRequest;
import com.samwallflower.safewalk.request.auth.UserLoginRequest;
import com.samwallflower.safewalk.request.auth.VerifyUserRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.response.JwtResponse;
import com.samwallflower.safewalk.security.jwt.JwtUtils;
import com.samwallflower.safewalk.security.user.AppUserDetails;
import com.samwallflower.safewalk.service.auth.AuthVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final AuthVerificationService authVerificationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody UserLoginRequest request){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt);
        return ResponseEntity.ok(new ApiResponse("Login successful", jwtResponse));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody VerifyUserRequest request){
        authVerificationService.verifyUser(request);
        return ResponseEntity.ok(new ApiResponse("Email verified successfully",null));

    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerificationCode(@Valid @RequestBody ResendVerificationRequest request){

        authVerificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new ApiResponse("Verification code resent successfully",null));
    }
}
