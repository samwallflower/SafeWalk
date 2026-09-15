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
import com.samwallflower.safewalk.service.emergencyauthority.IEmergencyAuthorityService;
import com.samwallflower.safewalk.service.notification.INotificationService;
import com.samwallflower.safewalk.websocket.AlertMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public void triggerEmergencyByUser(Long sessionId, Long userId) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        if(!session.getUser().getId().equals(userId)){
            throw new ResourceProcessingException("Walk session with id: " + sessionId + " does not belong to the user with id: " + userId);
        }

        executeEmergencyProtocol(session, EmergencyTriggerSource.MANUAL_SOS);

    }

    @Override
    @Transactional
    public void triggerEmergencySystem(Long sessionId, EmergencyTriggerSource source) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));

        executeEmergencyProtocol(session, source);

    }

    @Override
    public EmergencyDto createEmergency(Long sessionId, EmergencyTriggerSource source) {
        WalkSession session = walkSessionRepository.findById(sessionId)
                .orElseThrow(()-> new ResourceNotFoundException("Walk session not found with id: " + sessionId));
        Emergency emergency = createEmergency(session, source);
        return convertToDto(emergencyRepository.save(emergency));
    }

    @Override
    public List<EmergencyDto> getAllEmergencies() {
        return emergencyRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<EmergencyDto> getAllEmergenciesByTriggerSource(EmergencyTriggerSource source) {
        return emergencyRepository.findByTriggerSource(source).stream()
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
    public EmergencyDto convertToDto(Emergency emergency) {
        return modelMapper.map(emergency, EmergencyDto.class);
    }

    private void executeEmergencyProtocol(WalkSession session, EmergencyTriggerSource source) {
        if (session.getStatus()== SessionStatus.EMERGENCY){
            log.info("Session {} is already in EMERGENCY status - skipping duplicate trigger", session.getId());
            return;
        }
        if(session.getStatus()==SessionStatus.COMPLETED || session.getStatus()==SessionStatus.ABANDONED){
            log.info("Session is in {} status", session.getStatus());
            throw new ResourceProcessingException("Session in "+ session.getStatus() +" status.");
        }
        session.setStatus(SessionStatus.EMERGENCY);
        walkSessionRepository.save(session);

        User user = session.getUser();
        List<EmergencyContact> contacts = user.getEmergencyContacts();
        if(contacts==null || contacts.isEmpty()){
            log.warn("User {} has no emergency contacts to notify for session {}", user.getId(), session.getId());
            throw new ResourceNotFoundException("No emergency contacts found for user with id: " + user.getId());
        }else{
            String trackingLink = buildTrackingLink(session);
            String authorityLine = buildAuthorityLine(session);
            String smsBody = String.format(
                    "%s %s may need help. Live location: %s",
                    user.getFirstName(), user.getLastName(), trackingLink
            );

            for (EmergencyContact emergencyContact : contacts) {
                try {
                    twilioClient.sendSms(emergencyContact.getContactPhone(), smsBody);
                    log.info("Emergency SMS sent to contact {} for session {}", emergencyContact.getId(), session.getId());
                }catch (ResourceProcessingException e){
                    log.error("Failed to notify contact {} for session {}: {}",
                            emergencyContact.getId(), session.getId(), e.getMessage());
                }
            }
        }

        Emergency emergency = emergencyRepository.save(createEmergency(session, source));

        AlertMessage alert = new AlertMessage(
                session.getId(),
                AlertMessageType.EMERGENCY_TRIGGERED,
                "Emergency protocol activated for this session."
        );
        notificationService.pushEmergencyAlert(session.getId(), alert);

        log.info("Emergency protocol executed for session {}", session.getId());


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
        try{
            EmergencyAuthorityDto authority = emergencyAuthorityService.findEmergencyAuthorityByLocation(
                    session.getLastKnownLatitude(),
                    session.getLastKnownLongitude()
            );
            return String.format("Nearest emergency number: %s, Police Phone Number: %s, Ambulance Number: %s",
                    authority.getGeneralEmergencyNumber(), authority.getPoliceNumber(), authority.getAmbulanceNumber());
        }catch (ResourceNotFoundException e){
            log.warn("No emergency authority found for session {}", session.getId());
            throw e;
        }
    }

    private String buildTrackingLink(WalkSession session) {
        return String.format("https://maps.google.com/?q=%s,%s",
                session.getLastKnownLatitude(),
                session.getLastKnownLongitude());
    }
}
