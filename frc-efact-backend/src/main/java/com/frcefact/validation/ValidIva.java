package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación para verificar que la tasa de IVA sea válida en Paraguay.
 * Valores permitidos: 0, 5, 10
 */
@Documented
@Constraint(validatedBy = IvaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIva {
    
    String message() default "Tasa de IVA inválida. Valores permitidos: 0, 5, 10";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
