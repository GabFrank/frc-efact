package com.frcefact.controller;

import com.frcefact.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    public UserProfileController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
}

