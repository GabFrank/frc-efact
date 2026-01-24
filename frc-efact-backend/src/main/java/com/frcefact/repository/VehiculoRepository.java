package com.frcefact.repository;

import com.frcefact.model.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Vehiculo.
 */
@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    /**
     * Busca todos los vehículos activos de una empresa.
     */
    List<Vehiculo> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca vehículos de una empresa con paginación.
     */
    Page<Vehiculo> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    /**
     * Busca un vehículo por matrícula en una empresa.
     */
    Optional<Vehiculo> findByEmpresaIdAndMatricula(Long empresaId, String matricula);

    /**
     * Búsqueda general por matrícula o marca.
     */
    @Query("SELECT v FROM Vehiculo v WHERE v.empresa.id = :empresaId " +
           "AND (:activo IS NULL OR v.activo = :activo) " +
           "AND (:busqueda IS NULL OR :busqueda = '' OR " +
           "     LOWER(v.matricula) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "     LOWER(v.marca) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Vehiculo> buscarVehiculos(@Param("empresaId") Long empresaId,
                                    @Param("busqueda") String busqueda,
                                    @Param("activo") Boolean activo,
                                    Pageable pageable);

    /**
     * Búsqueda general sin paginación para autocomplete.
     */
    @Query("SELECT v FROM Vehiculo v WHERE v.empresa.id = :empresaId " +
           "AND v.activo = true " +
           "AND (LOWER(v.matricula) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "     LOWER(v.marca) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Vehiculo> buscarVehiculos(@Param("empresaId") Long empresaId,
                                    @Param("busqueda") String busqueda);

    /**
     * Cuenta vehículos activos de una empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Verifica si existe un vehículo con la matrícula dada en la empresa.
     */
    boolean existsByEmpresaIdAndMatriculaAndActivoTrue(Long empresaId, String matricula);
}
