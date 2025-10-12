package com.frcefact.annotation;

import com.frcefact.model.AccionEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que deben ser auditados.
 * Cuando un método anotado con @Auditable es ejecutado, se registra
 * automáticamente en la tabla de auditoría con los detalles de la operación.
 * 
 * Ejemplo de uso:
 * <pre>
 * {@code
 * @Auditable(entidad = "Empresa", accion = AccionEnum.CREATE)
 * public Empresa crearEmpresa(EmpresaDto dto) {
 *     // lógica de creación
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Nombre de la entidad que está siendo auditada.
     * Ejemplo: "Empresa", "Factura", "Cliente", "Producto"
     * 
     * @return el nombre de la entidad
     */
    String entidad();
    
    /**
     * Tipo de acción que se está realizando.
     * 
     * @return la acción (CREATE, UPDATE, DELETE)
     */
    AccionEnum accion();
    
    /**
     * Descripción opcional de la operación.
     * Si no se proporciona, se generará automáticamente.
     * 
     * @return descripción de la operación
     */
    String descripcion() default "";
}
