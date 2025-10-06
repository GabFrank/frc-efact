package com.frcefact.repository;

import com.frcefact.model.Usuario;
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
}
