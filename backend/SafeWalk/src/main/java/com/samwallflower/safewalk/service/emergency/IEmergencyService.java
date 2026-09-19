package com.samwallflower.safewalk.service.emergency;


import com.samwallflower.safewalk.dto.EmergencyDto;
import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.model.Emergency;

import java.util.List;

public interface IEmergencyService {

    // User - triggered manual SOS - verifies the session belongs to the user first
    void triggerEmergencyByUser(Long sessionId, Long userId);

    // System-triggered emergency ( called by Anomaly Detection Scheduler)
    // no ownership needed since it's not a user - initiated HTTP call
    void triggerEmergencySystem(Long sessionId, EmergencyTriggerSource source);

    EmergencyDto addEmergency(Long sessionId, String source);

    List<EmergencyDto> getAllEmergencies();
    List<EmergencyDto> getAllEmergenciesByTriggerSource(EmergencyTriggerSource source);
    List<EmergencyDto> getAllEmergenciesByWalkSessionId(Long sessionId);

    EmergencyDto convertToDto(Emergency emergency);
}
