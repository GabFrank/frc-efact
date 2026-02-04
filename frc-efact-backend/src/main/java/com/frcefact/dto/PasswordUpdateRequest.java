package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitud de actualización de contraseña del certificado.
 */
public class PasswordUpdateRequest {

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    // Constructores
    public PasswordUpdateRequest() {
    }

    public PasswordUpdateRequest(String password) {
        this.password = password;
    }

    // Getters y Setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

