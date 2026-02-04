package com.frcefact.repository;

import com.frcefact.model.NotaDebitoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaDebitoItemRepository extends JpaRepository<NotaDebitoItem, Long> {
}

