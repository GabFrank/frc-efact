package com.frcefact.controller;

import com.frcefact.dto.ChangePasswordRequest;
import com.frcefact.dto.UpdateProfileRequest;
import com.frcefact.dto.UsuarioDto;
import com.frcefact.dto.mapper.UsuarioMapper;
import com.frcefact.model.AuditLog;
import com.frcefact.security.OAuth2TokenFilter;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
            Page<AuditLog> actividad = usuarioService.obtenerActividadUsuario(username, pageable);
            return ResponseEntity.ok(actividad);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/actualizar-desde-auth0")
    @Operation(summary = "Actualizar información desde Auth0", description = "Actualiza la información del usuario (imagen de perfil, email, etc.) desde el token JWT de Auth0/Google. El token puede venir en el body de la request o en el header Authorization.")
    public ResponseEntity<?> actualizarDesdeAuth0(
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication,
            HttpServletRequest request) {
        String username = authentication.getName();
        
        try {
            Jwt jwt = null;
            String jwtTokenString = null;
            
            // 1. Intentar obtener el token del body de la request (si el frontend lo envía)
            if (requestBody != null && requestBody.containsKey("token")) {
                jwtTokenString = requestBody.get("token");
                System.out.println("DEBUG: Token obtenido del body de la request");
            }
            
            // 2. Si no está en el body, intentar obtener el JWT del Authentication object
            if (jwtTokenString == null) {
                if (authentication instanceof JwtAuthenticationToken) {
                    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                    jwt = jwtAuth.getToken();
                    System.out.println("DEBUG: JWT obtenido de JwtAuthenticationToken");
                } else if (authentication.getPrincipal() instanceof Jwt) {
                    jwt = (Jwt) authentication.getPrincipal();
                    System.out.println("DEBUG: JWT obtenido del Principal");
                } else if (authentication.getCredentials() instanceof Jwt) {
                    jwt = (Jwt) authentication.getCredentials();
                    System.out.println("DEBUG: JWT obtenido de Credentials");
                } else if (authentication.getDetails() instanceof Jwt) {
                    jwt = (Jwt) authentication.getDetails();
                    System.out.println("DEBUG: JWT obtenido de Details");
                } else {
                    // Intentar obtener el JWT del ThreadLocal (guardado por OAuth2TokenFilter)
                    jwt = OAuth2TokenFilter.getJwtFromThreadLocal();
                    if (jwt != null) {
                        System.out.println("DEBUG: JWT obtenido de ThreadLocal");
                    }
                }
            }
            
            // 3. Si no se encontró en el Authentication, intentar del header
            if (jwtTokenString == null && jwt == null) {
                String bearerToken = request.getHeader("Authorization");
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    jwtTokenString = bearerToken.substring(7);
                    System.out.println("DEBUG: Token obtenido del header Authorization");
                }
            }
            
            // 4. Si tenemos el token como String, decodificarlo y usar el servicio
            if (jwtTokenString != null) {
                var usuario = usuarioService.actualizarDesdeAuth0(username, jwtTokenString);
                UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);
                return ResponseEntity.ok(usuarioDto);
            }
            
            // 5. Si tenemos el JWT del Authentication, usarlo directamente
            if (jwt != null) {
                var usuario = usuarioService.actualizarDesdeAuth0Jwt(username, jwt);
                UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);
                return ResponseEntity.ok(usuarioDto);
            }
            
            // Si no se encontró el token en ningún lugar
            return ResponseEntity.badRequest().body(Map.of("error", "Token JWT no encontrado. Por favor, envíe el token en el body de la request con la clave 'token', o asegúrese de estar autenticado con Auth0/Google."));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al actualizar información desde Auth0: " + e.getMessage()));
        }
    }
}

