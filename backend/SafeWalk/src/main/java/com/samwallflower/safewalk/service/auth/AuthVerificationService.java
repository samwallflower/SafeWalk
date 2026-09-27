package com.samwallflower.safewalk.service.auth;

import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.request.auth.VerifyUserRequest;
import com.samwallflower.safewalk.service.email.EmailService;
import com.samwallflower.safewalk.service.email.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void sendVerificationCode(User user){
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        userRepository.save(user);

    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String body = EmailTemplates.verificationCode(verificationCode);

        emailService.sendEmail(user.getEmail(),subject,body);
    }

    public void verifyUser(VerifyUserRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if(user.isEnabled())
            throw new ResourceProcessingException("Account is already verified");

        if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now()))
            throw new ResourceProcessingException("Verification code expired.Please request a new one.");

        if(user.getVerificationCode().equals(request.getVerificationCode())){
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
        }else{
            throw new ResourceProcessingException("Invalid verification code.");
        }
    }

    public void resendVerificationCode(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if(user.isEnabled())
            throw new ResourceProcessingException("Account is already verified");

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        sendVerificationEmail(user);

    }

    private String generateVerificationCode(){
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // generates a random 6-digit code
        return String.valueOf(code);
    }

}
