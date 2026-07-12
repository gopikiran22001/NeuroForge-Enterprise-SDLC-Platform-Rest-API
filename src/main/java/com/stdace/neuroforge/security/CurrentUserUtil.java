package com.stdace.neuroforge.security;

import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.exception.UnauthorizedException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public class CurrentUserUtil {

    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof com.stdace.neuroforge.security.CustomUserDetails cud) {
            return cud.getUser().getId();
        }

        // Fallback: if principal is UserDetails with username as email, you may need to load user
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            // NOTE: this fallback requires calling the UserRepository to map username->id
            throw new UnauthorizedException("Principal is UserDetails only; prefer CustomUserDetails");
        }

        throw new UnauthorizedException("Unable to resolve current user");
    }

    public static UserRole getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof com.stdace.neuroforge.security.CustomUserDetails cud) {
            return cud.getUser().getRole();
        }

        // Fallback: if principal is UserDetails with username as email, you may need to load user
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            // NOTE: this fallback requires calling the UserRepository to map username->id
            throw new UnauthorizedException("Principal is UserDetails only; prefer CustomUserDetails");
        }

        throw new UnauthorizedException("Unable to resolve current user");
    }

}