package com.samwallflower.safewalk.security.jwt;

import com.samwallflower.safewalk.security.user.AppUserDetails;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Loads only JwtUtils, with the secret and expiry from src/test/resources/application.properties.
 * This also proves the TEST secret is a valid Base64 key that is long enough for HS256,
 * which every test that needs a real token (e.g. the WebSocket tests) depends on.
 */
@SpringBootTest(classes = JwtUtils.class)
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    private Authentication authenticationFor(Long id, String email, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        AppUserDetails user = new AppUserDetails(id, email, "x", true, authorities);
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    @Test
    void generatedToken_isValid_andCarriesTheEmailAsSubject() {
        String token = jwtUtils.generateJwtToken(authenticationFor(7L, "walker@email.com", "ROLE_USER"));

        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromToken(token)).isEqualTo("walker@email.com");
    }

    @Test
    void validateToken_rejectsGarbage() {
        assertThatThrownBy(() -> jwtUtils.validateToken("this.is.not-a-real-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validateToken_rejectsATamperedToken() {
        String token = jwtUtils.generateJwtToken(authenticationFor(7L, "walker@email.com", "ROLE_USER"));
        // flip the last character of the signature
        char last = token.charAt(token.length() - 1);
        String tampered = token.substring(0, token.length() - 1) + (last == 'A' ? 'B' : 'A');

        assertThatThrownBy(() -> jwtUtils.validateToken(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokensForDifferentUsers_areDifferent() {
        String a = jwtUtils.generateJwtToken(authenticationFor(1L, "a@email.com", "ROLE_USER"));
        String b = jwtUtils.generateJwtToken(authenticationFor(2L, "b@email.com", "ROLE_ADMIN"));

        assertThat(a).isNotEqualTo(b);
        assertThat(jwtUtils.getUserNameFromToken(b)).isEqualTo("b@email.com");
    }
}
