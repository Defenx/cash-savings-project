package com.kavencore.moneyharbor.app.infrastructure.exception;

public class InvalidOperationAmountException extends RuntimeException {
    public InvalidOperationAmountException(String message) {
        super(message);
    }
}
