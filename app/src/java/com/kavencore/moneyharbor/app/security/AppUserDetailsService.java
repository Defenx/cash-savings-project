package com.kavencore.moneyharbor.app.security;

import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email != null && !email.isEmpty()) {
            String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
            return userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } else {
            throw new UsernameNotFoundException("Email cannot be empty");
        }
    }
}
