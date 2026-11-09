package com.frcefact.dto.integracion;

import com.frcefact.model.ApiKey;

import java.time.LocalDateTime;
import java.util.Set;

/** Vista de una key para administración. Nunca incluye el secreto ni su hash. */
public record ApiKeyDto(
        Long id,
        Long cuentaServicioId,
        String nombre,
        String prefijo,
        Set<String> scopes,
        LocalDateTime expiraEn,
        LocalDateTime revocadaEn,
        String revocadaPor,
        LocalDateTime ultimoUsoEn,
        String ultimoUsoIp,
        boolean vigente,
        LocalDateTime creadoEn,
        String creadoPor) {

    public static ApiKeyDto desde(ApiKey k) {
        return new ApiKeyDto(k.getId(), k.getCuentaServicio().getId(), k.getNombre(), k.getPrefijo(),
                k.getScopesSet(), k.getExpiraEn(), k.getRevocadaEn(), k.getRevocadaPor(), k.getUltimoUsoEn(),
                k.getUltimoUsoIp(), k.estaVigente(LocalDateTime.now()), k.getCreadoEn(), k.getCreadoPor());
    }
}
