package com.frcefact.repository;

import com.frcefact.model.FacturaLegal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad FacturaLegal.
 */
@Repository
public interface FacturaLegalRepository extends JpaRepository<FacturaLegal, Long>, 
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<FacturaLegal> {

    /**
     * Busca todas las facturas activas de una empresa.
     */
    List<FacturaLegal> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca facturas de una empresa con paginación.
     */
    Page<FacturaLegal> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    /**
     * Busca una factura por timbrado detalle y número.
     */
    Optional<FacturaLegal> findByTimbradoDetalleIdAndNumeroFactura(Long timbradoDetalleId, Integer numeroFactura);

    /**
     * Busca facturas por cliente.
     */
    List<FacturaLegal> findByClienteIdAndActivoTrue(Long clienteId);

    /**
     * Busca facturas por cliente con paginación.
     */
    Page<FacturaLegal> findByClienteIdAndActivoTrue(Long clienteId, Pageable pageable);

    /**
     * Busca facturas por rango de fechas.
     */
    @Query("SELECT f FROM FacturaLegal f WHERE f.empresa.id = :empresaId " +
           "AND f.activo = true " +
           "AND f.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<FacturaLegal> findByFechaRange(@Param("empresaId") Long empresaId,
                                        @Param("fechaDesde") LocalDateTime fechaDesde,
                                        @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Busca facturas por rango de fechas con paginación.
     */
    @Query("SELECT f FROM FacturaLegal f WHERE f.empresa.id = :empresaId " +
           "AND f.activo = true " +
           "AND f.fecha BETWEEN :fechaDesde AND :fechaHasta")
    Page<FacturaLegal> findByFechaRange(@Param("empresaId") Long empresaId,
                                        @Param("fechaDesde") LocalDateTime fechaDesde,
                                        @Param("fechaHasta") LocalDateTime fechaHasta,
                                        Pageable pageable);


    /**
     * Busca facturas a crédito.
     */
    List<FacturaLegal> findByEmpresaIdAndCreditoAndActivoTrue(Long empresaId, Boolean credito);

    /**
     * Cuenta facturas de una empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Cuenta facturas de una empresa en un rango de fechas.
     * Excluye facturas con documentos electrónicos cancelados o rechazados.
     */
    @Query("SELECT COUNT(f) FROM FacturaLegal f " +
           "LEFT JOIN f.documentoElectronico de " +
           "WHERE f.empresa.id = :empresaId " +
           "AND f.activo = true " +
           "AND f.fecha BETWEEN :fechaDesde AND :fechaHasta " +
           "AND (de IS NULL OR de.estado NOT IN (com.frcefact.model.EstadoDE.CANCELADO, com.frcefact.model.EstadoDE.RECHAZADO, com.frcefact.model.EstadoDE.ERROR))")
    long countByFechaRange(@Param("empresaId") Long empresaId,
                          @Param("fechaDesde") LocalDateTime fechaDesde,
                          @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Suma total facturado por empresa en un rango de fechas.
     * Excluye facturas con documentos electrónicos cancelados o rechazados.
     */
    @Query("SELECT COALESCE(SUM(f.totalFinal), 0) FROM FacturaLegal f " +
           "LEFT JOIN f.documentoElectronico de " +
           "WHERE f.empresa.id = :empresaId " +
           "AND f.activo = true " +
           "AND f.fecha BETWEEN :fechaDesde AND :fechaHasta " +
           "AND (de IS NULL OR de.estado NOT IN (com.frcefact.model.EstadoDE.CANCELADO, com.frcefact.model.EstadoDE.RECHAZADO, com.frcefact.model.EstadoDE.ERROR))")
    BigDecimal sumTotalByFechaRange(@Param("empresaId") Long empresaId,
                                    @Param("fechaDesde") LocalDateTime fechaDesde,
                                    @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Busca últimas facturas de una empresa.
     */
    @Query("SELECT f FROM FacturaLegal f WHERE f.empresa.id = :empresaId " +
           "AND f.activo = true " +
           "ORDER BY f.fecha DESC")
    List<FacturaLegal> findUltimasFacturas(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * Busca facturas por timbrado detalle.
     */
    List<FacturaLegal> findByTimbradoDetalleIdAndActivoTrue(Long timbradoDetalleId);

    /**
     * Cuenta facturas por timbrado detalle.
     */
    long countByTimbradoDetalleIdAndActivoTrue(Long timbradoDetalleId);

    /**
     * Verifica si existe una factura con el número dado en el timbrado detalle.
     */
    boolean existsByTimbradoDetalleIdAndNumeroFactura(Long timbradoDetalleId, Integer numeroFactura);

    /**
     * Obtiene el número máximo de factura registrado para un timbrado detalle.
     */
    @Query("SELECT MAX(f.numeroFactura) FROM FacturaLegal f WHERE f.timbradoDetalle.id = :timbradoDetalleId")
    Integer findMaxNumeroFacturaByTimbradoDetalleId(@Param("timbradoDetalleId") Long timbradoDetalleId);

    /**
     * Busca facturas ordenadas por fecha descendente.
     */
    List<FacturaLegal> findByEmpresaIdAndActivoTrueOrderByFechaDesc(Long empresaId);
    
    /**
     * Busca facturas por rango de fechas para cálculos (excluye canceladas/rechazadas).
     */
    @Query("SELECT f FROM FacturaLegal f " +
           "LEFT JOIN f.documentoElectronico de " +
           "WHERE f.empresa.id = :empresaId " +
           "AND f.activo = true " +
           "AND f.fecha BETWEEN :fechaDesde AND :fechaHasta " +
           "AND (de IS NULL OR de.estado NOT IN (com.frcefact.model.EstadoDE.CANCELADO, com.frcefact.model.EstadoDE.RECHAZADO, com.frcefact.model.EstadoDE.ERROR))")
    List<FacturaLegal> findByFechaRangeParaCalculos(@Param("empresaId") Long empresaId,
                                                     @Param("fechaDesde") LocalDateTime fechaDesde,
                                                     @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Obtiene una factura por ID con todas las relaciones necesarias cargadas.
     * Usa JOIN FETCH para evitar LazyInitializationException.
     * Carga: empresa, cliente (si existe), documentoElectronico (si existe) y sus items.
     */
    @Query("SELECT DISTINCT f FROM FacturaLegal f " +
           "JOIN FETCH f.empresa " +
           "LEFT JOIN FETCH f.cliente " +
           "LEFT JOIN FETCH f.documentoElectronico de " +
           "LEFT JOIN FETCH de.loteDE " +
           "LEFT JOIN FETCH f.items " +
           "WHERE f.id = :id")
    Optional<FacturaLegal> findByIdWithEmpresa(@Param("id") Long id);
}
