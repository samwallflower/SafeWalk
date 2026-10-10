package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import com.samwallflower.safewalk.security.jwt.JwtUtils;
import com.samwallflower.safewalk.security.user.AppUserDetails;
import com.samwallflower.safewalk.security.user.AppUserDetailsService;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
import com.samwallflower.safewalk.websocket.message.LocationUpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalkSessionWebSocketControllerTest {

    // The user id must come from the JWT sent in the STOMP CONNECT frame, never from a hardcoded value
    private static final Long USER_ID = 77L;
    private static final String USER_EMAIL = "ws.walker@email.com";

    @LocalServerPort
    private int port;

    // Real service is mocked so this test isolates WebSocket plumbing,
    // not WalkSessionService's own business logic (already covered separately)
    @MockitoBean
    private IWalkSessionService walkSessionService;

    // The interceptor loads the user named in the token; the database is not needed for that
    @MockitoBean
    private AppUserDetailsService userDetailsService;

    // real bean: the test signs a genuine token with the test secret
    @Autowired
    private JwtUtils jwtUtils;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        SockJsClient sockJsClient = new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))
        );
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(testUser());
    }

    private String wsUrl() {
        return "ws://localhost:" + port + "/ws";
    }

    private AppUserDetails testUser() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new AppUserDetails(USER_ID, USER_EMAIL, "x", true, authorities);
    }

    private String validToken() {
        AppUserDetails user = testUser();
        return jwtUtils.generateJwtToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private StompHeaders connectHeaders(String authorization) {
        StompHeaders headers = new StompHeaders();
        if (authorization != null) {
            headers.add("Authorization", authorization);
        }
        return headers;
    }

    private StompSession connectAuthenticated() throws Exception {
        return stompClient
                .connectAsync(wsUrl(), new WebSocketHttpHeaders(), connectHeaders("Bearer " + validToken()),
                        new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
    }

    /** true only if the server accepted the STOMP CONNECT */
    private boolean connectionAccepted(String authorization) {
        AtomicBoolean connected = new AtomicBoolean(false);
        try {
            StompSession session = stompClient
                    .connectAsync(wsUrl(), new WebSocketHttpHeaders(), connectHeaders(authorization),
                            new StompSessionHandlerAdapter() {
                                @Override
                                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                                    connected.set(true);
                                }
                            })
                    .get(3, TimeUnit.SECONDS);
            boolean accepted = connected.get() && session.isConnected();
            if (session.isConnected()) {
                session.disconnect();
            }
            return accepted;
        } catch (Exception rejectedOrTimedOut) {
            return false;
        }
    }

    // ---------- authentication ----------

    @Test
    void connect_isRejected_whenThereIsNoAuthorizationHeader() {
        assertThat(connectionAccepted(null)).isFalse();

        verifyNoInteractions(walkSessionService);
    }

    @Test
    void connect_isRejected_whenTheTokenIsNotValid() {
        assertThat(connectionAccepted("Bearer this.is.not-a-real-token")).isFalse();

        verifyNoInteractions(walkSessionService);
    }

    @Test
    void connect_isAccepted_withAValidToken() {
        assertThat(connectionAccepted("Bearer " + validToken())).isTrue();
    }

    // ---------- location updates ----------

    @Test
    void locationUpdate_broadcastsUpdatedSessionToTopic() throws Exception {
        WalkSessionDto mockedResponse = new WalkSessionDto();
        mockedResponse.setId(4L);
        mockedResponse.setLastKnownLatitude(47.53);
        mockedResponse.setLastKnownLongitude(21.6255);
        mockedResponse.setStatus(SessionStatus.ACTIVE);

        // the user id passed to the service is the one inside the token
        when(walkSessionService.updateLocation(eq(4L), eq(USER_ID), any()))
                .thenReturn(mockedResponse);

        BlockingQueue<WalkSessionDto> receivedMessages = new LinkedBlockingDeque<>();

        StompSession session = connectAuthenticated();

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
    void locationUpdate_passesTheTokenUserAndThePublishedCoordinatesToTheService() throws Exception {
        WalkSessionDto response = new WalkSessionDto();
        response.setId(4L);
        when(walkSessionService.updateLocation(eq(4L), eq(USER_ID), any())).thenReturn(response);

        StompSession session = connectAuthenticated();

        LocationUpdateMessage outgoing = new LocationUpdateMessage();
        outgoing.setSessionId(4L);
        outgoing.setLatitude(47.5);
        outgoing.setLongitude(21.6);
        session.send("/app/session.location", outgoing);

        ArgumentCaptor<UpdateWalkSession> request = ArgumentCaptor.forClass(UpdateWalkSession.class);
        verify(walkSessionService, timeout(5000)).updateLocation(eq(4L), eq(USER_ID), request.capture());
        assertThat(request.getValue().getLatitude()).isEqualTo(47.5);
        assertThat(request.getValue().getLongitude()).isEqualTo(21.6);

        session.disconnect();
    }

    @Test
    void locationUpdate_onlyDeliversToSubscribersOfThatSpecificSession() throws Exception {
        WalkSessionDto response = new WalkSessionDto();
        response.setId(4L);

        when(walkSessionService.updateLocation(eq(4L), eq(USER_ID), any()))
                .thenReturn(response);

        BlockingQueue<WalkSessionDto> wrongTopicMessages = new LinkedBlockingDeque<>();

        StompSession session = connectAuthenticated();

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
