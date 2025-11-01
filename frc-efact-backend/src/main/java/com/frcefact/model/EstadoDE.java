package com.frcefact.model;

/**
 * Estado de un Documento Electrónico en el sistema.
 * Representa el ciclo de vida del documento desde su creación hasta su aprobación o rechazo por SIFEN.
 */
public enum EstadoDE {
    /**
     * Documento creado pero aún no enviado a SIFEN
     */
    PENDIENTE,
    
    /**
     * Documento en proceso de envío o validación en SIFEN
     */
    EN_PROCESO,
    
    /**
     * Documento aprobado por SIFEN
     */
    APROBADO,
    
    /**
     * Documento rechazado por SIFEN
     */
    RECHAZADO,
    
    /**
     * Documento cancelado mediante evento de cancelación
     */
    CANCELADO,
    
    /**
     * Error en el procesamiento del documento
     */
    ERROR
}
