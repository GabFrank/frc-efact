package com.frcefact.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para request de refresh token.
 */
@Schema(description = "Request para refrescar tokens JWT")
public class RefreshTokenRequest {

    @Schema(description = "Refresh token JWT válido", example = "eyJhbGciOiJIUzUxMiJ9...", required = true)
    @NotBlank(message = "Refresh token es requerido")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
