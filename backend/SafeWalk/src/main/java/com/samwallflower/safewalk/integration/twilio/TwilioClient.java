package com.samwallflower.safewalk.integration.twilio;

import com.samwallflower.safewalk.config.TwilioConfig;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioClient {
    private final TwilioConfig  twilioConfig;

    public void sendSms(String toPhoneNumber, String body){
        try{
            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioConfig.getFromNumber()),
                    body
            ).create();
        }catch(Exception e){
            log.error("Failed to send SMS to {} {}", toPhoneNumber, e.getMessage());
            throw new ResourceProcessingException("Failed to send emergency SMS to "+ toPhoneNumber);
        }
    }
}
