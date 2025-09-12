package com.kavencore.moneyharbor.app.infrastructure.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountNotFoundException extends RuntimeException {
    private final UUID id;

    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
        this.id = id;
    }

    public AccountNotFoundException(String message, UUID id) {
        super(message + id);
        this.id = id;
    }
}
