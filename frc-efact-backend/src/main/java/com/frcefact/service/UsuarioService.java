package com.frcefact.service;

import com.frcefact.model.Usuario;
import com.frcefact.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios.
 * Contiene la lógica de negocio para operaciones de usuarios.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crear un nuevo usuario.
     *
     * @param usuario el usuario a crear
     * @return el usuario creado
     * @throws IllegalArgumentException si el username o email ya existe
     */
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Username ya existe: " + usuario.getUsername());
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email ya existe: " + usuario.getEmail());
        }

        // Encriptar password
        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        usuario.setIsActive(true);
        usuario.setIntentosFallidosLogin(0);

        return usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuario por ID.
     *
     * @param id el ID del usuario
     * @return Optional con el usuario si se encuentra
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Buscar usuario por username.
     *
     * @param username el username a buscar
     * @return Optional con el usuario si se encuentra
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Buscar usuario por email.
     *
     * @param email el email a buscar
     * @return Optional con el usuario si se encuentra
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Buscar usuario por username o email.
     *
     * @param usernameOrEmail el username o email a buscar
     * @return Optional con el usuario si se encuentra
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsernameOEmail(String usernameOrEmail) {
        return usuarioRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    /**
     * Listar todos los usuarios.
     *
     * @return Lista de todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Listar usuarios activos.
     *
     * @return Lista de usuarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByIsActiveTrue();
    }

    /**
     * Actualizar usuario.
     *
     * @param usuario el usuario a actualizar
     * @return el usuario actualizado
     */
    public Usuario actualizarUsuario(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + usuario.getId());
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Cambiar password de usuario.
     *
     * @param usuarioId el ID del usuario
     * @param newPassword el nuevo password
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void cambiarPassword(Long usuarioId, String newPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Activar usuario.
     *
     * @param usuarioId el ID del usuario
     */
    public void activarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.setIsActive(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Desactivar usuario.
     *
     * @param usuarioId el ID del usuario
     */
    public void desactivarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.setIsActive(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Registrar intento fallido de login.
     *
     * @param username el username del usuario
     */
    public void registrarIntentoFallidoLogin(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            usuario.incrementFailedLoginAttempts();
            usuarioRepository.save(usuario);
        });
    }

    /**
     * Resetear intentos fallidos de login.
     *
     * @param username el username del usuario
     */
    public void resetearIntentosFallidos(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            usuario.resetFailedLoginAttempts();
            usuarioRepository.save(usuario);
        });
    }

    /**
     * Actualizar último login.
     *
     * @param username el username del usuario
     */
    public void actualizarUltimoLogin(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            usuario.updateLastLogin();
            usuarioRepository.save(usuario);
        });
    }

    /**
     * Verificar si una cuenta está bloqueada.
     *
     * @param username el username del usuario
     * @return true si la cuenta está bloqueada
     */
    @Transactional(readOnly = true)
    public boolean isCuentaBloqueada(String username) {
        return usuarioRepository.findByUsername(username)
                .map(Usuario::isAccountLocked)
                .orElse(false);
    }

    /**
     * Listar usuarios bloqueados.
     *
     * @return Lista de usuarios bloqueados
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosBloqueados() {
        return usuarioRepository.findLockedUsers(LocalDateTime.now());
    }

    /**
     * Contar usuarios activos.
     *
     * @return número de usuarios activos
     */
    @Transactional(readOnly = true)
    public long contarUsuariosActivos() {
        return usuarioRepository.countByIsActiveTrue();
    }

    /**
     * Eliminar usuario (soft delete - desactivar).
     *
     * @param usuarioId el ID del usuario
     */
    public void eliminarUsuario(Long usuarioId) {
        desactivarUsuario(usuarioId);
    }
}
