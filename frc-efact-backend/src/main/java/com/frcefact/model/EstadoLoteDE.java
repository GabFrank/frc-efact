package com.frcefact.model;

/**
 * Estado de un Lote de Documentos Electrónicos.
 * Representa el estado del lote durante su procesamiento en SIFEN.
 */
public enum EstadoLoteDE {
    /**
     * Lote creado pero aún no enviado a SIFEN
     */
    PENDIENTE,
    
    /**
     * Lote en proceso de validación en SIFEN
     */
    EN_PROCESO,
    
    /**
     * Lote aprobado por SIFEN
     */
    APROBADO,
    
    /**
     * Lote rechazado por SIFEN
     */
    RECHAZADO,
    
    /**
     * Error en el procesamiento del lote
     */
    ERROR
}
