package com.samwallflower.safewalk.service.emergency;


public interface IEmergencyService {

    // User - triggered manual SOS - verifies the session belongs to the user first
    void triggerEmergencyByUser(Long sessionId, Long userId);

    // System-triggered emergency ( called by Anomaly Detection Scheduler)
    // no ownership needed since it's not a user - initiated HTTP call
    void triggerEmergencySystem(Long sessionId);
}
