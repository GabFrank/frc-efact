package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para número de timbrado paraguayo.
 * Valida que el timbrado tenga exactamente 8 dígitos numéricos.
 */
@Documented
@Constraint(validatedBy = TimbradoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimbrado {
    
    String message() default "Número de timbrado inválido. Debe contener exactamente 8 dígitos";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
