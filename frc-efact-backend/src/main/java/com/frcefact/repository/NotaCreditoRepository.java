package com.frcefact.repository;

import com.frcefact.model.NotaCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long>, JpaSpecificationExecutor<NotaCredito> {
    
    @Query("SELECT DISTINCT nc FROM NotaCredito nc " +
           "LEFT JOIN FETCH nc.items " +
           "LEFT JOIN FETCH nc.empresa " +
           "LEFT JOIN FETCH nc.cliente " +
           "LEFT JOIN FETCH nc.facturaLegal " +
           "LEFT JOIN FETCH nc.timbradoDetalle td " +
           "LEFT JOIN FETCH td.timbrado " +
           "WHERE nc.empresa.id = :empresaId AND nc.activo = true")
    List<NotaCredito> findByEmpresaIdAndActivoTrueWithItems(@Param("empresaId") Long empresaId);
    
    @Query(value = "SELECT DISTINCT nc FROM NotaCredito nc " +
           "LEFT JOIN FETCH nc.items " +
           "LEFT JOIN FETCH nc.empresa " +
           "LEFT JOIN FETCH nc.cliente " +
           "LEFT JOIN FETCH nc.facturaLegal " +
           "LEFT JOIN FETCH nc.timbradoDetalle td " +
           "LEFT JOIN FETCH td.timbrado " +
           "WHERE nc.empresa.id = :empresaId AND nc.activo = true",
           countQuery = "SELECT COUNT(DISTINCT nc) FROM NotaCredito nc " +
           "WHERE nc.empresa.id = :empresaId AND nc.activo = true")
    Page<NotaCredito> findByEmpresaIdAndActivoTrueWithItems(@Param("empresaId") Long empresaId, Pageable pageable);
    
    Page<NotaCredito> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    
    List<NotaCredito> findByEmpresaIdAndActivoTrue(Long empresaId);
    
    Page<NotaCredito> findByClienteIdAndActivoTrue(Long clienteId, Pageable pageable);
    
    @Query("SELECT DISTINCT nc FROM NotaCredito nc " +
           "LEFT JOIN FETCH nc.empresa " +
           "LEFT JOIN FETCH nc.cliente " +
           "LEFT JOIN FETCH nc.timbradoDetalle td " +
           "LEFT JOIN FETCH td.timbrado " +
           "LEFT JOIN FETCH nc.facturaLegal " +
           "WHERE nc.id = :id")
    java.util.Optional<NotaCredito> findByIdWithEmpresa(@Param("id") Long id);
}

