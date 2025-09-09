package com.kavencore.moneyharbor.app.infrastructure.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    private final UUID id;

    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public AccountNotFoundException(String message, UUID id) {
        super(message + id);
        this.id = id;
    }
}
