package com.frcefact.validation;

import com.frcefact.util.CalcularVerificadorRuc;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para RUC paraguayo.
 * Implementa validación de formato y dígito verificador usando algoritmo módulo 11.
 */
public class RucValidator implements ConstraintValidator<ValidRuc, String> {

    private static final Pattern RUC_PATTERN = Pattern.compile("^\\d{8}-\\d$");

    @Override
    public void initialize(ValidRuc constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(String ruc, ConstraintValidatorContext context) {
        // Null es válido, usar @NotNull para requerir valor
        if (ruc == null || ruc.trim().isEmpty()) {
            return true;
        }

        // Validar formato
        if (!RUC_PATTERN.matcher(ruc).matches()) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                var builder = context.buildConstraintViolationWithTemplate("Formato inválido");
                if (builder != null) {
                    builder.addConstraintViolation();
                }
            }
            return false;
        }

        // Extraer partes del RUC
        String[] partes = ruc.split("-");
        String numeroBase = partes[0];
        int digitoVerificador;
        
        try {
            digitoVerificador = Integer.parseInt(partes[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        // Calcular dígito verificador usando la nueva utilidad
        Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numeroBase);

        if (digitoCalculado == null || digitoCalculado != digitoVerificador) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                var builder = context.buildConstraintViolationWithTemplate("Dígito verificador incorrecto");
                if (builder != null) {
                    builder.addConstraintViolation();
                }
            }
            return false;
        }

        return true;
    }
}
