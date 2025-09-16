package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.UserSignUpRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.SignUpResult;
import com.kavencore.moneyharbor.app.entity.Role;
import com.kavencore.moneyharbor.app.entity.RoleName;
import com.kavencore.moneyharbor.app.entity.User;
import com.kavencore.moneyharbor.app.infrastructure.exception.EmailTakenException;
import com.kavencore.moneyharbor.app.infrastructure.exception.MissingRoleException;
import com.kavencore.moneyharbor.app.infrastructure.repository.RoleRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Validated
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResult signUp(UserSignUpRequestDto req) {

        String email = normalizeEmail(req.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new EmailTakenException(email);
        }

        User u = User.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(roleUser()))
                .build();

        User savedUser;
        try {
            savedUser = userRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            throw new EmailTakenException(email);
        }

        return new SignUpResult(savedUser.getId(), savedUser.getEmail());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private Role roleUser() {
        return roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new MissingRoleException(RoleName.USER.name()));
    }
}
