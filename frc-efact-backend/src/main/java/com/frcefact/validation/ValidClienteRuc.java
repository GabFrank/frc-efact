package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para validar que el RUC sea requerido
 * según el tipo de cliente SIFEN (o tributa=true como fallback).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ClienteRucValidator.class)
@Documented
public @interface ValidClienteRuc {

    String message() default "El RUC es requerido cuando el cliente tributa";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
