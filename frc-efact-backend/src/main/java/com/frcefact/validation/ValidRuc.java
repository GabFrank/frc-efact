package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para RUC paraguayo.
 * Valida formato XXXXXXXX-X y dígito verificador.
 */
@Documented
@Constraint(validatedBy = RucValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRuc {
    
    String message() default "RUC inválido";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
