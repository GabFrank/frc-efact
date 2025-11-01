package com.frcefact.repository;

import com.frcefact.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Empresa.
 * Proporciona operaciones CRUD y consultas personalizadas para empresas.
 */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    /**
     * Busca una empresa por su RUC.
     *
     * @param ruc RUC de la empresa
     * @return Optional con la empresa si existe
     */
    Optional<Empresa> findByRuc(String ruc);

    /**
     * Busca empresas activas.
     *
     * @param activo estado de activación
     * @return lista de empresas activas
     */
    List<Empresa> findByActivo(Boolean activo);

    /**
     * Busca empresas por razón social (búsqueda parcial, case-insensitive).
     *
     * @param razonSocial texto a buscar en razón social
     * @return lista de empresas que coinciden
     */
    List<Empresa> findByRazonSocialContainingIgnoreCase(String razonSocial);

    /**
     * Busca empresas a las que un usuario tiene acceso.
     *
     * @param usuarioId ID del usuario
     * @return lista de empresas del usuario
     */
    @Query("SELECT DISTINCT e FROM Empresa e " +
           "JOIN e.usuarioEmpresas ue " +
           "WHERE ue.usuario.id = :usuarioId AND ue.activo = true AND e.activo = true")
    List<Empresa> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Verifica si existe una empresa con el RUC dado.
     *
     * @param ruc RUC a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByRuc(String ruc);

    /**
     * Busca empresas con certificado próximo a vencer.
     *
     * @return lista de empresas con certificado por vencer
     */
    @Query(value = "SELECT * FROM empresa.empresa e " +
           "WHERE e.activo = true " +
           "AND e.certificado_fecha_expiracion IS NOT NULL " +
           "AND e.certificado_fecha_expiracion <= CURRENT_DATE + INTERVAL '30 days' " +
           "AND e.certificado_fecha_expiracion > CURRENT_DATE", 
           nativeQuery = true)
    List<Empresa> findEmpresasConCertificadoPorVencer();
}
