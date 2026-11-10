package com.frcefact.dto.integracion;

/**
 * Respuesta al crear una key: la única vez que se ve la clave completa.
 *
 * @param clave clave para el header {@code X-API-Key}. No se guarda en eFact: si se pierde,
 *              hay que crear otra y revocar esta.
 */
public record ApiKeyCreadaDto(ApiKeyDto key, String clave, String aviso) {
}
