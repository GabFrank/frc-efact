package com.frcefact.model;

/**
 * Tipo de transacción u operación comercial según Manual Técnico SIFEN v1.50.
 * Campo D011 (iTipTra) indica el tipo de transacción, mientras que D012 (dDesTipTra) contiene la descripción.
 */
public enum TipoTransaccionProducto {
    /**
     * Código 1: Venta de mercadería - Para bienes físicos o mercaderías
     */
    VENTA_MERCADERIA(1, "Venta de mercadería"),
    
    /**
     * Código 2: Prestación de servicios - Para servicios puros, sin entrega de bienes
     */
    PRESTACION_SERVICIOS(2, "Prestación de servicios"),
    
    /**
     * Código 3: Mixto - Venta de mercadería y servicios combinados
     */
    MIXTO(3, "Mixto (Venta de mercadería y servicios)"),
    
    /**
     * Código 4: Venta de activo fijo - Para venta de activos fijos (equipos, maquinarias, vehículos)
     */
    VENTA_ACTIVO_FIJO(4, "Venta de activo fijo"),
    
    /**
     * Código 5: Venta de divisas - Para operaciones de cambio o venta de moneda extranjera
     */
    VENTA_DIVISAS(5, "Venta de divisas"),
    
    /**
     * Código 6: Compra de divisas - Compra de moneda extranjera
     */
    COMPRA_DIVISAS(6, "Compra de divisas"),
    
    /**
     * Código 7: Promoción o entrega de muestras - Para entrega gratuita o promocional de bienes
     */
    PROMOCION_MUESTRAS(7, "Promoción o entrega de muestras"),
    
    /**
     * Código 8: Donación - Para entrega sin contraprestación
     */
    DONACION(8, "Donación"),
    
    /**
     * Código 9: Anticipo - Cuando se cobra un anticipo de una operación futura
     */
    ANTICIPO(9, "Anticipo"),
    
    /**
     * Código 10: Compra de productos - Facturación de compras de bienes
     */
    COMPRA_PRODUCTOS(10, "Compra de productos"),
    
    /**
     * Código 11: Compra de servicios - Facturación de compras de servicios
     */
    COMPRA_SERVICIOS(11, "Compra de servicios"),
    
    /**
     * Código 12: Venta de crédito fiscal - Venta de derechos de crédito fiscal
     */
    VENTA_CREDITO_FISCAL(12, "Venta de crédito fiscal"),
    
    /**
     * Código 13: Muestras médicas - Entrega de muestras médicas (Art. 3 RG 24/2014)
     */
    MUESTRAS_MEDICAS(13, "Muestras médicas");

    private final Integer codigo;
    private final String descripcion;

    TipoTransaccionProducto(Integer codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el código numérico del tipo de transacción (D011 iTipTra)
     */
    public Integer getCodigo() {
        return codigo;
    }

    /**
     * Obtiene la descripción textual del tipo de transacción (D012 dDesTipTra)
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el enum a partir del código numérico
     */
    public static TipoTransaccionProducto fromCodigo(Integer codigo) {
        if (codigo == null) {
            return null;
        }
        for (TipoTransaccionProducto tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de transacción inválido: " + codigo);
    }

    /**
     * Indica si este tipo corresponde a un bien físico
     */
    public boolean esBienFisico() {
        return this == VENTA_MERCADERIA || 
               this == VENTA_ACTIVO_FIJO || 
               this == COMPRA_PRODUCTOS ||
               this == PROMOCION_MUESTRAS ||
               this == DONACION ||
               this == MUESTRAS_MEDICAS;
    }

    /**
     * Indica si este tipo corresponde a un servicio
     */
    public boolean esServicio() {
        return this == PRESTACION_SERVICIOS || 
               this == COMPRA_SERVICIOS;
    }

    /**
     * Indica si este tipo requiere precio cero o simbólico
     */
    public boolean requierePrecioCero() {
        return this == PROMOCION_MUESTRAS || 
               this == DONACION;
    }
}

