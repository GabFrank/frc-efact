package com.frcefact.model;

/**
 * Tipo de cliente según Manual Técnico SIFEN v1.50.
 * Combina la naturaleza del receptor (iNatRec), tipo de contribuyente (iTiContRec) 
 * y tipo de operación (iTiOpe) en una sola clasificación.
 * 
 * Campos SIFEN relacionados:
 * - B401 iNatRec: Naturaleza del receptor (1=Contribuyente, 2=No contribuyente)
 * - B402 iTiOpe: Tipo de operación (1=B2B, 2=B2C, 3=B2G, 4=B2F)
 * - B403 iTiContRec: Tipo de contribuyente (1=Persona física, 2=Persona jurídica)
 */
public enum TipoClienteSifen {
    /**
     * Persona Física Contribuyente
     * - Naturaleza: Contribuyente (iNatRec = 1)
     * - Tipo de contribuyente: Persona Física (iTiContRec = 1)
     * - Tipo de operación: B2B (iTiOpe = 1)
     */
    PERSONA_FISICA(1, 1, 1, "Persona Física Contribuyente", true),
    
    /**
     * Persona Jurídica Contribuyente
     * - Naturaleza: Contribuyente (iNatRec = 1)
     * - Tipo de contribuyente: Persona Jurídica (iTiContRec = 2)
     * - Tipo de operación: B2B (iTiOpe = 1)
     */
    PERSONA_JURIDICA(1, 2, 1, "Persona Jurídica Contribuyente", true),
    
    /**
     * No Contribuyente (Consumidor Final)
     * - Naturaleza: No contribuyente (iNatRec = 2)
     * - Tipo de contribuyente: No aplica
     * - Tipo de operación: B2C (iTiOpe = 2)
     */
    NO_CONTRIBUYENTE(2, null, 2, "No Contribuyente (Consumidor Final)", false),
    
    /**
     * Cliente Extranjero
     * - Naturaleza: No contribuyente (iNatRec = 2)
     * - Tipo de contribuyente: No aplica
     * - Tipo de operación: B2F (iTiOpe = 4)
     */
    EXTRANJERO(2, null, 4, "Cliente Extranjero", false),
    
    /**
     * Entidad Gubernamental
     * - Naturaleza: Contribuyente (iNatRec = 1)
     * - Tipo de contribuyente: Persona Jurídica (iTiContRec = 2)
     * - Tipo de operación: B2G (iTiOpe = 3)
     */
    GUBERNAMENTAL(1, 2, 3, "Entidad Gubernamental", true);

    private final Integer naturalezaReceptor; // iNatRec (1=Contribuyente, 2=No contribuyente)
    private final Integer tipoContribuyente;    // iTiContRec (1=PF, 2=PJ, null=No aplica)
    private final Integer tipoOperacion;        // iTiOpe (1=B2B, 2=B2C, 3=B2G, 4=B2F)
    private final String descripcion;
    private final Boolean esContribuyente;

    TipoClienteSifen(Integer naturalezaReceptor, Integer tipoContribuyente, 
                     Integer tipoOperacion, String descripcion, Boolean esContribuyente) {
        this.naturalezaReceptor = naturalezaReceptor;
        this.tipoContribuyente = tipoContribuyente;
        this.tipoOperacion = tipoOperacion;
        this.descripcion = descripcion;
        this.esContribuyente = esContribuyente;
    }

    /**
     * Obtiene el código de naturaleza del receptor según SIFEN (B401 iNatRec)
     */
    public Integer getNaturalezaReceptor() {
        return naturalezaReceptor;
    }

    /**
     * Obtiene el código de tipo de contribuyente según SIFEN (B403 iTiContRec)
     * Retorna null si no es contribuyente
     */
    public Integer getTipoContribuyente() {
        return tipoContribuyente;
    }

    /**
     * Obtiene el código de tipo de operación según SIFEN (B402 iTiOpe)
     */
    public Integer getTipoOperacion() {
        return tipoOperacion;
    }

    /**
     * Obtiene la descripción del tipo de cliente
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Indica si este tipo de cliente es contribuyente
     */
    public Boolean esContribuyente() {
        return esContribuyente;
    }

    /**
     * Indica si este tipo requiere RUC y dígito verificador
     */
    public boolean requiereRuc() {
        return esContribuyente;
    }

    /**
     * Obtiene el enum a partir del código de tipo de operación
     */
    public static TipoClienteSifen fromTipoOperacion(Integer tipoOperacion) {
        if (tipoOperacion == null) {
            return null;
        }
        for (TipoClienteSifen tipo : values()) {
            if (tipo.tipoOperacion.equals(tipoOperacion)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de operación inválido: " + tipoOperacion);
    }

    /**
     * Obtiene el enum a partir de naturaleza y tipo de contribuyente
     */
    public static TipoClienteSifen fromNaturalezaYTipo(Integer naturaleza, Integer tipoContribuyente) {
        if (naturaleza == null) {
            return null;
        }
        if (naturaleza == 2) {
            // No contribuyente - determinar si es extranjero o consumidor final
            // Por defecto retornamos NO_CONTRIBUYENTE, pero esto debería determinarse
            // en base al país del cliente
            return NO_CONTRIBUYENTE;
        }
        if (naturaleza == 1 && tipoContribuyente != null) {
            if (tipoContribuyente == 1) {
                return PERSONA_FISICA;
            } else if (tipoContribuyente == 2) {
                // Determinar si es gubernamental o jurídica regular
                // Por defecto retornamos PERSONA_JURIDICA
                return PERSONA_JURIDICA;
            }
        }
        throw new IllegalArgumentException("Combinación inválida: naturaleza=" + naturaleza + 
                                         ", tipoContribuyente=" + tipoContribuyente);
    }
}

