package com.frcefact.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para response de autenticación.
 */
@Schema(description = "Response de autenticación con tokens JWT y datos del usuario")
public class AuthResponse {

    @Schema(description = "Token JWT de acceso", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    @Schema(description = "Token JWT para refrescar el access token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;

    @Schema(description = "Tipo de token", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Información del usuario autenticado")
    private UsuarioDto usuario;

    public AuthResponse() {
    }

    public AuthResponse(String token, String refreshToken, UsuarioDto usuario) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UsuarioDto getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDto usuario) {
        this.usuario = usuario;
    }
}
