package com.frcefact.model;

/**
 * Estado de un Evento de Cancelación de Documento Electrónico.
 * Representa el estado del evento durante su procesamiento en SIFEN.
 */
public enum EstadoEvento {
    /**
     * Evento creado pero aún no enviado a SIFEN o esperando procesamiento
     */
    PENDIENTE,
    
    /**
     * Evento aprobado por SIFEN
     */
    APROBADO,
    
    /**
     * Evento rechazado por SIFEN
     */
    RECHAZADO,
    
    /**
     * Error al enviar el evento a SIFEN
     */
    ERROR_ENVIO
}
