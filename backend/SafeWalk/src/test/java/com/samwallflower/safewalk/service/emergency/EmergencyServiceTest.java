package com.samwallflower.safewalk.service.emergency;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
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
import com.samwallflower.safewalk.support.AsAdmin;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AsAdmin
class EmergencyServiceTest {

    @Mock private WalkSessionRepository walkSessionRepository;
    @Mock private EmergencyRepository emergencyRepository;
    @Mock private TwilioClient twilioClient;
    @Mock private INotificationService notificationService;
    @Mock private IEmergencyAuthorityService emergencyAuthorityService;
    @Mock private EmailService emailService;
    @Mock private WalkSessionConnectionRegistry connectionRegistry;

    private EmergencyService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyService(walkSessionRepository, emergencyRepository, twilioClient,
                notificationService, emergencyAuthorityService, emailService, connectionRegistry, new ModelMapper());

        lenient().when(emergencyAuthorityService.findEmergencyAuthorityByLocation(anyDouble(), anyDouble()))
                .thenReturn(buildAuthorityDto());
        lenient().when(emergencyRepository.save(any())).thenAnswer(inv -> {
            Emergency e = inv.getArgument(0);
            if (e.getId() == null) e.setId(100L); // simulate DB assigning an id on first save
            return e;
        });
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
        ws.setAlarmTriggered(false);
        ws.setDeviationTriggered(false);
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

    // ---------- triggerEmergencyByUser ----------

    @Test
    void triggerEmergencyByUser_throws_whenNotOwner() {
        User owner = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, owner, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.triggerEmergencyByUser(10L, 999L))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(twilioClient, emailService);
    }

    @Test
    void triggerEmergencyByUser_returnsNull_whenAlreadyEmergency() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        var result = service.triggerEmergencyByUser(10L, 1L);

        assertThat(result).isNull();
        verifyNoInteractions(twilioClient, emailService, emergencyRepository);
    }

    @Test
    void triggerEmergencyByUser_throws_whenSessionCompleted() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.COMPLETED);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.triggerEmergencyByUser(10L, 1L))
                .isInstanceOf(ResourceProcessingException.class);
    }

    @Test
    void triggerEmergencyByUser_success_savesBeforePushingAlert_withRealEmergencyId() {
        EmergencyContact contact = buildContact(1L, "+3611111111", "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.EMERGENCY);
        verify(twilioClient).sendSms(eq("+3611111111"), anyString());
        verify(emailService).sendEmail(eq("friend@example.com"), anyString(), anyString());

        ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
        verify(notificationService).pushEmergencyAlert(eq(10L), captor.capture());
        assertThat(captor.getValue().getEmergencyId()).isEqualTo(100L); // the simulated saved id
        assertThat(captor.getValue().getSessionId()).isEqualTo(10L);
    }

    @Test
    void triggerEmergencyByUser_doesNotThrow_whenNoContacts() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        service.triggerEmergencyByUser(10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.EMERGENCY);
        verify(emergencyRepository).save(any());
        verify(notificationService).pushEmergencyAlert(eq(10L), any());
    }

    @Test
    void triggerEmergencyByUser_stillNotifies_whenAuthorityLookupFails() {
        EmergencyContact contact = buildContact(1L, "+3611111111", null);
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyAuthorityService.findEmergencyAuthorityByLocation(anyDouble(), anyDouble()))
                .thenThrow(new ResourceNotFoundException("No authority"));

        service.triggerEmergencyByUser(10L, 1L);

        verify(twilioClient).sendSms(anyString(), anyString());
    }

    // ---------- resolveEmergency ----------

    @Test
    void resolveEmergency_throws_whenAlreadyResolved() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, true);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(5L, 10L, 1L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("already resolved");
    }

    @Test
    void resolveEmergency_throws_whenNotOwner() {
        User owner = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, owner, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(5L, 10L, 999L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void resolveEmergency_throws_whenSessionNotInEmergencyStatus() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(5L, 10L, 1L))
                .isInstanceOf(ResourceProcessingException.class);
    }

    @Test
    void resolveEmergency_throws_whenEmergencyBelongsToDifferentSession() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        WalkSession otherSession = buildSession(20L, user, SessionStatus.ACTIVE);
        Emergency emergency = buildEmergency(5L, otherSession, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        assertThatThrownBy(() -> service.resolveEmergency(5L, 10L, 1L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void resolveEmergency_idleTimeout_resetsAlarmTriggered() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setAlarmTriggered(true);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(5L, 10L, 1L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getAlarmTriggered()).isFalse();
        assertThat(emergency.getResolved()).isTrue();
        assertThat(emergency.getResolvedAt()).isNotNull();
    }

    @Test
    void resolveEmergency_idleTimeout_restartsTheIdleClock() {
        // after "I'm safe" the user may still be standing still: the idle clock must restart,
        // otherwise the next scheduler tick would flag them again immediately
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setAlarmTriggered(true);
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        session.setLastMovementDetectedAt(tenMinutesAgo);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(5L, 10L, 1L);

        assertThat(session.getLastMovementDetectedAt()).isAfter(tenMinutesAgo);
    }

    @Test
    void resolveEmergency_routeDeviation_resetsDeviationState() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setDeviationTriggered(true);
        session.setDeviationTriggeredAt(LocalDateTime.now());
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.ROUTE_DEVIATION, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(5L, 10L, 1L);

        assertThat(session.getDeviationTriggered()).isFalse();
        assertThat(session.getDeviationTriggeredAt()).isNull();
    }

    @Test
    void resolveEmergency_connectionLost_clearsRegistry() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.CONNECTION_LOST, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(5L, 10L, 1L);

        verify(connectionRegistry).clearDisconnect(10L);
    }

    @Test
    void resolveEmergency_pushesAlert_withEmergencyIdAndSessionId() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(5L, 10L, 1L);

        ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
        verify(notificationService).pushEmergencyAlert(eq(10L), captor.capture());
        assertThat(captor.getValue().getEmergencyId()).isEqualTo(5L);
        assertThat(captor.getValue().getSessionId()).isEqualTo(10L);
    }

    @Test
    void resolveEmergency_notifiesContactsOfResolution() {
        EmergencyContact contact = buildContact(1L, "+3611111111", "friend@example.com");
        User user = buildUser(1L, List.of(contact));
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.MANUAL_SOS, false);

        when(walkSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.resolveEmergency(5L, 10L, 1L);

        verify(twilioClient).sendSms(eq("+3611111111"), contains("safe"));
        verify(emailService).sendEmail(eq("friend@example.com"), eq("Emergency Resolved"), anyString());
    }

    // ---------- getActiveEmergencyByWalkSessionId ----------

    @Test
    void getActiveEmergencyByWalkSessionId_returnsUnresolvedOne() {
        Emergency emergency = buildEmergency(5L, null, EmergencyTriggerSource.MANUAL_SOS, false);
        when(emergencyRepository.findByWalkSessionIdAndResolved(10L, false))
                .thenReturn(List.of(emergency));

        var dto = service.getActiveEmergencyByWalkSessionId(10L);

        assertThat(dto.getId()).isEqualTo(5L);
    }

    @Test
    void getActiveEmergencyByWalkSessionId_throws_whenNoneUnresolved() {
        when(emergencyRepository.findByWalkSessionIdAndResolved(10L, false)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getActiveEmergencyByWalkSessionId(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- countEmergencyByTriggerSource ----------

    @Test
    void countEmergencyByTriggerSource_delegatesCorrectly() {
        when(emergencyRepository.countByTriggerSource(EmergencyTriggerSource.IDLE_TIMEOUT)).thenReturn(7L);

        long count = service.countEmergencyByTriggerSource("idle_timeout"); // lowercase input

        assertThat(count).isEqualTo(7L);
    }

    // ---------- updateEmergencyResolveById (with the fix applied) ----------

    @Test
    void updateEmergencyResolveById_setsResolvedAtOnlyWhenResolvedTrue() {
        Emergency emergency = buildEmergency(5L, null, EmergencyTriggerSource.MANUAL_SOS, false);
        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.updateEmergencyResolveById(5L, true);

        assertThat(emergency.getResolved()).isTrue();
        assertThat(emergency.getResolvedAt()).isNotNull();
    }


    // ---------- deleteEmergencyById (with the fix applied) ----------

    @Test
    void deleteEmergencyById_resetsIdleState_whenDeletingActiveIdleEmergency() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setAlarmTriggered(true);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.deleteEmergencyById(5L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getAlarmTriggered()).isFalse();
        verify(emergencyRepository).deleteById(5L);
    }

    @Test
    void deleteEmergencyById_idleTimeout_restartsTheIdleClock() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        session.setAlarmTriggered(true);
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        session.setLastMovementDetectedAt(tenMinutesAgo);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.IDLE_TIMEOUT, false);

        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.deleteEmergencyById(5L);

        assertThat(session.getLastMovementDetectedAt()).isAfter(tenMinutesAgo);
    }

    @Test
    void deleteEmergencyById_clearsConnectionRegistry_whenDeletingConnectionLostEmergency() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.EMERGENCY);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.CONNECTION_LOST, false);

        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.deleteEmergencyById(5L);

        verify(connectionRegistry).clearDisconnect(10L);
    }

    @Test
    void deleteEmergencyById_doesNotTouchSession_whenSessionNotInEmergency() {
        User user = buildUser(1L, List.of());
        WalkSession session = buildSession(10L, user, SessionStatus.ACTIVE);
        Emergency emergency = buildEmergency(5L, session, EmergencyTriggerSource.MANUAL_SOS, true);

        when(emergencyRepository.findById(5L)).thenReturn(Optional.of(emergency));

        service.deleteEmergencyById(5L);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE); // unchanged
        verify(walkSessionRepository, never()).save(any());
        verify(emergencyRepository).deleteById(5L);
    }

    @Test
    void deleteEmergencyById_throws_whenNotFound() {
        when(emergencyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEmergencyById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}