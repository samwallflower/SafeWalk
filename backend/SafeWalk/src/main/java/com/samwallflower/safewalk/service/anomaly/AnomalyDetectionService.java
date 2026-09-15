package com.samwallflower.safewalk.service.anomaly;

import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.model.WalkSession;
import com.samwallflower.safewalk.repository.WalkSessionRepository;
import com.samwallflower.safewalk.service.emergency.IEmergencyService;
import com.samwallflower.safewalk.service.notification.INotificationService;
import com.samwallflower.safewalk.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService implements IAnomalyDetectionService {

    private final WalkSessionRepository  walkSessionRepository;
    private final INotificationService notificationService;
    private final IEmergencyService emergencyService;

    @Value("${app.anomaly.arrival-radius-meters}")
    private double arrivalRadiusMeters;

    @Value("${app.anomaly.auto-end-grace-period-seconds}")
    private long autoEndGracePeriodSeconds;

    @Value("${app.anomaly.idle-threshold-seconds}")
    private long idleThresholdSeconds;

    @Value("${app.anomaly.alarm-grace-period-seconds}")
    private long alarmGracePeriodSeconds;

    @Value("${app.anomaly.deviation-warning-threshold-meters}")
    private double deviationWarningThresholdMeters;

    @Value("${app.anomaly.deviation-emergency-threshold-meters}")
    private double deviationEmergencyThresholdMeters;

    @Value("${app.anomaly.ws-timeout-seconds}")
    private long wsTimeoutSeconds;


    // so for all active session we are checking for anomalies
    // check idle time out - if we get no location update for a set amount of time
    // for connection loss check we wanna check even if the user is near destination
    // bcz even near destination we would like to make sure their phone hasn't lost connection
    @Override
    public void checkAllActiveSessions() {
        List<WalkSession> activeSessions = walkSessionRepository.findByStatus(SessionStatus.ACTIVE);
        log.debug("Running anomaly detection on {} active sessions", activeSessions.size());

        for(WalkSession session : activeSessions) {
            try{
                boolean nearDestination = checkArrival(session);
                checkConnectionLost(session); // always checking for added robustness
                if(nearDestination) {
                    continue;
                }
                checkIdleTimeout(session);
                checkRouteDeviation(session);

            }catch(Exception e){
                log.error("Anomaly detection failed for session {} : {}",session.getId(), e.getMessage());
            }
        }

    }

    /**
     * Arrival detection: returns true if the session is currently within
     * the arrival radius of its destination ( regardless of whether the
     * auto end grace period has elapsed yet) Sessions near their
     * destinations are expected to be stationary, so callers should skip
     * idle/deviation/connection checks if this returns true
     * @param session
     * @return
     */

    @Override
    public boolean checkArrival(WalkSession session) {
        double distance = GeoUtils.haversineMeters(
                session.getLastKnownLatitude(), session.getLastKnownLongitude(),
                session.getDestinationLatitude(), session.getDestinationLongitude()
        );
        if(distance <= arrivalRadiusMeters) {
            if(session.getLastArrivedAt()==null){
                session.setLastArrivedAt(LocalDateTime.now());
                walkSessionRepository.save(session);
                log.info("Session {} entered arrival radius", session.getId());
            }else{
                // meaning they arrived already
                long secondsSinceArrival= SECONDS.between(session.getLastArrivedAt(), LocalDateTime.now());
                if(secondsSinceArrival > autoEndGracePeriodSeconds){
                    autocompleteSession(session);
                }
            }
            // if someone is within 30 meters we are confirming they arrived
            // technically we are no longer checking for any anomaly
            // is that a great choice?
            return true;
        }else {
            // so if someone is at 31 meters or greater distance from their destination
            // we check if their last arrival is already set or no
            // if it is set we unset it
            if(session.getLastArrivedAt()!=null){
                session.setLastArrivedAt(null);
                walkSessionRepository.save(session);
            }
            return false;
        }
    }

    private void autocompleteSession(WalkSession session) {
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        session.setLastArrivedAt(LocalDateTime.now());
        session.setAutoCompleted(true);
        walkSessionRepository.save(session);
        log.info("Session {} auto completed after arrival grace period", session.getId());
        notificationService.pushAutoCompleteAlert(session.getId());
    }

    /***
     *EC-2: Stationary Emergency (User is attacked but still on route)
     * Trigger: GPS static for ≥3 minutes without user pausing the session
     * Solution:
     * - AnomalyDetectionService compares `lastLocationUpdate` to now
     * - Threshold: `NOW - lastLocationUpdate > 3 min AND alarmTriggered = false`
     * - Action: Push `IDLE_WARNING` to user's WebSocket channel; set `alarmTriggered = true`
     * - Grace period: 45 seconds — user must tap "I'm OK" button in UI to reset
     * - If unacknowledged: Trigger `EmergencyProtocol`
     * @param session
     */
    @Override
    public void checkIdleTimeout(WalkSession session) {
        if(session.getLastLocationUpdate()==null) return;
        long secondsSinceUpdate = SECONDS.between(session.getLastLocationUpdate(), LocalDateTime.now());

        if(secondsSinceUpdate > idleThresholdSeconds){
            if(!session.getAlarmTriggered()){
                session.setAlarmTriggered(true);
                walkSessionRepository.save(session);
                notificationService.pushIdleWarning(session.getId());
                log.info("Idle warning triggered for session {}", session.getId());

            }else{
                // so alarm is already triggered
                // and still no update
                // we check seconds since last update against idle threshold and
                // start emergency protocol if it exceeds alarm grace period
                long secondsSinceThreshold = secondsSinceUpdate - idleThresholdSeconds;
                if (secondsSinceThreshold > alarmGracePeriodSeconds) {
                    log.warn("Session {} unresponsive past grace period - triggering emergency", session.getId());
                    emergencyService.triggerEmergencySystem(session.getId());
                }
            }
        }

    }

    @Override
    public void checkRouteDeviation(WalkSession session) {
        if (session.getRoute() == null || session.getRoute().getPolyline() == null) return;

        List<LatLng> points = new EncodedPolyline(session.getRoute().getPolyline()).decodePath();
        if(points.size()<2) return;

        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng start  = points.get(i);
            LatLng end = points.get(i+1);
            double distance = GeoUtils.distanceToSegmentMeters(
                    session.getLastKnownLatitude(),session.getLastKnownLongitude(),
                    start.lat, start.lng, end.lat, end.lng
            );
            minDistance = Math.min(minDistance, distance);

        }

        if(minDistance > deviationEmergencyThresholdMeters){
            log.warn("Session {} deviated {}m from route - triggering emergency", session.getId(), minDistance);
            emergencyService.triggerEmergencySystem(session.getId());
        } else if (minDistance > deviationWarningThresholdMeters) {
            notificationService.pushRouteDeviationWarning(session.getId());
        }

    }

    @Override
    public void checkConnectionLost(WalkSession session) {
        if(session.getLastLocationUpdate()==null) return;
        long secondsSinceUpdate = SECONDS.between(session.getLastLocationUpdate(), LocalDateTime.now());
        if(secondsSinceUpdate > wsTimeoutSeconds){
            log.warn("Session {} has had no location update for {}s - treating as connection lost", session.getId(), secondsSinceUpdate);
            emergencyService.triggerEmergencySystem(session.getId());
        }

    }
}
