package com.frcefact.repository;

import com.frcefact.model.FacturaLegalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad FacturaLegalItem.
 */
@Repository
public interface FacturaLegalItemRepository extends JpaRepository<FacturaLegalItem, Long> {

    /**
     * Busca todos los items de una factura.
     */
    List<FacturaLegalItem> findByFacturaLegalId(Long facturaLegalId);

    /**
     * Busca items por producto.
     */
    List<FacturaLegalItem> findByProductoId(Long productoId);

    /**
     * Cuenta items de una factura.
     */
    long countByFacturaLegalId(Long facturaLegalId);

    /**
     * Suma total de items de una factura.
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM FacturaLegalItem i " +
           "WHERE i.facturaLegal.id = :facturaLegalId")
    BigDecimal sumTotalByFacturaLegal(@Param("facturaLegalId") Long facturaLegalId);

    /**
     * Busca items por producto en un rango de fechas.
     */
    @Query("SELECT i FROM FacturaLegalItem i " +
           "WHERE i.producto.id = :productoId " +
           "AND i.facturaLegal.empresa.id = :empresaId " +
           "AND i.facturaLegal.activo = true " +
           "AND i.facturaLegal.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<FacturaLegalItem> findByProductoAndFechaRange(@Param("productoId") Long productoId,
                                                        @Param("empresaId") Long empresaId,
                                                        @Param("fechaDesde") LocalDateTime fechaDesde,
                                                        @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Suma cantidad vendida de un producto en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM FacturaLegalItem i " +
           "WHERE i.producto.id = :productoId " +
           "AND i.facturaLegal.empresa.id = :empresaId " +
           "AND i.facturaLegal.activo = true " +
           "AND i.facturaLegal.fecha BETWEEN :fechaDesde AND :fechaHasta")
    BigDecimal sumCantidadByProductoAndFechaRange(@Param("productoId") Long productoId,
                                                   @Param("empresaId") Long empresaId,
                                                   @Param("fechaDesde") LocalDateTime fechaDesde,
                                                   @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Suma total vendido de un producto en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM FacturaLegalItem i " +
           "WHERE i.producto.id = :productoId " +
           "AND i.facturaLegal.empresa.id = :empresaId " +
           "AND i.facturaLegal.activo = true " +
           "AND i.facturaLegal.fecha BETWEEN :fechaDesde AND :fechaHasta")
    BigDecimal sumTotalByProductoAndFechaRange(@Param("productoId") Long productoId,
                                               @Param("empresaId") Long empresaId,
                                               @Param("fechaDesde") LocalDateTime fechaDesde,
                                               @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Busca productos más vendidos en un rango de fechas.
     */
    @Query("SELECT i.producto.id, i.producto.descripcion, " +
           "SUM(i.cantidad) as cantidadTotal, SUM(i.total) as montoTotal " +
           "FROM FacturaLegalItem i " +
           "WHERE i.facturaLegal.empresa.id = :empresaId " +
           "AND i.facturaLegal.activo = true " +
           "AND i.facturaLegal.fecha BETWEEN :fechaDesde AND :fechaHasta " +
           "GROUP BY i.producto.id, i.producto.descripcion " +
           "ORDER BY SUM(i.total) DESC")
    List<Object[]> findProductosMasVendidos(@Param("empresaId") Long empresaId,
                                           @Param("fechaDesde") LocalDateTime fechaDesde,
                                           @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * Elimina items de una factura.
     */
    void deleteByFacturaLegalId(Long facturaLegalId);
}
