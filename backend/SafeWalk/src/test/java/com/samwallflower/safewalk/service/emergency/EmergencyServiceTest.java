package com.samwallflower.safewalk.service.emergency;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.dto.EmergencyDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock private WalkSessionRepository walkSessionRepository;
    @Mock private EmergencyRepository emergencyRepository;
    @Mock private TwilioClient twilioClient;
    @Mock private INotificationService notificationService;
    @Mock private IEmergencyAuthorityService emergencyAuthorityService;
    @Mock private WalkSessionConnectionRegistry connectionRegistry;
    @Mock private EmailService emailService;

    private EmergencyService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyService(walkSessionRepository, emergencyRepository, twilioClient,
                notificationService, emergencyAuthorityService, emailService, connectionRegistry, new ModelMapper());

        lenient().when(emergencyAuthorityService.findEmergencyAuthorityByLocation(anyDouble(), anyDouble()))
                .thenReturn(buildAuthorityDto());
        lenient().when(emergencyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private EmergencyAuthorityDto buildAuthorityDto() {
        EmergencyAuthorityDto dto = new EmergencyAuthorityDto();
        dto.setGeneralEmergencyNumber("112");
        dto.setPoliceNumber("107");
        dto.setAmbulanceNumber("104");
        return dto;
    }

    private User buildUser(Long id, List<EmergencyContact> contacts) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Jane");
        u.setLastName("Doe");
        u.setEmergencyContacts(contacts);
        return u;
    }

    private EmergencyContact buildContact(Long id, String phone, String email) {
        EmergencyContact c = new EmergencyContact();
        c.setId(id);
        c.setContactPhone(phone);
        c.setContactEmail(email);
        return c;
    }

    private WalkSession buildSession(Long id, User user, SessionStatus status) {
        WalkSession ws = new WalkSession();
        ws.setId(id);
        ws.setUser(user);
        ws.setStatus(status);
        ws.setLastKnownLatitude(47.5);
        ws.setLastKnownLongitude(21.6);
        return ws;
    }

    private Emergency buildEmergency(Long id, WalkSession session, EmergencyTriggerSource source, boolean resolved) {
        Emergency e = new Emergency();
        e.setId(id);
        e.setWalkSession(session);
        e.setTriggerSource(source);
        e.setResolved(resolved);
        return e;
    }

    // --- triggerEmergencyByUser & triggerEmergencySystem Tests ---

    @Test
    void triggerEmergencyByUser_throws_whenNotOwner() {
        User owner = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, owner, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.triggerEmergencyByUser(10L, 999L))
                .isInstanceOf(ResourceProcessingException.class);

        verifyNoInteractions(twilioClient, emailService);
    }

    @Test
    void triggerEmergency_success_notifiesViaSmsAndEmail() {
        EmergencyContact contact = buildContact(1L, "+3611111111", "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.EMERGENCY);
        verify(twilioClient).sendSms(eq("+3611111111"), anyString());
        verify(emailService).sendEmail(eq("friend@example.com"), anyString(), anyString());
        verify(emergencyRepository).save(argThat(e -> e.getNotifiedEmergencyContacts().contains(contact)));
        verify(notificationService).pushEmergencyAlert(eq(10L), any());
    }

    @Test
    void triggerEmergency_smsOnly_whenContactHasNoEmail() {
        EmergencyContact contact = buildContact(1L, "+3611111111", null);
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        verify(twilioClient).sendSms(eq("+3611111111"), anyString());
        verifyNoInteractions(emailService);
    }

    @Test
    void triggerEmergency_emailOnly_whenContactHasNoPhone() {
        EmergencyContact contact = buildContact(1L, null, "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        verifyNoInteractions(twilioClient);
        verify(emailService).sendEmail(eq("friend@example.com"), anyString(), anyString());
    }

    @Test
    void triggerEmergency_contactNotCountedAsNotified_whenBothChannelsFail() {
        EmergencyContact contact = buildContact(1L, "+3611111111", "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        doThrow(new ResourceProcessingException("sms failed")).when(twilioClient).sendSms(anyString(), anyString());
        doThrow(new ResourceProcessingException("email failed")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        service.triggerEmergencyByUser(10L, 1L);

        verify(emergencyRepository).save(argThat(e -> e.getNotifiedEmergencyContacts().isEmpty()));
    }

    @Test
    void triggerEmergency_doesNotThrow_whenNoContactsAtAll() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.EMERGENCY);
        verify(emergencyRepository).save(any());
        verify(notificationService).pushEmergencyAlert(eq(10L), any());
    }

    @Test
    void triggerEmergency_isIdempotent_whenAlreadyInEmergencyStatus() {
        User user = buildUser(1L, List.of(buildContact(1L, "+3611111111", null)));
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        verifyNoInteractions(twilioClient, emailService, emergencyRepository);
    }

    @Test
    void triggerEmergency_throws_whenSessionCompleted() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.COMPLETED);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.triggerEmergencyByUser(10L, 1L))
                .isInstanceOf(ResourceProcessingException.class);
    }

    @Test
    void triggerEmergency_stillSendsNotifications_whenAuthorityLookupFails() {
        EmergencyContact contact = buildContact(1L, "+3611111111", null);
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyAuthorityService.findEmergencyAuthorityByLocation(anyDouble(), anyDouble()))
                .thenThrow(new ResourceNotFoundException("No authority for this country"));

        service.triggerEmergencyByUser(10L, 1L);

        verify(twilioClient).sendSms(anyString(), anyString());
        verify(emergencyRepository).save(any());
    }

    @Test
    void triggerEmergencySystem_success_noOwnershipCheck() {
        User user = buildUser(1L, List.of(buildContact(1L, "+3611111111", null)));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencySystem(10L, EmergencyTriggerSource.IDLE_TIMEOUT);

        verify(emergencyRepository).save(argThat(e -> e.getTriggerSource() == EmergencyTriggerSource.IDLE_TIMEOUT));
    }

    // --- addEmergency Tests ---

    @Test
    void addEmergency_success() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.addEmergency(10L, "MANUAL_SOS");

        verify(emergencyRepository).save(argThat(e -> e.getTriggerSource() == EmergencyTriggerSource.MANUAL_SOS));
    }

    @Test
    void addEmergency_throws_invalidSource() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.addEmergency(10L, "INVALID_SOURCE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Retrieval & Count Tests ---

    @Test
    void getAllEmergencies_success() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        when(emergencyRepository.findAll()).thenReturn(List.of(buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false)));

        List<EmergencyDto> results = service.getAllEmergencies();
        assertThat(results).hasSize(1);
    }

    @Test
    void getAllEmergenciesByTriggerSource_success() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        when(emergencyRepository.findByTriggerSource(EmergencyTriggerSource.IDLE_TIMEOUT))
                .thenReturn(List.of(buildEmergency(100L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false)));

        List<EmergencyDto> results = service.getAllEmergenciesByTriggerSource("IDLE_TIMEOUT");
        assertThat(results).hasSize(1);
    }

    @Test
    void getAllEmergenciesByWalkSessionId_success() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        when(emergencyRepository.findByWalkSessionId(10L))
                .thenReturn(List.of(buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false)));

        List<EmergencyDto> results = service.getAllEmergenciesByWalkSessionId(10L);
        assertThat(results).hasSize(1);
    }

    @Test
    void getEmergencyById_success() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        when(emergencyRepository.findById(100L))
                .thenReturn(Optional.of(buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false)));

        EmergencyDto dto = service.getEmergencyById(100L);
        assertThat(dto).isNotNull();
    }

    @Test
    void getEmergencyById_throws_notFound() {
        when(emergencyRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getEmergencyById(100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getActiveEmergencyByWalkSessionId_success() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        when(emergencyRepository.findByWalkSessionIdAndResolved(10L, false))
                .thenReturn(List.of(buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false)));

        EmergencyDto dto = service.getActiveEmergencyByWalkSessionId(10L);
        assertThat(dto).isNotNull();
    }

    @Test
    void getActiveEmergencyByWalkSessionId_throws_notFound() {
        when(emergencyRepository.findByWalkSessionIdAndResolved(10L, false)).thenReturn(List.of());
        assertThatThrownBy(() -> service.getActiveEmergencyByWalkSessionId(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void countEmergencyByTriggerSource_success() {
        when(emergencyRepository.countByTriggerSource(EmergencyTriggerSource.SYSTEM)).thenReturn(5L);
        long count = service.countEmergencyByTriggerSource("SYSTEM");
        assertThat(count).isEqualTo(5L);
    }

    // --- resolveEmergency Tests ---

    @Test
    void resolveEmergency_throws_alreadyResolved() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, true);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(100L, 10L, 1L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("already resolved");
    }

    @Test
    void resolveEmergency_throws_notOwner() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(100L, 10L, 999L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("does not belong to the user");
    }

    @Test
    void resolveEmergency_throws_notInEmergencyStatus() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.ACTIVE);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(100L, 10L, 1L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("not in EMERGENCY status");
    }

    @Test
    void resolveEmergency_throws_wrongSession() {
        WalkSession session = buildSession(10L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        WalkSession otherSession = buildSession(20L, buildUser(1L, List.of()), SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(100L, otherSession, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(100L, 10L, 1L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("does not belong to the walk session");
    }

    @Test
    void resolveEmergency_success_idleTimeout_resetsAlarm() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setAlarmTriggered(true);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(100L, 10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getAlarmTriggered()).isFalse();
        assertThat(emergency.getResolved()).isTrue();

        verify(walkSessionRepository).save(session);
        verify(emergencyRepository).save(emergency);
        verify(notificationService).pushEmergencyAlert(eq(10L), any());
    }

    @Test
    void resolveEmergency_success_routeDeviation_resetsDeviation() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setDeviationTriggered(true);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.ROUTE_DEVIATION, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(100L, 10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getDeviationTriggered()).isFalse();
        assertThat(emergency.getResolved()).isTrue();
    }

    @Test
    void resolveEmergency_success_connectionLost_clearsDisconnect() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.CONNECTION_LOST, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(100L, 10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        verify(connectionRegistry).clearDisconnect(10L);
    }

    @Test
    void resolveEmergency_success_notifiesContacts() {
        EmergencyContact contact = buildContact(1L, "+3611111111", "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(100L, 10L, 1L);

        verify(emailService).sendEmail(eq("friend@example.com"), eq("Emergency Resolved"), anyString());
        verify(twilioClient).sendSms(eq("+3611111111"), anyString());
        verify(notificationService).pushEmergencyAlert(eq(10L), any());
    }

    @Test
    void resolveEmergency_failsGracefullyWhenNotificationFails() {
        EmergencyContact contact = buildContact(1L, "+3611111111", "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(100L, session, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(100L)).thenReturn(Optional.of(emergency));

        doThrow(new ResourceProcessingException("Twilio Down")).when(twilioClient).sendSms(anyString(), anyString());
        doThrow(new ResourceProcessingException("Email Down")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Should not throw, should catch and continue resolving
        service.resolveEmergency(100L, 10L, 1L);

        assertThat(emergency.getResolved()).isTrue();
        verify(emergencyRepository).save(emergency);
    }
}