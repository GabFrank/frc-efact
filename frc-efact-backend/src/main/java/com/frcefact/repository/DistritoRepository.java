package com.frcefact.repository;

import com.frcefact.model.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Distrito.
 */
@Repository
public interface DistritoRepository extends JpaRepository<Distrito, Long> {

    /**
     * Busca un distrito por su código.
     */
    Optional<Distrito> findByCodigo(String codigo);

    /**
     * Busca distritos activos.
     */
    List<Distrito> findByActivoTrue();

    /**
     * Busca distritos por departamento.
     */
    List<Distrito> findByDepartamentoIdAndActivoTrue(Long departamentoId);

    /**
     * Busca distritos por código de departamento.
     */
    @Query("SELECT d FROM Distrito d WHERE d.departamento.codigo = :departamentoCodigo AND d.activo = true ORDER BY d.nombre")
    List<Distrito> findByDepartamentoCodigoAndActivoTrue(@Param("departamentoCodigo") String departamentoCodigo);

    /**
     * Busca distritos por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT d FROM Distrito d WHERE UPPER(d.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) AND d.activo = true")
    List<Distrito> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca distritos por código o nombre.
     */
    @Query("SELECT d FROM Distrito d WHERE (UPPER(d.codigo) LIKE UPPER(CONCAT('%', :busqueda, '%')) OR UPPER(d.nombre) LIKE UPPER(CONCAT('%', :busqueda, '%'))) AND d.activo = true ORDER BY d.nombre")
    List<Distrito> findByCodigoOrNombreContainingIgnoreCase(@Param("busqueda") String busqueda);

    /**
     * Verifica si existe un distrito con el código dado.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca un distrito por código y departamento.
     */
    Optional<Distrito> findByCodigoAndDepartamentoId(String codigo, Long departamentoId);
}