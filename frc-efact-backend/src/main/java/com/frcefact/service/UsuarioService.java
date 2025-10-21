package com.frcefact.service;

import com.frcefact.dto.CreateUserRequest;
import com.frcefact.dto.UpdateUserRequest;
import com.frcefact.dto.UserSearchRequest;
import com.frcefact.model.Rol;
import com.frcefact.model.Usuario;
import com.frcefact.model.UsuarioRol;
import com.frcefact.repository.RolRepository;
import com.frcefact.repository.UsuarioRepository;
import com.frcefact.repository.UsuarioRolRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios.
 * Contiene la lógica de negocio para operaciones de usuarios.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, 
                         RolRepository rolRepository,
                         UsuarioRolRepository usuarioRolRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
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
     * Listar usuarios disponibles para asignación a empresas (excluye ADMIN).
     *
     * @return Lista de usuarios que pueden ser asignados a empresas
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosAsignables() {
        return usuarioRepository.findUsuariosAsignables();
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

    // ========== MÉTODOS PARA ADMINISTRACIÓN DE USUARIOS ==========

    /**
     * Crear usuario desde request de administrador.
     *
     * @param request datos del usuario a crear
     * @return el usuario creado
     */
    public Usuario crearUsuarioAdmin(CreateUserRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username ya existe: " + request.getUsername());
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email ya existe: " + request.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setIsActive(request.getIsActive());
        usuario.setIntentosFallidosLogin(0);

        Usuario usuarioCreado = usuarioRepository.save(usuario);

        // Asignar roles
        asignarRoles(usuarioCreado, request.getRoles());

        return usuarioCreado;
    }

    /**
     * Actualizar usuario desde request de administrador.
     *
     * @param usuarioId el ID del usuario
     * @param request datos de actualización
     * @return el usuario actualizado
     */
    public Usuario actualizarUsuarioAdmin(Long usuarioId, UpdateUserRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        // Validar unicidad si se cambia username o email
        if (request.getUsername() != null && !request.getUsername().equals(usuario.getUsername())) {
            if (usuarioRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username ya existe: " + request.getUsername());
            }
            usuario.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email ya existe: " + request.getEmail());
            }
            usuario.setEmail(request.getEmail());
        }

        if (request.getIsActive() != null) {
            usuario.setIsActive(request.getIsActive());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        // Actualizar roles si se proporcionan
        if (request.getRoles() != null) {
            // Remover roles existentes
            usuarioRolRepository.deleteByUsuarioId(usuarioId);
            // Asignar nuevos roles
            asignarRoles(usuarioActualizado, request.getRoles());
        }

        return usuarioActualizado;
    }

    /**
     * Buscar usuarios con filtros y paginación.
     *
     * @param searchRequest criterios de búsqueda
     * @return página de usuarios
     */
    @Transactional(readOnly = true)
    public Page<Usuario> buscarUsuarios(UserSearchRequest searchRequest) {
        Sort sort = Sort.by(
            "DESC".equalsIgnoreCase(searchRequest.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            searchRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            return usuarioRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchRequest.getSearchTerm().trim(),
                searchRequest.getSearchTerm().trim(),
                pageable
            );
        }

        if (searchRequest.getIsActive() != null) {
            return usuarioRepository.findByIsActive(searchRequest.getIsActive(), pageable);
        }

        return usuarioRepository.findAll(pageable);
    }

    /**
     * Buscar usuarios por término de búsqueda.
     *
     * @param searchTerm término a buscar en username o email
     * @return lista de usuarios que coinciden
     */
    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuariosPorTermino(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return usuarioRepository.findAll();
        }
        return usuarioRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            searchTerm.trim(), searchTerm.trim()
        );
    }

    /**
     * Resetear contraseña de usuario.
     *
     * @param usuarioId el ID del usuario
     * @param newPassword la nueva contraseña
     * @param forcePasswordChange si debe forzar cambio en próximo login
     */
    public void resetearPassword(Long usuarioId, String newPassword, Boolean forcePasswordChange) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        
        // Si se requiere forzar cambio de contraseña, se podría implementar un campo adicional
        // Por ahora, resetear intentos fallidos
        usuario.resetFailedLoginAttempts();
        
        usuarioRepository.save(usuario);
    }

    /**
     * Desbloquear cuenta de usuario.
     *
     * @param usuarioId el ID del usuario
     */
    public void desbloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.resetFailedLoginAttempts();
        usuarioRepository.save(usuario);
    }

    /**
     * Obtener roles de un usuario.
     *
     * @param usuarioId el ID del usuario
     * @return conjunto de nombres de roles
     */
    @Transactional(readOnly = true)
    public Set<String> obtenerRolesUsuario(Long usuarioId) {
        return usuarioRolRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(usuarioRol -> usuarioRol.getRol().getNombre())
                .collect(Collectors.toSet());
    }

    /**
     * Asignar roles a un usuario.
     *
     * @param usuario el usuario
     * @param nombreRoles nombres de los roles a asignar
     */
    private void asignarRoles(Usuario usuario, Set<String> nombreRoles) {
        for (String nombreRol : nombreRoles) {
            Rol rol = rolRepository.findByNombre(nombreRol)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombreRol));
            
            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuario(usuario);
            usuarioRol.setRol(rol);
            usuarioRolRepository.save(usuarioRol);
        }
    }

    /**
     * Verificar si un usuario tiene un rol específico.
     *
     * @param usuarioId el ID del usuario
     * @param nombreRol el nombre del rol
     * @return true si el usuario tiene el rol
     */
    @Transactional(readOnly = true)
    public boolean usuarioTieneRol(Long usuarioId, String nombreRol) {
        return usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuarioId, nombreRol);
    }

    /**
     * Obtener estadísticas de usuarios.
     *
     * @return mapa con estadísticas básicas
     */
    @Transactional(readOnly = true)
    public UserStatsDto obtenerEstadisticasUsuarios() {
        long totalUsuarios = usuarioRepository.count();
        long usuariosActivos = usuarioRepository.countByIsActiveTrue();
        long usuariosInactivos = totalUsuarios - usuariosActivos;
        long usuariosBloqueados = usuarioRepository.countLockedUsers(LocalDateTime.now());

        return new UserStatsDto(totalUsuarios, usuariosActivos, usuariosInactivos, usuariosBloqueados);
    }

    /**
     * Verificar si un username está disponible.
     *
     * @param username el username a verificar
     * @param excludeUserId ID del usuario a excluir de la verificación (opcional)
     * @return true si el username está disponible
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username, Long excludeUserId) {
        if (excludeUserId != null) {
            // Verificar si existe otro usuario con el mismo username (excluyendo el usuario especificado)
            return !usuarioRepository.existsByUsernameAndIdNot(username, excludeUserId);
        } else {
            // Verificar si el username no existe
            return !usuarioRepository.existsByUsername(username);
        }
    }

    /**
     * Verificar si un email está disponible.
     *
     * @param email el email a verificar
     * @param excludeUserId ID del usuario a excluir de la verificación (opcional)
     * @return true si el email está disponible
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email, Long excludeUserId) {
        if (excludeUserId != null) {
            // Verificar si existe otro usuario con el mismo email (excluyendo el usuario especificado)
            return !usuarioRepository.existsByEmailAndIdNot(email, excludeUserId);
        } else {
            // Verificar si el email no existe
            return !usuarioRepository.existsByEmail(email);
        }
    }

    /**
     * Obtener todos los roles disponibles.
     *
     * @return lista de DTOs de roles
     */
    @Transactional(readOnly = true)
    public List<com.frcefact.dto.RolDto> obtenerTodosLosRoles() {
        List<Rol> roles = rolRepository.findAll();
        return roles.stream()
                .map(rol -> {
                    com.frcefact.dto.RolDto dto = new com.frcefact.dto.RolDto();
                    dto.setId(rol.getId());
                    dto.setNombre(rol.getNombre());
                    dto.setDescripcion(rol.getDescripcion());
                    dto.setCreadoEn(rol.getCreadoEn());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * DTO para estadísticas de usuarios.
     */
    public static class UserStatsDto {
        private final long totalUsuarios;
        private final long usuariosActivos;
        private final long usuariosInactivos;
        private final long usuariosBloqueados;

        public UserStatsDto(long totalUsuarios, long usuariosActivos, long usuariosInactivos, long usuariosBloqueados) {
            this.totalUsuarios = totalUsuarios;
            this.usuariosActivos = usuariosActivos;
            this.usuariosInactivos = usuariosInactivos;
            this.usuariosBloqueados = usuariosBloqueados;
        }

        public long getTotalUsuarios() { return totalUsuarios; }
        public long getUsuariosActivos() { return usuariosActivos; }
        public long getUsuariosInactivos() { return usuariosInactivos; }
        public long getUsuariosBloqueados() { return usuariosBloqueados; }
    }
}
