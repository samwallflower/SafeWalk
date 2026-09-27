package com.samwallflower.safewalk.service.emergency;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.dto.EmergencyDto;
import com.samwallflower.safewalk.enums.AlertMessageType;
import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.integration.twilio.TwilioClient;
import com.samwallflower.safewalk.model.Emergency;
import com.samwallflower.safewalk.model.EmergencyContact;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.model.WalkSession;
import com.samwallflower.safewalk.repository.EmergencyRepository;
import com.samwallflower.safewalk.repository.WalkSessionRepository;
import com.samwallflower.safewalk.service.email.EmailService;
import com.samwallflower.safewalk.service.emergencyauthority.IEmergencyAuthorityService;
import com.samwallflower.safewalk.service.notification.INotificationService;
import com.samwallflower.safewalk.websocket.connection.WalkSessionConnectionRegistry;
import com.samwallflower.safewalk.websocket.message.AlertMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyService implements IEmergencyService{
    private final WalkSessionRepository walkSessionRepository;
    private final EmergencyRepository emergencyRepository;
    private final TwilioClient twilioClient;
    private final INotificationService notificationService;
    private final IEmergencyAuthorityService emergencyAuthorityService;
    private final EmailService emailService;
    private final WalkSessionConnectionRegistry walkSessionConnectionRegistry;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public EmergencyDto triggerEmergencyByUser(Long sessionId, Long userId) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        if(!session.getUser().getId().equals(userId)){
            throw new ResourceProcessingException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
        }

        return executeEmergencyProtocol(session, EmergencyTriggerSource.MANUAL_SOS);

    }

    // will be called by system automatically
    @Override
    @Transactional
    public EmergencyDto triggerEmergencySystem(Long sessionId, EmergencyTriggerSource source) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        return executeEmergencyProtocol(session, source);

    }
    // for dev purposes
    @Override
    public EmergencyDto addEmergency(Long sessionId, String source) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        Emergency emergency = createEmergency(session, resolveTriggerSource(source));
        return convertToDto(emergencyRepository.save(emergency));
    }

    @Override
    public List<EmergencyDto> getAllEmergencies() {
        return emergencyRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<EmergencyDto> getAllEmergenciesByTriggerSource(String source) {
        return emergencyRepository.findByTriggerSource(resolveTriggerSource(source)).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<EmergencyDto> getAllEmergenciesByWalkSessionId(Long sessionId) {
        return emergencyRepository.findByWalkSessionId(sessionId).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     *
     * @param sessionId
     * @param userId
     */
    @Override
    @Transactional
    public EmergencyDto resolveEmergency(Long id, Long sessionId, Long userId) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency not found with id: " + id));

        if(emergency.getResolved())
            throw new ResourceProcessingException("Emergency with id: " + id + " is already resolved.");

        if(!session.getUser().getId().equals(userId)){
            throw new ResourceProcessingException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
        }
        if(session.getStatus() != SessionStatus.EMERGENCY){
            throw new ResourceProcessingException("Walk session with id: " + sessionId + " is not in EMERGENCY status.");
        }
        if(!emergency.getWalkSession().getId().equals(sessionId)){
            throw new ResourceProcessingException("Emergency with id: " + id + " does not belong to the walk session with id: " + sessionId);
        }


        session.setStatus(SessionStatus.ACTIVE);

        switch (emergency.getTriggerSource()) {
            case IDLE_TIMEOUT->
                session.setAlarmTriggered(false);
            case ROUTE_DEVIATION->{
                session.setDeviationTriggered(false);
                session.setDeviationTriggeredAt(null);
            }
            case CONNECTION_LOST->
                walkSessionConnectionRegistry.clearDisconnect(sessionId);
        }


        session.setLastLocationUpdate(LocalDateTime.now());
        walkSessionRepository.save(session);

        emergency.setResolved(true);
        emergency.setResolvedAt(LocalDateTime.now());
        Emergency saved = emergencyRepository.save(emergency);


        notifyContactsOfResolution(session);
        notificationService.pushEmergencyAlert(sessionId,
                new AlertMessage(sessionId, saved.getId(),AlertMessageType.EMERGENCY_RESOLVED,
                        "Emergency with id: "+ saved.getId()+" and type: "+ saved.getTriggerSource()+" resolved - user confirmed they are safe."));

        return convertToDto(saved);
    }

    private void notifyContactsOfResolution(WalkSession session) {
        User user = session.getUser();
        List<EmergencyContact> contacts = user.getEmergencyContacts();

        if(contacts == null || contacts.isEmpty()) return;

        String body = String.format(
                "Good news! %s %s has confirmed they are safe. The emergency situation has been resolved.",
                user.getFirstName(), user.getLastName()
        );

        for (EmergencyContact contact : contacts) {
            if(contact.getContactEmail() != null && !contact.getContactEmail().isEmpty()) {
                try {
                    emailService.sendEmail(contact.getContactEmail(), "Emergency Resolved", body);
                    log.info("Emergency resolution email sent to contact {} for session {}", contact.getId(), session.getId());
                } catch (ResourceProcessingException e) {
                    log.error("Failed to send resolution email to contact {} for session {}: {}",
                            contact.getId(), session.getId(), e.getMessage());
                }
            }
            if(contact.getContactPhone() != null && !contact.getContactPhone().isEmpty()) {
                try {
                    twilioClient.sendSms(contact.getContactPhone(), body);
                    log.info("Emergency resolution SMS sent to contact {} for session {}", contact.getId(), session.getId());
                } catch (ResourceProcessingException e) {
                    log.error("Failed to send resolution SMS to contact {} for session {}: {}",
                            contact.getId(), session.getId(), e.getMessage());
                }
            }
        }

    }

    @Override
    public EmergencyDto convertToDto(Emergency emergency) {
        return modelMapper.map(emergency, EmergencyDto.class);
    }

    @Override
    public EmergencyDto getEmergencyById(Long id) {
        return emergencyRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with id: " + id));
    }

    @Override
    public EmergencyDto getActiveEmergencyByWalkSessionId(Long sessionId) {
        return emergencyRepository.findByWalkSessionIdAndResolved(sessionId, false).stream()
                .findFirst()
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No active emergency found for walk session with id: " + sessionId));
    }

    @Override
    public long countEmergencyByTriggerSource(String source) {
        return emergencyRepository.countByTriggerSource(resolveTriggerSource(source));
    }

    private EmergencyDto executeEmergencyProtocol(WalkSession session, EmergencyTriggerSource source) {
        if (session.getStatus()== SessionStatus.EMERGENCY){
            log.info("Session {} is already in EMERGENCY status - skipping duplicate trigger", session.getId());
            return null;
        }
        if(session.getStatus()==SessionStatus.COMPLETED || session.getStatus()==SessionStatus.ABANDONED){
            log.info("Session is in {} status", session.getStatus());
            throw new ResourceProcessingException("Session in "+ session.getStatus() +" status.");
        }
        session.setStatus(SessionStatus.EMERGENCY);
        walkSessionRepository.save(session);

        User user = session.getUser();
        List<EmergencyContact> notifiedContacts = notifyEmergencyContacts(session, user);

        Emergency emergency = createEmergency(session, source);
        emergency.setNotifiedEmergencyContacts(notifiedContacts);

        Emergency saved = emergencyRepository.save(emergency);
        log.info("Emergency record saved with id {} for session {}", saved.getId(), session.getId());


        AlertMessage alert = new AlertMessage(
                session.getId(),
                saved.getId(),
                AlertMessageType.EMERGENCY_TRIGGERED,
                "Emergency protocol activated for this session with id:" + session.getId()
                + "for : " + saved.getTriggerSource()
        );
        // sending notification to user via websocket
        notificationService.pushEmergencyAlert(session.getId(), alert);

        log.info("Emergency protocol executed for session {}, {} of {} contacts notified", session.getId(),
                notifiedContacts.size(), session.getUser().getEmergencyContacts()!=null ? session.getUser().getEmergencyContacts().size() : 0);
        

        return convertToDto(saved);
    }

    private List<EmergencyContact> notifyEmergencyContacts(WalkSession session, User user) {
        List<EmergencyContact> contacts = user.getEmergencyContacts();
        List<EmergencyContact> notifiedContacts = new ArrayList<>();


        // EMERGENCY CONTACT NOTIFICATION
        // removed throw new ResourceNotFoundException for robust architecture
        // an exception would roll back session status change to emergency
        // we would like to avoid that
        // hence we just log the warning
        if(contacts==null || contacts.isEmpty()){
            log.warn("User {} has no emergency contacts to notify for session {}", user.getId(), session.getId());
        }else {
            // building the tracking link
            String trackingLink = buildTrackingLink(session);
            String authorityLine = buildAuthorityLine(session);
            String subject = "Emergency Alert: " + user.getFirstName() + " " + user.getLastName() + " may need help!";
            String plainBody = String.format(
                    "%s %s may need help. %nLive location: %s%n%s",
                    user.getFirstName(), user.getLastName(), trackingLink, authorityLine
            );
            // looping through emergency contacts
            for (EmergencyContact emergencyContact : contacts) {
                boolean smsSucceeded = false;
                boolean emailSucceeded = false;

                // Email sending
                if (emergencyContact.getContactEmail() != null && !emergencyContact.getContactEmail().isEmpty()) {
                    try {
                        String htmlBody = buildEmergencyEmailHtml(user, trackingLink, authorityLine);
                        emailService.sendEmail(emergencyContact.getContactEmail(), subject, htmlBody);
                        emailSucceeded = true;
                        log.info("Emergency email sent to contact {} for session {}", emergencyContact.getId(), session.getId());
                    } catch (ResourceProcessingException e) {
                        log.error("Failed to send email to contact {} for session {}: {}",
                                emergencyContact.getId(), session.getId(), e.getMessage());
                    }
                }

                // SMS sending
                if (emergencyContact.getContactPhone() != null && !emergencyContact.getContactPhone().isEmpty()) {
                    try {
                        twilioClient.sendSms(emergencyContact.getContactPhone(), plainBody);
                        smsSucceeded = true;
                        log.info("Emergency SMS sent to contact {} for session {}", emergencyContact.getId(), session.getId());
                    } catch (ResourceProcessingException e) {
                        log.error("Failed to send SMS to contact {} for session {}: {}",
                                emergencyContact.getId(), session.getId(), e.getMessage());
                    }
                }


                if(smsSucceeded || emailSucceeded){
                    notifiedContacts.add(emergencyContact);
                }
            }
        }
        return notifiedContacts;
    }

    private String buildEmergencyEmailHtml(User user, String trackingLink, String authorityLine) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Emergency Alert</title>
                </head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0;">
                  <table role="presentation" width="100%%" style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden;">
                    <tr>
                      <td style="background-color: #d32f2f; padding: 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 20px;">⚠ Emergency Alert</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding: 24px;">
                        <p style="font-size: 16px; color: #333333;">
                          <strong>%s %s</strong> may need help right now.
                        </p>
                        <p style="text-align: center; margin: 24px 0;">
                          <a href="%s" style="background-color: #d32f2f; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;">
                            View Live Location
                          </a>
                        </p>
                        %s
                        <p style="font-size: 13px; color: #888888; margin-top: 24px;">
                          This alert was sent automatically by SafeWalk on behalf of %s %s.
                        </p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                user.getFirstName(), user.getLastName(),
                trackingLink,
                authorityLine.isBlank() ? "" : "<p style=\"font-size: 14px; color: #555555; background-color: #fdecea; padding: 12px; border-radius: 4px;\">" + authorityLine + "</p>",
                user.getFirstName(), user.getLastName()
        );
    }

    private Emergency createEmergency(WalkSession session, EmergencyTriggerSource source) {
        Emergency emergency = new Emergency();
        emergency.setWalkSession(session);
        emergency.setTriggerSource(source);
        emergency.setTriggerLatitude(session.getLastKnownLatitude());
        emergency.setTriggerLongitude(session.getLastKnownLongitude());
        return emergency;
    }

    private String buildAuthorityLine(WalkSession session) {
        try {
            EmergencyAuthorityDto authority = emergencyAuthorityService.findEmergencyAuthorityByLocation(
                    session.getLastKnownLatitude(),
                    session.getLastKnownLongitude()
            );
            return String.format("Nearest emergency number: %s, Police Phone Number: %s, Ambulance Number: %s",
                    authority.getGeneralEmergencyNumber(), authority.getPoliceNumber(), authority.getAmbulanceNumber());
        } catch (ResourceNotFoundException e) {
            log.warn("No emergency authority found for session {}: {}", session.getId(), e.getMessage());
            return "";
        }
    }

    private String buildTrackingLink(WalkSession session) {
        return String.format("https://maps.google.com/?q=%s,%s",
                session.getLastKnownLatitude(),
                session.getLastKnownLongitude());
    }

    private EmergencyTriggerSource resolveTriggerSource(String source) {
        return switch (source.toUpperCase()) {
            case "MANUAL_SOS" -> EmergencyTriggerSource.MANUAL_SOS;
            case "IDLE_TIMEOUT" -> EmergencyTriggerSource.IDLE_TIMEOUT;
            case "CONNECTION_LOST" -> EmergencyTriggerSource.CONNECTION_LOST;
            case "ROUTE_DEVIATION" -> EmergencyTriggerSource.ROUTE_DEVIATION;
            case "SYSTEM" -> EmergencyTriggerSource.SYSTEM;
            default -> throw new IllegalArgumentException("Unknown emergency trigger source: " + source);
        };
    }
}

