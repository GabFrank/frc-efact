package com.frcefact.security.apikey;

import java.util.Arrays;
import java.util.Optional;

/**
 * Operaciones que una API key puede tener habilitadas.
 *
 * <p>Es el "catálogo de funciones" del servicio: cada función nueva que se exponga a otros
 * sistemas (emitir DE, notas, eventos...) se agrega acá como un scope, sin cambiar el modelo
 * de keys. En Spring Security cada scope se traduce a la authority {@code SCOPE_<valor>}.
 */
public enum ScopeIntegracion {

    /** Consultar en SIFEN un DE por su CDC (modo consultor). */
    DE_CONSULTAR("de:consultar");

    private final String valor;

    ScopeIntegracion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public String getAuthority() {
        return "SCOPE_" + valor;
    }

    public static Optional<ScopeIntegracion> desdeValor(String valor) {
        return Arrays.stream(values()).filter(s -> s.valor.equals(valor)).findFirst();
    }
}
