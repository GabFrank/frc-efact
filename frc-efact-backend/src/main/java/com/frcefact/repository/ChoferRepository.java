package com.frcefact.repository;

import com.frcefact.model.Chofer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Chofer.
 */
@Repository
public interface ChoferRepository extends JpaRepository<Chofer, Long> {

    /**
     * Busca todos los choferes activos de una empresa.
     */
    List<Chofer> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca choferes de una empresa con paginación.
     */
    Page<Chofer> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    /**
     * Busca un chofer por documento en una empresa.
     */
    Optional<Chofer> findByEmpresaIdAndDocumento(Long empresaId, String documento);

    /**
     * Búsqueda general por nombre o documento.
     */
    @Query("SELECT c FROM Chofer c WHERE c.empresa.id = :empresaId " +
           "AND (:activo IS NULL OR c.activo = :activo) " +
           "AND (:busqueda IS NULL OR :busqueda = '' OR " +
           "     LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "     LOWER(c.documento) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Chofer> buscarChoferes(@Param("empresaId") Long empresaId,
                                 @Param("busqueda") String busqueda,
                                 @Param("activo") Boolean activo,
                                 Pageable pageable);

    /**
     * Búsqueda general sin paginación para autocomplete.
     */
    @Query("SELECT c FROM Chofer c WHERE c.empresa.id = :empresaId " +
           "AND c.activo = true " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "     LOWER(c.documento) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Chofer> buscarChoferes(@Param("empresaId") Long empresaId,
                                 @Param("busqueda") String busqueda);

    /**
     * Cuenta choferes activos de una empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);
}
