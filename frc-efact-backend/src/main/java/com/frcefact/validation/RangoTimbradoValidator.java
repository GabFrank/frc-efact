package com.frcefact.validation;

import com.frcefact.dto.TimbradoDetalleDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para rangos de timbrado detalle.
 * Verifica que rangoDesde < rangoHasta.
 */
public class RangoTimbradoValidator implements ConstraintValidator<ValidRangoTimbrado, TimbradoDetalleDto> {

    @Override
    public void initialize(ValidRangoTimbrado constraintAnnotation) {
    }

    @Override
    public boolean isValid(TimbradoDetalleDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        Long rangoDesde = dto.getRangoDesde();
        Long rangoHasta = dto.getRangoHasta();

        if (rangoDesde == null || rangoHasta == null) {
            return true; // Las validaciones @NotNull se encargan de esto
        }

        if (rangoHasta < rangoDesde) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "El rango hasta (" + rangoHasta + ") no puede ser menor que el rango desde (" + rangoDesde + ")")
                    .addPropertyNode("rangoHasta")
                    .addConstraintViolation();
            return false;
        }

        if (rangoDesde.equals(rangoHasta)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "El rango debe tener al menos un número. Desde y hasta no pueden ser iguales")
                    .addPropertyNode("rangoHasta")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
