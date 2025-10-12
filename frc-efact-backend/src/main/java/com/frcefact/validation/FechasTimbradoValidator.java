package com.frcefact.validation;

import com.frcefact.dto.TimbradoDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Validador para fechas de timbrado.
 * Verifica que fechaInicio < fechaFin.
 */
public class FechasTimbradoValidator implements ConstraintValidator<ValidFechasTimbrado, TimbradoDto> {

    @Override
    public void initialize(ValidFechasTimbrado constraintAnnotation) {
    }

    @Override
    public boolean isValid(TimbradoDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        LocalDate fechaInicio = dto.getFechaInicio();
        LocalDate fechaFin = dto.getFechaFin();

        if (fechaInicio == null || fechaFin == null) {
            return true; // Las validaciones @NotNull se encargan de esto
        }

        if (fechaFin.isBefore(fechaInicio)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "La fecha de fin no puede ser anterior a la fecha de inicio")
                    .addPropertyNode("fechaFin")
                    .addConstraintViolation();
            return false;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "La fecha de inicio no puede ser posterior a la fecha de fin")
                    .addPropertyNode("fechaInicio")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
