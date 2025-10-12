package com.frcefact.validation;

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
                var builder = context.buildConstraintViolationWithTemplate("Formato de RUC inválido. Debe ser XXXXXXXX-X");
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

        // Calcular dígito verificador
        int digitoCalculado = calcularDigitoVerificador(numeroBase);

        if (digitoCalculado != digitoVerificador) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                var builder = context.buildConstraintViolationWithTemplate("Dígito verificador del RUC es incorrecto");
                if (builder != null) {
                    builder.addConstraintViolation();
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Calcula el dígito verificador del RUC paraguayo usando el algoritmo módulo 11.
     *
     * @param numeroBase los primeros 8 dígitos del RUC
     * @return el dígito verificador calculado
     */
    private int calcularDigitoVerificador(String numeroBase) {
        int[] multiplicadores = {2, 3, 4, 5, 6, 7, 2, 3};
        int suma = 0;

        for (int i = 0; i < numeroBase.length(); i++) {
            int digito = Character.getNumericValue(numeroBase.charAt(i));
            suma += digito * multiplicadores[i];
        }

        int resto = suma % 11;
        int digitoVerificador = 11 - resto;

        // Si el resultado es 11, el dígito verificador es 0
        // Si el resultado es 10, el dígito verificador es 1
        if (digitoVerificador == 11) {
            return 0;
        } else if (digitoVerificador == 10) {
            return 1;
        }

        return digitoVerificador;
    }
}
