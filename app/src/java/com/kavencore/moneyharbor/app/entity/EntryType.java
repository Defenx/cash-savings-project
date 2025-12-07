package com.kavencore.moneyharbor.app.entity;

public enum EntryType {
    DEBIT("Дебет"),
    CREDIT("Кредит");

    private final String description;

    EntryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}