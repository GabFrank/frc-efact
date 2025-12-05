package com.frcefact.repository;

import com.frcefact.model.NotaCreditoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaCreditoItemRepository extends JpaRepository<NotaCreditoItem, Long> {
    List<NotaCreditoItem> findByNotaCreditoId(Long notaCreditoId);
}

