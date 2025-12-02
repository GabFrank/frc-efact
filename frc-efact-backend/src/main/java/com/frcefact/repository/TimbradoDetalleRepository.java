package com.frcefact.repository;

import com.frcefact.model.TimbradoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gestión de TimbradoDetalle.
 */
@Repository
public interface TimbradoDetalleRepository extends JpaRepository<TimbradoDetalle, Long> {

    /**
     * Busca todos los detalles de un timbrado ordenados por punto de expedición.
     */
    List<TimbradoDetalle> findByTimbradoIdOrderByPuntoExpedicionAsc(Long timbradoId);

    /**
     * Busca todos los detalles activos de un timbrado.
     */
    List<TimbradoDetalle> findByTimbradoIdAndActivoTrue(Long timbradoId);

    /**
     * Busca un detalle específico por timbrado y punto de expedición.
     */
    Optional<TimbradoDetalle> findByTimbradoIdAndPuntoExpedicion(Long timbradoId, String puntoExpedicion);

    /**
     * Busca un detalle específico por timbrado y punto de expedición excluyendo un ID específico.
     * Útil para validar unicidad al actualizar.
     */
    Optional<TimbradoDetalle> findByTimbradoIdAndPuntoExpedicionAndIdNot(Long timbradoId, String puntoExpedicion, Long id);

    /**
     * Cuenta cuántos detalles activos tiene un timbrado.
     */
    long countByTimbradoIdAndActivoTrue(Long timbradoId);

    /**
     * Busca detalles que están por agotarse (porcentaje de uso alto).
     */
    @Query("SELECT td FROM TimbradoDetalle td WHERE td.timbrado.id = :timbradoId " +
           "AND td.activo = true " +
           "AND ((td.numeroActual - td.rangoDesde) * 100.0 / td.cantidad) >= :umbralPorcentaje")
    List<TimbradoDetalle> findDetallesPorAgotarse(@Param("timbradoId") Long timbradoId, 
                                                  @Param("umbralPorcentaje") double umbralPorcentaje);

    /**
     * Verifica si existe algún detalle con rangos superpuestos para un timbrado.
     */
    @Query("SELECT COUNT(td) > 0 FROM TimbradoDetalle td WHERE td.timbrado.id = :timbradoId " +
           "AND td.activo = true " +
           "AND td.id != :excludeId " +
           "AND ((td.rangoDesde <= :rangoHasta AND td.rangoHasta >= :rangoDesde))")
    boolean existsRangoSuperpuesto(@Param("timbradoId") Long timbradoId,
                                   @Param("rangoDesde") Long rangoDesde,
                                   @Param("rangoHasta") Long rangoHasta,
                                   @Param("excludeId") Long excludeId);

    /**
     * Busca todos los detalles activos de una empresa.
     */
    @Query("SELECT td FROM TimbradoDetalle td WHERE td.timbrado.empresa.id = :empresaId " +
           "AND td.activo = true " +
           "ORDER BY td.puntoExpedicion ASC")
    List<TimbradoDetalle> findByEmpresaIdAndActivoTrue(@Param("empresaId") Long empresaId);
}