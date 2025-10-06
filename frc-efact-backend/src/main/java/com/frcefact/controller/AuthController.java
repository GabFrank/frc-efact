package com.frcefact.controller;

import com.frcefact.dto.AuthResponse;
import com.frcefact.dto.LoginRequest;
import com.frcefact.dto.RefreshTokenRequest;
import com.frcefact.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 * Proporciona endpoints para login, refresh de tokens y logout.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios con JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint de login.
     *
     * @param loginRequest credenciales del usuario
     * @return AuthResponse con tokens y datos del usuario
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login de usuario",
            description = "Autentica un usuario con username y password, retorna tokens JWT (access y refresh) junto con información del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request inválido - campos requeridos faltantes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Demasiados intentos de login - rate limit excedido",
                    content = @Content
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para refrescar token.
     *
     * @param refreshTokenRequest request con refresh token
     * @return AuthResponse con nuevos tokens
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar token",
            description = "Genera nuevos access y refresh tokens usando un refresh token válido. Útil para mantener la sesión sin requerir login nuevamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens refrescados exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request inválido",
                    content = @Content
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        AuthResponse response = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de logout.
     *
     * @param authentication el usuario autenticado
     * @return mensaje de confirmación
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Logout de usuario",
            description = "Cierra la sesión del usuario autenticado. Requiere token JWT válido en el header Authorization"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso",
                    content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - token JWT inválido o faltante",
                    content = @Content
            )
    })
    public ResponseEntity<String> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            authService.logout(authentication.getName());
        }
        return ResponseEntity.ok("Logout exitoso");
    }
}
