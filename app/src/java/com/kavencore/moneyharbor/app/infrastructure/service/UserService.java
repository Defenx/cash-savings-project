package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.UserProfileResponseDto;
import com.kavencore.moneyharbor.app.api.model.UserSignUpRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.SignUpResult;
import com.kavencore.moneyharbor.app.entity.Role;
import com.kavencore.moneyharbor.app.entity.RoleName;
import com.kavencore.moneyharbor.app.entity.User;
import com.kavencore.moneyharbor.app.infrastructure.exception.EmailTakenException;
import com.kavencore.moneyharbor.app.infrastructure.exception.MissingRoleException;
import com.kavencore.moneyharbor.app.infrastructure.mapper.UserMapper;
import com.kavencore.moneyharbor.app.infrastructure.repository.RoleRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import com.kavencore.moneyharbor.app.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Validated
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthFacade authFacade;
    private final UserMapper userMapper;

    @Transactional
    public SignUpResult signUp(UserSignUpRequestDto req) {

        String email = normalizeEmail(req.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new EmailTakenException(email);
        }

        User u = User.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(List.of(roleUser()))
                .build();

        User savedUser;
        try {
            savedUser = userRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            throw new EmailTakenException(email);
        }

        return new SignUpResult(savedUser.getId(), savedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile() {
        String email = authFacade.email();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return userMapper.toDto(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private Role roleUser() {
        return roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new MissingRoleException(RoleName.USER.name()));
    }
}
