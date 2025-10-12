package com.frcefact.validation;

import com.frcefact.dto.ClienteDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador que verifica que el RUC sea requerido cuando tributa=true.
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

        // Si tributa es true, el RUC debe estar presente
        if (Boolean.TRUE.equals(clienteDto.getTributa())) {
            String ruc = clienteDto.getRuc();
            if (ruc == null || ruc.trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("El RUC es requerido cuando el cliente tributa")
                        .addPropertyNode("ruc")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
