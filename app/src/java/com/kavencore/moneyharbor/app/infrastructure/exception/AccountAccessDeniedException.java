package com.kavencore.moneyharbor.app.infrastructure.exception;

import java.util.UUID;

public class AccountAccessDeniedException extends RuntimeException {
    private final UUID id;

    public AccountAccessDeniedException(UUID id) {
        super("Access denied to account: " + id);
        this.id = id;
    }

    public AccountAccessDeniedException(String message, UUID id) {
        super(message + id);
        this.id = id;
    }
}
