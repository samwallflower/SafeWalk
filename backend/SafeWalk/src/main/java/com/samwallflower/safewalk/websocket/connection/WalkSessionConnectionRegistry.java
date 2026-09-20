package com.samwallflower.safewalk.websocket.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class for registering fresh connection and
 * keeping track of them via their stomp id session
 * also for keeping track of disconnects
 */

@Slf4j
@Component
public class WalkSessionConnectionRegistry {
    // STOMP session id -> walk session id currently associated with it
    private final Map<String, Long> stompSessionToWalkSession = new ConcurrentHashMap<>();
    // walk session id -> when it's connection was lost ( absent while connected/never seen)
    private final Map<Long, LocalDateTime> disconnectedAt = new ConcurrentHashMap<>();

    // here we are registering fresh connections
    public void registerActivity(String stompSessionId, Long walkSessionId){
        stompSessionToWalkSession.put(stompSessionId, walkSessionId);
        disconnectedAt.remove(walkSessionId);
        log.info("Registered activity for STOMP session {} and walk session {}", stompSessionId, walkSessionId);
    }

    public void markDisconnected(String stompSessionId){
        Long walkSessionId = stompSessionToWalkSession.remove(stompSessionId);
        if(walkSessionId != null){
            disconnectedAt.put(walkSessionId, LocalDateTime.now());
            log.info("WebSocket disconnected for walk session {} as disconnected at {}", walkSessionId, LocalDateTime.now());
        }
    }

    public Optional<LocalDateTime> getDisconnectedAt(Long walkSessionId){
        return Optional.ofNullable(disconnectedAt.get(walkSessionId));
    }

    public void clearDisconnect(Long walkSessionId){
        disconnectedAt.remove(walkSessionId);
    }
}
