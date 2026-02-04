package com.frcefact.repository;

import com.frcefact.model.NotaRemisionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRemisionItemRepository extends JpaRepository<NotaRemisionItem, Long> {
    List<NotaRemisionItem> findByNotaRemisionId(Long notaRemisionId);
}

