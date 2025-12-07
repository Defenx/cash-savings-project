package com.kavencore.moneyharbor.app.entity;

public enum AccountType {
    INCOME("Доход"),
    EXPENSE("Расход"),
    ASSET( "Актив");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}