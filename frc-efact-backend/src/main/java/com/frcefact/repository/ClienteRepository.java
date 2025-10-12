package com.frcefact.repository;

import com.frcefact.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Cliente.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca todos los clientes activos de una empresa.
     */
    List<Cliente> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca clientes de una empresa con paginación.
     */
    Page<Cliente> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    /**
     * Busca un cliente por RUC en una empresa.
     */
    Optional<Cliente> findByEmpresaIdAndRuc(Long empresaId, String ruc);

    /**
     * Busca clientes por nombre o razón social (búsqueda parcial).
     */
    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId " +
           "AND c.activo = true " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Cliente> buscarPorNombre(@Param("empresaId") Long empresaId,
                                   @Param("busqueda") String busqueda);

    /**
     * Busca clientes por RUC (búsqueda parcial).
     */
    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId " +
           "AND c.activo = true " +
           "AND c.ruc LIKE CONCAT(:ruc, '%')")
    List<Cliente> buscarPorRuc(@Param("empresaId") Long empresaId,
                               @Param("ruc") String ruc);

    /**
     * Búsqueda general por nombre, razón social o RUC.
     */
    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId " +
           "AND c.activo = true " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR c.ruc LIKE CONCAT(:busqueda, '%'))")
    List<Cliente> buscarClientes(@Param("empresaId") Long empresaId,
                                  @Param("busqueda") String busqueda);

    /**
     * Búsqueda general con paginación.
     */
    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId " +
           "AND c.activo = true " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR c.ruc LIKE CONCAT(:busqueda, '%'))")
    Page<Cliente> buscarClientes(@Param("empresaId") Long empresaId,
                                  @Param("busqueda") String busqueda,
                                  Pageable pageable);

    /**
     * Busca clientes que tributan.
     */
    List<Cliente> findByEmpresaIdAndTributaAndActivoTrue(Long empresaId, Boolean tributa);

    /**
     * Busca clientes por tipo de contribuyente.
     */
    List<Cliente> findByEmpresaIdAndTipoContribuyenteAndActivoTrue(Long empresaId, String tipoContribuyente);

    /**
     * Cuenta clientes activos de una empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Verifica si existe un cliente con el RUC dado en la empresa.
     */
    boolean existsByEmpresaIdAndRucAndActivoTrue(Long empresaId, String ruc);
}
