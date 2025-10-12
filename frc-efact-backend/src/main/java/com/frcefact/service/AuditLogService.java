package com.frcefact.service;

import com.frcefact.model.AccionEnum;
import com.frcefact.model.AuditLog;
import com.frcefact.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestión de registros de auditoría.
 * Proporciona métodos para consultar el historial de modificaciones
 * con diferentes filtros y criterios de búsqueda.
 */
@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Guarda un registro de auditoría.
     * 
     * @param auditLog el registro a guardar
     * @return el registro guardado
     */
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    /**
     * Obtiene las últimas actividades del sistema para el dashboard.
     * 
     * @param limit cantidad máxima de registros a retornar
     * @return lista de registros de auditoría más recientes
     */
    public List<AuditLog> getUltimasActividades(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        return page.getContent();
    }

    /**
     * Obtiene las últimas actividades de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @param limit cantidad máxima de registros
     * @return lista de actividades del usuario
     */
    public List<AuditLog> getUltimasActividadesUsuario(Long usuarioId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return auditLogRepository.findUltimasActividadesUsuario(usuarioId, pageable);
    }

    /**
     * Obtiene el historial completo de cambios de una entidad específica.
     * 
     * @param entidadTipo tipo de entidad (ej: "Empresa", "Factura")
     * @param entidadId ID de la entidad
     * @return lista de todos los cambios realizados a la entidad
     */
    public List<AuditLog> getHistorialEntidad(String entidadTipo, Long entidadId) {
        return auditLogRepository.findHistorialEntidad(entidadTipo, entidadId);
    }

    /**
     * Busca registros de auditoría con filtros dinámicos.
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
    public Page<AuditLog> buscarConFiltros(
            Long usuarioId,
            Long empresaId,
            String entidadTipo,
            AccionEnum accion,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable) {
        
        return auditLogRepository.buscarConFiltros(
            usuarioId, empresaId, entidadTipo, accion, fechaDesde, fechaHasta, pageable
        );
    }

    /**
     * Obtiene estadísticas de auditoría por tipo de acción.
     * 
     * @param empresaId ID de la empresa (opcional)
     * @param fechaDesde fecha inicial
     * @param fechaHasta fecha final
     * @return mapa con conteo de acciones por tipo
     */
    public java.util.Map<AccionEnum, Long> getEstadisticasPorAccion(
            Long empresaId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        
        // Buscar con filtros usando el método del repositorio
        Page<AuditLog> page = auditLogRepository.buscarConFiltros(
            null, empresaId, null, null, fechaDesde, fechaHasta, Pageable.unpaged()
        );
        
        List<AuditLog> logs = page.getContent();

        // Agrupar por acción
        java.util.Map<AccionEnum, Long> estadisticas = new java.util.HashMap<>();
        for (AuditLog log : logs) {
            estadisticas.merge(log.getAccion(), 1L, Long::sum);
        }

        return estadisticas;
    }

    /**
     * Obtiene el conteo total de registros de auditoría.
     * 
     * @return cantidad total de registros
     */
    public long contarTotal() {
        return auditLogRepository.count();
    }

    /**
     * Obtiene el conteo de registros por empresa.
     * 
     * @param empresaId ID de la empresa
     * @return cantidad de registros de la empresa
     */
    public long contarPorEmpresa(Long empresaId) {
        return auditLogRepository.countByEmpresaId(empresaId);
    }
}
