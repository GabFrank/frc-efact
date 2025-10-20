package com.frcefact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO para solicitud de actualización de usuario.
 * Utilizado por administradores para actualizar usuarios existentes.
 */
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    private String username;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    private Set<String> roles;

    private Boolean isActive;

    // Constructores
    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String username, String email, Set<String> roles, Boolean isActive) {
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.isActive = isActive;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", isActive=" + isActive +
                '}';
    }
}