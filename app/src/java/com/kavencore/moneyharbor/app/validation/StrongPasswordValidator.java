package com.kavencore.moneyharbor.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;

        if (value.length() < 8) return false;

        if (value.chars().noneMatch(Character::isDigit)) return false;

        int run = 1;
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) == value.charAt(i - 1)) {
                run++;
                if (run >= 3) return false;
            } else {
                run = 1;
            }
        }

        return true;
    }
}
