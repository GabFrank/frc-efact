package com.frcefact.repository;

import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.EstadoDE;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Documentos Electrónicos.
 */
@Repository
public interface DocumentoElectronicoRepository extends JpaRepository<DocumentoElectronico, Long> {

    /**
     * Busca un documento electrónico por su CDC
     */
    Optional<DocumentoElectronico> findByCdc(String cdc);

    /**
     * Busca un documento electrónico por el ID de la factura legal
     */
    Optional<DocumentoElectronico> findByFacturaLegalId(Long facturaLegalId);

    /**
     * Busca todos los documentos electrónicos por estado
     */
    List<DocumentoElectronico> findByEstado(EstadoDE estado);

    /**
     * Busca todos los documentos electrónicos por estado y activo
     */
    List<DocumentoElectronico> findByEstadoAndActivoTrue(EstadoDE estado);

    /**
     * Busca documentos electrónicos por empresa y estado
     */
    @Query("SELECT de FROM DocumentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE fl.empresa.id = :empresaId AND de.estado = :estado AND de.activo = true")
    List<DocumentoElectronico> findByEmpresaAndEstado(@Param("empresaId") Long empresaId, 
                                                        @Param("estado") EstadoDE estado);

    /**
     * Busca documentos electrónicos pendientes con CDC generado
     */
    @Query("SELECT de FROM DocumentoElectronico de " +
           "WHERE de.estado IN ('PENDIENTE', 'EN_PROCESO') " +
           "AND de.cdc IS NOT NULL " +
           "AND de.activo = true")
    List<DocumentoElectronico> findPendientesConCdc();

    /**
     * Busca documentos electrónicos por lote
     */
    @Query("SELECT de FROM DocumentoElectronico de " +
           "WHERE de.loteDE.id = :loteId")
    List<DocumentoElectronico> findByLoteId(@Param("loteId") Long loteId);

    /**
     * Cuenta documentos electrónicos por empresa y estado
     */
    @Query("SELECT COUNT(de) FROM DocumentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE fl.empresa.id = :empresaId AND de.estado = :estado AND de.activo = true")
    Long countByEmpresaAndEstado(@Param("empresaId") Long empresaId, 
                                  @Param("estado") EstadoDE estado);

    /**
     * Busca todos los documentos electrónicos de una empresa
     */
    @Query("SELECT de FROM DocumentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE fl.empresa.id = :empresaId AND de.activo = true " +
           "ORDER BY de.fechaEmision DESC")
    List<DocumentoElectronico> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Verifica si existe un documento electrónico para una factura legal
     */
    boolean existsByFacturaLegalId(Long facturaLegalId);

    /**
     * Busca documentos electrónicos por estado con paginación
     */
    Page<DocumentoElectronico> findByEstado(EstadoDE estado, Pageable pageable);

    /**
     * Busca documentos electrónicos por empresa con paginación
     */
    @Query("SELECT de FROM DocumentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE fl.empresa.id = :empresaId AND de.activo = true " +
           "ORDER BY de.fechaEmision DESC")
    Page<DocumentoElectronico> findByFacturaLegal_Empresa_Id(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * Busca documentos electrónicos por estado y empresa con paginación
     */
    @Query("SELECT de FROM DocumentoElectronico de " +
           "JOIN de.facturaLegal fl " +
           "WHERE de.estado = :estado AND fl.empresa.id = :empresaId AND de.activo = true " +
           "ORDER BY de.fechaEmision DESC")
    Page<DocumentoElectronico> findByEstadoAndFacturaLegal_Empresa_Id(
            @Param("estado") EstadoDE estado, 
            @Param("empresaId") Long empresaId, 
            Pageable pageable);
}
