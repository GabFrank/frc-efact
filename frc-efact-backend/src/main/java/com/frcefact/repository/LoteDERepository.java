package com.frcefact.repository;

import com.frcefact.model.EstadoLoteDE;
import com.frcefact.model.LoteDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la gestión de Lotes de Documentos Electrónicos.
 */
@Repository
public interface LoteDERepository extends JpaRepository<LoteDE, Long> {

    /**
     * Busca todos los lotes por estado
     */
    List<LoteDE> findByEstado(EstadoLoteDE estado);

    /**
     * Busca lotes por empresa y estado
     */
    List<LoteDE> findByEmpresaIdAndEstado(Long empresaId, EstadoLoteDE estado);

    /**
     * Busca lotes en proceso para consulta periódica
     */
    @Query("SELECT l FROM LoteDE l " +
           "WHERE l.estado = 'EN_PROCESO' " +
           "ORDER BY l.fechaUltimoIntento ASC")
    List<LoteDE> findLotesEnProceso();

    /**
     * Busca lotes con error que pueden ser reintentados
     */
    @Query("SELECT l FROM LoteDE l " +
           "WHERE l.estado IN ('ERROR', 'ERROR_ENVIO') " +
           "AND l.intentos < :maxIntentos " +
           "AND (l.fechaUltimoIntento IS NULL OR l.fechaUltimoIntento < :fechaLimite) " +
           "ORDER BY l.fechaUltimoIntento ASC")
    List<LoteDE> findLotesParaReintentar(@Param("maxIntentos") Integer maxIntentos,
                                          @Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Busca todos los lotes de una empresa ordenados por fecha de creación
     */
    @Query("SELECT l FROM LoteDE l " +
           "WHERE l.empresa.id = :empresaId " +
           "ORDER BY l.creadoEn DESC")
    List<LoteDE> findByEmpresaIdOrderByCreadoEnDesc(@Param("empresaId") Long empresaId);

    /**
     * Cuenta lotes por empresa y estado
     */
    Long countByEmpresaIdAndEstado(Long empresaId, EstadoLoteDE estado);

    /**
     * Busca lotes pendientes de una empresa
     */
    @Query("SELECT l FROM LoteDE l " +
           "WHERE l.empresa.id = :empresaId " +
           "AND l.estado = 'PENDIENTE' " +
           "ORDER BY l.creadoEn ASC")
    List<LoteDE> findLotesPendientesByEmpresa(@Param("empresaId") Long empresaId);
}
