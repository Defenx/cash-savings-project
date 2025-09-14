package com.kavencore.moneyharbor.app.infrastructure.exception;

import lombok.Getter;

public class EmailTakenException extends RuntimeException {
    @Getter
    String email;

    public EmailTakenException(String email) {
        super("Email is already taken: " + email);
        this.email = email;
    }

    public EmailTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}
