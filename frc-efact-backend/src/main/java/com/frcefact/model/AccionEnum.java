package com.frcefact.model;

/**
 * Enum que representa las acciones de auditoría en el sistema.
 * Utilizado para registrar operaciones CRUD en el historial de modificaciones.
 */
public enum AccionEnum {
    /**
     * Acción de creación de un nuevo registro
     */
    CREATE,
    
    /**
     * Acción de actualización de un registro existente
     */
    UPDATE,
    
    /**
     * Acción de eliminación (lógica o física) de un registro
     */
    DELETE,
    
    /**
     * Acción de lectura o consulta de datos sensibles
     */
    READ
}
