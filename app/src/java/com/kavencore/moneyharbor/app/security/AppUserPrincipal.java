package com.kavencore.moneyharbor.app.security;

import com.kavencore.moneyharbor.app.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AppUserPrincipal implements UserDetails {
    @Getter
    private final UUID id;
    @Getter
    private final String email;
    private final String passwordHash;
    private final List<? extends GrantedAuthority> authorities;

    public AppUserPrincipal(User u) {
        this.id = u.getId();
        this.email = u.getEmail();
        this.passwordHash = u.getPassword();
        this.authorities = u.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleName().name().toUpperCase(Locale.ROOT))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
