package com.frcefact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación para validar que rangoDesde < rangoHasta en timbrados detalle.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RangoTimbradoValidator.class)
@Documented
public @interface ValidRangoTimbrado {
    String message() default "El rango desde debe ser menor que el rango hasta";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
