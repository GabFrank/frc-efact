package com.frcefact.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para CDC (Código de Control) de documentos electrónicos SIFEN.
 * Verifica que el CDC tenga exactamente 44 caracteres alfanuméricos según especificación SIFEN.
 */
public class CdcValidator implements ConstraintValidator<ValidCdc, String> {

    private static final Pattern CDC_PATTERN = Pattern.compile("^[0-9]{44}$");
    private static final int CDC_LENGTH = 44;

    @Override
    public void initialize(ValidCdc constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(String cdc, ConstraintValidatorContext context) {
        // Null es válido, usar @NotNull para requerir valor
        if (cdc == null || cdc.trim().isEmpty()) {
            return true;
        }

        // Validar longitud
        if (cdc.length() != CDC_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("CDC inválido. Debe contener exactamente %d caracteres", CDC_LENGTH))
                   .addConstraintViolation();
            return false;
        }

        // Validar formato: 44 dígitos numéricos
        if (!CDC_PATTERN.matcher(cdc).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CDC inválido. Debe contener solo dígitos numéricos")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
