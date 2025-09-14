package com.kavencore.moneyharbor.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Пароль должен быть ≥8 символов, содержать хотя бы одну цифру и не иметь трёх одинаковых подряд";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
