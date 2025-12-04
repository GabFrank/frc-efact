package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitud de cambio de contraseña.
 * Utilizado por usuarios para cambiar su propia contraseña.
 */
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es requerida")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String newPassword;

    @NotBlank(message = "La confirmación de contraseña es requerida")
    private String confirmPassword;

    // Constructores
    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Getters y Setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "currentPassword='***'" +
                ", newPassword='***'" +
                ", confirmPassword='***'" +
                '}';
    }
}

