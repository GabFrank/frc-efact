package com.frcefact.repository;

import com.frcefact.model.NotaRemision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaRemisionRepository extends JpaRepository<NotaRemision, Long>, JpaSpecificationExecutor<NotaRemision> {
    
    @Query("SELECT nr FROM NotaRemision nr " +
           "LEFT JOIN FETCH nr.items " +
           "LEFT JOIN FETCH nr.empresa " +
           "LEFT JOIN FETCH nr.cliente " +
           "LEFT JOIN FETCH nr.facturaLegal " +
           "LEFT JOIN FETCH nr.vehiculo " +
           "LEFT JOIN FETCH nr.chofer " +
           "LEFT JOIN FETCH nr.timbradoDetalle td " +
           "LEFT JOIN FETCH td.timbrado " +
           "LEFT JOIN FETCH nr.documentoElectronico de " +
           "WHERE nr.id = :id")
    Optional<NotaRemision> findByIdWithRelations(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT nr FROM NotaRemision nr " +
           "LEFT JOIN FETCH nr.items " +
           "LEFT JOIN FETCH nr.empresa " +
           "LEFT JOIN FETCH nr.cliente " +
           "LEFT JOIN FETCH nr.facturaLegal " +
           "LEFT JOIN FETCH nr.vehiculo " +
           "LEFT JOIN FETCH nr.chofer " +
           "LEFT JOIN FETCH nr.timbradoDetalle td " +
           "LEFT JOIN FETCH td.timbrado " +
           "LEFT JOIN FETCH nr.documentoElectronico de " +
           "LEFT JOIN FETCH de.loteDE " +
           "WHERE nr.empresa.id = :empresaId AND nr.activo = true",
           countQuery = "SELECT COUNT(DISTINCT nr) FROM NotaRemision nr " +
           "WHERE nr.empresa.id = :empresaId AND nr.activo = true")
    Page<NotaRemision> findByEmpresaIdAndActivoTrueWithItems(@Param("empresaId") Long empresaId, Pageable pageable);
    
    Page<NotaRemision> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    
    List<NotaRemision> findByEmpresaIdAndActivoTrue(Long empresaId);
    
    /**
     * Obtiene el número máximo de nota de remisión registrado para un timbrado detalle.
     */
    @Query("SELECT MAX(nr.numeroNotaRemision) FROM NotaRemision nr WHERE nr.timbradoDetalle.id = :timbradoDetalleId")
    Integer findMaxNumeroNotaRemisionByTimbradoDetalleId(@Param("timbradoDetalleId") Long timbradoDetalleId);
}

