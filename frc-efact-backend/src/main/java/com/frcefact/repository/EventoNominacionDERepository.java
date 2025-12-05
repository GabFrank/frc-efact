package com.frcefact.repository;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoNominacionDE;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Eventos de Nominación de Documentos Electrónicos.
 */
@Repository
public interface EventoNominacionDERepository extends JpaRepository<EventoNominacionDE, Long> {

    /**
     * Busca un evento por su ID único
     */
    Optional<EventoNominacionDE> findByEventoId(String eventoId);

    /**
     * Busca eventos por estado
     */
    List<EventoNominacionDE> findByEstado(EstadoEvento estado);

    /**
     * Busca eventos por estado y activo
     */
    List<EventoNominacionDE> findByEstadoAndActivoTrue(EstadoEvento estado);

    /**
     * Busca todos los eventos de un documento electrónico
     */
    List<EventoNominacionDE> findByDocumentoElectronicoId(Long documentoElectronicoId);

    /**
     * Busca eventos pendientes para procesamiento
     */
    @Query("SELECT e FROM EventoNominacionDE e " +
           "WHERE e.estado = 'PENDIENTE' " +
           "AND e.activo = true " +
           "ORDER BY e.creadoEn ASC")
    List<EventoNominacionDE> findEventosPendientes();

    /**
     * Busca eventos por CDC del documento
     */
    List<EventoNominacionDE> findByCdcDocumento(String cdcDocumento);

    /**
     * Busca eventos activos por CDC del documento
     */
    List<EventoNominacionDE> findByCdcDocumentoAndActivoTrue(String cdcDocumento);

    /**
     * Busca eventos de nominación por empresa
     */
    @Query("SELECT e FROM EventoNominacionDE e " +
           "JOIN e.documentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE fl.empresa.id = :empresaId " +
           "AND e.activo = true " +
           "ORDER BY e.creadoEn DESC")
    List<EventoNominacionDE> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Verifica si existe un evento aprobado para un documento
     */
    @Query("SELECT COUNT(e) > 0 FROM EventoNominacionDE e " +
           "WHERE e.documentoElectronico.id = :documentoId " +
           "AND e.estado = 'APROBADO' " +
           "AND e.activo = true")
    boolean existsEventoAprobadoForDocumento(@Param("documentoId") Long documentoId);

    /**
     * Busca eventos con filtros y paginación
     */
    @Query("SELECT e FROM EventoNominacionDE e " +
           "JOIN e.documentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE (:empresaId IS NULL OR fl.empresa.id = :empresaId) AND " +
           "(:estado IS NULL OR e.estado = :estado) AND " +
           "(:cdcDocumento IS NULL OR e.cdcDocumento LIKE CONCAT('%', :cdcDocumento, '%')) AND " +
           "(:nombreReceptor IS NULL OR e.nombreReceptor LIKE CONCAT('%', :nombreReceptor, '%')) AND " +
           "(:fechaInicio IS NULL OR e.creadoEn >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR e.creadoEn <= :fechaFin) AND " +
           "(e.activo = true) " +
           "ORDER BY e.creadoEn DESC")
    Page<EventoNominacionDE> findWithFilters(
            @Param("empresaId") Long empresaId,
            @Param("estado") EstadoEvento estado,
            @Param("cdcDocumento") String cdcDocumento,
            @Param("nombreReceptor") String nombreReceptor,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}









