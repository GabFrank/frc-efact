package com.frcefact.repository;

import com.frcefact.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /** Trae la key con su cuenta y la empresa de la cuenta, para autenticar en una sola consulta. */
    @Query("SELECT k FROM ApiKey k JOIN FETCH k.cuentaServicio c LEFT JOIN FETCH c.empresa WHERE k.prefijo = :prefijo")
    Optional<ApiKey> findByPrefijoConCuenta(@Param("prefijo") String prefijo);

    boolean existsByPrefijo(String prefijo);

    List<ApiKey> findByCuentaServicioIdOrderByCreadoEnDesc(Long cuentaServicioId);
}
