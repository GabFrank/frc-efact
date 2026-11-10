package com.frcefact.security.apikey;

import java.util.Set;

/**
 * Identidad de un sistema externo autenticado con API key.
 *
 * @param apiKeyId         id de la key usada
 * @param prefijo          parte pública de la key (para logs; nunca el secreto)
 * @param cuentaServicioId cuenta dueña de la key
 * @param cuentaNombre     nombre de la cuenta
 * @param empresaId        empresa de eFact asociada a la cuenta, o {@code null} si no tiene
 * @param scopes           scopes habilitados en la key
 */
public record ApiKeyPrincipal(
        Long apiKeyId,
        String prefijo,
        Long cuentaServicioId,
        String cuentaNombre,
        Long empresaId,
        Set<String> scopes) {

    /** Nombre que queda en {@code creado_por} y en los logs. Cabe en VARCHAR(50). */
    public String nombreAuditoria() {
        return "apikey:" + prefijo;
    }
}
