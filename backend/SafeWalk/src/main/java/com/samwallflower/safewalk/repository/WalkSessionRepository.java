package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.model.WalkSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalkSessionRepository extends JpaRepository<WalkSession, Long> {
    List<WalkSession> findByUserId(Long userId);
    List<WalkSession> findByStatus(SessionStatus status);
    List<WalkSession> findByUserIdAndStatus(Long userId, SessionStatus status);
    Optional<WalkSession> findByUserIdAndId(Long userId, Long id);
    Optional<WalkSession> findByRouteIdAndUserId(Long routeId, Long userId);

    List<WalkSession> findByRouteId(Long routeId);

    Long user(User user);
}
