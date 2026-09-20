package com.samwallflower.safewalk.websocket.listener;

import com.samwallflower.safewalk.websocket.connection.WalkSessionConnectionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WalkSessionWebSocketEventListener {
    private final WalkSessionConnectionRegistry connectionRegistry;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String stompSessionId = accessor.getSessionId();
        connectionRegistry.markDisconnected(stompSessionId);
    }
}
