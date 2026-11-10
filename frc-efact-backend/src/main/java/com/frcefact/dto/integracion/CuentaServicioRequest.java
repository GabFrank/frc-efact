package com.frcefact.dto.integracion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta o modificación de una cuenta de servicio. */
public record CuentaServicioRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 200) String nombre,
        @Size(max = 20) String ruc,
        @Email @Size(max = 150) String emailContacto,
        /** Empresa de eFact asociada. Opcional: un consultor no tiene. */
        Long empresaId,
        Boolean activa,
        String observacion) {
}
