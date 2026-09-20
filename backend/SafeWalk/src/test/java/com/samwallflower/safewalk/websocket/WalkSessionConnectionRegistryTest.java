package com.samwallflower.safewalk.websocket;

import com.samwallflower.safewalk.websocket.connection.WalkSessionConnectionRegistry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class WalkSessionConnectionRegistryTest {

    private final WalkSessionConnectionRegistry registry = new WalkSessionConnectionRegistry();

    @Test
    void registerActivity_thenMarkDisconnected_recordsDisconnectTime() {
        registry.registerActivity("stomp-1", 5L);
        registry.markDisconnected("stomp-1");

        Optional<LocalDateTime> disconnectedAt = registry.getDisconnectedAt(5L);
        assertThat(disconnectedAt).isPresent();
    }

    @Test
    void getDisconnectedAt_returnsEmpty_whenNeverRegistered() {
        assertThat(registry.getDisconnectedAt(999L)).isEmpty();
    }

    @Test
    void registerActivity_clearsPriorDisconnectRecord() {
        registry.registerActivity("stomp-1", 5L);
        registry.markDisconnected("stomp-1");
        assertThat(registry.getDisconnectedAt(5L)).isPresent();

        registry.registerActivity("stomp-2", 5L); // reconnected
        assertThat(registry.getDisconnectedAt(5L)).isEmpty();
    }

    @Test
    void markDisconnected_isNoOp_forUnknownStompSession() {
        registry.markDisconnected("never-registered");
        // no exception, no phantom entries
    }

    @Test
    void clearDisconnect_removesRecord() {
        registry.registerActivity("stomp-1", 5L);
        registry.markDisconnected("stomp-1");
        registry.clearDisconnect(5L);
        assertThat(registry.getDisconnectedAt(5L)).isEmpty();
    }
}