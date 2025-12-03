package com.frcefact.controller;

import com.frcefact.dto.*;
import com.frcefact.model.Usuario;
import com.frcefact.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controlador REST para gestión de usuarios.
 * Proporciona endpoints para obtener información del perfil de usuario y administración completa de usuarios.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios")
@SecurityRequirement(name = "bearer-jwt")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final com.frcefact.dto.mapper.UsuarioMapper usuarioMapper;

    public UsuarioController(UsuarioService usuarioService,
                            com.frcefact.dto.mapper.UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Obtener perfil del usuario autenticado.
     *
     * @param authentication el usuario autenticado
     * @return UsuarioDto con información del perfil
     */
    @GetMapping("/perfil")
    @Operation(
            summary = "Obtener perfil de usuario",
            description = "Retorna la información del perfil del usuario autenticado. Requiere token JWT válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - token JWT inválido o faltante",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<UsuarioDto> obtenerPerfil(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Usuario no autenticado");
        }

        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        // Inicializar roles dentro de la transacción
        usuario.getUsuarioRoles().size();

        UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);
        return ResponseEntity.ok(usuarioDto);
    }

    // ========== ENDPOINTS DE ADMINISTRACIÓN DE USUARIOS ==========

    /**
     * Obtener usuarios disponibles para asignación a empresas (excluye ADMIN).
     * Disponible para ADMIN y EMPRESA_ADMIN.
     *
     * @return lista de usuarios asignables
     */
    @GetMapping("/asignables")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Listar usuarios asignables", description = "Obtiene lista de usuarios que pueden ser asignados a empresas (excluye ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosAsignables() {
        logger.debug("GET /usuarios/asignables - Obteniendo usuarios asignables a empresas");

        List<Usuario> usuarios = usuarioService.listarUsuariosAsignables();
        List<UsuarioDto> usuariosDto = usuarioMapper.toDtoList(usuarios);

        return ResponseEntity.ok(usuariosDto);
    }

    /**
     * Obtener todos los usuarios (solo administradores).
     *
     * @return lista de todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene lista de todos los usuarios del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<List<UsuarioDto>> obtenerTodosLosUsuarios(
            org.springframework.security.core.Authentication authentication) {
        logger.warn("⚠️ ============================================");
        logger.warn("⚠️ WRONG ENDPOINT CALLED: GET /usuarios (ADMIN only)");
        logger.warn("⚠️ User: {}", authentication != null ? authentication.getName() : "null");
        if (authentication != null) {
            logger.warn("⚠️ Authorities: {}", authentication.getAuthorities());
        }
        logger.warn("⚠️ This endpoint requires ADMIN role!");
        logger.warn("⚠️ Use /usuarios/asignables instead for EMPRESA_ADMIN");
        logger.warn("⚠️ ============================================");

        List<Usuario> usuarios = usuarioService.listarTodos();
        List<UsuarioDto> usuariosDto = usuarioMapper.toDtoList(usuarios);

        return ResponseEntity.ok(usuariosDto);
    }

    /**
     * Buscar usuarios con filtros y paginación.
     *
     * @param searchTerm término de búsqueda
     * @param isActive filtro por estado activo
     * @param page número de página
     * @param size tamaño de página
     * @param sortBy campo para ordenar
     * @param sortDirection dirección de ordenamiento
     * @return página de usuarios
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios con filtros y paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<Page<UsuarioDto>> buscarUsuarios(
            @Parameter(description = "Término a buscar en username o email") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Filtrar por estado activo") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "username") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        logger.debug("GET /usuarios/buscar - searchTerm: {}, isActive: {}, page: {}, size: {}", 
                    searchTerm, isActive, page, size);

        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        searchRequest.setIsActive(isActive);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        Page<Usuario> usuarios = usuarioService.buscarUsuarios(searchRequest);
        Page<UsuarioDto> usuariosDto = usuarios.map(usuarioMapper::toDto);

        return ResponseEntity.ok(usuariosDto);
    }

    /**
     * Obtener usuario por ID.
     *
     * @param id ID del usuario
     * @return usuario encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioDto> obtenerUsuarioPorId(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        logger.debug("GET /usuarios/{} - Obteniendo usuario", id);

        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);
        return ResponseEntity.ok(usuarioDto);
    }

    /**
     * Crear nuevo usuario.
     *
     * @param request datos del usuario a crear
     * @return usuario creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o username/email duplicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<UsuarioDto> crearUsuario(@Valid @RequestBody CreateUserRequest request) {
        logger.info("POST /usuarios - Creando nuevo usuario: {}", request.getUsername());

        try {
            Usuario usuario = usuarioService.crearUsuarioAdmin(request);
            UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);

            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioDto);
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al crear usuario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear usuario", e);
            throw e;
        }
    }

    /**
     * Actualizar usuario existente.
     *
     * @param id ID del usuario
     * @param request datos de actualización
     * @return usuario actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioDto> actualizarUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        logger.info("PUT /usuarios/{} - Actualizando usuario", id);

        Usuario usuario = usuarioService.actualizarUsuarioAdmin(id, request);
        UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);

        return ResponseEntity.ok(usuarioDto);
    }

    /**
     * Eliminar usuario (soft delete).
     *
     * @param id ID del usuario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Desactiva un usuario (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        logger.info("DELETE /usuarios/{} - Eliminando usuario", id);

        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resetear contraseña de usuario.
     *
     * @param request datos de reseteo de contraseña
     * @return respuesta sin contenido
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resetear contraseña", description = "Resetea la contraseña de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña reseteada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> resetearPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("POST /usuarios/reset-password - Reseteando contraseña para usuario ID: {}", request.getUserId());

        usuarioService.resetearPassword(request.getUserId(), request.getNewPassword(), request.getForcePasswordChange());
        return ResponseEntity.ok().build();
    }

    /**
     * Activar usuario.
     *
     * @param id ID del usuario
     * @return respuesta sin contenido
     */
    @PostMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar usuario", description = "Activa un usuario desactivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> activarUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        logger.info("POST /usuarios/{}/activar - Activando usuario", id);

        usuarioService.activarUsuario(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Desactivar usuario.
     *
     * @param id ID del usuario
     * @return respuesta sin contenido
     */
    @PostMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar usuario", description = "Desactiva un usuario activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario desactivado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> desactivarUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        logger.info("POST /usuarios/{}/desactivar - Desactivando usuario", id);

        usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Desbloquear usuario.
     *
     * @param id ID del usuario
     * @return respuesta sin contenido
     */
    @PostMapping("/{id}/desbloquear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desbloquear usuario", description = "Desbloquea una cuenta de usuario bloqueada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> desbloquearUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        logger.info("POST /usuarios/{}/desbloquear - Desbloqueando usuario", id);

        usuarioService.desbloquearUsuario(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtener roles de un usuario.
     *
     * @param id ID del usuario
     * @return conjunto de roles del usuario
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener roles de usuario", description = "Obtiene los roles asignados a un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Set<String>> obtenerRolesUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        logger.debug("GET /usuarios/{}/roles - Obteniendo roles del usuario", id);

        Set<String> roles = usuarioService.obtenerRolesUsuario(id);
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtener todos los roles disponibles.
     *
     * @return lista de todos los roles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los roles", description = "Obtiene la lista de todos los roles disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<List<RolDto>> obtenerTodosLosRoles() {
        logger.debug("GET /usuarios/roles - Obteniendo todos los roles");

        List<RolDto> roles = usuarioService.obtenerTodosLosRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtener estadísticas de usuarios.
     *
     * @return estadísticas básicas de usuarios
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estadísticas de usuarios", description = "Obtiene estadísticas básicas del sistema de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<UsuarioService.UserStatsDto> obtenerEstadisticasUsuarios() {
        logger.debug("GET /usuarios/estadisticas - Obteniendo estadísticas de usuarios");

        UsuarioService.UserStatsDto stats = usuarioService.obtenerEstadisticasUsuarios();
        return ResponseEntity.ok(stats);
    }

    /**
     * Buscar usuarios por término simple.
     *
     * @param term término de búsqueda
     * @return lista de usuarios que coinciden
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Búsqueda simple de usuarios", description = "Busca usuarios por término en username o email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<List<UsuarioDto>> buscarUsuariosPorTermino(
            @Parameter(description = "Término a buscar") @RequestParam String term) {
        logger.debug("GET /usuarios/search?term={}", term);

        List<Usuario> usuarios = usuarioService.buscarUsuariosPorTermino(term);
        List<UsuarioDto> usuariosDto = usuarioMapper.toDtoList(usuarios);

        return ResponseEntity.ok(usuariosDto);
    }

    /**
     * Verificar disponibilidad de username.
     *
     * @param username username a verificar
     * @param excludeUserId ID del usuario a excluir de la verificación (opcional)
     * @return objeto con información de disponibilidad
     */
    @GetMapping("/check-username")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar disponibilidad de username", description = "Verifica si un username está disponible para uso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<java.util.Map<String, Boolean>> checkUsernameAvailability(
            @Parameter(description = "Username a verificar") @RequestParam String username,
            @Parameter(description = "ID del usuario a excluir (opcional)") @RequestParam(required = false) Long excludeUserId) {
        logger.debug("GET /usuarios/check-username?username={}&excludeUserId={}", username, excludeUserId);

        boolean available = usuarioService.isUsernameAvailable(username, excludeUserId);
        
        java.util.Map<String, Boolean> response = new java.util.HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verificar disponibilidad de email.
     *
     * @param email email a verificar
     * @param excludeUserId ID del usuario a excluir de la verificación (opcional)
     * @return objeto con información de disponibilidad
     */
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar disponibilidad de email", description = "Verifica si un email está disponible para uso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<java.util.Map<String, Boolean>> checkEmailAvailability(
            @Parameter(description = "Email a verificar") @RequestParam String email,
            @Parameter(description = "ID del usuario a excluir (opcional)") @RequestParam(required = false) Long excludeUserId) {
        logger.debug("GET /usuarios/check-email?email={}&excludeUserId={}", email, excludeUserId);

        boolean available = usuarioService.isEmailAvailable(email, excludeUserId);
        
        java.util.Map<String, Boolean> response = new java.util.HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }

}
