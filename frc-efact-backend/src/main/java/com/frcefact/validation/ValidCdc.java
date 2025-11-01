package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para CDC (Código de Control) de documentos electrónicos SIFEN.
 * Valida que el CDC tenga exactamente 44 caracteres numéricos según especificación SIFEN.
 */
@Documented
@Constraint(validatedBy = CdcValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCdc {
    
    String message() default "CDC inválido. Debe contener exactamente 44 dígitos numéricos";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
