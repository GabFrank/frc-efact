package com.frcefact.repository;

import com.frcefact.model.CuentaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaServicioRepository extends JpaRepository<CuentaServicio, Long> {
}
