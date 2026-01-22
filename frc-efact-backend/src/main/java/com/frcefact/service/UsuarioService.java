package com.frcefact.service;

import com.frcefact.dto.ChangePasswordRequest;
import com.frcefact.dto.CreateUserRequest;
import com.frcefact.dto.UpdateProfileRequest;
import com.frcefact.dto.UpdateUserRequest;
import com.frcefact.dto.UserSearchRequest;
import com.frcefact.model.AuditLog;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
    private final AuditLogService auditLogService;
    private final JwtDecoder jwtDecoder;

    public UsuarioService(UsuarioRepository usuarioRepository, 
                         RolRepository rolRepository,
                         UsuarioRolRepository usuarioRolRepository,
                         PasswordEncoder passwordEncoder,
                         AuditLogService auditLogService,
                         JwtDecoder jwtDecoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.jwtDecoder = jwtDecoder;
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
     * Vincular cuenta Auth0 a usuario existente.
     * 
     * @param username el username del usuario
     * @param auth0Id el ID de Auth0
     */
    public void vincularAuth0(String username, String auth0Id) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        
        // Verificar si el Auth0 ID ya está en uso por otro usuario
        usuarioRepository.findByAuth0Id(auth0Id).ifPresent(u -> {
            if (!u.getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("La cuenta de Auth0 ya está vinculada a otro usuario.");
            }
        });
        
        usuario.setAuth0Id(auth0Id);
        usuarioRepository.save(usuario);
    }

    /**
     * Desvincular cuenta Auth0.
     * 
     * @param username el username del usuario
     */
    public void desvincularAuth0(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        
        usuario.setAuth0Id(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Actualizar perfil propio del usuario.
     * Permite al usuario actualizar su propio username y email.
     * No permite cambios de roles o estado (solo admin puede hacerlo).
     *
     * @param username el username del usuario autenticado
     * @param request datos de actualización
     * @return el usuario actualizado
     * @throws IllegalArgumentException si el username o email ya existe o si el usuario no se encuentra
     */
    public Usuario actualizarPerfilPropio(String username, UpdateProfileRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        // Validar y actualizar username si se proporciona
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(usuario.getUsername())) {
                if (usuarioRepository.existsByUsernameAndIdNot(newUsername, usuario.getId())) {
                    throw new IllegalArgumentException("Username ya existe: " + newUsername);
                }
                usuario.setUsername(newUsername);
            }
        }

        // Validar y actualizar email si se proporciona
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmailAndIdNot(newEmail, usuario.getId())) {
                    throw new IllegalArgumentException("Email ya existe: " + newEmail);
                }
                usuario.setEmail(newEmail);
            }
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambiar contraseña del usuario.
     * Verifica la contraseña actual antes de permitir el cambio.
     *
     * @param username el username del usuario autenticado
     * @param request datos de cambio de contraseña
     * @throws IllegalArgumentException si la contraseña actual es incorrecta, 
     *                                  si las contraseñas no coinciden,
     *                                  si el usuario no se encuentra,
     *                                  o si el usuario tiene cuenta Auth0 vinculada
     */
    public void cambiarPassword(String username, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        // Verificar si tiene cuenta Auth0 vinculada
        if (usuario.getAuth0Id() != null && !usuario.getAuth0Id().isEmpty()) {
            throw new IllegalArgumentException("No se puede cambiar la contraseña. El usuario tiene una cuenta Auth0/Google vinculada.");
        }

        // Verificar que tenga contraseña (no debería pasar, pero por seguridad)
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isEmpty()) {
            throw new IllegalArgumentException("El usuario no tiene contraseña configurada.");
        }

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        // Verificar que las nuevas contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
        }

        // Verificar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la contraseña actual.");
        }

        // Actualizar contraseña
        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuario.resetFailedLoginAttempts(); // Resetear intentos fallidos al cambiar contraseña
        usuarioRepository.save(usuario);
    }

    /**
     * Obtener actividad del usuario con paginación.
     *
     * @param username el username del usuario
     * @param pageable configuración de paginación
     * @return página de registros de auditoría del usuario
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> obtenerActividadUsuario(String username, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        // Crear un Pageable sin sort ya que la consulta JPQL ya tiene ORDER BY
        Pageable pageableSinSort = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize()
        );
        return auditLogService.buscarConFiltros(usuario.getId(), null, null, null, null, null, pageableSinSort);
    }

    /**
     * Actualizar información del usuario desde el token JWT de Auth0.
     * Extrae información como imagen de perfil, email, etc. del token y actualiza el usuario.
     *
     * @param username el username del usuario autenticado
     * @param jwtTokenString el token JWT como String
     * @return el usuario actualizado
     * @throws IllegalArgumentException si el usuario no se encuentra, no tiene cuenta Auth0 vinculada,
     *                                  o si el token no es válido o no es de Auth0
     */
    public Usuario actualizarDesdeAuth0(String username, String jwtTokenString) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        // Verificar que el usuario tenga cuenta Auth0 vinculada
        if (usuario.getAuth0Id() == null || usuario.getAuth0Id().isEmpty()) {
            throw new IllegalArgumentException("El usuario no tiene una cuenta Auth0/Google vinculada.");
        }

        try {
            // Decodificar el token JWT sin validar la firma (solo para leer los claims)
            // Esto es seguro porque el usuario ya está autenticado y solo estamos leyendo información
            // Dividimos el JWT en sus partes: header.payload.signature
            String[] parts = jwtTokenString.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Token JWT inválido: formato incorrecto");
            }
            
            // Decodificar el payload (segunda parte)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            
            // Extraer información del token
            String auth0IdFromToken = (String) claims.get("sub");
            String email = (String) claims.get("email");
            Boolean emailVerified = claims.get("email_verified") instanceof Boolean 
                ? (Boolean) claims.get("email_verified")
                : Boolean.parseBoolean(String.valueOf(claims.get("email_verified")));
            String picture = (String) claims.get("picture");
            
            // Verificar que el token pertenezca al usuario
            // Verificamos tanto el auth0Id como el email para mayor flexibilidad
            boolean tokenBelongsToUser = false;
            if (auth0IdFromToken != null && auth0IdFromToken.equals(usuario.getAuth0Id())) {
                tokenBelongsToUser = true;
            } else if (email != null && email.equals(usuario.getEmail()) && Boolean.TRUE.equals(emailVerified)) {
                // Si el email coincide y está verificado, también es válido
                tokenBelongsToUser = true;
                // Si el auth0Id del token es diferente pero el email coincide, actualizar el auth0Id
                if (auth0IdFromToken != null && !auth0IdFromToken.equals(usuario.getAuth0Id())) {
                    usuario.setAuth0Id(auth0IdFromToken);
                }
            }
            
            if (!tokenBelongsToUser) {
                throw new IllegalArgumentException("El token no pertenece a este usuario. Auth0Id del token: " + auth0IdFromToken + ", Auth0Id del usuario: " + usuario.getAuth0Id());
            }

            boolean needsUpdate = false;

            // Actualizar email si está verificado y es diferente
            if (email != null && Boolean.TRUE.equals(emailVerified) && !email.equals(usuario.getEmail())) {
                // Verificar que el email no esté en uso por otro usuario
                Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(usuario.getId())) {
                    throw new IllegalArgumentException("El email ya está en uso por otro usuario.");
                }
                usuario.setEmail(email);
                needsUpdate = true;
            }

            // Actualizar imagen de perfil si está disponible y es diferente
            if (picture != null && !picture.equals(usuario.getImagenPerfil())) {
                usuario.setImagenPerfil(picture);
                needsUpdate = true;
            }

            if (needsUpdate) {
                usuario = usuarioRepository.save(usuario);
            }

            return usuario;
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token JWT inválido o no es de Auth0: " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al decodificar el token JWT: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Re-lanzar IllegalArgumentException sin modificar
            throw e;
        }
    }

    /**
     * Actualizar información del usuario desde el objeto JWT de Auth0.
     * Versión sobrecargada que recibe directamente el objeto Jwt.
     *
     * @param username el username del usuario autenticado
     * @param jwt el objeto Jwt ya decodificado
     * @return el usuario actualizado
     * @throws IllegalArgumentException si el usuario no se encuentra, no tiene cuenta Auth0 vinculada,
     *                                  o si el token no pertenece al usuario
     */
    public Usuario actualizarDesdeAuth0Jwt(String username, Jwt jwt) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        // Verificar que el usuario tenga cuenta Auth0 vinculada
        if (usuario.getAuth0Id() == null || usuario.getAuth0Id().isEmpty()) {
            throw new IllegalArgumentException("El usuario no tiene una cuenta Auth0/Google vinculada.");
        }

        // Verificar que el token pertenezca al usuario
        String auth0Id = jwt.getSubject();
        if (!auth0Id.equals(usuario.getAuth0Id())) {
            throw new IllegalArgumentException("El token no pertenece a este usuario.");
        }

        // Extraer información del token
        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaim("email_verified");
        String picture = jwt.getClaimAsString("picture");

        boolean needsUpdate = false;

        // Actualizar email si está verificado y es diferente
        if (email != null && Boolean.TRUE.equals(emailVerified) && !email.equals(usuario.getEmail())) {
            // Verificar que el email no esté en uso por otro usuario
            Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("El email ya está en uso por otro usuario.");
            }
            usuario.setEmail(email);
            needsUpdate = true;
        }

        // Actualizar imagen de perfil si está disponible y es diferente
        if (picture != null && !picture.equals(usuario.getImagenPerfil())) {
            usuario.setImagenPerfil(picture);
            needsUpdate = true;
        }

        if (needsUpdate) {
            usuario = usuarioRepository.save(usuario);
        }

        return usuario;
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
