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


    public static final String USER_PATH = "/user";
    public static final String USER_PATH_WITH_SLASH = USER_PATH + "/";
    public static final String SIGN_UP_PATH = "/user/sign-up";
    private final UserService userService;

    @Override
    public ResponseEntity<UserResponseDto> userSignUpPost(UserSignUpRequestDto req) {
        SignUpResult result = userService.signUp(req);
        UserResponseDto body = new UserResponseDto().id(result.id()).email(result.email());
        URI location = URI.create(USER_PATH_WITH_SLASH + result.id());

        return ResponseEntity.created(location).body(body);
    }
}
