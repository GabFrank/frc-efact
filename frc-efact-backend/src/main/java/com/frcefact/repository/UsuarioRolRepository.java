package com.frcefact.repository;

import com.frcefact.model.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad UsuarioRol.
 * Proporciona operaciones CRUD y queries personalizadas para la relación usuario-rol.
 */
@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    /**
     * Busca todas las relaciones usuario-rol por ID de usuario.
     *
     * @param usuarioId ID del usuario
     * @return lista de relaciones usuario-rol
     */
    List<UsuarioRol> findByUsuarioId(Long usuarioId);

    /**
     * Busca todas las relaciones usuario-rol por ID de rol.
     *
     * @param rolId ID del rol
     * @return lista de relaciones usuario-rol
     */
    List<UsuarioRol> findByRolId(Long rolId);

    /**
     * Verifica si existe una relación usuario-rol específica.
     *
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return true si existe la relación
     */
    boolean existsByUsuarioIdAndRolId(Long usuarioId, Long rolId);

    /**
     * Verifica si un usuario tiene un rol específico por nombre.
     *
     * @param usuarioId ID del usuario
     * @param rolNombre nombre del rol
     * @return true si el usuario tiene el rol
     */
    @Query("SELECT COUNT(ur) > 0 FROM UsuarioRol ur WHERE ur.usuario.id = :usuarioId AND ur.rol.nombre = :rolNombre")
    boolean existsByUsuarioIdAndRolNombre(@Param("usuarioId") Long usuarioId, @Param("rolNombre") String rolNombre);

    /**
     * Elimina todas las relaciones usuario-rol de un usuario específico.
     *
     * @param usuarioId ID del usuario
     */
    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Elimina todas las relaciones usuario-rol de un rol específico.
     *
     * @param rolId ID del rol
     */
    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.rol.id = :rolId")
    void deleteByRolId(@Param("rolId") Long rolId);

    /**
     * Elimina una relación usuario-rol específica.
     *
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     */
    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.usuario.id = :usuarioId AND ur.rol.id = :rolId")
    void deleteByUsuarioIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("rolId") Long rolId);

    /**
     * Cuenta el número de usuarios con un rol específico.
     *
     * @param rolNombre nombre del rol
     * @return número de usuarios con el rol
     */
    @Query("SELECT COUNT(DISTINCT ur.usuario.id) FROM UsuarioRol ur WHERE ur.rol.nombre = :rolNombre")
    long countUsuariosByRolNombre(@Param("rolNombre") String rolNombre);
}