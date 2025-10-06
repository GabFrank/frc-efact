package com.frcefact.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para request de login.
 */
@Schema(description = "Request para autenticación de usuario")
public class LoginRequest {

    @Schema(description = "Nombre de usuario", example = "admin", required = true)
    @NotBlank(message = "Username es requerido")
    private String username;

    @Schema(description = "Contraseña del usuario", example = "password123", required = true)
    @NotBlank(message = "Password es requerido")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
