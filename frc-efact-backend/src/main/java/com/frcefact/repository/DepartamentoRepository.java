package com.frcefact.repository;

import com.frcefact.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Departamento.
 */
@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    /**
     * Busca un departamento por su código.
     */
    Optional<Departamento> findByCodigo(String codigo);

    /**
     * Busca departamentos activos.
     */
    List<Departamento> findByActivoTrue();

    /**
     * Busca departamentos por país.
     */
    List<Departamento> findByPaisIdAndActivoTrue(Long paisId);

    /**
     * Busca departamentos por código de país.
     */
    @Query("SELECT d FROM Departamento d WHERE d.pais.codigo = :paisCodigo AND d.activo = true ORDER BY d.nombre")
    List<Departamento> findByPaisCodigoAndActivoTrue(@Param("paisCodigo") String paisCodigo);

    /**
     * Busca departamentos por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT d FROM Departamento d WHERE UPPER(d.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) AND d.activo = true")
    List<Departamento> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca departamentos por código o nombre.
     */
    @Query("SELECT d FROM Departamento d WHERE (UPPER(d.codigo) LIKE UPPER(CONCAT('%', :busqueda, '%')) OR UPPER(d.nombre) LIKE UPPER(CONCAT('%', :busqueda, '%'))) AND d.activo = true ORDER BY d.nombre")
    List<Departamento> findByCodigoOrNombreContainingIgnoreCase(@Param("busqueda") String busqueda);

    /**
     * Verifica si existe un departamento con el código dado.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca un departamento por código y país.
     */
    Optional<Departamento> findByCodigoAndPaisId(String codigo, Long paisId);
}