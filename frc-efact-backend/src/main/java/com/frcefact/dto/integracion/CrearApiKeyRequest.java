package com.frcefact.dto.integracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Pedido de una key nueva.
 *
 * @param nombre   para qué es la key (ej: "ERP producción"), para reconocerla al rotarla
 * @param scopes   operaciones habilitadas; hoy solo {@code de:consultar}
 * @param expiraEn vencimiento opcional; sin él, la key vale hasta que se revoque
 */
public record CrearApiKeyRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
        @NotEmpty(message = "Debe indicar al menos un scope") Set<String> scopes,
        LocalDateTime expiraEn) {
}
