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
import com.samwallflower.safewalk.security.util.SecurityUtils;
import com.samwallflower.safewalk.service.email.EmailService;
import com.samwallflower.safewalk.service.email.EmailTemplates;
import com.samwallflower.safewalk.service.emergencyauthority.IEmergencyAuthorityService;
import com.samwallflower.safewalk.service.notification.INotificationService;
import com.samwallflower.safewalk.websocket.connection.WalkSessionConnectionRegistry;
import com.samwallflower.safewalk.websocket.message.AlertMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
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
        SecurityUtils.checkOwnershipOrAdmin(userId);
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        if(!session.getUser().getId().equals(userId)){
            throw new AccessDeniedException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
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
    @Transactional
    public EmergencyDto addEmergency(Long sessionId, String source) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        Emergency emergency = createEmergency(session, resolveTriggerSource(source));
        session.setStatus(SessionStatus.EMERGENCY);
        walkSessionRepository.save(session);
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

    @Override
    public List<EmergencyDto> getAllEmergenciesByWalkSessionIdAndUserId(Long sessionId, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));
        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
        }
        return emergencyRepository.findByWalkSessionId(sessionId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public EmergencyDto resolveEmergency(Long id, Long sessionId, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency not found with id: " + id));

        if(emergency.getResolved())
            throw new ResourceProcessingException("Emergency with id: " + id + " is already resolved.");

        if(!session.getUser().getId().equals(userId)){
            throw new AccessDeniedException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
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
        String htmlBody = EmailTemplates.emergencyResolved(user.getFirstName()+" "+ user.getLastName());

        for (EmergencyContact contact : contacts) {
            if(contact.getContactEmail() != null && !contact.getContactEmail().isEmpty()) {
                try {
                    emailService.sendEmail(contact.getContactEmail(), "Emergency Resolved", htmlBody);
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

    // okay so even if the logged in user and userId value are equal
    // we are fetching emergency from the db as an entity
    // we would like to make sure the user cannot call for emergencies that particularly
    // does not belong to them
    // we should throw an exception if the emergency does not belong to the user
    @Override
    public EmergencyDto getEmergencyByIdAndUserId(Long id, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with id: " + id));
        if (!emergency.getWalkSession().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Emergency with id: " + id + " does not belong to the user with id: " + userId);
        }
        return convertToDto(emergency);
    }

    @Override
    public EmergencyDto getActiveEmergencyByWalkSessionId(Long sessionId) {
        return emergencyRepository.findByWalkSessionIdAndResolved(sessionId, false).stream()
                .findFirst()
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No active emergency found for walk session with id: " + sessionId));
    }

    @Override
    public EmergencyDto getActiveEmergencyByWalkSessionIdAndUserId(Long sessionId, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Walk session not found with id: " + sessionId));
        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
        }
        return emergencyRepository.findByWalkSessionIdAndResolved(sessionId, false).stream()
                .findFirst()
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No active emergency found for walk session with id: " + sessionId));
    }

    @Override
    public long countEmergencyByTriggerSource(String source) {
        return emergencyRepository.countByTriggerSource(resolveTriggerSource(source));
    }

    @Override
    @Transactional
    public EmergencyDto updateEmergencyResolveById(Long id, Boolean resolved) {
        return emergencyRepository.findById(id)
                .map(emergency -> {
                    emergency.setResolved(resolved);
                    emergency.setResolvedAt(LocalDateTime.now()); // timestamp of last change to `resolved`, not "when it became true"
                    Emergency updated = emergencyRepository.save(emergency);
                    return convertToDto(updated);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteEmergencyById(Long id) {
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency not found with id: " + id));

        WalkSession session = emergency.getWalkSession();

        if (session.getStatus() == SessionStatus.EMERGENCY) {
            session.setStatus(SessionStatus.ACTIVE);

            switch (emergency.getTriggerSource()) {
                case IDLE_TIMEOUT -> session.setAlarmTriggered(false);
                case ROUTE_DEVIATION -> {
                    session.setDeviationTriggered(false);
                    session.setDeviationTriggeredAt(null);
                }
                case CONNECTION_LOST -> walkSessionConnectionRegistry.clearDisconnect(session.getId());
                default -> { /* MANUAL_SOS, SYSTEM — no per-trigger state to clear */ }
            }

            session.setLastLocationUpdate(LocalDateTime.now());
            walkSessionRepository.save(session);
        }

        emergencyRepository.deleteById(id);
    }

    @Override
    public long countEmergencyByWalkSessionUserIdAndResolved(Long walkSessionUserId, Boolean resolved) {
        SecurityUtils.checkOwnershipOrAdmin(walkSessionUserId);
        return emergencyRepository.countEmergenciesByWalkSession_User_IdAndResolved(walkSessionUserId, resolved);
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
                        String htmlBody = EmailTemplates.emergencyAlert(user.getFirstName()+ " " + user.getLastName(),
                                trackingLink, authorityLine);
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

