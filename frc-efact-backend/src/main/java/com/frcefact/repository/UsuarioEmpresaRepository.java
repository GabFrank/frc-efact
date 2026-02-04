package com.frcefact.repository;

import com.frcefact.model.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad UsuarioEmpresa.
 * Proporciona operaciones CRUD y consultas personalizadas para la relación usuario-empresa.
 */
@Repository
public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, Long> {

    /**
     * Busca la relación entre un usuario y una empresa.
     *
     * @param usuarioId ID del usuario
     * @param empresaId ID de la empresa
     * @return Optional con la relación si existe
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue " +
           "WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId")
    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(
            @Param("usuarioId") Long usuarioId,
            @Param("empresaId") Long empresaId);

    /**
     * Busca todas las relaciones activas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return lista de relaciones usuario-empresa activas
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue " +
           "WHERE ue.usuario.id = :usuarioId AND ue.activo = true")
    List<UsuarioEmpresa> findByUsuarioIdAndActivoTrue(@Param("usuarioId") Long usuarioId);

    /**
     * Busca todas las relaciones activas de una empresa.
     * Incluye fetch join de usuario, empresa y roles del usuario para evitar LazyInitializationException.
     *
     * @param empresaId ID de la empresa
     * @return lista de relaciones usuario-empresa activas
     */
    @Query("SELECT DISTINCT ue FROM UsuarioEmpresa ue " +
           "LEFT JOIN FETCH ue.usuario u " +
           "LEFT JOIN FETCH u.usuarioRoles ur " +
           "LEFT JOIN FETCH ur.rol " +
           "LEFT JOIN FETCH ue.empresa " +
           "WHERE ue.empresa.id = :empresaId AND ue.activo = true")
    List<UsuarioEmpresa> findByEmpresaIdAndActivoTrue(@Param("empresaId") Long empresaId);

    /**
     * Busca relaciones por usuario y rol en empresa.
     *
     * @param usuarioId ID del usuario
     * @param rolEmpresa rol en la empresa (ADMINISTRADOR, FACTURADOR o LECTOR)
     * @return lista de relaciones que coinciden
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue " +
           "WHERE ue.usuario.id = :usuarioId " +
           "AND ue.rolEmpresa = :rolEmpresa " +
           "AND ue.activo = true")
    List<UsuarioEmpresa> findByUsuarioIdAndRolEmpresa(
            @Param("usuarioId") Long usuarioId,
            @Param("rolEmpresa") String rolEmpresa);

    /**
     * Verifica si un usuario tiene acceso a una empresa.
     *
     * @param usuarioId ID del usuario
     * @param empresaId ID de la empresa
     * @return true si el usuario tiene acceso activo, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(ue) > 0 THEN true ELSE false END " +
           "FROM UsuarioEmpresa ue " +
           "WHERE ue.usuario.id = :usuarioId " +
           "AND ue.empresa.id = :empresaId " +
           "AND ue.activo = true")
    boolean existsByUsuarioIdAndEmpresaIdAndActivoTrue(
            @Param("usuarioId") Long usuarioId,
            @Param("empresaId") Long empresaId);

    /**
     * Verifica si un usuario tiene rol de administrador en una empresa.
     *
     * @param usuarioId ID del usuario
     * @param empresaId ID de la empresa
     * @return true si el usuario es administrador, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(ue) > 0 THEN true ELSE false END " +
           "FROM UsuarioEmpresa ue " +
           "WHERE ue.usuario.id = :usuarioId " +
           "AND ue.empresa.id = :empresaId " +
           "AND ue.rolEmpresa = 'ADMINISTRADOR' " +
           "AND ue.activo = true")
    boolean isUsuarioAdministradorDeEmpresa(
            @Param("usuarioId") Long usuarioId,
            @Param("empresaId") Long empresaId);

    /**
     * Cuenta cuántos usuarios activos tiene una empresa.
     *
     * @param empresaId ID de la empresa
     * @return cantidad de usuarios activos
     */
    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue " +
           "WHERE ue.empresa.id = :empresaId AND ue.activo = true")
    long countUsuariosByEmpresaId(@Param("empresaId") Long empresaId);
}
