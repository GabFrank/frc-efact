package com.frcefact.validation;

import com.frcefact.dto.ClienteDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida el RUC del {@link ClienteDto} según el tipo de cliente SIFEN.
 *
 * <p>La regla vive en {@link ReglaRucCliente}, compartida con
 * {@code ClienteService.validarRucSegunTipoCliente}. Este validador solo la aplica y traduce el
 * resultado al formato de Bean Validation.
 *
 * <p><b>Qué cambió.</b> Antes esta clase tenía la regla escrita a mano y solo verificaba la
 * <i>presencia del guion</i> ({@code ruc.contains("-")}), no que el dígito verificador fuese el
 * correcto: un contribuyente cargado como {@code 80099482-1} pasaba la validación y recién SIFEN
 * lo rechazaba, sin decir cuál era el problema. Ahora se verifica el DV por módulo 11 y el mensaje
 * de error indica el dígito correcto.
 */
public class ClienteRucValidator implements ConstraintValidator<ValidClienteRuc, ClienteDto> {

    @Override
    public void initialize(ValidClienteRuc constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(ClienteDto clienteDto, ConstraintValidatorContext context) {
        if (clienteDto == null) {
            return true; // Los null los maneja @NotNull
        }

        boolean requiereRuc = ReglaRucCliente.requiereRuc(
                clienteDto.getTipoClienteSifen(), clienteDto.getTributa());

        ReglaRucCliente.Resultado resultado =
                ReglaRucCliente.validar(requiereRuc, clienteDto.getRuc());

        if (resultado.valido()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(resultado.mensaje())
                .addPropertyNode("ruc")
                .addConstraintViolation();
        return false;
    }
}
