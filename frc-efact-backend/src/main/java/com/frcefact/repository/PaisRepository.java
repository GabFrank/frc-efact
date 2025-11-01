package com.frcefact.repository;

import com.frcefact.model.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Pais.
 */
@Repository
public interface PaisRepository extends JpaRepository<Pais, Long> {

    /**
     * Busca un país por su código.
     */
    Optional<Pais> findByCodigo(String codigo);

    /**
     * Busca países activos.
     */
    List<Pais> findByActivoTrue();

    /**
     * Busca países por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT p FROM Pais p WHERE UPPER(p.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) AND p.activo = true")
    List<Pais> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Verifica si existe un país con el código dado.
     */
    boolean existsByCodigo(String codigo);
}