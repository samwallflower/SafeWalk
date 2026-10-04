package com.samwallflower.safewalk.support;

import com.samwallflower.safewalk.security.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {
    @AfterEach void tearDown() { AuthenticatedAs.clear(); }

    @Test
    void noAuthenticatedUser_isDenied() {
        assertThatThrownBy(() -> SecurityUtils.checkOwnershipOrAdmin(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void owner_isAllowed() {
        AuthenticatedAs.user(5L);
        assertThatCode(() -> SecurityUtils.checkOwnershipOrAdmin(5L)).doesNotThrowAnyException();
        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(5L);
        assertThat(SecurityUtils.isCurrentUserAdmin()).isFalse();
    }

    @Test
    void otherUser_isDenied() {
        AuthenticatedAs.user(5L);
        assertThatThrownBy(() -> SecurityUtils.checkOwnershipOrAdmin(6L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void admin_isAllowedForAnyUser() {
        AuthenticatedAs.admin();
        assertThatCode(() -> SecurityUtils.checkOwnershipOrAdmin(42L)).doesNotThrowAnyException();
        assertThat(SecurityUtils.isCurrentUserAdmin()).isTrue();
    }
}
