package com.kavencore.moneyharbor.app.api.v1.controller;

import com.kavencore.moneyharbor.app.api.controller.AccountsApi;
import com.kavencore.moneyharbor.app.api.model.AccountResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateAccountRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.CreatedAccountResult;
import com.kavencore.moneyharbor.app.infrastructure.service.AccountService;
import com.kavencore.moneyharbor.app.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AccountsController implements AccountsApi {

    public static final String ACCOUNTS_PATH = "/accounts";
    public static final String ACCOUNTS_PATH_WITH_SLASH = "/accounts/";

    private final AccountService accountService;
    private final AuthFacade authFacade;

    @Override
    public ResponseEntity<AccountResponseDto> createAccount(CreateAccountRequestDto createAccountRequestDto) {
        CreatedAccountResult result = accountService.createAccount(createAccountRequestDto, authFacade.userId());
        URI location = UriComponentsBuilder.fromPath(ACCOUNTS_PATH + "/{id}").build(result.id().toString());
        return ResponseEntity.created(location).body(result.body());
    }

    @Override
    public ResponseEntity<AccountResponseDto> getAccountById(UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id, authFacade.userId()));
    }

    @Override
    public ResponseEntity<Void> deleteAccount(UUID id) {
        boolean deleted = accountService.delete(id, authFacade.userId());
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.ok().build();
    }
}
