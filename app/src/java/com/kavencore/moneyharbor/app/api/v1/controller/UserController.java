package com.kavencore.moneyharbor.app.api.v1.controller;

import com.kavencore.moneyharbor.app.api.controller.UserApi;
import com.kavencore.moneyharbor.app.api.model.UserResponseDto;
import com.kavencore.moneyharbor.app.api.model.UserSignUpRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.SignUpResult;
import com.kavencore.moneyharbor.app.infrastructure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponseDto> userSignUpPost(UserSignUpRequestDto req) {
        SignUpResult result = userService.signUp(req);
        UserResponseDto body = new UserResponseDto().id(result.id()).email(result.email());
        URI location = URI.create("/user/" + result.id());

        return ResponseEntity.created(location).body(body);
    }
}
