package com.frcefact.repository;

import com.frcefact.model.AccionEnum;
import com.frcefact.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la gestión de registros de auditoría.
 * Los registros de auditoría son inmutables y solo permiten operaciones de lectura.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Busca registros de auditoría por usuario
     */
    List<AuditLog> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    /**
     * Busca registros de auditoría por usuario con paginación
     */
    Page<AuditLog> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId, Pageable pageable);

    /**
     * Busca registros de auditoría por empresa
     */
    List<AuditLog> findByEmpresaIdOrderByFechaHoraDesc(Long empresaId);

    /**
     * Busca registros de auditoría por empresa con paginación
     */
    Page<AuditLog> findByEmpresaIdOrderByFechaHoraDesc(Long empresaId, Pageable pageable);

    /**
     * Busca registros de auditoría por tipo de entidad
     */
    List<AuditLog> findByEntidadTipoOrderByFechaHoraDesc(String entidadTipo);

    /**
     * Busca el historial completo de una entidad específica
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.entidadTipo = :entidadTipo " +
           "AND a.entidadId = :entidadId " +
           "ORDER BY a.fechaHora DESC")
    List<AuditLog> findHistorialEntidad(@Param("entidadTipo") String entidadTipo,
                                         @Param("entidadId") Long entidadId);

    /**
     * Busca registros de auditoría por acción
     */
    List<AuditLog> findByAccionOrderByFechaHoraDesc(AccionEnum accion);

    /**
     * Busca registros de auditoría por rango de fechas
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fechaHora DESC")
    List<AuditLog> findByFechaHoraBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca registros de auditoría por rango de fechas con paginación
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fechaHora DESC")
    Page<AuditLog> findByFechaHoraBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin,
                                           Pageable pageable);

    /**
     * Busca las últimas N actividades de un usuario
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.usuario.id = :usuarioId " +
           "ORDER BY a.fechaHora DESC")
    List<AuditLog> findUltimasActividadesUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /**
     * Busca las últimas N actividades de una empresa
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.empresa.id = :empresaId " +
           "ORDER BY a.fechaHora DESC")
    List<AuditLog> findUltimasActividadesEmpresa(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:usuarioId IS NULL OR a.usuario.id = :usuarioId) " +
           "AND (:empresaId IS NULL OR a.empresa.id = :empresaId) " +
           "AND (:entidadTipo IS NULL OR a.entidadTipo = :entidadTipo) " +
           "AND (:accion IS NULL OR a.accion = :accion) " +
           "AND (:fechaInicio IS NULL OR a.fechaHora >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR a.fechaHora <= :fechaFin) " +
           "ORDER BY a.fechaHora DESC")
    Page<AuditLog> buscarConFiltros(@Param("usuarioId") Long usuarioId,
                                     @Param("empresaId") Long empresaId,
                                     @Param("entidadTipo") String entidadTipo,
                                     @Param("accion") AccionEnum accion,
                                     @Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin,
                                     Pageable pageable);

    /**
     * Cuenta registros de auditoría por usuario
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Cuenta registros de auditoría por empresa
     */
    long countByEmpresaId(Long empresaId);

    /**
     * Cuenta registros de auditoría por acción
     */
    long countByAccion(AccionEnum accion);

    /**
     * Busca registros de auditoría por IP address
     */
    List<AuditLog> findByIpAddressOrderByFechaHoraDesc(String ipAddress);
}
