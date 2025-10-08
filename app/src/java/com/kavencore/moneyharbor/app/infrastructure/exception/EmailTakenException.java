package com.kavencore.moneyharbor.app.infrastructure.exception;

import lombok.Getter;

public class EmailTakenException extends RuntimeException {
    @Getter
    private final String email;

    public EmailTakenException(String email) {
        super("Email is already taken: " + email);
        this.email = email;
    }
}
