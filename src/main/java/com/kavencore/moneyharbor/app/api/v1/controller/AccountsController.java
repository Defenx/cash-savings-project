package com.kavencore.moneyharbor.app.api.v1.controller;

import com.kavencore.moneyharbor.app.api.controller.AccountsApi;
import com.kavencore.moneyharbor.app.api.model.AccountResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateAccountRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.CreatedAccountResult;
import com.kavencore.moneyharbor.app.infrastructure.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AccountsController implements AccountsApi {
    private final AccountService accountService;

    @Override
    public ResponseEntity<AccountResponseDto> createAccount(CreateAccountRequestDto createAccountRequestDto) {
        CreatedAccountResult result  = accountService.createAccount(createAccountRequestDto);
        URI location = UriComponentsBuilder.fromPath("/accounts/{id}").build(result.id().toString());
        return ResponseEntity.created(location).body(result.body());
    }

    @Override
    public ResponseEntity<AccountResponseDto> getAccountById(UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @Override
    public ResponseEntity<Void> deleteAccount(UUID id) {
        boolean deleted = accountService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.ok().build();
    }
}
