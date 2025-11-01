package com.frcefact.repository;

import com.frcefact.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de la entidad Usuario.
 * Proporciona queries personalizadas para autenticación y gestión de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Buscar usuario por username.
     *
     * @param username el username a buscar
     * @return Optional con el usuario si se encuentra
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Buscar usuario por email.
     *
     * @param email el email a buscar
     * @return Optional con el usuario si se encuentra
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Buscar usuario por username o email.
     *
     * @param username el username a buscar
     * @param email el email a buscar
     * @return Optional con el usuario si se encuentra
     */
    Optional<Usuario> findByUsernameOrEmail(String username, String email);

    /**
     * Verificar si existe un username.
     *
     * @param username el username a verificar
     * @return true si el username existe
     */
    boolean existsByUsername(String username);

    /**
     * Verificar si existe un email.
     *
     * @param email el email a verificar
     * @return true si el email existe
     */
    boolean existsByEmail(String email);

    /**
     * Verificar si existe un username excluyendo un usuario específico.
     *
     * @param username el username a verificar
     * @param id el ID del usuario a excluir
     * @return true si el username existe en otro usuario
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * Verificar si existe un email excluyendo un usuario específico.
     *
     * @param email el email a verificar
     * @param id el ID del usuario a excluir
     * @return true si el email existe en otro usuario
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Buscar todos los usuarios activos.
     *
     * @return Lista de usuarios activos
     */
    List<Usuario> findByIsActiveTrue();

    /**
     * Buscar usuarios creados después de una fecha específica.
     *
     * @param date la fecha para filtrar
     * @return Lista de usuarios creados después de la fecha
     */
    List<Usuario> findByCreadoEnAfter(LocalDateTime date);

    /**
     * Buscar usuarios bloqueados (usuarios con bloqueadoHasta en el futuro).
     *
     * @param now timestamp actual
     * @return Lista de usuarios bloqueados
     */
    @Query("SELECT u FROM Usuario u WHERE u.bloqueadoHasta > :now")
    List<Usuario> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Buscar usuarios con intentos fallidos de login mayor al umbral.
     *
     * @param threshold el número mínimo de intentos fallidos
     * @return Lista de usuarios con intentos fallidos por encima del umbral
     */
    @Query("SELECT u FROM Usuario u WHERE u.intentosFallidosLogin >= :threshold")
    List<Usuario> findUsersWithFailedAttempts(@Param("threshold") Integer threshold);

    /**
     * Contar usuarios activos.
     *
     * @return número de usuarios activos
     */
    long countByIsActiveTrue();

    // ========== MÉTODOS PARA ADMINISTRACIÓN DE USUARIOS ==========

    /**
     * Buscar usuarios por término en username o email con paginación.
     *
     * @param username término a buscar en username
     * @param email término a buscar en email
     * @param pageable configuración de paginación
     * @return página de usuarios que coinciden
     */
    Page<Usuario> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    /**
     * Buscar usuarios por término en username o email.
     *
     * @param username término a buscar en username
     * @param email término a buscar en email
     * @return lista de usuarios que coinciden
     */
    List<Usuario> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email);

    /**
     * Buscar usuarios por estado activo con paginación.
     *
     * @param isActive estado activo a filtrar
     * @param pageable configuración de paginación
     * @return página de usuarios con el estado especificado
     */
    Page<Usuario> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Contar usuarios bloqueados.
     *
     * @param now timestamp actual
     * @return número de usuarios bloqueados
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.bloqueadoHasta > :now")
    long countLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Buscar usuarios por rol.
     *
     * @param rolNombre nombre del rol
     * @return lista de usuarios con el rol especificado
     */
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.usuarioRoles ur WHERE ur.rol.nombre = :rolNombre")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);

    /**
     * Buscar usuarios ordenados por último login.
     *
     * @param pageable configuración de paginación
     * @return página de usuarios ordenados por último login
     */
    @Query("SELECT u FROM Usuario u ORDER BY u.ultimoLogin DESC NULLS LAST")
    Page<Usuario> findAllOrderByUltimoLoginDesc(Pageable pageable);

    /**
     * Buscar usuarios disponibles para asignación a empresas (excluye ADMIN).
     *
     * @return lista de usuarios que pueden ser asignados a empresas
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "WHERE u.isActive = true " +
           "AND u.id NOT IN (" +
           "    SELECT DISTINCT u2.id FROM Usuario u2 " +
           "    JOIN u2.usuarioRoles ur " +
           "    WHERE ur.rol.nombre = 'ADMIN'" +
           ")")
    List<Usuario> findUsuariosAsignables();
}
