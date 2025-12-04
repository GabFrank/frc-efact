package com.frcefact.controller;

import com.frcefact.dto.ChangePasswordRequest;
import com.frcefact.dto.UpdateProfileRequest;
import com.frcefact.dto.UsuarioDto;
import com.frcefact.dto.mapper.UsuarioMapper;
import com.frcefact.model.AuditLog;
import com.frcefact.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/perfil")
@Tag(name = "Perfil de Usuario", description = "Gestión del perfil y vinculación de cuentas")
@SecurityRequirement(name = "bearer-jwt")
public class UserProfileController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    public UserProfileController(UsuarioService usuarioService, UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }

    @PostMapping("/vincular-auth0")
    @Operation(summary = "Vincular cuenta Auth0", description = "Vincula la cuenta actual con un ID de Auth0")
    public ResponseEntity<?> vincularAuth0(@RequestBody Map<String, String> request, Authentication authentication) {
        String auth0Id = request.get("auth0Id");
        if (auth0Id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "auth0Id es requerido"));
        }

        String username = authentication.getName();
        try {
            usuarioService.vincularAuth0(username, auth0Id);
            return ResponseEntity.ok(Map.of("message", "Cuenta vinculada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/desvincular-auth0")
    @Operation(summary = "Desvincular cuenta Auth0", description = "Elimina la vinculación con Auth0")
    public ResponseEntity<?> desvincularAuth0(Authentication authentication) {
        String username = authentication.getName();
        try {
            usuarioService.desvincularAuth0(username);
            return ResponseEntity.ok(Map.of("message", "Cuenta desvinculada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/actualizar")
    @Operation(summary = "Actualizar perfil propio", description = "Permite al usuario actualizar su propio perfil (username, email)")
    public ResponseEntity<?> actualizarPerfil(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            var usuario = usuarioService.actualizarPerfilPropio(username, request);
            UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);
            return ResponseEntity.ok(usuarioDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña", description = "Permite al usuario cambiar su contraseña. Requiere validar la contraseña actual.")
    public ResponseEntity<?> cambiarPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            usuarioService.cambiarPassword(username, request);
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actividad")
    @Operation(summary = "Obtener actividad del usuario", description = "Retorna las últimas actividades/auditorías del usuario autenticado")
    public ResponseEntity<Page<AuditLog>> obtenerActividad(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = authentication.getName();
        try {
            Page<AuditLog> actividad = usuarioService.obtenerActividadUsuario(
                    username, 
                    pageable.getPageNumber(), 
                    pageable.getPageSize()
            );
            return ResponseEntity.ok(actividad);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

