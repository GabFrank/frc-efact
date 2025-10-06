package com.frcefact.controller;

import com.frcefact.dto.UsuarioDto;
import com.frcefact.model.Usuario;
import com.frcefact.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestión de usuarios.
 * Proporciona endpoints para obtener información del perfil de usuario.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
    @SecurityRequirement(name = "bearerAuth")
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
    public ResponseEntity<UsuarioDto> obtenerPerfil(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Usuario no autenticado");
        }

        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        UsuarioDto usuarioDto = UsuarioDto.fromEntity(usuario);
        return ResponseEntity.ok(usuarioDto);
    }
}
