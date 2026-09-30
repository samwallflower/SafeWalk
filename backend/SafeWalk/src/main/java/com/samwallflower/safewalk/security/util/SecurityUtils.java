package com.samwallflower.safewalk.security.util;

import com.samwallflower.safewalk.security.user.AppUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static AppUserDetails getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserDetails userDetails) {
            return userDetails;
        }else {
            throw new AccessDeniedException("Access Denied: No authenticated user found.");
        }
    }

    public static Long getCurrentUserId(){
        return getCurrentUser().getId();
    }

    public static boolean isCurrentUserAdmin(){
        return getCurrentUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    /**
     * Throws if the caller is neither the owner of the resource nor an admin.
     * @param resourceOwnerId
     */

    public static void checkOwnershipOrAdmin(Long resourceOwnerId){
        AppUserDetails currentUser = getCurrentUser();
        boolean isOwner = currentUser.getId().equals(resourceOwnerId);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Access Denied: You do not have permission to access this resource.");
        }
    }
}
