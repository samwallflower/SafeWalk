package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
import com.samwallflower.safewalk.websocket.LocationUpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalkSessionWebSocketControllerTest {

    @LocalServerPort
    private int port;

    // Real service is mocked so this test isolates WebSocket plumbing,
    // not WalkSessionService's own business logic (already covered separately)
    @MockitoBean
    private IWalkSessionService walkSessionService;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        SockJsClient sockJsClient = new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))
        );
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    }

    private String wsUrl() {
        return "ws://localhost:" + port + "/ws";
    }

    @Test
    void locationUpdate_broadcastsUpdatedSessionToTopic() throws Exception {
        WalkSessionDto mockedResponse = new WalkSessionDto();
        mockedResponse.setId(4L);
        mockedResponse.setLastKnownLatitude(47.53);
        mockedResponse.setLastKnownLongitude(21.6255);
        mockedResponse.setStatus(SessionStatus.ACTIVE);

        // hardcoded userId=1L in the controller — must match here
        when(walkSessionService.updateLocation(eq(4L), eq(1L), any()))
                .thenReturn(mockedResponse);

        BlockingQueue<WalkSessionDto> receivedMessages = new LinkedBlockingDeque<>();

        StompSession session = stompClient
                .connectAsync(wsUrl(), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/session/4", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WalkSessionDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((WalkSessionDto) payload);
            }
        });

        // give the subscription a moment to register server-side before publishing
        Thread.sleep(200);

        LocationUpdateMessage outgoing = new LocationUpdateMessage();
        outgoing.setSessionId(4L);
        outgoing.setLatitude(47.53);
        outgoing.setLongitude(21.6255);

        session.send("/app/session.location", outgoing);

        WalkSessionDto received = receivedMessages.poll(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.getId()).isEqualTo(4L);
        assertThat(received.getLastKnownLatitude()).isEqualTo(47.53);
        assertThat(received.getLastKnownLongitude()).isEqualTo(21.6255);

        session.disconnect();
    }

    @Test
    void locationUpdate_onlyDeliversToSubscribersOfThatSpecificSession() throws Exception {
        WalkSessionDto response = new WalkSessionDto();
        response.setId(4L);

        when(walkSessionService.updateLocation(eq(4L), eq(1L), any()))
                .thenReturn(response);

        BlockingQueue<WalkSessionDto> wrongTopicMessages = new LinkedBlockingDeque<>();

        StompSession session = stompClient
                .connectAsync(wsUrl(), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // subscribe to a DIFFERENT session's topic
        session.subscribe("/topic/session/999", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WalkSessionDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                wrongTopicMessages.add((WalkSessionDto) payload);
            }
        });

        Thread.sleep(200);

        LocationUpdateMessage outgoing = new LocationUpdateMessage();
        outgoing.setSessionId(4L); // publishing to session 4, not 999
        outgoing.setLatitude(47.53);
        outgoing.setLongitude(21.6255);

        session.send("/app/session.location", outgoing);

        // nothing should arrive on the 999 topic within a reasonable window
        WalkSessionDto shouldBeNull = wrongTopicMessages.poll(2, TimeUnit.SECONDS);

        assertThat(shouldBeNull).isNull();

        session.disconnect();
    }
}