package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación para validar que fechaInicio < fechaFin en timbrados.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FechasTimbradoValidator.class)
@Documented
public @interface ValidFechasTimbrado {
    String message() default "La fecha de inicio debe ser anterior a la fecha de fin";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
