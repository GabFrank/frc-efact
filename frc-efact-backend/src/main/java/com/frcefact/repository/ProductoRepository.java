package com.frcefact.repository;

import com.frcefact.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Producto.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca todos los productos activos de una empresa.
     */
    List<Producto> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca productos de una empresa con paginación.
     */
    Page<Producto> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    /**
     * Busca un producto por código en una empresa.
     */
    Optional<Producto> findByEmpresaIdAndCodigo(Long empresaId, String codigo);

    /**
     * Busca productos por descripción (búsqueda parcial).
     */
    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%'))")
    List<Producto> buscarPorDescripcion(@Param("empresaId") Long empresaId,
                                        @Param("descripcion") String descripcion);

    /**
     * Busca productos por código (búsqueda parcial).
     */
    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "AND p.codigo LIKE CONCAT(:codigo, '%')")
    List<Producto> buscarPorCodigo(@Param("empresaId") Long empresaId,
                                   @Param("codigo") String codigo);

    /**
     * Búsqueda general por código o descripción.
     */
    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "AND (p.codigo LIKE CONCAT(:busqueda, '%') " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Producto> buscarProductos(@Param("empresaId") Long empresaId,
                                   @Param("busqueda") String busqueda);

    /**
     * Búsqueda general con paginación.
     */
    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "AND (p.codigo LIKE CONCAT(:busqueda, '%') " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Producto> buscarProductos(@Param("empresaId") Long empresaId,
                                   @Param("busqueda") String busqueda,
                                   Pageable pageable);

    /**
     * Busca productos por tasa de IVA.
     */
    List<Producto> findByEmpresaIdAndIvaAndActivoTrue(Long empresaId, Integer iva);

    /**
     * Busca productos que requieren balanza.
     */
    List<Producto> findByEmpresaIdAndBalanzaAndActivoTrue(Long empresaId, Boolean balanza);

    /**
     * Cuenta productos activos de una empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Verifica si existe un producto con el código dado en la empresa.
     */
    boolean existsByEmpresaIdAndCodigoAndActivoTrue(Long empresaId, String codigo);

    /**
     * Busca productos ordenados por descripción.
     */
    List<Producto> findByEmpresaIdAndActivoTrueOrderByDescripcionAsc(Long empresaId);
}
