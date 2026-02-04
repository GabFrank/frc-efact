package com.frcefact.repository;

import com.frcefact.model.AccionEnum;
import com.frcefact.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Interfaz personalizada para métodos de repositorio que requieren Criteria Builder.
 */
public interface AuditLogRepositoryCustom {
    
    /**
     * Busca registros de auditoría con filtros dinámicos usando Criteria Builder.
     * 
     * @param usuarioId ID del usuario (opcional)
     * @param empresaId ID de la empresa (opcional)
     * @param entidadTipo tipo de entidad (opcional)
     * @param accion acción realizada (opcional)
     * @param fechaDesde fecha inicial (opcional)
     * @param fechaHasta fecha final (opcional)
     * @param pageable configuración de paginación
     * @return página de registros que cumplen los filtros
     */
    Page<AuditLog> buscarConFiltros(
            Long usuarioId,
            Long empresaId,
            String entidadTipo,
            AccionEnum accion,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable);
}




