package com.kavencore.moneyharbor.app.security;

import com.kavencore.moneyharbor.app.entity.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.kavencore.moneyharbor.app.security.SecurityMessages.AUTHENTICATION_REQUIRED;

@Component
public class AuthFacade {

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User principal)) {
            throw new AuthenticationCredentialsNotFoundException(AUTHENTICATION_REQUIRED);
        }
        return (User) auth.getPrincipal();
    }

    public UUID userId() {
        return currentUser().getId();
    }

    public String email() {
        return currentUser().getEmail();
    }
}
