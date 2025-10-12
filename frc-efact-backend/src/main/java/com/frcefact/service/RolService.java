package com.frcefact.service;

import com.frcefact.model.Rol;
import com.frcefact.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de roles del sistema.
 * Maneja operaciones CRUD y validaciones de roles.
 */
@Service
@Transactional
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    /**
     * Obtiene todos los roles del sistema.
     *
     * @return Lista de todos los roles
     */
    @Transactional(readOnly = true)
    public List<Rol> obtenerTodosLosRoles() {
        return rolRepository.findAll();
    }

    /**
     * Busca un rol por su ID.
     *
     * @param id ID del rol
     * @return Optional con el rol si existe
     */
    @Transactional(readOnly = true)
    public Optional<Rol> obtenerRolPorId(Long id) {
        return rolRepository.findById(id);
    }

    /**
     * Busca un rol por su nombre.
     *
     * @param nombre Nombre del rol (ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR)
     * @return Optional con el rol si existe
     */
    @Transactional(readOnly = true)
    public Optional<Rol> obtenerRolPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    /**
     * Crea un nuevo rol en el sistema.
     *
     * @param rol Rol a crear
     * @return Rol creado
     * @throws IllegalArgumentException si ya existe un rol con ese nombre
     */
    public Rol crearRol(Rol rol) {
        if (rolRepository.existsByNombre(rol.getNombre())) {
            throw new IllegalArgumentException(
                "Ya existe un rol con el nombre: " + rol.getNombre()
            );
        }
        return rolRepository.save(rol);
    }

    /**
     * Actualiza un rol existente.
     *
     * @param id ID del rol a actualizar
     * @param rolActualizado Datos actualizados del rol
     * @return Rol actualizado
     * @throws IllegalArgumentException si el rol no existe
     */
    public Rol actualizarRol(Long id, Rol rolActualizado) {
        Rol rolExistente = rolRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + id));

        // Verificar si el nuevo nombre ya existe en otro rol
        if (!rolExistente.getNombre().equals(rolActualizado.getNombre()) &&
            rolRepository.existsByNombre(rolActualizado.getNombre())) {
            throw new IllegalArgumentException(
                "Ya existe un rol con el nombre: " + rolActualizado.getNombre()
            );
        }

        rolExistente.setNombre(rolActualizado.getNombre());
        rolExistente.setDescripcion(rolActualizado.getDescripcion());

        return rolRepository.save(rolExistente);
    }

    /**
     * Elimina un rol del sistema.
     *
     * @param id ID del rol a eliminar
     * @throws IllegalArgumentException si el rol no existe
     */
    public void eliminarRol(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new IllegalArgumentException("Rol no encontrado con ID: " + id);
        }
        rolRepository.deleteById(id);
    }

    /**
     * Verifica si un rol existe por su nombre.
     *
     * @param nombre Nombre del rol
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existeRolPorNombre(String nombre) {
        return rolRepository.existsByNombre(nombre);
    }
}
