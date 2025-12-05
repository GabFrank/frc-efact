package com.frcefact.repository;

import com.frcefact.model.NotaRemision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRemisionRepository extends JpaRepository<NotaRemision, Long>, JpaSpecificationExecutor<NotaRemision> {
    
    Page<NotaRemision> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    
    List<NotaRemision> findByEmpresaIdAndActivoTrue(Long empresaId);
}

