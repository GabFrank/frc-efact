package com.frcefact.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para número de timbrado paraguayo.
 * Verifica que el timbrado tenga exactamente 8 dígitos numéricos.
 */
public class TimbradoValidator implements ConstraintValidator<ValidTimbrado, String> {

    private static final Pattern TIMBRADO_PATTERN = Pattern.compile("^\\d{8}$");

    @Override
    public void initialize(ValidTimbrado constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(String timbrado, ConstraintValidatorContext context) {
        // Null es válido, usar @NotNull para requerir valor
        if (timbrado == null || timbrado.trim().isEmpty()) {
            return true;
        }

        // Validar formato: exactamente 8 dígitos
        if (!TIMBRADO_PATTERN.matcher(timbrado).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Número de timbrado inválido. Debe contener exactamente 8 dígitos")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
