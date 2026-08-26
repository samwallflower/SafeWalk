package com.samwallflower.safewalk.scheduler;

import com.samwallflower.safewalk.service.anomaly.IAnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnomalyDetectionScheduler {
    private final IAnomalyDetectionService anomalyDetectionService;

    @Scheduled(fixedRate = 30000)
    public void run(){
        anomalyDetectionService.checkAllActiveSessions();
    }
}
