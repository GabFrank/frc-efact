package com.frcefact.repository;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoInutilizacionDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Eventos de Inutilización de Numeración de Documentos Electrónicos.
 */
@Repository
public interface EventoInutilizacionDERepository extends JpaRepository<EventoInutilizacionDE, Long>,
        JpaSpecificationExecutor<EventoInutilizacionDE> {

    /**
     * Busca un evento por su ID único
     */
    Optional<EventoInutilizacionDE> findByEventoId(String eventoId);

    /**
     * Busca eventos por estado
     */
    List<EventoInutilizacionDE> findByEstado(EstadoEvento estado);

    /**
     * Busca eventos por estado y activo
     */
    List<EventoInutilizacionDE> findByEstadoAndActivoTrue(EstadoEvento estado);

    /**
     * Busca eventos por timbrado
     */
    List<EventoInutilizacionDE> findByTimbradoId(Long timbradoId);

    /**
     * Busca eventos activos por timbrado
     */
    List<EventoInutilizacionDE> findByTimbradoIdAndActivoTrue(Long timbradoId);

    /**
     * Busca eventos por timbrado detalle
     */
    List<EventoInutilizacionDE> findByTimbradoDetalleId(Long timbradoDetalleId);

    /**
     * Busca eventos pendientes para procesamiento
     */
    @Query("SELECT e FROM EventoInutilizacionDE e " +
           "WHERE e.estado = 'PENDIENTE' " +
           "AND e.activo = true " +
           "ORDER BY e.creadoEn ASC")
    List<EventoInutilizacionDE> findEventosPendientes();

    /**
     * Busca eventos de inutilización por empresa
     */
    @Query("SELECT e FROM EventoInutilizacionDE e " +
           "JOIN e.timbrado t " +
           "WHERE t.empresa.id = :empresaId " +
           "AND e.activo = true " +
           "ORDER BY e.creadoEn DESC")
    List<EventoInutilizacionDE> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca eventos con filtros usando Specification.
     * Este método se implementa usando JpaSpecificationExecutor.
     * Usar EventoInutilizacionDESpecification.withFilters() para crear la specification.
     */
}


