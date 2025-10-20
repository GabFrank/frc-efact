package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitud de reseteo de contraseña.
 * Utilizado por administradores para resetear contraseñas de usuarios.
 */
public class ResetPasswordRequest {

    @NotNull(message = "ID de usuario es requerido")
    private Long userId;

    @NotBlank(message = "Nueva contraseña es requerida")
    @Size(min = 8, message = "Password debe tener al menos 8 caracteres")
    private String newPassword;

    private Boolean forcePasswordChange = false;

    // Constructores
    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(Long userId, String newPassword) {
        this.userId = userId;
        this.newPassword = newPassword;
    }

    public ResetPasswordRequest(Long userId, String newPassword, Boolean forcePasswordChange) {
        this.userId = userId;
        this.newPassword = newPassword;
        this.forcePasswordChange = forcePasswordChange;
    }

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "userId=" + userId +
                ", forcePasswordChange=" + forcePasswordChange +
                '}';
    }
}