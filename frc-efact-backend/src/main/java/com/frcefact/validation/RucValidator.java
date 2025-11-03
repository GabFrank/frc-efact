package com.frcefact.validation;

import com.frcefact.util.CalcularVerificadorRuc;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para RUC paraguayo.
 * Acepta dos formatos:
 * 1. Con guion y dígito verificador: XXXXXXX-X o XXXXXXXX-X (7 u 8 dígitos + guion + 1 dígito)
 *    - Valida el dígito verificador usando algoritmo módulo 11
 * 2. Sin guion ni dígito verificador: solo números
 *    - Se acepta para clientes no contribuyentes (debe validarse en ClienteRucValidator)
 */
public class RucValidator implements ConstraintValidator<ValidRuc, String> {

    // Patrón para RUC con guion y dígito verificador: 6, 7 u 8 dígitos + guion + 1 dígito
    private static final Pattern RUC_CON_GUION = Pattern.compile("^\\d{6,8}-\\d$");
    // Patrón para RUC sin guion: solo números (para no contribuyentes)
    private static final Pattern RUC_SIN_GUION = Pattern.compile("^\\d+$");

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

        String rucTrimmed = ruc.trim();

        // Caso 1: Formato con guion y dígito verificador (ej: "123456-7", "4043581-4" o "80016875-5")
        if (RUC_CON_GUION.matcher(rucTrimmed).matches()) {
            // Extraer partes del RUC
            String[] partes = rucTrimmed.split("-");
            String numeroBase = partes[0];
            int digitoVerificador;
            
            try {
                digitoVerificador = Integer.parseInt(partes[1]);
            } catch (NumberFormatException e) {
                if (context != null) {
                    context.disableDefaultConstraintViolation();
                    var builder = context.buildConstraintViolationWithTemplate("Formato inválido");
                    if (builder != null) {
                        builder.addConstraintViolation();
                    }
                }
                return false;
            }

            // Calcular dígito verificador usando la utilidad
            Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numeroBase, 11);

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

        // Caso 2: Formato sin guion (solo números) - válido para no contribuyentes
        if (RUC_SIN_GUION.matcher(rucTrimmed).matches()) {
            // El formato es válido, pero debe validarse que el cliente sea no contribuyente
            // Esto se hace en ClienteRucValidator
            return true;
        }

        // Formato no reconocido
        if (context != null) {
            context.disableDefaultConstraintViolation();
            var builder = context.buildConstraintViolationWithTemplate("Formato inválido. Use formato: XXXXXX-X, XXXXXXX-X o XXXXXXXX-X (6-8 dígitos con guión y DV) o solo números (sin DV)");
            if (builder != null) {
                builder.addConstraintViolation();
            }
        }
        return false;
    }
}
