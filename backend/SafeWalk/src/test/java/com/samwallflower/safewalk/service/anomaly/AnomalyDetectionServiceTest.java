package com.samwallflower.safewalk.service.anomaly;

import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.model.Route;
import com.samwallflower.safewalk.model.WalkSession;
import com.samwallflower.safewalk.repository.WalkSessionRepository;
import com.samwallflower.safewalk.service.emergency.IEmergencyService;
import com.samwallflower.safewalk.service.notification.INotificationService;
import com.samwallflower.safewalk.websocket.connection.WalkSessionConnectionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock private WalkSessionRepository walkSessionRepository;
    @Mock private INotificationService notificationService;
    @Mock private IEmergencyService emergencyService;
    @Mock private WalkSessionConnectionRegistry connectionRegistry;

    private AnomalyDetectionService service;

    @BeforeEach
    void setUp() {
        service = new AnomalyDetectionService(walkSessionRepository, notificationService, emergencyService, connectionRegistry);
        ReflectionTestUtils.setField(service, "arrivalRadiusMeters", 30.0);
        ReflectionTestUtils.setField(service, "autoEndGracePeriodSeconds", 600L);
        ReflectionTestUtils.setField(service, "idleThresholdSeconds", 180L);
        ReflectionTestUtils.setField(service, "alarmGracePeriodSeconds", 45L);
        ReflectionTestUtils.setField(service, "deviationWarningThresholdMeters", 50.0);
        ReflectionTestUtils.setField(service, "deviationEmergencyThresholdMeters", 200.0);
        ReflectionTestUtils.setField(service, "wsTimeoutSeconds", 60L);
    }

    private WalkSession buildSession(Long id, double lastLat, double lastLng, double destLat, double destLng) {
        WalkSession ws = new WalkSession();
        ws.setId(id);
        ws.setLastKnownLatitude(lastLat);
        ws.setLastKnownLongitude(lastLng);
        ws.setDestinationLatitude(destLat);
        ws.setDestinationLongitude(destLng);
        ws.setStatus(SessionStatus.ACTIVE);
        return ws;
    }

    // ---------- checkArrival ----------

    @Test
    void checkArrival_returnsFalse_whenFarFromDestination() {
        WalkSession session = buildSession(1L, 47.4, 21.4, 47.5, 21.6); // far apart
        boolean result = service.checkArrival(session);
        assertThat(result).isFalse();
    }

    @Test
    void checkArrival_returnsTrue_andSetsLastArrivedAt_whenFirstEnteringRadius() {
        WalkSession session = buildSession(1L, 47.5316, 21.6273, 47.5316, 21.6273); // exact same point
        session.setLastArrivedAt(null);

        boolean result = service.checkArrival(session);

        assertThat(result).isTrue();
        assertThat(session.getLastArrivedAt()).isNotNull();
        verify(walkSessionRepository).save(session);
    }

    @Test
    void checkArrival_autoCompletes_whenGracePeriodElapsed() {
        WalkSession session = buildSession(1L, 47.5316, 21.6273, 47.5316, 21.6273);
        session.setLastArrivedAt(LocalDateTime.now().minusSeconds(1000)); // past 600s grace period

        boolean result = service.checkArrival(session);

        assertThat(result).isTrue();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getAutoCompleted()).isTrue();
        verify(notificationService).pushAutoCompleteAlert(1L);
    }

    @Test
    void checkArrival_doesNotAutoComplete_beforeGracePeriodElapsed() {
        WalkSession session = buildSession(1L, 47.5316, 21.6273, 47.5316, 21.6273);
        session.setLastArrivedAt(LocalDateTime.now().minusSeconds(100)); // well under 600s

        service.checkArrival(session);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        verify(notificationService, never()).pushAutoCompleteAlert(anyLong());
    }

    @Test
    void checkArrival_clearsLastArrivedAt_whenMovedBackOutOfRadius() {
        WalkSession session = buildSession(1L, 47.4, 21.4, 47.5, 21.6); // far
        session.setLastArrivedAt(LocalDateTime.now()); // was previously near

        boolean result = service.checkArrival(session);

        assertThat(result).isFalse();
        assertThat(session.getLastArrivedAt()).isNull();
        verify(walkSessionRepository).save(session);
    }

    // ---------- checkIdleTimeout ----------

    @Test
    void checkIdleTimeout_doesNothing_whenLastUpdateRecent() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        session.setLastLocationUpdate(LocalDateTime.now());

        service.checkIdleTimeout(session);

        verify(notificationService, never()).pushIdleWarning(anyLong());
        verifyNoInteractions(emergencyService);
    }

    @Test
    void checkIdleTimeout_triggersWarning_whenFirstExceedingThreshold() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        session.setLastLocationUpdate(LocalDateTime.now().minusSeconds(200)); // > 180s
        session.setAlarmTriggered(false);

        service.checkIdleTimeout(session);

        assertThat(session.getAlarmTriggered()).isTrue();
        verify(notificationService).pushIdleWarning(1L);
        verifyNoInteractions(emergencyService);
    }

    @Test
    void checkIdleTimeout_triggersEmergency_whenGracePeriodAlsoElapsed() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        // 180 (threshold) + 50 (past 45s grace) = 230s
        session.setLastLocationUpdate(LocalDateTime.now().minusSeconds(230));
        session.setAlarmTriggered(true); // already warned

        service.checkIdleTimeout(session);

        verify(emergencyService).triggerEmergencySystem(1L, EmergencyTriggerSource.IDLE_TIMEOUT);
    }

    @Test
    void checkIdleTimeout_doesNotEscalate_whenAlarmTriggeredButGracePeriodNotYetElapsed() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        session.setLastLocationUpdate(LocalDateTime.now().minusSeconds(190)); // just past threshold
        session.setAlarmTriggered(true);

        service.checkIdleTimeout(session);

        verifyNoInteractions(emergencyService);
    }

    @Test
    void checkIdleTimeout_doesNothing_whenLastLocationUpdateNull() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        session.setLastLocationUpdate(null);

        service.checkIdleTimeout(session);

        verifyNoInteractions(notificationService, emergencyService);
    }

    // ---------- checkRouteDeviation ----------

    @Test
    void checkRouteDeviation_doesNothing_whenNoRouteAttached() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        session.setRoute(null);

        service.checkRouteDeviation(session);

        verifyNoInteractions(notificationService, emergencyService);
    }

    @Test
    void checkRouteDeviation_triggersWarning_whenBeyondWarningThreshold() {
        // A short line near the origin should produce a warning, not an emergency.
        Route route = new Route();
        route.setPolyline("cBcB?gE");

        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        session.setRoute(route);

        service.checkRouteDeviation(session);

        verify(notificationService).pushRouteDeviationWarning(1L);
        verifyNoInteractions(emergencyService);
    }

    // ---------- checkConnectionLost ----------

    @Test
    void checkConnectionLost_doesNothing_whenNeverDisconnected() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        when(connectionRegistry.getDisconnectedAt(1L)).thenReturn(Optional.empty());

        service.checkConnectionLost(session);

        verifyNoInteractions(emergencyService);
    }

    @Test
    void checkConnectionLost_doesNothing_whenDisconnectedRecently() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        when(connectionRegistry.getDisconnectedAt(1L)).thenReturn(Optional.of(LocalDateTime.now().minusSeconds(10)));

        service.checkConnectionLost(session);

        verifyNoInteractions(emergencyService);
    }

    @Test
    void checkConnectionLost_triggersEmergency_whenDisconnectedPastTimeout() {
        WalkSession session = buildSession(1L, 0, 0, 1, 1);
        when(connectionRegistry.getDisconnectedAt(1L)).thenReturn(Optional.of(LocalDateTime.now().minusSeconds(70)));

        service.checkConnectionLost(session);

        verify(emergencyService).triggerEmergencySystem(1L, EmergencyTriggerSource.CONNECTION_LOST);
    }

    // ---------- checkAllActiveSessions ----------

    @Test
    void checkAllActiveSessions_skipsIdleAndDeviation_whenNearDestination() {
        WalkSession session = buildSession(1L, 47.5316, 21.6273, 47.5316, 21.6273); // exact match -> arrived
        session.setLastLocationUpdate(LocalDateTime.now().minusSeconds(999)); // would normally trigger idle
        when(walkSessionRepository.findByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(session));
        when(connectionRegistry.getDisconnectedAt(1L)).thenReturn(Optional.empty());

        service.checkAllActiveSessions();

        verify(notificationService, never()).pushIdleWarning(anyLong());
    }

    @Test
    void checkAllActiveSessions_continuesProcessing_whenOneSessionThrows() {
        WalkSession broken = buildSession(1L, 47.5316, 21.6273, 47.5316, 21.6273);
        broken.setLastLocationUpdate(LocalDateTime.now().minusSeconds(300));
        broken.setLastArrivedAt(null);
        broken.setRoute(null);

        WalkSession good = buildSession(2L, 0, 0, 1, 1);
        good.setLastLocationUpdate(LocalDateTime.now());

        when(walkSessionRepository.findByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(broken, good));
        when(connectionRegistry.getDisconnectedAt(anyLong())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("boom")).when(walkSessionRepository).save(broken);

        service.checkAllActiveSessions();

        verify(connectionRegistry).getDisconnectedAt(2L);
    }
}