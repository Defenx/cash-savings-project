package com.kavencore.moneyharbor.app.infrastructure.exception;

import lombok.Getter;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    @Getter
    UUID categoryId;

    public CategoryNotFoundException(UUID categoryId) {
        super("Category with id " + categoryId + " not found");
        this.categoryId = categoryId;
    }
}
