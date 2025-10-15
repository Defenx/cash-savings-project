package com.kavencore.moneyharbor.app.infrastructure.exception;

import lombok.Getter;

public class TitleAlreadyExistsException extends RuntimeException {
    @Getter
    private final String title;
    @Getter
    private final String currency;

    public TitleAlreadyExistsException(String title, String currency) {
        super("Account with title '" + title + "' already exists for currency '" + currency + "'");
        this.title = title;
        this.currency = currency;
    }
}
