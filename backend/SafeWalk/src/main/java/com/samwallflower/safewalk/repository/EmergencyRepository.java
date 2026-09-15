package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import com.samwallflower.safewalk.model.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    List<Emergency> findByTriggerSource(EmergencyTriggerSource triggerSource);
    long countByTriggerSource(EmergencyTriggerSource triggerSource);
    // session -> Walk session
    List<Emergency> findByWalkSessionId(Long  sessionId);
}
