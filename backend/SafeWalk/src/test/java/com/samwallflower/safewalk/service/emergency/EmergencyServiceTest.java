package com.samwallflower.safewalk.service.emergency;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.integration.twilio.TwilioClient;
import com.samwallflower.safewalk.model.EmergencyContact;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.model.WalkSession;
import com.samwallflower.safewalk.repository.EmergencyRepository;
import com.samwallflower.safewalk.repository.WalkSessionRepository;
import com.samwallflower.safewalk.service.email.EmailService;
import com.samwallflower.safewalk.service.emergencyauthority.IEmergencyAuthorityService;
import com.samwallflower.safewalk.service.notification.INotificationService;
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
    @Mock private EmailService emailService;

    private EmergencyService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyService(walkSessionRepository, emergencyRepository, twilioClient,
                notificationService, emergencyAuthorityService, emailService, new ModelMapper());

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

        service.triggerEmergencyByUser(10L, 1L); // must NOT throw

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

        service.triggerEmergencyByUser(10L, 1L); // must NOT throw despite authority lookup failing

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
}