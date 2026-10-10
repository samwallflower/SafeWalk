package com.samwallflower.safewalk.websocket.security;

import com.samwallflower.safewalk.security.jwt.JwtUtils;
import com.samwallflower.safewalk.security.user.AppUserDetailsService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ExecutorChannelInterceptor {
    private final JwtUtils jwtUtils;
    private final AppUserDetailsService userDetailsService;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor!=null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String header = accessor.getFirstNativeHeader("Authorization");

            if(header == null || !header.startsWith("Bearer ")){
                throw new MessageDeliveryException("Missing or invalid Authorization header");
            }

            String jwt = header.substring(7);
            try {
                jwtUtils.validateToken(jwt);
            } catch (JwtException e) {
                throw new MessageDeliveryException("Invalid or expired token.");
            }

            UserDetails user = userDetailsService.loadUserByUsername(jwtUtils.getUserNameFromToken(jwt));

            accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        }

        return message;
    }

    @Override
    public @Nullable Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor!=null && accessor.getUser() instanceof Authentication authentication){
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, @Nullable Exception ex) {
        SecurityContextHolder.clearContext();
    }
}
