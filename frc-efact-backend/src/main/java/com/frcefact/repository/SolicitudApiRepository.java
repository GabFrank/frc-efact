package com.frcefact.repository;

import com.frcefact.model.SolicitudApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudApiRepository extends JpaRepository<SolicitudApi, Long> {
}
