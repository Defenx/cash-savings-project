package com.kavencore.moneyharbor.app.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthFacade {

    public AppUserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        return (AppUserPrincipal) auth.getPrincipal();
    }

    public UUID userId() {
        return currentUser().getId();
    }

    public String email() {
        return currentUser().getEmail();
    }
}
