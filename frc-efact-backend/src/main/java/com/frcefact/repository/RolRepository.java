package com.frcefact.repository;

import com.frcefact.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Rol.
 * Proporciona operaciones CRUD y queries personalizadas para roles del sistema.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre.
     *
     * @param nombre Nombre del rol (ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR)
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Verifica si existe un rol con el nombre especificado.
     *
     * @param nombre Nombre del rol
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}
