package com.frcefact.dto;

import com.frcefact.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para Usuario.
 */
@Schema(description = "Información del usuario")
public class UsuarioDto {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario", example = "admin", required = true)
    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    private String username;

    @Schema(description = "Email del usuario", example = "admin@frcefact.com", required = true)
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    @Schema(description = "Estado activo del usuario", example = "true")
    private Boolean isActive;

    @Schema(description = "Fecha del último login", example = "2024-01-20T14:45:00")
    private LocalDateTime ultimoLogin;

    @Schema(description = "Fecha de creación del usuario", example = "2024-01-15T10:30:00")
    private LocalDateTime creadoEn;

    public UsuarioDto() {
    }

    public UsuarioDto(Long id, String username, String email, Boolean isActive, 
                     LocalDateTime ultimoLogin, LocalDateTime creadoEn) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isActive = isActive;
        this.ultimoLogin = ultimoLogin;
        this.creadoEn = creadoEn;
    }

    /**
     * Crear UsuarioDto desde entidad Usuario.
     */
    public static UsuarioDto fromEntity(Usuario usuario) {
        return new UsuarioDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getIsActive(),
                usuario.getUltimoLogin(),
                usuario.getCreadoEn()
        );
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
}
