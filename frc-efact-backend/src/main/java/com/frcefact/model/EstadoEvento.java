package com.frcefact.model;

/**
 * Estado de un Evento de Cancelación de Documento Electrónico.
 * Representa el estado del evento durante su procesamiento en SIFEN.
 */
public enum EstadoEvento {
    /**
     * Evento creado pero aún no enviado a SIFEN
     */
    PENDIENTE,
    
    /**
     * Evento aprobado por SIFEN - el documento ha sido cancelado
     */
    APROBADO,
    
    /**
     * Evento rechazado por SIFEN - el documento no puede ser cancelado
     */
    RECHAZADO
}
