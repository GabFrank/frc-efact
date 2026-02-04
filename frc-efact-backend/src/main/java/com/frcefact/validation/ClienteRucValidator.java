package com.frcefact.validation;

import com.frcefact.dto.ClienteDto;
import com.frcefact.model.TipoClienteSifen;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador que verifica que el RUC sea requerido según el tipo de cliente SIFEN.
 * Prioriza tipoClienteSifen sobre el campo legacy tributa.
 */
public class ClienteRucValidator implements ConstraintValidator<ValidClienteRuc, ClienteDto> {

    @Override
    public void initialize(ValidClienteRuc constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(ClienteDto clienteDto, ConstraintValidatorContext context) {
        if (clienteDto == null) {
            return true; // Null values are handled by @NotNull
        }

        boolean requiereRuc = false;

        // Priorizar tipoClienteSifen sobre tributa
        if (clienteDto.getTipoClienteSifen() != null && !clienteDto.getTipoClienteSifen().isEmpty()) {
            try {
                TipoClienteSifen tipoCliente = TipoClienteSifen.valueOf(clienteDto.getTipoClienteSifen());
                requiereRuc = tipoCliente.requiereRuc();
            } catch (IllegalArgumentException e) {
                // Si el valor del enum no es válido, usar fallback a tributa
                requiereRuc = Boolean.TRUE.equals(clienteDto.getTributa());
            }
        } else {
            // Fallback a campo legacy tributa
            requiereRuc = Boolean.TRUE.equals(clienteDto.getTributa());
        }

        String ruc = clienteDto.getRuc();
        
        // Si requiere RUC, validar que esté presente
        if (requiereRuc) {
            if (ruc == null || ruc.trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("El RUC es requerido para este tipo de cliente")
                        .addPropertyNode("ruc")
                        .addConstraintViolation();
                return false;
            }
            
            // Si requiere RUC, debe tener formato con guion y dígito verificador
            // Formato: XXXXXXX-X o XXXXXXXX-X
            if (!ruc.contains("-")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Los contribuyentes deben tener RUC con formato: XXXXXXX-X (con dígito verificador)")
                        .addPropertyNode("ruc")
                        .addConstraintViolation();
                return false;
            }
        } else {
            // Si NO requiere RUC (NO_CONTRIBUYENTE o EXTRANJERO)
            // Puede no tener RUC o tener RUC sin guion (solo números)
            if (ruc != null && !ruc.trim().isEmpty()) {
                // Si proporciona RUC, no debe tener guion ni dígito verificador
                if (ruc.contains("-")) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Los no contribuyentes no deben tener RUC con formato de contribuyente. Use solo números o deje vacío")
                            .addPropertyNode("ruc")
                            .addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
