package com.frcefact.repository;

import com.frcefact.model.TimbradoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad TimbradoDetalle.
 */
@Repository
public interface TimbradoDetalleRepository extends JpaRepository<TimbradoDetalle, Long> {

    /**
     * Busca todos los detalles de un timbrado.
     */
    List<TimbradoDetalle> findByTimbradoIdAndActivoTrue(Long timbradoId);

    /**
     * Busca un detalle por timbrado, punto de expedición y código de establecimiento.
     */
    Optional<TimbradoDetalle> findByTimbradoIdAndPuntoExpedicionAndCodigoEstablecimientoFactura(
        Long timbradoId, String puntoExpedicion, String codigoEstablecimientoFactura);

    /**
     * Busca un detalle con lock pesimista para actualización concurrente.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT td FROM TimbradoDetalle td WHERE td.id = :id")
    Optional<TimbradoDetalle> findByIdWithLock(@Param("id") Long id);

    /**
     * Busca detalles con números disponibles.
     */
    @Query("SELECT td FROM TimbradoDetalle td WHERE td.timbrado.id = :timbradoId " +
           "AND td.activo = true " +
           "AND td.numeroActual <= td.rangoHasta")
    List<TimbradoDetalle> findDetallesConNumerosDisponibles(@Param("timbradoId") Long timbradoId);

    /**
     * Busca detalles que están por agotar su rango.
     */
    @Query("SELECT td FROM TimbradoDetalle td WHERE td.timbrado.empresa.id = :empresaId " +
           "AND td.activo = true " +
           "AND (td.rangoHasta - td.numeroActual + 1) <= :umbral")
    List<TimbradoDetalle> findDetallesPorAgotarse(@Param("empresaId") Long empresaId,
                                                   @Param("umbral") Long umbral);

    /**
     * Busca detalles activos de una empresa.
     */
    @Query("SELECT td FROM TimbradoDetalle td WHERE td.timbrado.empresa.id = :empresaId " +
           "AND td.activo = true")
    List<TimbradoDetalle> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Cuenta detalles con números disponibles de un timbrado.
     */
    @Query("SELECT COUNT(td) FROM TimbradoDetalle td WHERE td.timbrado.id = :timbradoId " +
           "AND td.activo = true " +
           "AND td.numeroActual <= td.rangoHasta")
    long countDetallesConNumerosDisponibles(@Param("timbradoId") Long timbradoId);

    /**
     * Verifica si existe un detalle con el punto de expedición y código de establecimiento.
     */
    boolean existsByTimbradoIdAndPuntoExpedicionAndCodigoEstablecimientoFactura(
        Long timbradoId, String puntoExpedicion, String codigoEstablecimientoFactura);
}
