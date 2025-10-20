package com.frcefact.util;

/**
 * Utilidad para calcular el dígito verificador del RUC paraguayo.
 * Implementa el algoritmo oficial usando módulo 11.
 */
public class CalcularVerificadorRuc {

    /**
     * Calcula el dígito verificador para un RUC usando base 11.
     *
     * @param ruc el RUC sin el dígito verificador
     * @return el dígito verificador calculado
     */
    public static int getDigitoVerificador(String ruc) {
        Integer resultado = getDigitoVerificador(ruc, 11);
        return resultado != null ? resultado : 0;
    }

    /**
     * Calcula el dígito verificador como string con formato "-X".
     *
     * @param ruc el RUC sin el dígito verificador
     * @return el dígito verificador con formato "-X" o cadena vacía si es inválido
     */
    public static String getDigitoVerificadorString(String ruc) {
        Integer digito = getDigitoVerificador(ruc, 11);
        if (digito != null) {
            return "-" + digito;
        }
        return "";
    }

    /**
     * Calcula el dígito verificador usando la base especificada.
     *
     * @param ruc  el RUC a procesar
     * @param base la base para el cálculo (normalmente 11)
     * @return el dígito verificador calculado o null si el RUC es muy corto
     */
    public static Integer getDigitoVerificador(String ruc, int base) {
        if (ruc.length() < 4) return null;

        int k = 2;
        int total = 0;
        String alRevez = invertirCadena(eliminarNoDigitos(ruc));

        for (char numero : alRevez.toCharArray()) {
            total += (numero - '0') * k++;
            if (k > base)
                k = 2;
        }

        int resto = total % base;
        return resto > 1 ? base - resto : 0;
    }

    /**
     * Invierte una cadena de caracteres.
     *
     * @param ruc la cadena a invertir
     * @return la cadena invertida
     */
    protected static String invertirCadena(String ruc) {
        // si se dispone de apache commons se puede usar
        // StringUtils.reverse(ruc);
        return new StringBuilder(ruc).reverse().toString();
    }

    /**
     * Elimina todos los no dígitos de la cadena.
     *
     * @param ruc ruc con números, símbolos y letras.
     * @return una versión del ruc consistente de solo dígitos.
     */
    protected static String eliminarNoDigitos(String ruc) {
        String toRet = "";
        for (char c : ruc.toCharArray()) {
            if (Character.isDigit(c)) {
                toRet += c;
            } else {
                toRet += (int) c;
            }
        }
        return toRet;
    }
}