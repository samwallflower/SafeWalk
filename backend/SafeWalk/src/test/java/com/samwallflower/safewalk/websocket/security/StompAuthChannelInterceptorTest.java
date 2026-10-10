package com.samwallflower.safewalk.websocket.security;

import com.samwallflower.safewalk.security.jwt.JwtUtils;
import com.samwallflower.safewalk.security.user.AppUserDetails;
import com.samwallflower.safewalk.security.user.AppUserDetailsService;
import com.samwallflower.safewalk.security.util.SecurityUtils;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StompAuthChannelInterceptorTest {

    private static final Long USER_ID = 5L;
    private static final String EMAIL = "walker@email.com";

    @Mock private JwtUtils jwtUtils;
    @Mock private AppUserDetailsService userDetailsService;
    @Mock private MessageChannel channel;
    @Mock private MessageHandler handler;

    private StompAuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new StompAuthChannelInterceptor(jwtUtils, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AppUserDetails appUser() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new AppUserDetails(USER_ID, EMAIL, "x", true, authorities);
    }

    /** A STOMP CONNECT frame. The accessor is left mutable so the test can read back what the interceptor sets. */
    private StompHeaderAccessor connectAccessor(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.addNativeHeader("Authorization", authorizationHeader);
        }
        accessor.setLeaveMutable(true);
        return accessor;
    }

    private Message<byte[]> messageOf(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    // ---------- preSend: authentication on CONNECT ----------

    @Test
    void connect_withValidToken_attachesTheAuthenticatedUserToTheSession() {
        StompHeaderAccessor accessor = connectAccessor("Bearer good.jwt.token");
        when(jwtUtils.validateToken("good.jwt.token")).thenReturn(true);
        when(jwtUtils.getUserNameFromToken("good.jwt.token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(appUser());

        Message<?> result = interceptor.preSend(messageOf(accessor), channel);

        assertThat(result).isNotNull();
        assertThat(accessor.getUser()).isInstanceOf(Authentication.class);
        Authentication authentication = (Authentication) accessor.getUser();
        assertThat(authentication.getPrincipal()).isInstanceOf(AppUserDetails.class);
        assertThat(((AppUserDetails) authentication.getPrincipal()).getId()).isEqualTo(USER_ID);
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

    @Test
    void connect_isRejected_whenThereIsNoAuthorizationHeader() {
        StompHeaderAccessor accessor = connectAccessor(null);

        assertThatThrownBy(() -> interceptor.preSend(messageOf(accessor), channel))
                .isInstanceOf(MessageDeliveryException.class);

        verifyNoInteractions(jwtUtils, userDetailsService);
        assertThat(accessor.getUser()).isNull();
    }

    @Test
    void connect_isRejected_whenTheHeaderIsNotABearerToken() {
        StompHeaderAccessor accessor = connectAccessor("Basic dXNlcjpwYXNz");

        assertThatThrownBy(() -> interceptor.preSend(messageOf(accessor), channel))
                .isInstanceOf(MessageDeliveryException.class);

        verifyNoInteractions(jwtUtils, userDetailsService);
        assertThat(accessor.getUser()).isNull();
    }

    @Test
    void connect_isRejected_whenTheTokenIsInvalidOrExpired() {
        StompHeaderAccessor accessor = connectAccessor("Bearer expired.or.forged");
        when(jwtUtils.validateToken("expired.or.forged")).thenThrow(new JwtException("expired"));

        assertThatThrownBy(() -> interceptor.preSend(messageOf(accessor), channel))
                .isInstanceOf(MessageDeliveryException.class);

        verifyNoInteractions(userDetailsService);
        assertThat(accessor.getUser()).isNull();
    }

    @Test
    void otherFrames_passThroughUntouched_andDoNotNeedAToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        Message<byte[]> message = messageOf(accessor);

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
        verifyNoInteractions(jwtUtils, userDetailsService);
    }

    // ---------- beforeHandle / afterMessageHandled: SecurityContext for the handler thread ----------

    private Message<byte[]> sendMessageFrom(Authentication user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        if (user != null) {
            accessor.setUser(user);
        }
        accessor.setLeaveMutable(true);
        return messageOf(accessor);
    }

    @Test
    void beforeHandle_exposesTheSessionUserToSecurityUtils() {
        AppUserDetails user = appUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        interceptor.beforeHandle(sendMessageFrom(authentication), channel, handler);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(USER_ID);
        assertThat(SecurityUtils.isCurrentUserAdmin()).isFalse();
    }

    @Test
    void beforeHandle_leavesTheContextEmpty_whenTheMessageHasNoUser() {
        interceptor.beforeHandle(sendMessageFrom(null), channel, handler);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void afterMessageHandled_clearsTheSecurityContext_soItCannotLeakToTheNextMessage() {
        AppUserDetails user = appUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        Message<byte[]> message = sendMessageFrom(authentication);
        interceptor.beforeHandle(message, channel, handler);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        interceptor.afterMessageHandled(message, channel, handler, null);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void afterMessageHandled_clearsTheContext_evenWhenTheHandlerFailed() {
        AppUserDetails user = appUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        Message<byte[]> message = sendMessageFrom(authentication);
        interceptor.beforeHandle(message, channel, handler);

        interceptor.afterMessageHandled(message, channel, handler, new IllegalStateException("handler blew up"));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
