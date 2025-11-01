package com.frcefact.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * Validador para la tasa de IVA en Paraguay.
 * Verifica que el valor sea 0, 5 o 10.
 */
public class IvaValidator implements ConstraintValidator<ValidIva, Integer> {

    private static final List<Integer> TASAS_IVA_VALIDAS = Arrays.asList(0, 5, 10);

    @Override
    public void initialize(ValidIva constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(Integer iva, ConstraintValidatorContext context) {
        // Null es manejado por @NotNull
        if (iva == null) {
            return true;
        }

        return TASAS_IVA_VALIDAS.contains(iva);
    }
}
