package com.frcefact.sifen.util;

import com.frcefact.model.Cliente;
import com.frcefact.model.TipoClienteSifen;

/**
 * Utilidades para mapear información del receptor según reglas SIFEN.
 */
public final class SifenReceptorHelper {

    private SifenReceptorHelper() {
        // Utility
    }

    public static Integer getNaturalezaReceptor(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        TipoClienteSifen tipo = cliente.getTipoClienteSifen();
        return tipo != null ? tipo.getNaturalezaReceptor() : null;
    }

    public static Integer getTipoContribuyente(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        TipoClienteSifen tipo = cliente.getTipoClienteSifen();
        return tipo != null ? tipo.getTipoContribuyente() : null;
    }

    public static Integer getTipoOperacion(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        TipoClienteSifen tipo = cliente.getTipoClienteSifen();
        return tipo != null ? tipo.getTipoOperacion() : null;
    }

    public static boolean requiereRuc(Cliente cliente) {
        if (cliente == null) {
            return false;
        }
        TipoClienteSifen tipo = cliente.getTipoClienteSifen();
        return tipo != null && tipo.requiereRuc();
    }
}








