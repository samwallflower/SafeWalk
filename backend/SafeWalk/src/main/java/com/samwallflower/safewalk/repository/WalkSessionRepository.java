package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.model.WalkSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalkSessionRepository extends JpaRepository<WalkSession, Long> {
    List<WalkSession> findByUserId(Long userId);
    List<WalkSession> findByStatus(SessionStatus status);
    List<WalkSession> findByUserAndStatus(Long userId, SessionStatus status);
    WalkSession findByUserAndId(Long userId, Long id);



}
