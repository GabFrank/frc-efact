package com.frcefact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitud de actualización de perfil propio.
 * Utilizado por usuarios para actualizar su propia información.
 */
public class UpdateProfileRequest {

    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    private String username;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    // Constructores
    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

