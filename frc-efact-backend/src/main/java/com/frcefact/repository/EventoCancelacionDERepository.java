package com.frcefact.repository;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoCancelacionDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Eventos de Cancelación de Documentos Electrónicos.
 */
@Repository
public interface EventoCancelacionDERepository extends JpaRepository<EventoCancelacionDE, Long> {

    /**
     * Busca un evento por su ID único
     */
    Optional<EventoCancelacionDE> findByEventoId(String eventoId);

    /**
     * Busca eventos por estado
     */
    List<EventoCancelacionDE> findByEstado(EstadoEvento estado);

    /**
     * Busca eventos por estado y activo
     */
    List<EventoCancelacionDE> findByEstadoAndActivoTrue(EstadoEvento estado);

    /**
     * Busca todos los eventos de un documento electrónico
     */
    List<EventoCancelacionDE> findByDocumentoElectronicoId(Long documentoElectronicoId);

    /**
     * Busca eventos pendientes para procesamiento
     */
    @Query("SELECT e FROM EventoCancelacionDE e " +
           "WHERE e.estado = 'PENDIENTE' " +
           "AND e.activo = true " +
           "ORDER BY e.creadoEn ASC")
    List<EventoCancelacionDE> findEventosPendientes();

    /**
     * Busca eventos por CDC del documento
     */
    List<EventoCancelacionDE> findByCdcDocumento(String cdcDocumento);

    /**
     * Busca eventos activos por CDC del documento
     */
    List<EventoCancelacionDE> findByCdcDocumentoAndActivoTrue(String cdcDocumento);

    /**
     * Busca eventos de cancelación por empresa
     */
    @Query("SELECT e FROM EventoCancelacionDE e " +
           "JOIN e.documentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE fl.empresa.id = :empresaId " +
           "AND e.activo = true " +
           "ORDER BY e.creadoEn DESC")
    List<EventoCancelacionDE> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Cuenta eventos por estado
     */
    Long countByEstado(EstadoEvento estado);

    /**
     * Verifica si existe un evento aprobado para un documento
     */
    @Query("SELECT COUNT(e) > 0 FROM EventoCancelacionDE e " +
           "WHERE e.documentoElectronico.id = :documentoId " +
           "AND e.estado = 'APROBADO' " +
           "AND e.activo = true")
    boolean existsEventoAprobadoForDocumento(@Param("documentoId") Long documentoId);
}
