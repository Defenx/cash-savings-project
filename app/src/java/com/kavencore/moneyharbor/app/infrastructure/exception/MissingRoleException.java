package com.kavencore.moneyharbor.app.infrastructure.exception;

public class MissingRoleException extends RuntimeException {
    public MissingRoleException(String message) {
        super(message);
    }

    public MissingRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
