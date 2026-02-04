package com.frcefact.repository;

import com.frcefact.model.NotaDebito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaDebitoRepository extends JpaRepository<NotaDebito, Long>, JpaSpecificationExecutor<NotaDebito> {
    
    Page<NotaDebito> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    
    List<NotaDebito> findByEmpresaIdAndActivoTrue(Long empresaId);
}

