package com.frcefact.repository;

import com.frcefact.model.Barrio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Barrio.
 */
@Repository
public interface BarrioRepository extends JpaRepository<Barrio, Long> {

    /**
     * Busca un barrio por su código.
     */
    Optional<Barrio> findByCodigo(String codigo);

    /**
     * Busca barrios activos.
     */
    List<Barrio> findByActivoTrue();

    /**
     * Busca barrios por ciudad.
     */
    List<Barrio> findByCiudadIdAndActivoTrue(Long ciudadId);

    /**
     * Busca barrios por código de ciudad.
     */
    @Query("SELECT b FROM Barrio b WHERE b.ciudad.codigo = :ciudadCodigo AND b.activo = true ORDER BY b.nombre")
    List<Barrio> findByCiudadCodigoAndActivoTrue(@Param("ciudadCodigo") String ciudadCodigo);

    /**
     * Busca barrios por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT b FROM Barrio b WHERE UPPER(b.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) AND b.activo = true")
    List<Barrio> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca barrios por código o nombre.
     */
    @Query("SELECT b FROM Barrio b WHERE (UPPER(b.codigo) LIKE UPPER(CONCAT('%', :busqueda, '%')) OR UPPER(b.nombre) LIKE UPPER(CONCAT('%', :busqueda, '%'))) AND b.activo = true ORDER BY b.nombre")
    List<Barrio> findByCodigoOrNombreContainingIgnoreCase(@Param("busqueda") String busqueda);

    /**
     * Verifica si existe un barrio con el código dado.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca un barrio por código y ciudad.
     */
    Optional<Barrio> findByCodigoAndCiudadId(String codigo, Long ciudadId);
}