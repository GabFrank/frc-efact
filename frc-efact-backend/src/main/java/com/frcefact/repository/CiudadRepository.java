package com.frcefact.repository;

import com.frcefact.model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Ciudad.
 */
@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {

    /**
     * Busca una ciudad por su código.
     */
    Optional<Ciudad> findByCodigo(String codigo);

    /**
     * Busca ciudades activas.
     */
    List<Ciudad> findByActivoTrue();

    /**
     * Busca ciudades por distrito.
     */
    List<Ciudad> findByDistritoIdAndActivoTrue(Long distritoId);

    /**
     * Busca ciudades por código de distrito.
     */
    @Query("SELECT c FROM Ciudad c WHERE c.distrito.codigo = :distritoCodigo AND c.activo = true ORDER BY c.nombre")
    List<Ciudad> findByDistritoCodigoAndActivoTrue(@Param("distritoCodigo") String distritoCodigo);

    /**
     * Busca ciudades por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT c FROM Ciudad c WHERE UPPER(c.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<Ciudad> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca ciudades por código o nombre.
     */
    @Query("SELECT c FROM Ciudad c WHERE (UPPER(c.codigo) LIKE UPPER(CONCAT('%', :busqueda, '%')) OR UPPER(c.nombre) LIKE UPPER(CONCAT('%', :busqueda, '%'))) AND c.activo = true ORDER BY c.nombre")
    List<Ciudad> findByCodigoOrNombreContainingIgnoreCase(@Param("busqueda") String busqueda);

    /**
     * Verifica si existe una ciudad con el código dado.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca una ciudad por código y distrito.
     */
    Optional<Ciudad> findByCodigoAndDistritoId(String codigo, Long distritoId);
}