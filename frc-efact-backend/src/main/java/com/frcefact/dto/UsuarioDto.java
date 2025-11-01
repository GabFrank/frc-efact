package com.frcefact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para transferencia de datos de Usuario.
 * Utilizado en operaciones CRUD de usuarios.
 */
public class UsuarioDto {

    private Long id;

    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    // Password solo se usa en creación, no se retorna en consultas
    @Size(min = 8, message = "Password debe tener al menos 8 caracteres")
    private String password;

    private Boolean isActive;
    private LocalDateTime ultimoLogin;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Roles del usuario
    private Set<RolDto> roles = new HashSet<>();

    // Empresas del usuario
    private Set<UsuarioEmpresaDto> empresas = new HashSet<>();

    // Constructores
    public UsuarioDto() {
    }

    public UsuarioDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public Set<RolDto> getRoles() {
        return roles;
    }

    public void setRoles(Set<RolDto> roles) {
        this.roles = roles;
    }

    public Set<UsuarioEmpresaDto> getEmpresas() {
        return empresas;
    }

    public void setEmpresas(Set<UsuarioEmpresaDto> empresas) {
        this.empresas = empresas;
    }

    @Override
    public String toString() {
        return "UsuarioDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
