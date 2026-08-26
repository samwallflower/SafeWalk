package com.samwallflower.safewalk.service.anomaly;

import com.samwallflower.safewalk.model.WalkSession;

public interface IAnomalyDetectionService {
    void checkAllActiveSessions();
    boolean checkArrival(WalkSession session);
    void checkIdleTimeout(WalkSession session);
    void checkRouteDeviation(WalkSession session);
    void checkConnectionLost(WalkSession session);
}
