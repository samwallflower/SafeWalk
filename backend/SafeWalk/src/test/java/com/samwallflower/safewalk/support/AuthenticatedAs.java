package com.samwallflower.safewalk.support;

import com.samwallflower.safewalk.security.user.AppUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

/** Puts an authenticated user in the SecurityContext, like the JWT filter does in production. */
public final class AuthenticatedAs {
    private AuthenticatedAs() {}

    public static void user(Long id) {
        set(id, "ROLE_USER");
    }

    public static void admin() {
        set(0L, "ROLE_ADMIN");
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void set(Long id, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        AppUserDetails principal = new AppUserDetails(id, "test" + id + "@email.com", "x", true, authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }
}
